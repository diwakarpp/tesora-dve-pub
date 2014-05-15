// OS_STATUS: public
package com.tesora.dve.sql.transform.strategy.join;

import java.util.List;

import com.tesora.dve.sql.expression.ExpressionUtils;
import com.tesora.dve.sql.expression.TableKey;
import com.tesora.dve.sql.node.expression.ColumnInstance;
import com.tesora.dve.sql.node.expression.ExpressionNode;
import com.tesora.dve.sql.node.expression.FunctionCall;
import com.tesora.dve.sql.node.expression.Wildcard;
import com.tesora.dve.sql.node.test.EngineConstant;
import com.tesora.dve.sql.schema.Column;
import com.tesora.dve.sql.schema.SchemaContext;
import com.tesora.dve.sql.statement.dml.SelectStatement;
import com.tesora.dve.sql.transform.ColumnInstanceCollector;
import com.tesora.dve.sql.util.ListSet;

class P1ProjectionBuffer extends Buffer {

	public P1ProjectionBuffer(Buffer prev) {
		super(BufferKind.P1, prev);
	}
	
	private void analyze(SchemaContext sc, SelectStatement stmt, boolean forceExpansion) {
		for(BufferEntry be : getPreviousBuffer().getEntries()) {
			be.setAfterOffsetBegin(size());
			List<ExpressionNode> nexts = be.getNext();
			for(ExpressionNode en : nexts) {
				ListSet<ColumnInstance> cols = ColumnInstanceCollector.getColumnInstances(en);
				BufferEntry nbe = null;
				if (cols.isEmpty()) {
					if (!forceExpansion)
						continue;
					ExpressionNode targ = ExpressionUtils.getTarget(en);
					if (EngineConstant.FUNCTION.has(targ, EngineConstant.COUNT)) {
						FunctionCall fc = (FunctionCall)targ;
						if (fc.getParametersEdge().get(0) instanceof Wildcard) {
							ListSet<TableKey> tabs = EngineConstant.TABLES.getValue(stmt,sc);
							cols = new ListSet<ColumnInstance>();
							for(TableKey tk : tabs) {
								for(Column<?> c : tk.getTable().getColumns(sc)) {
									cols.add(new ColumnInstance(c,tk.toInstance()));
								}
							}
						}
					}
					if (cols.isEmpty())
						continue;
					nbe = new SubstitutingBufferEntry(en, cols);
				} else if (!(en instanceof ColumnInstance)) {
					ListSet<ExpressionNode> ugh = new ListSet<ExpressionNode>();
					for(ColumnInstance ci : cols)
						ugh.add(ci);
					nbe = new ExplodingBufferEntry(en,ugh);						
				} else {
					nbe = new BufferEntry(en);					
				}
				be.addDependency(nbe);
				add(nbe);
			}
			be.setAfterOffsetEnd(size());
		}					
	}
	
	@Override
	public void adapt(SchemaContext sc, SelectStatement stmt) {
		analyze(sc, stmt, false);
		if (size() == 0)
			analyze(sc, stmt, true);
	}
	
}