package com.tesora.dve.worker;

/*
 * #%L
 * Tesora Inc.
 * Database Virtualization Engine
 * %%
 * Copyright (C) 2011 - 2014 Tesora Inc.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.tesora.dve.concurrent.*;
import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.tesora.dve.db.GroupDispatch;
import com.tesora.dve.db.mysql.DefaultSetVariableBuilder;
import com.tesora.dve.db.mysql.SharedEventLoopHolder;
import com.tesora.dve.server.global.HostService;
import com.tesora.dve.server.statistics.manager.LogSiteStatisticRequest;
import com.tesora.dve.singleton.Singletons;
import com.tesora.dve.worker.agent.Agent;

import org.apache.log4j.Logger;

import com.tesora.dve.common.PECollectionUtils;
import com.tesora.dve.common.PEStringUtils;
import com.tesora.dve.common.catalog.PersistentDatabase;
import com.tesora.dve.common.catalog.PersistentGroup;
import com.tesora.dve.common.catalog.StorageGroup;
import com.tesora.dve.common.catalog.StorageSite;
import com.tesora.dve.db.DBEmptyTextResultConsumer;
import com.tesora.dve.db.mysql.portal.protocol.ClientCapabilities;
import com.tesora.dve.exceptions.PECodingException;
import com.tesora.dve.exceptions.PECommunicationsException;
import com.tesora.dve.exceptions.PEException;
import com.tesora.dve.exceptions.PESQLException;
import com.tesora.dve.server.connectionmanager.PerHostConnectionManager;
import com.tesora.dve.server.connectionmanager.SSConnection;
import com.tesora.dve.server.connectionmanager.SSContext;
import com.tesora.dve.server.connectionmanager.WorkerSetSessionVariableRequest;
import com.tesora.dve.server.messaging.CloneWorkerRequest;
import com.tesora.dve.server.messaging.GetWorkerRequest;
import com.tesora.dve.server.messaging.GetWorkerResponse;
import com.tesora.dve.server.messaging.ResetWorkerRequest;
import com.tesora.dve.server.messaging.ReturnWorkerRequest;
import com.tesora.dve.server.messaging.WorkerCreateDatabaseRequest;
import com.tesora.dve.server.messaging.WorkerRequest;
import com.tesora.dve.sql.util.Functional;
import com.tesora.dve.sql.util.UnaryFunction;
import com.tesora.dve.sql.util.UnaryPredicate;
import com.tesora.dve.worker.agent.Envelope;

public class WorkerGroup {
	
	static Logger logger = Logger.getLogger( WorkerGroup.class );

	public static final String WORKER_GROUP_SUPPRESS_CACHING = "WorkerGroup.suppressCaching";

	static AtomicInteger nextGroupId = new AtomicInteger(0);
	int displayId = nextGroupId.incrementAndGet();
	
	static boolean suppressCaching = Boolean.getBoolean(WORKER_GROUP_SUPPRESS_CACHING); 
	
	List<WorkerRequest> cleanupSteps = new ArrayList<WorkerRequest>();

	long idleTime;
	
	boolean toPurge = false; // if true, the ssConnection will discard the group rather than caching it

	// pin count - we increment for each create temporary table and decrement for each drop table that
	// ends up being on a temporary table.  if the pin count > 0 we cannot purge this worker group,
	// and this overrides toPruge.
	int pinned = 0;
	
    EventLoopGroup clientEventLoop = SharedEventLoopHolder.getLoop();  //this is the thread that handles all IO for this group.

	public interface Manager {
		abstract void returnWorkerGroup(WorkerGroup wg) throws PEException;
	}
	
	public static class MappingSolution {
		public static final MappingSolution AllWorkers = new MappingSolution(Synchronization.ASYNCHRONOUS);
		public static final MappingSolution AllWorkersSerialized = new MappingSolution(Synchronization.SYNCHRONOUS);
		public static final MappingSolution AnyWorker = new MappingSolution(Synchronization.ASYNCHRONOUS);
		public static final MappingSolution AnyWorkerSerialized = new MappingSolution(Synchronization.SYNCHRONOUS);
		
		enum Synchronization { SYNCHRONOUS, ASYNCHRONOUS }
		
		public boolean isWorkerSerializationRequired() {
			return workerSerializationRequired;
		}

		StorageSite site;
		final boolean workerSerializationRequired;
		
		public MappingSolution(StorageSite site) {
			this.site = site;
			this.workerSerializationRequired = false;
		}
		
		public MappingSolution(Synchronization sync) {
			workerSerializationRequired = (sync == Synchronization.SYNCHRONOUS);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((site == null) ? 0 : site.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MappingSolution other = (MappingSolution) obj;
			if (site == null) {
				if (other.site != null)
					return false;
			} else if (!site.equals(other.site))
				return false;
			return true;
		}
		
		public MappingSolution lockMapping(WorkerGroup wg) {
			return wg.lockWorkerMapping(this);
		}

		public int computeSize(WorkerGroup workerGroup) {
			return (this == AllWorkers || this == AllWorkersSerialized) ? workerGroup.size() : 1;
		}
		
		public StorageSite getSite() {
			return site;
		}

		@Override
		public String toString() {
			String val;
			if (this == AllWorkers || this == AllWorkersSerialized)
				val = "AllWorkers";
			else if (this == AnyWorker || this == AnyWorkerSerialized)
				val = "AnyWorker";
			else 
				val = site.toString();
			return this.getClass().getSimpleName() + "{" + val + "}";
		}
	}
	
	UserAuthentication userAuthentication;
	
	SortedMap<StorageSite, Worker> workerMap =
			new TreeMap<StorageSite,Worker>(new Comparator<StorageSite>() {
				@Override // sort by StorageSite.name
				public int compare(StorageSite o1, StorageSite o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});

	final StorageGroup group;
	Boolean overrideTemp = null;
	Manager manager = null;
	final String name;
	
	int connectionId = -1;
	
	private WorkerGroup(PersistentGroup group) {
		this.group = group;
		this.name = group.getName();
	}
	
	private WorkerGroup(StorageGroup group) {
		this.group = group;
		this.name = "DynamicWG";
	}

	private WorkerGroup provision(Agent sender, Manager manager, UserAuthentication userAuth, EventLoopGroup preferredEventLoop) throws PEException, PEException {
		this.manager = manager;
		this.userAuthentication = userAuth;
        if (preferredEventLoop == null)
            this.clientEventLoop = SharedEventLoopHolder.getLoop();
        else
            this.clientEventLoop = preferredEventLoop;

		GetWorkerRequest req = new GetWorkerRequest(userAuth, getAdditionalConnInfo(sender), preferredEventLoop, group);
        Envelope e = sender.newEnvelope(req).to(Singletons.require(HostService.class).getWorkerManagerAddress());
		GetWorkerResponse resp = (GetWorkerResponse) sender.sendAndReceive(e);
		workerMap.putAll(resp.getWorkers());
		return this;
	}
	
	public WorkerGroup clone(Agent sender) throws PEException {
		CloneWorkerRequest req = new CloneWorkerRequest(userAuthentication, getAdditionalConnInfo(sender), clientEventLoop, workerMap.keySet());
        Envelope e = sender.newEnvelope(req).to(Singletons.require(HostService.class).getWorkerManagerAddress());
		GetWorkerResponse resp = (GetWorkerResponse) sender.sendAndReceive(e);
		WorkerGroup newWG = new WorkerGroup(group);
		newWG.setTemporaryOverride(true);
		newWG.workerMap.putAll(resp.getWorkers());
		return newWG /* .markForPurge() */;
		
	}
	
