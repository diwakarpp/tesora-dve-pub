package com.tesora.dve.queryplan;

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

import org.apache.log4j.Logger;

import com.tesora.dve.common.catalog.StorageGroup;
import com.tesora.dve.common.catalog.UserDatabase;
import com.tesora.dve.common.catalog.UserTable;
import com.tesora.dve.db.DBResultConsumer;
import com.tesora.dve.exceptions.PEException;
import com.tesora.dve.server.messaging.WorkerExecuteRequest;
import com.tesora.dve.server.messaging.WorkerRequest;
import com.tesora.dve.worker.WorkerGroup;
import com.tesora.dve.worker.WorkerGroup.MappingSolution;

public class QueryStepDropTempTableOperation extends QueryStepOperation {


	static Logger logger = Logger.getLogger( QueryStepDropTempTableOperation.class );
	
	UserDatabase database;
	String tableName;
	
	public QueryStepDropTempTableOperation(StorageGroup sg, UserDatabase udb, String tableName) throws PEException {
		super(sg);
		this.database = udb;
		this.tableName = tableName;
	}

	@Override
	public void executeSelf(ExecutionState estate, WorkerGroup wg, DBResultConsumer resultConsumer)
			throws Throwable {
		WorkerRequest req = 
				new WorkerExecuteRequest(estate.getNonTransactionalContext(), UserTable.getDropTableStmt(estate.getConnection(), tableName, false)).
				onDatabase(database);
		wg.execute(MappingSolution.AllWorkers, req, resultConsumer);
	}

	@Override
	public boolean requiresTransactionSelf() {
		return false;
	}

}
