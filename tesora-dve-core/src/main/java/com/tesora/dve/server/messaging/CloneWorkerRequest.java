// OS_STATUS: public
package com.tesora.dve.server.messaging;

import java.util.Collection;
import java.util.Map;

import com.tesora.dve.common.catalog.StorageSite;
import com.tesora.dve.comms.client.messages.MessageType;
import com.tesora.dve.comms.client.messages.MessageVersion;
import com.tesora.dve.comms.client.messages.ResponseMessage;
import com.tesora.dve.exceptions.PEException;
import com.tesora.dve.sql.util.Functional;
import com.tesora.dve.sql.util.UnaryProcedure;
import com.tesora.dve.worker.UserAuthentication;
import com.tesora.dve.worker.Worker;
import com.tesora.dve.worker.WorkerManager;
import com.tesora.dve.worker.agent.Envelope;

public class CloneWorkerRequest extends WorkerManagerRequest {

	private static final long serialVersionUID = 1L;

	UserAuthentication userAuth;
	Collection<? extends StorageSite> storageSites;

	public CloneWorkerRequest(UserAuthentication userAuth,
			Collection<? extends StorageSite> storageSites) {
		super();
		this.userAuth = userAuth;
		this.storageSites = storageSites;
	}

	@Override
	public ResponseMessage executeRequest(Envelope e, WorkerManager wm)
			throws PEException {
		Map<StorageSite, Worker> theWorkers = wm.getWorkerMap(userAuth, storageSites);
		Functional.apply(theWorkers.keySet(), new UnaryProcedure<StorageSite>() {
			public void execute(StorageSite site) {
				site.incrementUsageCount();
			}
		});
		return new GetWorkerResponse(theWorkers);
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.WM_CLONE_WORKER_REQUEST;
	}

	@Override
	public MessageVersion getVersion() {
		return MessageVersion.VERSION1;
	}

}


