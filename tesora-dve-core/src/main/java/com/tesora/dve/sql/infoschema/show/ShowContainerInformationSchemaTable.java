package com.tesora.dve.sql.infoschema.show;

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
import java.util.HashMap;
import java.util.List;

import com.tesora.dve.common.catalog.CatalogEntity;
import com.tesora.dve.common.catalog.UserTable;
import com.tesora.dve.resultset.ColumnSet;
import com.tesora.dve.resultset.IntermediateResultSet;
import com.tesora.dve.resultset.ResultRow;
import com.tesora.dve.sql.infoschema.LogicalInformationSchemaTable;
import com.tesora.dve.sql.schema.Name;
import com.tesora.dve.sql.schema.SchemaContext;
import com.tesora.dve.sql.schema.UnqualifiedName;

public class ShowContainerInformationSchemaTable extends ShowInformationSchemaTable {

	public ShowContainerInformationSchemaTable(
			LogicalInformationSchemaTable basedOn, UnqualifiedName viewName,
			UnqualifiedName pluralViewName, boolean isPriviledged,
			boolean isExtension) {
		super(basedOn, viewName, pluralViewName, isPriviledged, isExtension);
	}

	@Override
	public IntermediateResultSet executeUniqueSelect(SchemaContext sc,
			Name onName) {
		String query = null;
		HashMap<String, Object> params = new HashMap<String, Object>();
		query = "select ut from UserTable ut, Container c where c.id=ut.container.id and c.name = :containername";
		params.put("containername", onName.getUnquotedName().get());
		List<CatalogEntity> ents = sc.getCatalog().query(query, params);

		ColumnSet md = new ColumnSet();
		md.addColumn("Table", 255, "varchar", java.sql.Types.VARCHAR);
		md.addColumn("Type", 10, "varchar", java.sql.Types.VARCHAR);

		List<ResultRow> rows = new ArrayList<ResultRow>();
		for (CatalogEntity ent : ents) {
			ResultRow rr = new ResultRow();
			rr.addResultColumn(((UserTable) ent).getName());
			rr.addResultColumn(((UserTable) ent).isContainerBaseTable() ? "base"
					: "member");
			rows.add(rr);
		}

		return new IntermediateResultSet(md, rows);
	}
}