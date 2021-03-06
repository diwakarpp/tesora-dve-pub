package com.tesora.dve.sql.transform.strategy;

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

import java.util.List;

import com.tesora.dve.sql.node.expression.ExpressionNode;
import com.tesora.dve.sql.schema.SchemaContext;
import com.tesora.dve.sql.statement.dml.SelectStatement;
import com.tesora.dve.sql.transform.CopyVisitor;

public class MutatorState {

	protected SelectStatement original;
	protected SelectStatement statement;
	
	public MutatorState(SelectStatement orig) {
		original = orig;
		statement = original;
	}
	
	public SelectStatement getStatement() {
		return statement;
	}
	
	public void combine(SchemaContext sc, List<ExpressionNode> projection, boolean makeCopy) {
		if (makeCopy)
			statement = CopyVisitor.copy(original);
		statement.setProjection(projection);
		statement.normalize(sc);
	}
	

}
