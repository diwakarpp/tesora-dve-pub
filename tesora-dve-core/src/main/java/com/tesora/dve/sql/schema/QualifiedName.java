package com.tesora.dve.sql.schema;

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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.tesora.dve.sql.util.Functional;
import com.tesora.dve.sql.util.UnaryFunction;
import com.tesora.dve.sql.util.UnaryPredicate;

public class QualifiedName extends Name {

	private static final long serialVersionUID = 1L;
	public static final String PART_SEPARATOR = ".";

	private List<UnqualifiedName> names;
	
	public QualifiedName(List<UnqualifiedName> parts) {
		names = parts;
	}

	public QualifiedName(UnqualifiedName ...parts) {
		names = Arrays.asList(parts);
	}
	
	public QualifiedName(String name) {
		this(StringUtils.split(name, PART_SEPARATOR));
	}

	protected QualifiedName(String... parts) {
		names = new ArrayList<UnqualifiedName>(parts.length);
		for (final String part : parts) {
			names.add(new UnqualifiedName(part));
		}
	}

	@Override
	public String get() {
		return join(new UnaryFunction<String, UnqualifiedName>() {

			@Override
			public String evaluate(UnqualifiedName object) {
				return object.get();
			}
			
		});
	}

	@Override
	public String getQuoted() {
		return join(new UnaryFunction<String, UnqualifiedName>() {

			@Override
			public String evaluate(UnqualifiedName object) {
				return object.getQuoted();
			}
			
		});
	}

	@Override
	public boolean isQuoted() {
		return Functional.all(names, new UnaryPredicate<UnqualifiedName>() {

			@Override
			public boolean test(UnqualifiedName object) {
				return object.isQuoted();
			}
			
		});
	}

	@Override
	public boolean isQualified() {
		return true;
	}

	@Override
	public String getSQL() {
		return join(new UnaryFunction<String, UnqualifiedName>() {

			@Override
			public String evaluate(UnqualifiedName object) {
				return object.getSQL();
			}
			
		});
	}
	
	private String join(UnaryFunction<String, UnqualifiedName> proc) {
		StringBuilder buf = new StringBuilder();
		List<String> strs = Functional.apply(names, proc);
		Functional.join(strs, buf, PART_SEPARATOR);
		return buf.toString();
	}

	@Override
	public Name getCapitalized() {
		if (isQuoted())
			return null;
		return new QualifiedName(Functional.apply(names, new UnaryFunction<UnqualifiedName, UnqualifiedName>() {

			@Override
			public UnqualifiedName evaluate(UnqualifiedName object) {
				return (UnqualifiedName)object.getCapitalized();
			}
			
		}));
	}

	@Override
	public Name getQuotedName() {
		if (isQuoted())
			return this;
		return new QualifiedName(Functional.apply(names, new UnaryFunction<UnqualifiedName, UnqualifiedName>() {

			@Override
			public UnqualifiedName evaluate(UnqualifiedName object) {
				return (UnqualifiedName)object.getQuotedName();
			}
			
		}));
	}
	
	@Override
	public Name getUnquotedName() {
		return new QualifiedName(Functional.apply(names, new UnaryFunction<UnqualifiedName, UnqualifiedName>() {

			@Override
			public UnqualifiedName evaluate(UnqualifiedName object) {
				return object.getUnquotedName().getUnqualified();
			}
			
		}));
	}
	
	@Override
	public UnqualifiedName getUnqualified() {
		return names.get(names.size() - 1);
	}
	
	public int getQualifiedDepth() { return names.size(); }
	public UnqualifiedName getNamespace() {
		if (names.size() > 1)
			return names.get(names.size() - 2);
		return null;
	}
	
	@Override
	public List<UnqualifiedName> getParts() { return Collections.unmodifiableList(names); }
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((names == null) ? 0 : names.hashCode());
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
		QualifiedName other = (QualifiedName) obj;
		if (names == null) {
			if (other.names != null)
				return false;
		} else if (!names.equals(other.names))
			return false;
		return true;
	}

	@Override
	public Name copy() {
		return new QualifiedName(new ArrayList<UnqualifiedName>(names));
	}
	
	
	
}
