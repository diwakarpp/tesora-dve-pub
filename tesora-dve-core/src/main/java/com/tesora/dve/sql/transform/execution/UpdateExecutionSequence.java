// OS_STATUS: public
package com.tesora.dve.sql.transform.execution;

import java.util.ArrayList;
import java.util.List;

import com.tesora.dve.common.catalog.StorageGroup;
import com.tesora.dve.exceptions.PEException;
import com.tesora.dve.queryplan.QueryStep;
import com.tesora.dve.queryplan.QueryStepUpdateSequenceOperation;
import com.tesora.dve.resultset.ProjectionInfo;
import com.tesora.dve.sql.schema.SchemaContext;

public class UpdateExecutionSequence extends ExecutionSequence {

	public UpdateExecutionSequence(ExecutionPlan p) {
		super(p);
	}

	@Override
	protected String sequenceName() {
		return "UPDATE SEQUENCE";
	}
	
	@Override
	public void schedule(ExecutionPlanOptions opts, List<QueryStep> qsteps, ProjectionInfo projection, SchemaContext sc)
			throws PEException {
		if (steps.isEmpty()) return;
		ArrayList<QueryStep> mine = new ArrayList<QueryStep>();
		// can only be used if they all use the same storage group
		StorageGroup sg = null;
		for(HasPlanning hp : steps) {
			if (hp instanceof ExecutionStep) {
				ExecutionStep es = (ExecutionStep) hp;
				if (sg == null)
					sg = es.getStorageGroup(sc);
				else if (!sg.equals(es.getStorageGroup(sc)))
					throw new PEException("UpdateExecutionSequence created with multiple groups");
			}
			hp.schedule(opts, mine, projection, sc);
		}
		QueryStepUpdateSequenceOperation uo = new QueryStepUpdateSequenceOperation();
		for(QueryStep qs : mine) {
			uo.addOperation(qs.getOperation());
		}
		qsteps.add(new QueryStep(sg, uo));
	}


	
}
