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

import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import com.tesora.dve.common.catalog.PersistentDatabase;
import com.tesora.dve.common.catalog.StorageGroup;
import com.tesora.dve.db.DBResultConsumer;
import com.tesora.dve.distribution.IKeyValue;
import com.tesora.dve.exceptions.PEException;
import com.tesora.dve.server.connectionmanager.SSConnection;
import com.tesora.dve.server.messaging.SQLCommand;
import com.tesora.dve.server.messaging.WorkerExecuteRequest;
import com.tesora.dve.sql.schema.SchemaContext.DistKeyOpType;
import com.tesora.dve.worker.MysqlParallelResultConsumer;
import com.tesora.dve.worker.WorkerGroup;

@XmlType(name="QueryStepQueryByKeyOperation")
public class QueryStepSelectByKeyOperation extends QueryStepResultsOperation {

	static Logger logger = Logger.getLogger( QueryStepSelectByKeyOperation.class );

	SQLCommand command;
	IKeyValue distValue;
	
	public QueryStepSelectByKeyOperation(StorageGroup sg, PersistentDatabase execCtxDBName, IKeyValue distValue, SQLCommand command)
			throws PEException {
		super(sg, execCtxDBName);
		if (command.isEmpty())
			throw new PEException("Cannot create QueryStep with empty SQL command");

		this.command = command;
		this.distValue = distValue;
	}

	public QueryStepSelectByKeyOperation(StorageGroup sg, final SSConnection ssCon, PersistentDatabase execCtxDBName, IKeyValue distValue, String command)
			throws PEException {
		this(sg, execCtxDBName, distValue, new SQLCommand(ssCon, command));
	}
	
	@Override
	public void executeSelf(ExecutionState estate, WorkerGroup wg, DBResultConsumer resultConsumer) throws Throwable {
		resultConsumer.setResultsLimit(getResultsLimit());

		SSConnection ssCon = estate.getConnection();
		
		SQLCommand bound = bindCommand(estate,command);
		        
		WorkerExecuteRequest req =
				new WorkerExecuteRequest(ssCon.getTransactionalContext(), bound).
				onDatabase(database);
		
		WorkerGroup.MappingSolution mappingSolution = 
				distValue.getDistributionModel().mapKeyForQuery(
						ssCon.getCatalogDAO(), wg.getGroup(), distValue,
						bound.isForUpdateStatement() ? DistKeyOpType.SELECT_FOR_UPDATE : DistKeyOpType.QUERY);
		if (logger.isDebugEnabled())
			logger.debug(this.getClass().getSimpleName() + " maps dv " + distValue + " to " + mappingSolution);
		
		resultConsumer.setResultsLimit(getResultsLimit());
		beginExecution();
		wg.execute(mappingSolution, req, resultConsumer);
		if (resultConsumer instanceof MysqlParallelResultConsumer) 
			endExecution(((MysqlParallelResultConsumer)resultConsumer).getNumRowsAffected());

		
		
//		ResultCollectorData results = ResultCollectorFactory.getInstance(ssCon.getTransactionalContext(), wg, mappingSolution);
//		try {
//			resultConsumer.setResultsLimit(getResultsLimit());
//			resultConsumer.setSenderCount(results.getWorkerGroup().size());
//			
//			results.sendToWorkers(req);
//			results.adjustUpdateCountForDistributionModel(distValue.getDistributionModel());
//		} catch (Throwable t) {
//			ResultCollectorFactory.returnInstance(results);
//			throw t;
//		}
//
//		return results;
	}

	@Override
	public boolean requiresTransactionSelf() {
		return false;
	}
	
	@Override
	public String describeForLog() {
		StringBuilder buf = new StringBuilder();
		buf.append("SelectByKey key=").append(distValue).append(", stmt=").append(command.getRawSQL());
		return buf.toString();
	}
}