//	public void send(Agent sender, MappingSolution mappingSolution, Object m) throws PEException {
//		validateForSend();
//		
//		Envelope e = sender.newEnvelope(m).from(sender.getReplyAddress());
//		
//		for (Worker worker : getTargetWorkers(mappingSolution))
//			sender.send(e.to(worker.getWorkerId()));
//	}
	
//	public Collection<ResponseMessage> sendAndReceiveSerialized(Agent sender, MappingSolution mappingSolution, Serializable m) throws PEException {
//		List<ResponseMessage> responses = new ArrayList<ResponseMessage>();
//		
//		validateForSend();
//		
//		Envelope e = sender.newEnvelope(m).from(sender.getReplyAddress());
//		
//		for (Worker worker : getTargetWorkers(mappingSolution))
//			responses.add(sender.sendAndReceive(e.to(worker.getWorkerId())));
//		
//		return responses;
//	}

	public Collection<Worker> getTargetWorkers(MappingSolution mappingSolution) throws PEException {
		Collection<Worker> targetWorkers;
		if (mappingSolution == MappingSolution.AllWorkers || mappingSolution == MappingSolution.AllWorkersSerialized) {
			targetWorkers = workerMap.values();
		} else {
			targetWorkers = new ArrayList<Worker>();
			StorageSite selectedSite;
			if (mappingSolution == MappingSolution.AnyWorker)
				selectedSite = PECollectionUtils.selectRandom(workerMap.keySet());
			else if (mappingSolution == MappingSolution.AnyWorkerSerialized)
				selectedSite = workerMap.firstKey();
			else
				selectedSite = mappingSolution.site;
			targetWorkers.add(selectedSite.pickWorker(workerMap));
		}
		
		if (logger.isDebugEnabled()) {
			String workerSet = "sites{" +
					Functional.join(workerMap.keySet(), ",", new UnaryFunction<String, StorageSite>() {
						@Override
						public String evaluate(StorageSite object) {
							return object.getName();
						}
					}) + "}";
			logger.debug("WorkerGroup" + displayId + " on " + group.toString() + " maps " + workerSet
					+ " to " + PEStringUtils.toString("targetWorkers", targetWorkers));
		}
		return targetWorkers;
	}

