package com.tesora.dve.sql.node.expression;

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

import com.tesora.dve.sql.schema.ConnectionValues;
import com.tesora.dve.sql.schema.cache.ConstantType;
import com.tesora.dve.sql.schema.cache.IAutoIncrementLiteralExpression;

public class CachedAutoIncrementLiteralExpression extends
		CachedDelegatingLiteralExpression implements IAutoIncrementLiteralExpression {

	private final int blockIndex;
	
	public CachedAutoIncrementLiteralExpression(int type, int offset, int blockIndex) {
		super(type,offset,null);
		this.blockIndex = blockIndex;
	}
	
	@Override
	public Object getValue(ConnectionValues cv) {
		return cv.getAutoincValue(this);
	}

	@Override
	public ConstantType getConstantType() {
		return ConstantType.AUTOINCREMENT_LITERAL;
	}

	@Override
	public int getBlockIndex() {
		return blockIndex;
	}

}
