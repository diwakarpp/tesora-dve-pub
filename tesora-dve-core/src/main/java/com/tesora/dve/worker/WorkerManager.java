// OS_STATUS: public
package com.tesora.dve.worker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.tesora.dve.server.connectionmanager.log.ShutdownLog;
import com.tesora.dve.worker.agent.Agent;
import org.apache.log4j.Logger;

import com.tesora.dve.common.RemoteException;
import com.tesora.dve.common.catalog.StorageGroup;
import com.tesora.dve.common.catalog.StorageSite;
import com.tesora.dve.comms.client.messages.GenericResponse;
import com.tesora.dve.comms.client.messages.ResponseMessage;
import com.tesora.dve.exceptions.PEException;
import com.tesora.dve.worker.agent.Envelope;
import com.tesora.dve.server.messaging.WorkerManagerRequest;

public class WorkerManager extends Agent {
	
	Logger logger = Logger.getLogger(WorkerManager.class);
	
	Set<Worker> activeWorkers = new HashSet<Worker>();
	
	public WorkerManager() throws PEException {
		super(WorkerManager.class.getSimpleName());
	}

	@Override
	public void onMessage(Envelope e) throws PEException {
		Object payload = e.getPayload();	
		if (payload instanceof WorkerManagerRequest) {
			WorkerManagerRequest m = (WorkerManagerRequest)payload;
			ResponseMessage resp;
			try {
				resp =  m.executeRequest(e, this);
			} catch (Exception ex) {
				resp = new GenericResponse().setException(new RemoteException(getName(), ex));
			}
			if (resp != null)
				returnResponse(e, resp);
		} else {
			throw new PEException("WorkerManager received message of invalid type (" + payload.toString() + ")");
		}
	}

	public Worker getWorker(UserAuthentication auth, StorageSite site) throws PEException {
		Worker theWorker = site.createWorker(auth);
		activeWorkers.add(theWorker);
		return theWorker;
	}
	
	private void returnWorker(Worker w) throws PEException {
		activeWorkers.remove(w);
		w.close();
	}
	
	public void returnWorkerList(StorageGroup group, Collection<Worker> theWorkers) throws PEException {
		ArrayList<StorageSite> groupSites = new ArrayList<StorageSite>();
		for (Worker worker : theWorkers) {
			groupSites.add(worker.getWorkerSite());
			returnWorker(worker);
		}
		group.returnWorkerSites(this, groupSites);
	}
	
	public boolean allWorkersReturned() throws PEException {
		boolean allWorkersReturned =  (activeWorkers.size() == 0);
		for (Worker w : activeWorkers)
			logger.error("active worker: " + w);
		return allWorkersReturned;
	}

	private void quiesceWorkerManager() throws PEException {
		sendAndReceive(newEnvelope(new WorkerManagerSync()).to(getAddress()));
	}
	
	public void shutdown() {
		try {
			quiesceWorkerManager();
			super.close();
			activeWorkers.clear();
		} catch (PEException e) {
			ShutdownLog.logShutdownError("Error shutting down " + getClass().getSimpleName(), e);
		}
	}

	public Map<StorageSite, Worker> getWorkerMap(UserAuthentication auth,
			Collection<? extends StorageSite> storageSites) throws PEException {
		Map<StorageSite, Worker> workerMap = new HashMap<StorageSite, Worker>();
		
		for (StorageSite site : storageSites) {
			workerMap.put(site, getWorker(auth, site));
		}
		return workerMap;
	}
}
