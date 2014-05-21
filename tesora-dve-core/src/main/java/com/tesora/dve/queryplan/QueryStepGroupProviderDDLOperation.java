// OS_STATUS: public
package com.tesora.dve.queryplan;

import com.tesora.dve.common.catalog.CatalogDAO;
import com.tesora.dve.db.DBResultConsumer;
import com.tesora.dve.exceptions.PEException;
import com.tesora.dve.server.connectionmanager.SSConnection;
import com.tesora.dve.server.messaging.SQLCommand;
import com.tesora.dve.siteprovider.SiteProviderContextInitialisation;
import com.tesora.dve.siteprovider.SiteProviderPlugin;
import com.tesora.dve.siteprovider.SiteProviderPlugin.SiteProviderContext;
import com.tesora.dve.siteprovider.SiteProviderPlugin.SiteProviderFactory;
import com.tesora.dve.worker.SiteManagerCommand;
import com.tesora.dve.worker.WorkerGroup;

public class QueryStepGroupProviderDDLOperation extends QueryStepDDLOperation {

	protected SiteManagerCommand smc;
	protected SiteProviderPlugin target;

	public QueryStepGroupProviderDDLOperation(SiteManagerCommand smc) {
		super(null, SQLCommand.EMPTY,null);
		this.smc = smc;
		// delay creation until we prepare
		this.target = null;
	}

	// allow derived classes to step in
	@Override
	protected void prepareAction(SSConnection ssCon, CatalogDAO c, WorkerGroup wg, DBResultConsumer resultConsumer) throws PEException {
		if (target == null) {
			// we don't know if this particular site manager has ever been
			// created before, so we're going to
			// use the two arg version of getInstance - which will register this
			// thing in the map if it hasn't been
			// note that if it has been created before - this has no adverse
			// effect
			SiteProviderContext ctxt = new SiteProviderContextInitialisation(smc.getTarget().getName(), c);
			target = SiteProviderFactory.getInstance(ctxt, smc.getTarget().getName(), smc.getTarget().getPlugin());
		}
		this.smc = target.prepareUpdate(smc);
	}

	@Override
	protected void executeAction(SSConnection conn, CatalogDAO c, WorkerGroup wg, DBResultConsumer resultConsumer) throws PEException {
		int rowcount = target.update(this.smc);
		resultConsumer.setNumRowsAffected(rowcount);
	}

	@Override
	protected void onRollback() throws PEException {
		if (target != null)
			target.rollback(smc);
	}

	@Override
	public boolean requiresWorkers() {
		// could be executed before the database is set
		return false;
	}

}
