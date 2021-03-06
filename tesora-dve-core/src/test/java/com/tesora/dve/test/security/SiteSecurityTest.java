package com.tesora.dve.test.security;

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

import java.sql.SQLException;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import com.tesora.dve.common.DBHelper;
import com.tesora.dve.common.PEConstants;
import com.tesora.dve.common.PEFileUtils;
import com.tesora.dve.common.PEUrl;
import com.tesora.dve.common.catalog.TemplateMode;
import com.tesora.dve.common.catalog.TestCatalogHelper;
import com.tesora.dve.exceptions.PEException;
import com.tesora.dve.server.bootstrap.BootstrapHost;
import com.tesora.dve.sql.util.ProxyConnectionResource;
import com.tesora.dve.standalone.PETest;
import com.tesora.dve.test.simplequery.SimpleQueryTest;
import com.tesora.dve.variable.VariableConstants;

public class SiteSecurityTest extends PETest {

	static Properties props;

	@BeforeClass
	public static void setup() throws Throwable {
		Class<?> bootClass = PETest.class;

		SimpleQueryTest.cleanupSites(2, "TestDB");
		TestCatalogHelper helper = null;
		try {
			helper = new TestCatalogHelper(bootClass);
			helper.createTestCatalogWithDB(2, false, "root2", "password2");
		} finally {
			if (helper != null) {
				helper.close();
				helper = null;
			}
		}

		bootHost = BootstrapHost.startServices(bootClass);

		ProxyConnectionResource pcr = new ProxyConnectionResource("root2","password2");
		
		pcr.execute(String.format("alter dve set %s = '%s'",VariableConstants.TEMPLATE_MODE_NAME, TemplateMode.OPTIONAL));
		pcr.execute("create database TestDB default character set utf8 default persistent group DefaultGroup");

		pcr.execute("use TestDB");
		pcr.execute("create table foo (id int, value varchar(20)) random distribute");
		pcr.execute("create table bar (id int, value varchar(20)) random distribute");

		pcr.disconnect();
		pcr.close();
		
		props = PEFileUtils.loadPropertiesFile(SiteSecurityTest.class, PEConstants.CONFIG_FILE_NAME);

		DBHelper dbHelper = new DBHelper(props.getProperty(PEConstants.PROP_JDBC_URL), "root2", "password2");

		try {
			dbHelper.connect();
			dbHelper.executeQuery("USE TestDB");
			dbHelper.executeQuery("CREATE TABLE table1 ( col1 int, col2 varchar(10))");
			for (int i = 0; i < 100; i++) {
				dbHelper.executeQuery("INSERT INTO table1 VALUES (" + i + " , 'val" + i + "')");
			}

			// Create a new user as 'root2'
			dbHelper.executeQuery("CREATE USER 'test1'@'localhost' IDENTIFIED BY 'test1'");
			dbHelper.executeQuery("GRANT ALL ON *.* to 'test1'@'localhost'");
		} finally {
			dbHelper.disconnect();
		}
	}

	@Test
	public void connectTest() throws PEException, SQLException {
		// Attempt to connect as root - should fail
		PEUrl myURL = PEUrl.fromUrlString(props.getProperty(PEConstants.PROP_JDBC_URL));
		final ExpectedExceptionTester exceptionTester = new ExpectedExceptionTester() {
			@Override
			public void test() throws Throwable {
				DBHelper dbh = new DBHelper(props.getProperty(PEConstants.PROP_JDBC_URL),
						props.getProperty(PEConstants.PROP_JDBC_USER),
						props.getProperty(PEConstants.PROP_JDBC_PASSWORD));

				try {
					dbh.connect();
				} finally {
					dbh.disconnect();
				}
			}
		};
		exceptionTester
				.assertException(PEException.class,
						String.format("Error connecting to database 'jdbc:mysql://localhost:%d' - PEException: Connection refused - User 'root' not found",
								myURL.getPort()));

		// Attempt to connect as root2 - should work
		DBHelper dbh = new DBHelper(props.getProperty(PEConstants.PROP_JDBC_URL), "root2", "password2");
		try {
			dbh.connect();
		} finally {
			dbh.disconnect();
		}
		
		// Attempt to connect as test1 - should work
		dbh = new DBHelper(props.getProperty(PEConstants.PROP_JDBC_URL), "test1", "test1");
		try {
			dbh.connect();
		} finally {
			dbh.disconnect();
		}


	}

	@Test
	public void selectTest() throws PEException, SQLException {
		// We should be able to read the data in table1 as root2 or as test1

		DBHelper dbh = new DBHelper(props.getProperty(PEConstants.PROP_JDBC_URL), "root2", "password2");
		try {
			dbh.connect();
			dbh.executeQuery("USE TestDB");
			dbh.executeQuery("SELECT * from table1");
		} finally {
			dbh.disconnect();
		}
		
		dbh = new DBHelper(props.getProperty(PEConstants.PROP_JDBC_URL), "test1", "test1");
		try {
			dbh.connect();
			dbh.executeQuery("USE TestDB");
			dbh.executeQuery("SELECT * from table1");
		} finally {
			dbh.disconnect();
		}
	}
	
	
	@Test
	public void redistTest() throws PEException, SQLException {
		// We should be able to read the data in table1 as root2 or as test1

		DBHelper dbh = new DBHelper(props.getProperty(PEConstants.PROP_JDBC_URL), "root2", "password2");
		try {
			dbh.connect();
			dbh.executeQuery("USE TestDB");
			dbh.executeQuery("SELECT * from table1 ORDER BY col2 DESC");
		} finally {
			dbh.disconnect();
		}
		
		dbh = new DBHelper(props.getProperty(PEConstants.PROP_JDBC_URL), "test1", "test1");
		try {
			dbh.connect();
			dbh.executeQuery("USE TestDB");
			dbh.executeQuery("SELECT * from table1 ORDER BY col2 DESC");
		} finally {
			dbh.disconnect();
		}
	}
}
