// OS_STATUS: public
package com.tesora.dve.queryplan;

import javax.xml.bind.annotation.XmlType;

import com.tesora.dve.server.global.HostService;
import com.tesora.dve.singleton.Singletons;
import org.apache.log4j.Logger;

import com.tesora.dve.common.catalog.PersistentDatabase;
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
	
	public QueryStepSelectByKeyOperation(PersistentDatabase execCtxDBName, IKeyValue distValue, SQLCommand command)
			throws PEException {
		super(execCtxDBName);
		if (command.isEmpty())
			throw new PEException("Cannot create QueryStep with empty SQL command");

		this.command = command;
		this.distValue = distValue;
	}

	public QueryStepSelectByKeyOperation(PersistentDatabase execCtxDBName, IKeyValue distValue, String command) 
			throws PEException {
		this(execCtxDBName, distValue, new SQLCommand(command));
	}
	
	@Override
	public void execute(SSConnection ssCon, WorkerGroup wg, DBResultConsumer resultConsumer) throws Throwable {
		resultConsumer.setResultsLimit(getResultsLimit());

        final boolean savepointRequired = ssCon.getTransId() != null && command.isForUpdateStatement()
				&& "5.6".equals(Singletons.require(HostService.class).getDveVersion(ssCon));
		
		WorkerExecuteRequest req =
				new WorkerExecuteRequest(ssCon.getTransactionalContext(), command).
				onDatabase(database).withLockRecovery(savepointRequired);
		
		WorkerGroup.MappingSolution mappingSolution = 
				distValue.getDistributionModel().mapKeyForQuery(
						ssCon.getCatalogDAO(), wg.getGroup(), distValue,
						command.isForUpdateStatement() ? DistKeyOpType.SELECT_FOR_UPDATE : DistKeyOpType.QUERY);
		if (logger.isDebugEnabled())
			logger.debug(this.getClass().getSimpleName() + " maps dv " + distValue + " to " + mappingSolution);
		
		resultConsumer.setResultsLimit(getResultsLimit());
		resultConsumer.setSenderCount(mappingSolution.computeSize(wg));
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
	public boolean requiresTransaction() {
		return false;
	}
	
	@Override
	public String describeForLog() {
		StringBuilder buf = new StringBuilder();
		buf.append("SelectByKey key=").append(distValue).append(", stmt=").append(command.getRawSQL());
		return buf.toString();
	}
}
