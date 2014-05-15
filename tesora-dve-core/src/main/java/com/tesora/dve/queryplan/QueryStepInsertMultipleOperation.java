// OS_STATUS: public
package com.tesora.dve.queryplan;

import java.util.Collection;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.tesora.dve.common.catalog.DistributionModel;
import com.tesora.dve.common.catalog.UserDatabase;
import com.tesora.dve.db.DBResultConsumer;
import com.tesora.dve.distribution.KeyValue;
import com.tesora.dve.server.connectionmanager.SSConnection;
import com.tesora.dve.server.messaging.SQLCommand;
import com.tesora.dve.worker.WorkerGroup;

public class QueryStepInsertMultipleOperation extends QueryStepDMLOperation {
	
	static Logger logger = Logger.getLogger( QueryStepInsertMultipleOperation.class );

	DistributionModel distModel;
	Collection<Entry<SQLCommand, KeyValue>> insertList;
	
	public QueryStepInsertMultipleOperation(UserDatabase database,
			DistributionModel distModel, Collection<Entry<SQLCommand, KeyValue>> insertList) {
		super(database);
		this.distModel = distModel;
		this.insertList = insertList;
	}

	@Override
	public void execute(SSConnection ssCon, WorkerGroup wg, DBResultConsumer resultConsumer) throws Throwable {
		resultConsumer.setSenderCount(insertList.size());
		beginExecution();
		for (Entry<SQLCommand, KeyValue> entry : insertList) {
			QueryStepInsertByKeyOperation
					.executeInsertByKey(ssCon, wg, resultConsumer, database, entry.getKey(),
							entry.getValue());
		}
		endExecution(resultConsumer.getUpdateCount());
	}
}
