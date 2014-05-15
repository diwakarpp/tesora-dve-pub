// OS_STATUS: public
package com.tesora.dve.sql.statement.ddl;

import com.tesora.dve.common.catalog.ExternalService;
import com.tesora.dve.exceptions.PEException;
import com.tesora.dve.sql.schema.Name;
import com.tesora.dve.sql.schema.PEDatabase;
import com.tesora.dve.sql.schema.PEExternalService;
import com.tesora.dve.sql.schema.Persistable;
import com.tesora.dve.sql.schema.SchemaContext;
import com.tesora.dve.sql.schema.UnqualifiedName;
import com.tesora.dve.sql.schema.cache.CacheInvalidationRecord;
import com.tesora.dve.sql.statement.Statement;
import com.tesora.dve.sql.transform.execution.CatalogModificationExecutionStep;
import com.tesora.dve.sql.transform.execution.ExecutionSequence;
import com.tesora.dve.sql.transform.execution.ExternalServiceExecutionStep;

public class PEDropExternalServiceStatement extends
		PEDropStatement<PEExternalService, ExternalService> {

	public PEDropExternalServiceStatement(
			Class<?> tschemaClass, Boolean ifExists, boolean peOnly,
			Persistable<PEExternalService, ExternalService> targ, String tag) {
		super(tschemaClass, ifExists, peOnly, targ, tag);
	}

	@Override
	protected CatalogModificationExecutionStep buildStep(SchemaContext pc) throws PEException {
		return new ExternalServiceExecutionStep(getDatabase(pc),
				getStorageGroup(pc), getRoot(), getAction(), getSQLCommand(pc),
				getDeleteObjects(pc), getCatalogObjects(pc), CacheInvalidationRecord.GLOBAL);
	}

	@Override
	public void plan(SchemaContext pc, ExecutionSequence es) throws PEException {
		// drop the storage database if necessary
		ExternalService externalService = (ExternalService) pc.getBacking(getTarget());
		if (externalService.usesDataStore()) {
			Name dataStoreName = new UnqualifiedName(
					externalService.getDataStoreName());
			boolean isMT = false;
			PEDatabase peDb = pc.findPEDatabase(dataStoreName);
			if (peDb != null) {
				isMT = peDb.getMTMode().isMT();
			}
			Statement dropDbStatement = pc.getPolicyContext()
					.buildDropDatabaseStatement(dataStoreName, isIfExists(),
							isMT, tag);
			dropDbStatement.plan(pc,es);
		}

		// add drop of external service
		super.plan(pc,es);
	}
}
