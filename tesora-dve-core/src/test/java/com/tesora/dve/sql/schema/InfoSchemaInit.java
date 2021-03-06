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

import java.util.List;

import com.tesora.dve.server.global.HostService;
import com.tesora.dve.singleton.Singletons;
import com.tesora.dve.sql.infoschema.InformationSchemaService;
import org.junit.Test;

import com.tesora.dve.persist.InsertEngine;
import com.tesora.dve.persist.PersistedEntity;
import com.tesora.dve.sql.infoschema.InformationSchemaTable;
import com.tesora.dve.sql.infoschema.InformationSchemas;
import com.tesora.dve.sql.transform.TransformTest;

public class InfoSchemaInit extends TransformTest {

	public InfoSchemaInit() {
		super("InfoSchemaInit");
		// TODO Auto-generated constructor stub
	}

	@Test
	public void testShow() {
		InformationSchemaService schema = Singletons.require(InformationSchemaService.class);
		System.out.println("Show tables:");
		for(InformationSchemaTable list : schema.getShowSchema().getTables(null)) {
			System.out.println(list);
			for(Object isc : list.getColumns(null))
				System.out.println("   " + isc);
		}		
	}
	
	@Test
	public void testInfoSchema() {
		InformationSchemaService schema = Singletons.require(InformationSchemaService.class);
		System.out.println("Info schema tables:");
		for(InformationSchemaTable list : schema.getInfoSchema().getTables(null)) {
			System.out.println(list);
			for(Object isc : list.getColumns(null))
				System.out.println("   " + isc);
		}				
	}

	@Test
	public void testMysqlSchema() {
        InformationSchemaService schema = Singletons.require(InformationSchemaService.class);
		System.out.println("Mysql tables:");
		for(InformationSchemaTable list : schema.getMysqlSchema().getTables(null)) {
			System.out.println(list);
			for(Object isc : list.getColumns(null))
				System.out.println("   " + isc);
		}		
	}
	
	@Test
	public void testGen() throws Throwable {
        InformationSchemaService schema = Singletons.require(InformationSchemaService.class);
		List<PersistedEntity> ents = schema.buildEntities(1, 2, "mycharset", "mycollation");
		InsertEngine ie = new InsertEngine(ents,null);
		List<String> gen = ie.dryrun();
		for(String s : gen) {
			System.out.println(s + ";");
		}
		
	}
	
}
