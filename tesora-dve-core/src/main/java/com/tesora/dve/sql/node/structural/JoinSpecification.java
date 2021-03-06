package com.tesora.dve.sql.node.structural;

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

import java.util.HashMap;
import java.util.Map;

import com.tesora.dve.sql.ParserException.Pass;
import com.tesora.dve.sql.SchemaException;

public final class JoinSpecification {

	public enum OuterJoin {
		LEFT("LEFT OUTER"), 
		RIGHT("RIGHT OUTER"), 
		FULL("FULL OUTER");
		
		private final String sql;
		private OuterJoin(String sql) {
			this.sql = sql;
		}
		
		public String getSQL() { 
			return this.sql;
		}
	}

	// if null, then this is an inner join
	private OuterJoin outerJoinKind; 
	// true if this is a cartesian join
	private boolean cross;
	// true if this is a natural join
	private boolean natural;
	
	// default is inner
	private JoinSpecification() {
		outerJoinKind = null;
		cross = false;
		natural = false;
	}
	
	private JoinSpecification(boolean isCross, boolean isNatural) {
		if (isCross && isNatural) {
			throw new IllegalArgumentException("A join cannot be both cross and natural at the same time.");
		}

		outerJoinKind = null;
		this.cross = isCross;
		this.natural = isNatural;
	}
	
	private JoinSpecification(OuterJoin variety) {
		outerJoinKind = variety;
		this.cross = false;
		this.natural = false;
	}
	
	private JoinSpecification(OuterJoin variety, boolean isNatural) {
		outerJoinKind = variety;
		this.cross = false;
		this.natural = isNatural;
	}

	public boolean isInnerJoin() { return outerJoinKind == null; }
	public boolean isCrossJoin() { return isInnerJoin() && this.cross; }
	public boolean isNaturalJoin() { return this.natural; }
	
	public OuterJoin getOuterJoinKind() { return outerJoinKind; }
	
	public boolean isLeftOuterJoin() { return outerJoinKind == OuterJoin.LEFT; }
	public boolean isRightOuterJoin() { return outerJoinKind == OuterJoin.RIGHT; }
	public boolean isFullOuterJoin() { return outerJoinKind == OuterJoin.FULL; }
	public boolean isOuterJoin() { return outerJoinKind != null; }
	public static final JoinSpecification INNER_JOIN = new JoinSpecification();
	// note: this is mysql specific!
	public static final JoinSpecification CROSS_JOIN = INNER_JOIN;
	public static final JoinSpecification LEFT_OUTER_JOIN = new JoinSpecification(OuterJoin.LEFT);
	public static final JoinSpecification NATURAL_JOIN = new JoinSpecification(false, true);
	public static final JoinSpecification NATURAL_LEFT_OUTER_JOIN = new JoinSpecification(OuterJoin.LEFT, true);
	private static Map<String, JoinSpecification> supported = buildSupportedJoins();
	
	private static Map<String, JoinSpecification> buildSupportedJoins() {
		HashMap<String, JoinSpecification> ret = new HashMap<String, JoinSpecification>();
		ret.put("INNER", INNER_JOIN);
		ret.put("LEFT OUTER", LEFT_OUTER_JOIN);
		ret.put("RIGHT OUTER", new JoinSpecification(OuterJoin.RIGHT));
		ret.put("FULL OUTER", new JoinSpecification(OuterJoin.FULL));
		ret.put("LEFT", LEFT_OUTER_JOIN);
		ret.put("RIGHT", new JoinSpecification(OuterJoin.RIGHT));
		ret.put("FULL", new JoinSpecification(OuterJoin.FULL));
		ret.put("CROSS",INNER_JOIN);
		ret.put("NATURAL", NATURAL_JOIN);
		ret.put("NATURAL LEFT", NATURAL_LEFT_OUTER_JOIN);
		ret.put("NATURAL LEFT OUTER", NATURAL_LEFT_OUTER_JOIN);

		return ret;
	}
	
	public static JoinSpecification makeJoinSpecification(String joinWords) {
		JoinSpecification js = supported.get(joinWords.trim());
		if (js == null) 
			throw new SchemaException(Pass.SECOND, "No support for join type : " + joinWords);
		return js;
	}

	public String getSQL() {
		if (outerJoinKind == null) {
			if (natural) {
				return "NATURAL";
			}
			return "INNER";
		}

		final String outerJoinSql = outerJoinKind.getSQL();
		return (this.natural) ? "NATURAL " + outerJoinSql : outerJoinSql;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (cross ? 1231 : 1237);
		result = prime * result + (natural ? 1231 : 1237);
		result = prime * result
				+ ((outerJoinKind == null) ? 0 : outerJoinKind.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JoinSpecification other = (JoinSpecification) obj;
		if (cross != other.cross)
			return false;
		if (natural != other.natural)
			return false;
		if (outerJoinKind != other.outerJoinKind)
			return false;
		return true;
	}
	
}
