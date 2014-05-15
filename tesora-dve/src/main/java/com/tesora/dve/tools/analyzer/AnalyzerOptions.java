// OS_STATUS: public
package com.tesora.dve.tools.analyzer;

import java.util.ArrayList;
import java.util.List;

import com.tesora.dve.exceptions.PECodingException;
import com.tesora.dve.exceptions.PEException;

public final class AnalyzerOptions {

	public static final String FREQUENCY_ANALYSIS_CHECKPOINT_INTERVAL = "frequency_checkpoint";
	public static final String REDIST_CUTOFF_NAME = "redist_cutoff";
	public static final String SUPPRESS_DUPLICATES_NAME = "suppress_duplicates";
	public static final String VALIDATE_FKS_NAME = "validate_fks";
	public static final String ENABLE_TEMPLATE_WILDCARDS = "enable_template_wildcards";
	public static final String CORPUS_SCALE_FACTOR = "corpus_scale_factor";
	public static final String RDS_FORMAT = "rds_format";

	private final List<AnalyzerOption> options = new ArrayList<AnalyzerOption>();

	public AnalyzerOptions() {
		options.add(new AnalyzerOption(FREQUENCY_ANALYSIS_CHECKPOINT_INTERVAL,
				"The checkpoint interval for frequency analysis."
						+ " Specifies the number of processed statements"
						+ " after which the analyzer saves an intermediate copy of the corpus.",
				100000));
		options.add(new AnalyzerOption(REDIST_CUTOFF_NAME,
				"Do not emit plans for statements with fewer than n redistributions. Set to -1 to disable.", -1));
		options.add(new AnalyzerOption(SUPPRESS_DUPLICATES_NAME,
				"Suppress duplicate queries in the output of dynamic analysis.", false));
		options.add(new AnalyzerOption(VALIDATE_FKS_NAME,
				"Validate that all foreign keys are colocated in loaded schema and generated templates.", true));
		options.add(new AnalyzerOption(ENABLE_TEMPLATE_WILDCARDS,
				"Enable to replace common name prefixes in generated templates with wildcards.", false));
		options.add(new AnalyzerOption(
				CORPUS_SCALE_FACTOR,
				"This constant controls how far into the future will the template generator extrapolate table cardinalities.",
				3000));
		options.add(new AnalyzerOption(RDS_FORMAT, "Set to true to indicate that the log to be processed is from Amazon RDS", false));
	}

	public void reset() {
		for (final AnalyzerOption ao : options) {
			ao.resetToDefault();
		}
	}

	public List<AnalyzerOption> getOptions() {
		return options;
	}

	public void setOption(final String name, final String value) throws PEException {
		for (final AnalyzerOption ao : options) {
			if (ao.getName().equals(name)) {
				ao.setValue(value);
				return;
			}
		}

		throw new PEException("No such option: '" + name + "'");
	}

	public boolean isValidateFKsEnabled() {
		return getBooleanValue(VALIDATE_FKS_NAME);
	}

	public boolean isTemplateWildcardsEnabled() {
		return getBooleanValue(ENABLE_TEMPLATE_WILDCARDS);
	}

	public boolean isRdsFormat() {
		return getBooleanValue(RDS_FORMAT);
	}

	public boolean isSuppressDuplicatesEnabled() {
		return getBooleanValue(SUPPRESS_DUPLICATES_NAME);
	}

	public int getFrequencyAnalysisCheckpointInterval() {
		return getIntegerValue(FREQUENCY_ANALYSIS_CHECKPOINT_INTERVAL);
	}

	public int getCorpusScaleFactor() {
		return getIntegerValue(CORPUS_SCALE_FACTOR);
	}

	public int getRedistributionCutoff() {
		return getIntegerValue(REDIST_CUTOFF_NAME);
	}

	private Object getValue(final String name) {
		for (final AnalyzerOption ao : options) {
			if (ao.getName().equals(name)) {
				return ao.getCurrentValue();
			}
		}

		throw new PECodingException("No such option: '" + name + "'");
	}

	private boolean getBooleanValue(final String name) {
		final Object value = this.getValue(name);
		return Boolean.valueOf(value.toString()).booleanValue();
	}

	private int getIntegerValue(final String name) {
		final Object value = this.getValue(name);
		return Integer.valueOf(value.toString()).intValue();
	}
}