//	public void dispatch(Agent sender, MappingSolution mappingSolution, Object m) throws PEException {
//		if (mappingSolution == MappingSolution.AllWorkersSerialized)
//			mappingSolution = MappingSolution.AllWorkers;
//		
//		send(sender, mappingSolution, m);
//	}
	
	public MappingSolution lockWorkerMapping(MappingSolution mappingSolution) {
		MappingSolution newMappingSolution = mappingSolution;
		if (MappingSolution.AnyWorker == mappingSolution)
			newMappingSolution = new MappingSolution(PECollectionUtils.selectRandom(workerMap.keySet()));
		else if (MappingSolution.AnyWorkerSerialized == mappingSolution)
			newMappingSolution = new MappingSolution(workerMap.firstKey());
		return newMappingSolution;
	}
	
	void releaseWorkers(Agent sender) {
		try {
			resetWorkers(sender);
		} catch (PEException e2) {
			e2.printStackTrace();
		}
		if (workerMap != null) {
//			sendToAllWorkers(sender, new ResetWorkerRequest());
//			sender.consumeReplies(size());
			ReturnWorkerRequest req = new ReturnWorkerRequest(group, workerMap.values());
            Envelope e = sender.newEnvelope(req).to(Singletons.require(HostService.class).getWorkerManagerAddress());
			try {
				sender.sendAndReceive(e);
			} catch (PEException e1) {
				// We do this on a best-effort basis
				logger.error("Exception encountered during cleanup", e1);
			}
			workerMap = null;
		}
	}
	
	public void resetWorkers(Agent sender) throws PEException {
		if (workerMap != null) {
			if (logger.isDebugEnabled())
				logger.debug("WorkerGroup executes cleanup steps: " + this);
			ChainedNotifier<Worker> prev = submit((MappingSolution) MappingSolution.AllWorkers, (WorkerRequest) new ResetWorkerRequest(new SSContext(connectionId)), (GroupDispatch) DBEmptyTextResultConsumer.INSTANCE, (int) MappingSolution.AllWorkers.computeSize(this), null);

			for (WorkerRequest req : cleanupSteps) {
				prev = submit(MappingSolution.AllWorkers, req, DBEmptyTextResultConsumer.INSTANCE, MappingSolution.AllWorkers.computeSize(this),prev);
			}
			syncWorkers(prev);
			cleanupSteps.clear();
		}
		//NOTE: we don't clear the binding on a reset, there is a chance the same event will pick up the group and be able to re-use the binding.
        //bindToClientThread(null);
	}

    /**
     * Changes the event loop used to process IO for all workers.  When a worker group is in use by a SSConnection,
     * backend connections held by its workers should share the same event loop as the client connection,
     * so that passing IO between frontend and backend sockets doesn't require any locking or context switching.
     *
     * @param preferredEventLoop
     */
    public void bindToClientThread(EventLoopGroup preferredEventLoop) {
        if (preferredEventLoop == null)
            this.clientEventLoop = SharedEventLoopHolder.getLoop();
        else
            this.clientEventLoop = preferredEventLoop;

        if (workerMap != null){
            for (Worker worker : workerMap.values()){
                try {
                    worker.bindToClientThread(preferredEventLoop);
                } catch (PESQLException e) {
                    logger.warn(this+" encountered problem binding worker to client event loop",e);
                }
            }
        }
    }
	
	public void returnToManager() throws PEException {
		if (manager != null) {
				manager.returnWorkerGroup(this);
			manager = null;
		}
	}
	
	public int size() {
		try {
			return workerMap.size();
		} catch (NullPointerException npe) {
			logger.debug("NPE: caught on WorkerGroup" + displayId);
			throw npe;
		}
	}

	public String getName() {
		return name;
	}

	public int getCommectionId() {
		return this.connectionId;
	}

	public boolean isProvisioned() {
		return workerMap != null;
	}
	
	@Override
	public String toString() {
		List<String> names = new ArrayList<String>();
		for (Entry<StorageSite,Worker> entry : workerMap.entrySet()) {
			names.add(entry.getKey().getName() + "/" + entry.getValue());
		}
		return PEStringUtils.toString(this.getClass().getSimpleName()+"@" + displayId +"("+group.getName()+ ")", names);
	}

	public Manager setManager(Manager manager) {
		Manager orig = this.manager;
		this.manager = manager;
		return orig;
	}

	public StorageGroup getGroup() {
		return group;
	}

	public boolean isTemporaryGroup() {
		if (overrideTemp == null)
			return group.isTemporaryGroup();
		return overrideTemp.booleanValue();
	}
	
	public void setTemporaryOverride(boolean v) {
		overrideTemp = v;
	}
	
	public Collection<StorageSite> getStorageSites() {
		return workerMap.keySet();
	}

	// unconditionally makes sure the database exists
	public void assureDatabase(SSConnection ssCon, final PersistentDatabase uvd) throws PEException {
		execute(MappingSolution.AllWorkers, new WorkerCreateDatabaseRequest(ssCon.getTransactionalContext(),uvd,true),
				DBEmptyTextResultConsumer.INSTANCE);
	}		
	
	// unconditionally sets the session variables for the connection on the worker group
	public void assureSessionVariables(SSConnection ssCon) throws PEException {
        Map<String,String> currentSessionVars = ssCon.getSessionVariables();

		Collection<Worker> targetWorkers = getTargetWorkers(MappingSolution.AllWorkers);

		if (targetWorkers.isEmpty())
			return;

		ChainedNotifier<Boolean> promise = null;
		for (Worker w : targetWorkers) {
			promise = ChainedNotifier.newInstance(promise); //chains results.
			w.updateSessionVariables(currentSessionVars, new DefaultSetVariableBuilder(), promise);
		}
		try {
			promise.sync();
		} catch (PEException e) {
			throw e;
		} catch (Exception e) {
			if (e.getCause() instanceof PEException)
				throw (PEException)e.getCause();
			else
				throw new PEException(e);
		}
//        execute(
//                MappingSolution.AllWorkers,
//				new WorkerSetSessionVariableRequest(ssCon.getNonTransactionalContext(), currentSessionVars, new DefaultSetVariableBuilder()),
//				DBEmptyTextResultConsumer.INSTANCE
//        );
	}
		
	public void setDatabase(SSConnection ssCon, final PersistentDatabase uvd) throws PEException {
		if (group.isTemporaryGroup()) {
			// see if any of the sites does not have this db
			// if so, send down the create database to all of them.
			boolean any = Functional.any(workerMap.keySet(), new UnaryPredicate<StorageSite>() {

				@Override
				public boolean test(StorageSite object) {
					return !object.hasDatabase(uvd);
				}
				
			});
			if (!any) return;
			assureDatabase(ssCon,uvd);
			// ok, that's done, mark all the sites
			for(StorageSite ss : workerMap.keySet())
				ss.setHasDatabase(uvd);
		}
	}
	
	
	public void execute(MappingSolution mappingSolution, final WorkerRequest req, final GroupDispatch resultConsumer) throws PEException {
		syncWorkers(submit((MappingSolution) mappingSolution, (WorkerRequest) req, (GroupDispatch) resultConsumer, (int) mappingSolution.computeSize(this), null));
	}

	public static void executeOnAllGroups(Collection<WorkerGroup> allGroups, MappingSolution mappingSolution, WorkerRequest req, GroupDispatch resultConsumer) throws PEException {
		ChainedNotifier<Worker> prev = null;
		for (WorkerGroup wg : allGroups)
			prev = wg.submit((MappingSolution) mappingSolution, (WorkerRequest) req, (GroupDispatch) resultConsumer, (int) mappingSolution.computeSize(wg), prev);
		syncWorkers(prev);
	}

	public static void syncWorkers(Future<Worker> fut) throws PEException {
		if (fut == null)
			return;

		try {
			fut.get();
		} catch (InterruptedException e) {
			throw new PEException("Worker operation interrupted", e);
		} catch (ExecutionException e) {
			throw new PEException("Worker exception", e.getCause());
		}
	}

	public static void syncWorkers(Collection<Future<Worker>> workerFutures) throws PEException {
		//wait for these to finish in reverse order, which reduces collisions with the producer.
		ArrayList<Future<Worker>> workerList = new ArrayList<>(workerFutures);
		Collections.reverse(workerList);
		for (Future<Worker> f : workerList)
			syncWorkers(f);
	}


	public Collection<Future<Worker>> submit(MappingSolution mappingSolution,
			final WorkerRequest req, final GroupDispatch resultConsumer)
			throws PEException {
		Collection<Future<Worker>> results = new ArrayList<>();
		results.add(submit((MappingSolution) mappingSolution, (WorkerRequest) req, (GroupDispatch) resultConsumer, (int) mappingSolution.computeSize(this),null));
		return results;
	}

	public Collection<Future<Worker>> submit(MappingSolution mappingSolution,
			final WorkerRequest req, final GroupDispatch resultConsumer, int senderCount)
			throws PEException {
		Collection<Future<Worker>> results = new ArrayList<>();
		results.add(submit(mappingSolution, req, resultConsumer, senderCount, null));
		return results;
	}

	protected ChainedNotifier<Worker> submit(MappingSolution mappingSolution, final WorkerRequest req, final GroupDispatch resultConsumer, int senderCount, CompletionNotifier<Worker> previousDependency) throws PEException {
		resultConsumer.setSenderCount(senderCount);

		final Collection<Worker> workers = getTargetWorkers(mappingSolution);
		final ArrayList<WorkerAndFuture> workersAndFutures = new ArrayList<>();

		if (workers.isEmpty()){
			return ChainedNotifier.noop(null);
		}

		ChainedNotifier<Worker> lastWorker = null;
		for (final Worker w: workers) {
			//make sure we have open connections for all targets, since connecting right now is synchronous, and we don't want to block an async netty thread.
			ensureOpenConnection(w);

			//construct a synchronous callback for each worker.
			ChainedNotifier<Worker> workerFuture;
			if (previousDependency == null)
				workerFuture = new ChainedNotifier<>();
			else
				workerFuture = new ChainedNotifier<>(previousDependency);
			previousDependency = workerFuture;
			lastWorker = workerFuture;

			workersAndFutures.add(new WorkerAndFuture(w,workerFuture,workerFuture));
		}


		final boolean firstMustRunSerial = mappingSolution.isWorkerSerializationRequired() && workers.size() > 1;

		if (firstMustRunSerial){
			//remove the first entry.
			Iterator<WorkerAndFuture> iterator = workersAndFutures.iterator();
			final WorkerAndFuture first = iterator.next();
			final CompletionTarget<Worker> firstWorkerTarget = first.target;
			final Future<Worker> firstWorkerSync = first.sync;
			iterator.remove();

			final CompletionTarget<Worker> submitOtherWorkersOnSuccess = new CompletionTarget<Worker>() {
                @Override
                public void success(Worker returnValue) {
					firstWorkerTarget.success(returnValue);
					for (final WorkerAndFuture w: workersAndFutures) {
                        submitWork(w.worker, req, resultConsumer,w.target);
                    }
				}

                @Override
                public void failure(Exception e) {
					firstWorkerTarget.failure(e);
                }
            };

			clientEventLoop.submit(new Runnable() {
				@Override
				public void run() {
					submitWork(first.worker, req, resultConsumer, submitOtherWorkersOnSuccess);
				}
			});

			// blocks this thread on success/fail of first task, and for failure throws related exception here.
			try {
				firstWorkerSync.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new PEException("Exception encountered synchronizing site updates", e);
            }
		} else {
			clientEventLoop.submit(new Runnable() {
				@Override
				public void run() {
					for (final WorkerAndFuture w: workersAndFutures) {
                        submitWork(w.worker, req, resultConsumer,w.target);
                    }
				}
			});
		}

		return lastWorker;
	}

	private void submitWork(final Worker w, final WorkerRequest req, final GroupDispatch resultConsumer, final CompletionTarget<Worker> syncListener ) {
        final CompletionHandle<Boolean> promise = buildSubmitHandle(w, req, syncListener);

		req.withGroupDispatch(resultConsumer);
		req.executeRequest(w, promise);
    }

	private CompletionHandle<Boolean> buildSubmitHandle(final Worker w, final WorkerRequest req, final CompletionTarget<Worker> syncListener) {
		return new DefaultCompletionHandle<Boolean>(){
            final long reqStartTime = System.currentTimeMillis();
            @Override
            protected void onSuccess(Boolean returnValue) {
                try {
                    WorkerGroup.sendStatistics(w, req, System.currentTimeMillis() - reqStartTime);
                    syncListener.success(w);
                } catch (PEException e) {
                    this.failure(e);
                }
            }

            @Override
            protected void onFailure(Exception t) {
                try{
                    w.setLastException(t);
                    if (t instanceof PEException && ((PEException)t).hasCause(PECommunicationsException.class)){
                        markForPurge();
                    }
                } finally {
                    syncListener.failure(t);
                }
            }
        };
	}

	private void ensureOpenConnection(Worker w) {
		try {
            //This call forces the worker to get a database connection immediately in the client thread, before we submit to the netty thread (where blocking would be bad).
            //TODO: the worker's lazy getConnection() call shouldn't block, any following calls should get chained off the pending connection. -sgossard
            w.getConnectionId();
        } catch (Exception e) {
            //any exception here needs to be ignored so the normal code path can fail just like it used to.
        }
	}

	static void sendStatistics(Worker worker, WorkerRequest wReq, long execTime) throws PEException {
        LogSiteStatisticRequest sNotice = wReq.getStatisticsNotice();
        if (sNotice != null) {
            worker.site.annotateStatistics(sNotice);
            sNotice.setExecutionDetails(worker.site.getName(), (int)execTime);
            Agent.dispatch(Singletons.require(HostService.class).getStatisticsManagerAddress(), sNotice);
        }
    }

