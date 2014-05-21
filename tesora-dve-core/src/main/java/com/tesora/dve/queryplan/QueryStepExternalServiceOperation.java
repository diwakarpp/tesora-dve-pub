// OS_STATUS: public
package com.tesora.dve.queryplan;

import com.tesora.dve.common.catalog.CatalogDAO;
import com.tesora.dve.common.catalog.PersistentDatabase;
import com.tesora.dve.db.DBResultConsumer;
import com.tesora.dve.exceptions.PEException;
import com.tesora.dve.externalservice.ExternalServiceFactory;
import com.tesora.dve.externalservice.ExternalServicePlugin;
import com.tesora.dve.server.connectionmanager.SSConnection;
import com.tesora.dve.server.messaging.SQLCommand;
import com.tesora.dve.sql.schema.PEExternalService;
import com.tesora.dve.sql.transform.execution.CatalogModificationExecutionStep.Action;
import com.tesora.dve.worker.WorkerGroup;

public class QueryStepExternalServiceOperation extends QueryStepDDLOperation {

	Action action;
	PEExternalService rootEntity;

	public QueryStepExternalServiceOperation(PersistentDatabase execCtxDBName,
			SQLCommand command, Action action, PEExternalService rootEntity2) {
		super(execCtxDBName, command,null);

		this.action = action;
		this.rootEntity = rootEntity2;
	}

	@Override
	protected void prepareAction(SSConnection ssCon, CatalogDAO c, WorkerGroup wg, DBResultConsumer resultConsumer) throws PEException {
		super.prepareAction(ssCon,c,wg,resultConsumer);
		
		switch(action) {
		case DROP:
		{
			if ( ExternalServiceFactory.isRegistered(rootEntity.getExternalServiceName())) {
				ExternalServicePlugin plugin = ExternalServiceFactory.getInstance(rootEntity.getExternalServiceName());
				plugin.stop();
			}
			
			// do this all the time
			ExternalServiceFactory.deregister(rootEntity.getExternalServiceName());
			break;
		}
		case ALTER:
		{
			// Alter is allowed if the service isn't currently registered, so check for that
			if ( ExternalServiceFactory.isRegistered(rootEntity.getExternalServiceName())) {
				ExternalServicePlugin plugin = ExternalServiceFactory.getInstance(rootEntity.getExternalServiceName());
				if (plugin.isStarted()) {
					throw new PEException("Cannot change external service while service is started.");
				}
			}
			break;
		}
		default :
			break;
		}
	}
	
	@Override
	public void execute(SSConnection ssCon, WorkerGroup wg, DBResultConsumer resultConsumer)
			throws Throwable {
		// call base first to get the external service into the catalog
		super.execute(ssCon, wg, resultConsumer);

		// we do this here instead of prepareAction because we need the service
		// to be committed (by super.execute) to the catalog before registering 
		// and starting it
		switch(action) {
		case CREATE:
			// no need to check if service exists in map already
//			ExternalServiceContextImpl ctxt = new ExternalServiceContextImpl(
//					rootEntity.getExternalServiceName());
//			ExternalServicePlugin plugin = ExternalServiceFactory.register(ctxt,
//					rootEntity.getPlugin(), rootEntity.getExternalServiceName());
//			if (ctxt.getServiceAutoStart()) {
//				plugin.start();
//			}
			ExternalServiceFactory.register(rootEntity.getExternalServiceName(), rootEntity.getPlugin());
			
			break;
			
		case ALTER:
			// Alter is allowed if the service isn't registered
			if ( ExternalServiceFactory.isRegistered(rootEntity.getExternalServiceName())) {
				ExternalServicePlugin pi = ExternalServiceFactory.getInstance(rootEntity.getExternalServiceName());
				pi.reload();
			}
			break;
			
		default :
			break;
		}
	}
}
