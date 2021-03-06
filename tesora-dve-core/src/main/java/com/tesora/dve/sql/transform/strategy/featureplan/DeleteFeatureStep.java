package com.tesora.dve.sql.transform.strategy.featureplan;

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

import com.tesora.dve.exceptions.PEException;
import com.tesora.dve.sql.expression.TableKey;
import com.tesora.dve.sql.schema.DistributionKey;
import com.tesora.dve.sql.schema.PEStorageGroup;
import com.tesora.dve.sql.statement.dml.DeleteStatement;
import com.tesora.dve.sql.transform.execution.DeleteExecutionStep;
import com.tesora.dve.sql.transform.execution.ExecutionSequence;
import com.tesora.dve.sql.transform.strategy.PlannerContext;

public class DeleteFeatureStep extends NonQueryFeatureStep {

	public DeleteFeatureStep(FeaturePlanner planner, DeleteStatement ds, TableKey onTable, PEStorageGroup srcGroup, DistributionKey dk) {
		super(planner, ds, onTable, srcGroup, dk);
	}
	
	@Override
	public void scheduleSelf(PlannerContext pc, ExecutionSequence es)
			throws PEException {
		es.append(DeleteExecutionStep.build(
				pc.getContext(),
				getTable().getAbstractTable().getDatabase(pc.getContext()),
				getSourceGroup(),
				getTable(),
				getDistributionKey(),
				getPlannedStatement(),
				getPlannedStatement().getDerivedInfo().doSetTimestampVariable(),
				getExplainRecord()));
	}

}
