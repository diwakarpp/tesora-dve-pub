// OS_STATUS: public
package com.tesora.dve.sql.infoschema;

import com.tesora.dve.sql.infoschema.annos.InfoView;
import com.tesora.dve.sql.node.LanguageNode;
import com.tesora.dve.sql.node.expression.ColumnInstance;
import com.tesora.dve.sql.node.expression.ExpressionNode;
import com.tesora.dve.sql.node.expression.LiteralExpression;
import com.tesora.dve.sql.schema.UnqualifiedName;
import com.tesora.dve.sql.schema.types.Type;

// columns with constant value.
public class ConstantSyntheticInformationSchemaColumn extends
		SyntheticInformationSchemaColumn {

	private final Object constantValue;
	private SyntheticLogicalInformationSchemaColumn logicalColumn = null;
	
	public ConstantSyntheticInformationSchemaColumn(InfoView view,
			UnqualifiedName nameInView, Type type, Object value) {
		super(view, nameInView, type);
		constantValue = value;
	}

	@Override
	public ExpressionNode buildReplacement(ColumnInstance in) {
		if (constantValue instanceof String)
			return LiteralExpression.makeStringLiteral((String)constantValue);
		else if (constantValue instanceof Long)
			return LiteralExpression.makeLongLiteral((Long)constantValue);
		else if (constantValue instanceof Integer)
			return LiteralExpression.makeLongLiteral(((Integer)constantValue).longValue());
		else if (constantValue == null)
			return LiteralExpression.makeNullLiteral();
		else
			throw new InformationSchemaException("Invalid constant value: " + constantValue);
	}

	@Override
	public LogicalInformationSchemaColumn getLogicalColumn() {
		if (logicalColumn != null) return logicalColumn;
		
		logicalColumn = new SyntheticLogicalInformationSchemaColumn(getName().getUnqualified(), getType()) {

			@Override
			public LanguageNode explode(ColumnInstance subject) {
				return buildReplacement(subject);
			}
			
			@Override
			public Object getValue() {
				return constantValue;
			}
		};
		
		return logicalColumn;
	}
}
