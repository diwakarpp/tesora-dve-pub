// OS_STATUS: public
package com.tesora.dve.server.messaging;


import java.sql.SQLException;

import javax.transaction.xa.XAException;

import org.apache.log4j.Logger;

import com.tesora.dve.common.catalog.PersistentDatabase;
import com.tesora.dve.comms.client.messages.ExecuteResponse;
import com.tesora.dve.comms.client.messages.MessageType;
import com.tesora.dve.comms.client.messages.MessageVersion;
import com.tesora.dve.comms.client.messages.ResponseMessage;
import com.tesora.dve.db.DBEmptyTextResultConsumer;
import com.tesora.dve.db.DBResultConsumer;
import com.tesora.dve.exceptions.PEException;
import com.tesora.dve.resultset.ColumnSet;
import com.tesora.dve.server.connectionmanager.SSContext;
import com.tesora.dve.server.statistics.manager.LogSiteStatisticRequest;
import com.tesora.dve.server.statistics.SiteStatKey.OperationClass;
import com.tesora.dve.worker.Worker;
import com.tesora.dve.worker.WorkerStatement;

public class WorkerExecuteRequest extends WorkerRequest {
	
	static Logger logger = Logger.getLogger( WorkerExecuteRequest.class );

	private static final long serialVersionUID = 1L;

	PersistentDatabase defaultDatabase;
	final SQLCommand command;

	boolean recoverLocks;

	public WorkerExecuteRequest(SSContext ssContext, SQLCommand command) {
		super(ssContext);
		this.command = command;
	}
	
	public WorkerExecuteRequest onDatabase(PersistentDatabase database) {
		defaultDatabase = database;
		return this;
	}

	public WorkerExecuteRequest withLockRecovery(boolean recoverLocks) {
		this.recoverLocks = recoverLocks;
		return this;
	}
	
	@Override
	public ResponseMessage executeRequest(Worker w, DBResultConsumer resultConsumer) throws SQLException, XAException, PEException {
		return executeStatement(w, getCommand(), resultConsumer);
	}
	
	protected ResponseMessage executeStatement(Worker w, SQLCommand stmtCommand, DBResultConsumer resultConsumer) throws SQLException, PEException, XAException {
		ResponseMessage resp = null;
		long rowCount = -1;
		boolean hasResults = false;
		ColumnSet rsmd = null;
		Exception anyException = null;
				
		w.setCurrentDatabase(defaultDatabase);
		
		// do any late resolution
		
		if (isAutoTransact())
			w.startTrans(getTransId());
		
		try {
			String savepointId = null;
			
			if (recoverLocks) {
				savepointId = "barrier" + w.getUniqueValue();
				w.getStatement().execute(getConnectionId(), new SQLCommand("savepoint " + savepointId),
						DBEmptyTextResultConsumer.INSTANCE);
			}

			WorkerStatement stmt;
//			if (stmtCommand.isPreparedStatement()) {
//				WorkerPreparedStatement pstmt = w.prepareStatement(stmtCommand);
//				stmtCommand.fillParameters(pstmt);
//				hasResults = pstmt.execute();
//				stmt = pstmt;
//			} else {
				stmt = w.getStatement();
				hasResults = stmt.execute(getConnectionId(), stmtCommand, resultConsumer);
//			}
			
			if (recoverLocks) {
				boolean rowsFound = (hasResults && stmt.getResultSet().isBeforeFirst()) 
						|| (!hasResults && resultConsumer.getUpdateCount() > 0);
				if (!rowsFound) {
					w.getStatement().execute(getConnectionId(), new SQLCommand("rollback to " + savepointId),
							DBEmptyTextResultConsumer.INSTANCE);
				}
			}
			
			
//			if (hasResults) {
//				ResultChunkManager rcm = new ResultChunkManager(stmt.getResultSet(), Host.getProperties(), "worker", command); 
//				w.setChunkManager( rcm );
//				rsmd = rcm.getMetaData();
//			}
//			else
				rowCount = resultConsumer.getUpdateCount();
			
			resp = new ExecuteResponse(hasResults, rowCount, rsmd ).from(w.getAddress()).success();			
		} catch (PEException pe) {
			anyException = pe;
			throw pe;
		} finally {
			if (logger.isDebugEnabled())
				logger.debug(new StringBuilder("WorkerExecuteRequest/w(").append(w.getName()).append("/").append(w.getCurrentDatabaseName()).append("): exec'd \"")
						.append(stmtCommand).append("\" updating ").append(rowCount).append(" rows (hasResults=")
						.append(hasResults ? "true" : "false")
						.append(")").append(" except=")
						.append(anyException == null ? "none" : anyException.getMessage())						
						.toString());
		}
		return resp;
	}

	@Override
	public String toString() {
		return new StringBuffer().append("WorkerExecuteRequest("+getCommand()+")").toString();
	}

	public SQLCommand getCommand() {
		return command;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.W_EXECUTE_REQUEST;
	}

	@Override
	public MessageVersion getVersion() {
		return MessageVersion.VERSION1;
	}


	@Override
	public LogSiteStatisticRequest getStatisticsNotice() {
		return new LogSiteStatisticRequest(OperationClass.EXECUTE);
	}
}
