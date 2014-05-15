// OS_STATUS: public
package com.tesora.dve.db;

import io.netty.channel.Channel;

import java.util.List;

import com.tesora.dve.common.catalog.StorageSite;
import com.tesora.dve.concurrent.PEFuture;
import com.tesora.dve.concurrent.PEPromise;
import com.tesora.dve.db.mysql.portal.protocol.MysqlGroupedPreparedStatementId;
import com.tesora.dve.db.mysql.MysqlStmtCloseCommand;
import com.tesora.dve.db.mysql.libmy.MyPreparedStatement;
import com.tesora.dve.exceptions.PEException;
import com.tesora.dve.resultset.ColumnSet;
import com.tesora.dve.resultset.ResultRow;
import com.tesora.dve.server.messaging.SQLCommand;

public class MysqlStmtCloseDiscarder implements DBResultConsumer {
	
	final MyPreparedStatement<MysqlGroupedPreparedStatementId> pstmt;

	public MysqlStmtCloseDiscarder(
			MyPreparedStatement<MysqlGroupedPreparedStatementId> pstmt) {
		super();
		this.pstmt = pstmt;
	}

	@Override
	public void setSenderCount(int senderCount) {
	}

	@Override
	public boolean hasResults() {
		return false;
	}

	@Override
	public long getUpdateCount() throws PEException {
		return 0;
	}

	@Override
	public void setResultsLimit(long resultsLimit) {
	}

	@Override
	public void inject(ColumnSet metadata, List<ResultRow> rows)
			throws PEException {
	}

	@Override
	public void setRowAdjuster(RowCountAdjuster rowAdjuster) {
	}

	@Override
	public void setNumRowsAffected(long rowcount) {
	}

	@Override
	public PEFuture<Boolean> writeCommandExecutor(Channel channel,
			StorageSite site, DBConnection.Monitor connectionMonitor, SQLCommand sql, PEPromise<Boolean> promise) {
		channel.write(new MysqlStmtCloseCommand(pstmt));
		return promise.success(false);
	}

	@Override
	public boolean isSuccessful() {
		return false;
	}

	@Override
	public void rollback() {
	}

}