//	public void sendToAllWorkers(Agent agent, Object m) throws PEException {
//		send(agent, MappingSolution.AllWorkers, m);
//	}

	
	void markIdle() {
		this.idleTime = System.currentTimeMillis();
	}
	
	void markActive() {
		this.idleTime = 0;
	}
	
	public boolean hasExpired() {
        return (System.currentTimeMillis() - idleTime) > Singletons.require(HostService.class).getDBConnectionTimeout();
	}
	
	public WorkerGroup markForPurge() {
		this.toPurge = true;
		return this;
	}
	
	public boolean isMarkedForPurge() {
		if (this.pinned > 0) return false;
		return this.toPurge;
	}
	
	public void markPinned() {
		this.pinned++;
	}
	
	public void clearPinned() {
		this.pinned--;
	}
	
	public boolean isPinned() {
		return pinned > 0;
	}
	
	public void associateWithConnection(int connectionId) {
		this.connectionId = connectionId;
		try {
			for (StorageSite site : workerMap.keySet()) {
				Worker worker = workerMap.get(site);
				PerHostConnectionManager.INSTANCE.registerSiteConnection(connectionId, site, worker.getConnectionId());
			}
		} catch (PESQLException e) {
			throw new PECodingException("Failed to register worker site connection", e);
		}
	}
	
	public void disassociateFromConnection() {
		try {
			for (StorageSite site : workerMap.keySet()) {
				PerHostConnectionManager.INSTANCE.unregisterSiteConnection(connectionId, site);
			}
		} finally {
			this.connectionId = -1;
		}
	}

	public boolean usesSiteInstance(Agent agent, int siteInstanceId) throws PEException {
		return false;
	}
	
	public void addCleanupStep(WorkerRequest req) {
		cleanupSteps.add(req);
	}

	AdditionalConnectionInfo getAdditionalConnInfo(Agent conn) {
		AdditionalConnectionInfo additionalConnInfo = new AdditionalConnectionInfo();
		additionalConnInfo.setClientCapabilities(getPSiteClientCapabilities(conn));
		return additionalConnInfo;
	}

	long getPSiteClientCapabilities(Agent conn) {
		long psiteClientCapabilities = 0;
		if (conn instanceof SSConnection) {
			psiteClientCapabilities = ((SSConnection)conn).getClientCapabilities().getClientCapability();
		}
		// just make sure the default psite flags are set
		psiteClientCapabilities |= ClientCapabilities.DEFAULT_PSITE_CAPABILITIES;
		return psiteClientCapabilities;
	}
	
	public static class WorkerGroupFactory {
		
		private static final class WorkerGroupPool {

			private WorkerManager workerManager;
			private final Timer poolCleanupTimer = new Timer();

			private final ConcurrentHashMap<CacheKey, ConcurrentLinkedQueue<WorkerGroup>> pool =
					new ConcurrentHashMap<CacheKey, ConcurrentLinkedQueue<WorkerGroup>>();

			/*
			 * read lock: allow concurrent get/put operations, which are safe on the underlying concurrent collections
			 * write lock: exclusive lock to iterate over the pool and make multiple updates 
			 */
			private final ReadWriteLock locks = new ReentrantReadWriteLock();

			
			WorkerGroupPool(WorkerManager mgr) {
				this.workerManager = mgr;
				poolCleanupTimer.schedule(new TimerTask() {
					@Override
					public void run() {
						purgeExpired();
					}
				}, 20 * 1000, 10 * 1000);
			}

			public WorkerGroup get(StorageGroup sg, UserAuthentication ua) {
				WorkerGroup wg = null;
				locks.readLock().lock();
				try {
					Queue<WorkerGroup> queue = pool.get(new CacheKey(sg, ua));
					if (queue != null)
						wg = queue.poll();
					if (wg != null)
						wg.markActive();
				} finally {
					locks.readLock().unlock();
				}
				return wg;
			}

			public void put(WorkerGroup wg) {
				locks.readLock().lock();
				try {
					wg.markIdle();
					CacheKey key = new CacheKey(wg);
					Queue<WorkerGroup> queue = pool.get(key);
					if (queue == null) {
						ConcurrentLinkedQueue<WorkerGroup> newQueue = new ConcurrentLinkedQueue<WorkerGroup>();
						queue = pool.putIfAbsent(key, newQueue);
						if (queue == null)
							queue = newQueue;
					}
					queue.add(wg);
				} finally {
					locks.readLock().unlock();
				}
			}

			public void clearGroup(Agent agent, StorageGroup sg) {
				locks.writeLock().lock();
				try {
					Iterator<CacheKey> iter = pool.keySet().iterator();
					while (iter.hasNext()) {
						CacheKey key = iter.next();
						if (sg.equals(key.getStorageGroup())) {
							ConcurrentLinkedQueue<WorkerGroup> queue = pool.get(key);
							iter.remove();
							for (WorkerGroup wg : queue) {
								if (logger.isDebugEnabled())
									logger.debug("NPE: WorkerGroupFactory.clearGroup clears " + wg);
								wg.releaseWorkers(agent);
							}
						}
					}
				} finally {
					locks.writeLock().unlock();
				}
			}
			
			public void clearPool() {
				locks.writeLock().lock();
				try {
					for (Queue<WorkerGroup> queue : pool.values()) {
						for (WorkerGroup wg : queue) {
							if (logger.isDebugEnabled())
								logger.debug("NPE: WorkerGroupPool.close() calls releaseWorkers() on " + wg);
							wg.releaseWorkers(workerManager);
						}
					}
				} finally {
					pool.clear();
					locks.writeLock().unlock();
				}
			}
			
			public void close() {
				clearPool();
				poolCleanupTimer.cancel();
			}
			
			private void purgeExpired() {
				locks.writeLock().lock();
				List<WorkerGroup> toRelease = new LinkedList<WorkerGroup>();
				try {
					Iterator<Entry<CacheKey, ConcurrentLinkedQueue<WorkerGroup>>> poolIter = pool.entrySet().iterator();
					Queue<WorkerGroup> queue = null;
					while (poolIter.hasNext()) {
						queue = poolIter.next().getValue();
						for (Iterator<WorkerGroup> iter = queue.iterator(); iter.hasNext();) {
							WorkerGroup wg = iter.next();
							if (wg.hasExpired()) {
								iter.remove();
								toRelease.add(wg);
							}
						}
						if (queue.isEmpty())
							poolIter.remove();
					}
				} finally {
					locks.writeLock().unlock();
				}

				for (WorkerGroup wg : toRelease) {
					if (logger.isDebugEnabled())
						logger.debug("NPE: WorkerGroupPool.purgeExpired() calls releaseWorkers on " + wg);
					wg.releaseWorkers(workerManager);
				}
			}

		}

		private static WorkerGroupPool workerGroupPool;
		
		public static WorkerGroup newInstance(SSConnection ssCon, StorageGroup sg, PersistentDatabase ctxDB) throws PEException {
			WorkerGroup wg = workerGroupPool.get(sg, ssCon.getUserAuthentication());
            Channel channel = ssCon.getChannel();
            EventLoop eventLoop = channel == null ? null : channel.eventLoop();

			if (wg == null) {
				wg = new WorkerGroup(sg).provision(ssCon, ssCon, ssCon.getUserAuthentication(), eventLoop);
			} else {
                wg.bindToClientThread(eventLoop);
            }
			try {
				if (ctxDB != null) 
					wg.setDatabase(ssCon, ctxDB);
				wg.assureSessionVariables(ssCon);
			} catch (PEException e) {
                if (logger.isDebugEnabled())
					logger.debug("NPE: WorkerGroupFactory.newInstance() calls releaseWorkers() on "+ wg);
				wg.releaseWorkers(ssCon);
				throw e;
			}
			if (wg.workerMap == null)
				throw new PECodingException("WorkerGroupFactory.newInstance() returns previously closed worker group");
			return wg;
		}

		public static void returnInstance(final SSConnection ssCon, final WorkerGroup wg) throws PEException {
			ssCon.verifyWorkerGroupCleanup(wg); 
			if (suppressCaching || wg.isTemporaryGroup()  || wg.isMarkedForPurge()) {
				if (logger.isDebugEnabled())
					logger.debug("NPE: WorkerGroupFactory.returnInstance() calls releaseWorkers on " + wg);
				wg.releaseWorkers(ssCon);
			} else {
				try {
					wg.resetWorkers(ssCon);
					workerGroupPool.put(wg);
				} catch (PEException e) {
					logger.warn("Exception encountered resetting workers to add to cache - removing workers from pool", e);
					wg.releaseWorkers(ssCon);
				}
			}
		}

		public static void clearGroupFromCache(Agent agent, PersistentGroup groupToClear) {
			workerGroupPool.clearGroup(agent, groupToClear);
		}
		
		public static void purgeInstance(SSConnection ssCon, WorkerGroup wg) throws PEException {
			returnInstance(ssCon, wg.markForPurge());
		}

		public static void startup(final WorkerManager wm) {
			if (suppressCaching)
				logger.warn("WorkerGroup caching is disabled");
			workerGroupPool = new WorkerGroupPool(wm);
		}
		
		public static void shutdown(Agent wm) {
			if (workerGroupPool != null) {
				workerGroupPool.close();
			}
		}
		
		public static void clearPool() {
			if (workerGroupPool != null) {
				workerGroupPool.clearPool();
			}
		}
		
		public static void suppressCaching() {
			suppressCaching = true;
			clearPool();
		}

		public static void restoreCaching() throws PEException {
			if ( workerGroupPool == null )
				throw new PEException("Worker Group caching cannot be restored as it wasn't initialized");
			suppressCaching = false;
		}

	}
	
	private static class CacheKey {
		
		private StorageGroup sg;
		private UserAuthentication auth;
		
		public CacheKey(WorkerGroup wg) {
			sg = wg.getGroup();
			auth = wg.userAuthentication;
		}

		public CacheKey(StorageGroup sg, UserAuthentication ua) {
			this.sg = sg;
			this.auth = ua;
		}
		
		public StorageGroup getStorageGroup() {
			return sg;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((auth == null) ? 0 : auth.hashCode());
			result = prime * result + ((sg == null) ? 0 : sg.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CacheKey other = (CacheKey) obj;
			if (auth == null) {
				if (other.auth != null)
					return false;
			} else if (!auth.equals(other.auth))
				return false;
			if (sg == null) {
				if (other.sg != null)
					return false;
			} else if (!sg.equals(other.sg))
				return false;
			return true;
		}
		
		
	}

	public int activeTransactionCount() throws PESQLException {
		int modifedWorkerCount = 0;
		for (Worker w : workerMap.values())
			if (w.hasActiveTransaction())
				++modifedWorkerCount;
		return modifedWorkerCount;
	}

	public boolean isModified() throws PESQLException {
		for (Worker w : workerMap.values())
			if (w.isModified())
				return true;
		return false;
	}

	public StorageSite resolveSite(StorageSite site) throws PEException {
		Worker w = site.pickWorker(workerMap);
		if (w == null)
			throw new PEException("Site " + site + " could not resolve to WorkerGroup " + this);
		return w.getWorkerSite();
	}

	public int getDisplayId() {
		return displayId;
	}

	public void pushDebugContext() {
	}

	static class WorkerAndFuture {
		Worker worker;
		CompletionTarget<Worker> target;
		Future<Worker> sync;

		public WorkerAndFuture(Worker worker, SynchronousListener<Worker> future) {
			this.worker = worker;
			this.target = future;
			this.sync = future;
		}

		public WorkerAndFuture(Worker worker, CompletionTarget<Worker> target, Future<Worker> sync) {
			this.worker = worker;
			this.target = target;
			this.sync = sync;
		}
	}

}
