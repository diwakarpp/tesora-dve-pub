// OS_STATUS: public
package com.tesora.dve.queryplan;

import com.tesora.dve.common.catalog.CatalogDAO;
import com.tesora.dve.common.catalog.UserDatabase;
import com.tesora.dve.db.DBResultConsumer;
import com.tesora.dve.exceptions.PEException;
import com.tesora.dve.server.connectionmanager.SSConnection;
import com.tesora.dve.server.messaging.WorkerAlterDatabaseRequest;
import com.tesora.dve.server.messaging.WorkerRequest;
import com.tesora.dve.sql.schema.cache.CacheInvalidationRecord;
import com.tesora.dve.worker.WorkerGroup;
import com.tesora.dve.worker.WorkerGroup.MappingSolution;

public class QueryStepAlterDatabaseOperation extends QueryStepOperation {

	private final UserDatabase alteredDatabase;
	private final CacheInvalidationRecord invalidationRecord;

	public QueryStepAlterDatabaseOperation(final UserDatabase alteredDatabase, final CacheInvalidationRecord invalidationRecord) {
		this.alteredDatabase = alteredDatabase;
		this.invalidationRecord = invalidationRecord;
	}

	@Override
	public void execute(final SSConnection ssCon, final WorkerGroup wg, final DBResultConsumer resultConsumer) throws Throwable {
		if (ssCon.hasActiveTransaction()) {
			throw new PEException("Cannot execute DDL within active transaction: ALTER DATABASE " + this.alteredDatabase.getName());
		}

		CatalogDAO c = ssCon.getCatalogDAO();
		c.begin();
		try {
			QueryPlanner.invalidateCache(this.invalidationRecord);
			c.persistToCatalog(this.alteredDatabase);

			// TODO:
			// start a transaction with the transaction manager so that DDL can
			// be
			// registered to back out the DDL we are about to execute in the
			// event of a failure after the DDL is executed but before the txn
			// is committed.

			final WorkerRequest request = new WorkerAlterDatabaseRequest(ssCon.getNonTransactionalContext(), this.alteredDatabase);
			wg.execute(MappingSolution.AllWorkers, request, resultConsumer);

			c.commit();
		} catch (Throwable t) {
			c.rollback(t);
			throw t;
		} finally {
			QueryPlanner.invalidateCache(this.invalidationRecord);
		}
	}

	@Override
	public boolean requiresTransaction() {
		return false;
	}

	@Override
	public boolean requiresImplicitCommit() {
		return true;
	}

}
