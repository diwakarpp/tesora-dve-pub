// OS_STATUS: public
package com.tesora.dve.sql.statement.session;

import java.util.ArrayList;
import java.util.List;

import com.tesora.dve.distribution.StaticDistributionModel;
import com.tesora.dve.exceptions.PEException;
import com.tesora.dve.queryplan.QueryStep;
import com.tesora.dve.queryplan.QueryStepSelectAllOperation;
import com.tesora.dve.resultset.ProjectionInfo;
import com.tesora.dve.server.global.HostService;
import com.tesora.dve.singleton.Singletons;
import com.tesora.dve.sql.node.expression.TableInstance;
import com.tesora.dve.sql.schema.Database;
import com.tesora.dve.sql.schema.PEPersistentGroup;
import com.tesora.dve.sql.schema.PEStorageGroup;
import com.tesora.dve.sql.schema.SchemaContext;
import com.tesora.dve.sql.statement.StatementType;
import com.tesora.dve.sql.transform.execution.ExecutionPlanOptions;
import com.tesora.dve.sql.transform.execution.ExecutionSequence;
import com.tesora.dve.sql.transform.execution.SessionExecutionStep;

public class TableMaintenanceStatement extends SessionStatement {
	public enum MaintenanceCommandType {
		UNKNOWN("UNKNOWN"),
		ANALYZE("ANALYZE"),
		CHECK("CHECK"),
		OPTIMIZE("OPTIMIZE")
		;

		private final String sqlCommand;
		
		private MaintenanceCommandType(String command) {
			this.sqlCommand = command;
		}
		
		public String getSqlCommand() {
			return sqlCommand;
		}
	}

	public enum MaintenanceOptionType {
		NONE(""),
		LOCAL("LOCAL"),
		NO_WRITE_TO_BINLOG("NO_WRITE_TO_BINLOG")
		;

		private final String sql;
		
		private MaintenanceOptionType(String sql) {
			this.sql = sql;
		}
		
		public String getSql() {
			return sql;
		}
	}

	private MaintenanceCommandType command = MaintenanceCommandType.UNKNOWN;
	protected MaintenanceOptionType option = MaintenanceOptionType.NONE;
	protected List<TableInstance> tableInstanceList = new ArrayList<TableInstance>();

	public TableMaintenanceStatement(MaintenanceCommandType command, MaintenanceOptionType option, List<TableInstance> tbls) {
		super();
		this.command = command;
		if (option != null) {
			this.option = option;
		}
		// we don't support table maintenance across Persistent Groups at the moment, and the check
		// has already been done in TranslatorUtils
		this.tableInstanceList = tbls;
	}

	public List<TableInstance> getTableInstanceList() {
		return tableInstanceList;
	}

	@Override
	public void plan(SchemaContext pc, ExecutionSequence es) throws PEException {
		StringBuilder buf = new StringBuilder();
        Singletons.require(HostService.class).getDBNative().getEmitter().emitTableMaintenanceStatement(pc, this, buf, -1);
		// we only allow on a single Persistent Group, so grab the first one
		PEPersistentGroup sg = tableInstanceList.get(0).getAbstractTable().getPersistentStorage(pc);
		es.append(new TableMaintenanceExecutionStep(pc.getCurrentDatabase(), sg, buf.toString()));
	}

	public MaintenanceCommandType getCommand() {
		return command;
	}
	
	public MaintenanceOptionType getOption() {
		return option;
	}
	
	@Override
	public StatementType getStatementType() {
		StatementType st = super.getStatementType();
		
		switch(command) {
		case ANALYZE:
			st = StatementType.ANALYZE_TABLE;
			break;
			
		case CHECK:
			st = StatementType.CHECK_TABLE;
			break;
			
		case OPTIMIZE:
			st = StatementType.OPTIMIZE_TABLE;
			break;
			
		default:
			break;
		}
		return st;
	}

	private class TableMaintenanceExecutionStep extends SessionExecutionStep {
		
		public TableMaintenanceExecutionStep(Database<?> db, PEStorageGroup storageGroup, String sql) {
			super(db, storageGroup, sql);
		}

		@Override
		public void schedule(ExecutionPlanOptions opts, List<QueryStep> qsteps, ProjectionInfo projection, SchemaContext sc)
				throws PEException {
			addStep(sc,qsteps,new QueryStepSelectAllOperation(
					getPersistentDatabase(), StaticDistributionModel.SINGLETON, getSQLCommand())) ;
		}

	}
}
