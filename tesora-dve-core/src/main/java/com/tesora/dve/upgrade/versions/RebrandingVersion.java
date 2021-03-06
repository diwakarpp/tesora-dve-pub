package com.tesora.dve.upgrade.versions;

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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.tesora.dve.common.DBHelper;
import com.tesora.dve.common.InformationCallback;
import com.tesora.dve.exceptions.PEException;
import com.tesora.dve.sql.util.Pair;

public class RebrandingVersion extends ComplexCatalogVersion {

	private static Logger logger = Logger.getLogger(RebrandingVersion.class);

	public RebrandingVersion(int v) {
		super(v, false);
	}

	@Override
	public void upgrade(DBHelper helper, InformationCallback ic) throws PEException {
		try {
			ResultSet rs = null;
			try {
				helper.executeQuery("select count(name) from user_database where multitenant_mode != 'off'");
				rs = helper.getResultSet();
				rs.next();
				if (rs.getLong(1) > 0)
					throw new PEException("Catalog contains multitenant data - upgrade not supported");
			} finally {
				if (rs != null)
					rs.close();
			}
		} catch (SQLException sqle) {
			throw new PEException("Unable to determine if catalog has multitenant data", sqle);
		}

		execQuery(helper, "update config set name = 'dve_mysql_emulate_limit' where name='pe_mysql_emulate_limit'");

		Pair<Long, Long> providerBounds = getSimpleBounds(helper, "provider", "id");
		for (long i = providerBounds.getFirst(); i <= providerBounds.getSecond(); i++) {
			upgradeProvider(helper, i);
		}
	}

	private void upgradeProvider(DBHelper helper, long id) throws PEException {
		String plugin = null;
		boolean found = false;
		try {
			ResultSet rs = null;
			try {
				if (helper.executeQuery("select plugin from provider where id = " + id)) {
					rs = helper.getResultSet();
					if (rs.next()) {
						found = true;
						plugin = rs.getString(1);
					}
				}
			} finally {
				if (rs != null)
					rs.close();
			}
		} catch (SQLException sqle) {
			throw new PEException("Unable to get existing plugin for id " + id, sqle);
		}

		if (!found)
			return;

		if (!plugin.equals("com.parelastic.siteprovider.onpremise.OnPremiseSiteProvider")) {
			logger.warn("Unable to upgrade provider plugin value '" + plugin + "'");
			return;
		}


		try {
			helper.prepare("update provider set plugin = ? where id = ?");
			List<Object> params = new ArrayList<Object>();
			params.add("com.tesora.dve.siteprovider.onpremise.OnPremiseSiteProvider");
			params.add(id);
			helper.executePrepared(params);
		} catch (SQLException sqle) {
			throw new PEException("Unable to update plugin for id " + id, sqle);
		}
	}
}