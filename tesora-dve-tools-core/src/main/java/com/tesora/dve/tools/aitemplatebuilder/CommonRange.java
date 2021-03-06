package com.tesora.dve.tools.aitemplatebuilder;

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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;

import com.tesora.dve.common.HashMapFactory;
import com.tesora.dve.common.HashSetFactory;
import com.tesora.dve.common.LinkedHashMapFactory;
import com.tesora.dve.common.MathUtils;
import com.tesora.dve.common.MultiMap;
import com.tesora.dve.common.TwoDimensionalMultiMap;
import com.tesora.dve.exceptions.PECodingException;
import com.tesora.dve.exceptions.PEException;
import com.tesora.dve.sql.schema.Column;
import com.tesora.dve.sql.schema.types.Type;
import com.tesora.dve.tools.CLIBuilder.ColorStringBuilder;
import com.tesora.dve.tools.aitemplatebuilder.AiTemplateBuilder.MessageSeverity;
import com.tesora.dve.tools.aitemplatebuilder.AiTemplateBuilder.ScoreBonus;
import com.tesora.dve.tools.aitemplatebuilder.CorpusStats.JoinStats;
import com.tesora.dve.tools.aitemplatebuilder.CorpusStats.Relationship;
import com.tesora.dve.tools.aitemplatebuilder.CorpusStats.Relationship.RelationshipSpecification;
import com.tesora.dve.tools.aitemplatebuilder.CorpusStats.TableStats;
import com.tesora.dve.tools.aitemplatebuilder.CorpusStats.TableStats.TableColumn;

public class CommonRange extends FuzzyLinguisticVariable implements TemplateRangeItem {

	public enum Variables implements FlvName {
		JOINS_FLV_NAME {
			@Override
			public String get() {
				return "joinFrequency";
			}
		},
		CARDINALITY_FLV_NAME {
			@Override
			public String get() {
				return "joinCardinality";
			}
		};
	}

	private static final String FCL_BLOCK_NAME = "RangeModel";
	private final boolean isSafeMode;

	private double score = 0.0;

	private final Map<Relationship, Double> joins = new HashMap<Relationship, Double>();
	private final TwoDimensionalMultiMap<TableStats, TableColumn, ScoreBonus> tables = new TwoDimensionalMultiMap<TableStats, TableColumn, ScoreBonus>(
			new HashMapFactory<TableStats, MultiMap<TableColumn, ScoreBonus>>(),
			new LinkedHashMapFactory<TableColumn, Collection<ScoreBonus>>(),
			new HashSetFactory<ScoreBonus>());

	public static boolean haveCommonColumn(final Set<TableColumn> a, final Set<TableColumn> b) {
		for (final TableColumn column : a) {
			final Column<?> columnInstance = column.getColumnInstance();
			for (final TableColumn rangeColumn : b) {
				if (columnInstance.equals(rangeColumn.getColumnInstance())) {
					return true;
				}
			}
		}
		return false;
	}

	public static <T extends Comparable<? super T>> int findPositionFor(final T item, final SortedSet<T> values) {
		final SortedSet<T> head = values.headSet(item);
		return head.size();
	}

	/*
	 * It is required by range validation that the type sets of vector columns
	 * match the column order.
	 */
	public static Set<Type> getUniqueColumnTypes(final Set<TableColumn> columnSet) {
		final Set<Type> columnTypes = new LinkedHashSet<Type>();
		for (final TableColumn column : columnSet) {
			columnTypes.add(column.getType());
		}
		return columnTypes;
	}

	public static String buildUniqueRangeNameForTable(final TableStats table) {
		final String schemaName = table.getSchemaName();
		final String tableName = table.getTableName();

		return schemaName + "_" + tableName + "_range";
	}

	public static boolean isRangeCompatible(final Type a, final Type b) {
		return isRangeCompatibleNumeric(a, b) || isRangeCompatibleGeneric(a, b);
	}

	private static boolean isRangeCompatibleNumeric(final Type a, final Type b) {
		return ((a.isAcceptableRangeType() && b.isAcceptableRangeType())
				&& (a.isNumericType() && b.isNumericType())
				&& (!a.isFloatType() && !b.isFloatType()));
	}

	private static boolean isRangeCompatibleGeneric(final Type a, final Type b) {
		final int aDataType = a.getBaseType().getDataType();
		final int bDataType = b.getBaseType().getDataType();
		final String aTypeName = a.getTypeName();
		final String bTypeName = b.getTypeName();

		return ((a.isAcceptableRangeType() && b.isAcceptableRangeType())
				&& (aDataType == bDataType)
				&& (aTypeName.equals(bTypeName)));
	}

	public CommonRange(final Relationship join, final boolean isSafeMode) throws PEException {
		super(FCL_BLOCK_NAME);

		this.isSafeMode = isSafeMode;
		this.addJoin(join);
	}

	public CommonRange(final CommonRange other) {
		super(FCL_BLOCK_NAME);

		this.isSafeMode = other.isSafeMode;
		this.joins.putAll(other.joins);
		this.tables.putAll(other.tables);
	}

	protected CommonRange(final boolean isSafeMode) {
		super(FCL_BLOCK_NAME);

		this.isSafeMode = isSafeMode;
	}

	public boolean isSuitableFor(final Relationship join) {
		final Set<TableColumn> left = this.getRangeColumnsFor(join.getLHS());
		final Set<TableColumn> right = this.getRangeColumnsFor(join.getRHS());

		/*
		 * Handle the case when this range already contains both sides of the
		 * join.
		 */
		if ((left != null) && (right != null)) {
			return (left.equals(join.getLeftColumns()) && right.equals(join.getRightColumns()));
		}

		if (isSuitableForHelper(left, join) || isSuitableForHelper(right, join)) {
			return true;
		}

		return false;
	}

	public void addJoin(final Relationship join) throws PEException {
		if (!join.isRangeCompatible()) {
			throw new PEException(
					"Relationship between range-incompatible column types in '"
							+ join.toString() + "'.");
		}

		this.joins.put(join, 0.0);
		this.addTable(join.getLHS(), join.getLeftColumns());
		this.addTable(join.getRHS(), join.getRightColumns());
	}

	@Override
	public Set<Type> getUniqueColumnTypes() throws PEException {
		Set<Type> rangeTypes = null;
		for (final TableStats table : this.getTables()) {
			if (rangeTypes == null) {
				rangeTypes = getUniqueColumnTypes(getRangeColumnsFor(table));
			} else {
				final Set<Type> columnTypes = getUniqueColumnTypes(getRangeColumnsFor(table));

				if (rangeTypes.size() == columnTypes.size()) {
					rangeTypes = this.getLargest(rangeTypes, columnTypes);
				} else {
					throw new PEException(
							"Unequal numbers of range columns in '"
									+ this.getTemplateItemName() + "'.");
				}
			}
		}
		return rangeTypes;
	}

	@Override
	public boolean hasCommonColumn(final Set<TableColumn> columns) {
		return haveCommonColumn(columns, this.getColumns());
	}

	public Set<TableStats> getTables() {
		return this.tables.keySet();
	}

	public Set<TableColumn> getColumns() {
		final Set<TableColumn> uniqueColumns = new HashSet<TableColumn>();
		for (final TableStats table : this.getTables()) {
			uniqueColumns.addAll(this.getRangeColumnsFor(table));
		}

		return uniqueColumns;
	}

	/**
	 * Merge OUTER JOINs into existing compatible INNER JOINs provided that they
	 * do not require special handling.
	 */
	public void flattenJoins() {
		final Iterator<Entry<Relationship, Double>> joins = this.joins.entrySet().iterator();
		while (joins.hasNext()) {
			final Relationship join = joins.next().getKey();
			final RelationshipSpecification type = join.getType();
			if (type.isOuterJoin() && !AiTemplateBuilder.isJoinToBroadcastAndRequiresRedist(join)) {

				final JoinStats asInnerJoin = ((JoinStats) join).cloneAsInnerJoin();
				if (this.joins.containsKey(asInnerJoin)) {
					JoinStats matchingInnerJoin = null;
					for (final Relationship item : this.joins.keySet()) {
						if (item.equals(asInnerJoin)) {
							matchingInnerJoin = (JoinStats) item;
							break;
						}
					}

					if (matchingInnerJoin != null) {
						matchingInnerJoin.bumpJoinCount(join.getFrequency());
						joins.remove();
					} else {
						throw new PECodingException("Key contained in the collection, but matching join not found.");
					}

				}
			}
		}
	}

	public boolean hasIntersectionWith(final CommonRange other) {
		final Set<TableStats> difference = new HashSet<TableStats>(getTables());
		return difference.removeAll(other.getTables());
	}

	public boolean isSubrangeOf(final CommonRange other) {
		if (other.getSize() < this.getSize()) {
			return false;
		}

		for (final Relationship join : this.joins.keySet()) {
			if (!other.isSuitableFor(join)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Keep merging this range into the other until
	 * the target range changes and there are joins to merge.
	 */
	public void mergeInto(final CommonRange other) throws PEException {
		final Set<Relationship> joinsToMerge = new HashSet<Relationship>(this.joins.keySet());
		int numMergedJoins;
		do {
			numMergedJoins = 0;
			final Iterator<Relationship> joins = joinsToMerge.iterator();
			while (joins.hasNext()) {
				final Relationship join = joins.next();
				if (other.isSuitableFor(join)) {
					other.addJoin(join);
					joins.remove();
					++numMergedJoins;
				}
			}
		} while (numMergedJoins > 0);
	}

	public void remove(final TableStats table) {
		final Iterator<Entry<Relationship, Double>> items = this.joins.entrySet().iterator();
		while (items.hasNext()) {
			final Relationship join = items.next().getKey();
			if (join.involves(table)) {
				items.remove();
			}
		}

		final Set<TableStats> retainTables = new HashSet<TableStats>();
		for (final Relationship join : this.joins.keySet()) {
			retainTables.add(join.getLHS());
			retainTables.add(join.getRHS());
		}

		this.tables.keySet().retainAll(retainTables);
	}

	public int getSize() {
		return this.joins.size();
	}

	private Double getDataSizeKb() {
		return CorpusStats.computeTotalSizeKb(this.getTables());
	}

	@Override
	public String getFclName() {
		return FCL_BLOCK_NAME;
	}

	@Override
	public String getTemplateItemName() {
		final Set<TableStats> tables = getTables();
		final TableStats topTable = Collections.max(tables, new Comparator<TableStats>() {
			@Override
			public int compare(TableStats a, TableStats b) {
				final int nameLengthDiff = b.getTableName().length() - a.getTableName().length();
				if (nameLengthDiff != 0) {
					return nameLengthDiff;
				}
				return b.getTableName().compareTo(a.getTableName());
			}
		});
		return buildUniqueRangeNameForTable(topTable);
	}

	public void evaluate(final Set<Long> uniqueJoinFrequencies, final SortedSet<Long> sortedJoinCardinalities, final boolean isRowWidthWeightingEnabled) {
		final double averageJoinFrequency = MathUtils.mean(uniqueJoinFrequencies);
		assert (averageJoinFrequency > 0.0);

		for (final Relationship join : this.joins.keySet()) {
			final double relativeJoinFrequency = FuzzyLinguisticVariable.toPercent(join.getFrequency(), averageJoinFrequency);
			final long totalJoinCardinality = join.getLHS().getPredictedFutureSize(isRowWidthWeightingEnabled)
					+ join.getRHS().getPredictedFutureSize(isRowWidthWeightingEnabled);
			final double pcCardinality = FuzzyLinguisticVariable.toPercent(findPositionFor(totalJoinCardinality, sortedJoinCardinalities),
					sortedJoinCardinalities.size());

			this.setVariable(Variables.JOINS_FLV_NAME, relativeJoinFrequency);
			this.setVariable(Variables.CARDINALITY_FLV_NAME, pcCardinality);

			this.evaluate();

			final double joinScore = super.getScore();
			final float joinScoreFactor = this.computeBonusFactor(join);
			this.joins.put(join, joinScore);
			this.score += (joinScore * joinScoreFactor);
		}
	}

	@Override
	public double getScore() {
		return getBonusScore();
	}

	@Override
	public String toString() {
		final ColorStringBuilder value = new ColorStringBuilder();
		value.append(getTemplateItemName())
				.append(" [").append(this.getDataSizeKb()).append("KB]")
				.append(" (").append(this.getScore()).append("): {");

		/* Print range tables. */
		for (final TableStats table : this.getTables()) {
			value.append(AiTemplateBuilder.LINE_SEPARATOR).append(AiTemplateBuilder.LINE_INDENT).append(table);
		}

		value.append(AiTemplateBuilder.LINE_SEPARATOR);

		/* Print range relationships. */
		for (final Map.Entry<Relationship, Double> item : this.joins.entrySet()) {
			final Relationship relationship = item.getKey();
			final Double score = MathUtils.round(item.getValue(), AiTemplateBuilder.NUMBER_DISPLAY_PRECISION);
			value.append(AiTemplateBuilder.LINE_SEPARATOR).append(AiTemplateBuilder.LINE_INDENT)
					.append(relationship).append(": (").append(score).append(")");

			final MultiMap<TableColumn, ScoreBonus> relationshipColumnBonuses = this.getColumnBonusesFor(relationship);
			for (final TableColumn column : relationshipColumnBonuses.keySet()) {
				for (final ScoreBonus bonus : relationshipColumnBonuses.get(column)) {
					if (!bonus.isPerRangeOnly()) {
						value.append(" (")
								.append(bonus.print(column), (bonus.isNegative()) ? MessageSeverity.WARNING.getColor() : MessageSeverity.INFO.getColor())
								.append(")");
					}
				}
			}
		}

		value.append(AiTemplateBuilder.LINE_SEPARATOR);

		/* Print collected range bonuses. */
		for (final TableStats table : this.getTables()) {
			final MultiMap<TableColumn, ScoreBonus> tableColumnBonuses = this.tables.get(table);
			for (final TableColumn column : tableColumnBonuses.keySet()) {
				final Collection<ScoreBonus> columnBonuses = tableColumnBonuses.get(column);
				for (final ScoreBonus bonus : columnBonuses) {
					if (bonus.isPerRangeOnly()) {
						value.append(AiTemplateBuilder.LINE_SEPARATOR).append(AiTemplateBuilder.LINE_INDENT)
								.append(bonus.print(column), (bonus.isNegative()) ? MessageSeverity.WARNING.getColor() : MessageSeverity.INFO.getColor());
					}
				}
			}
		}

		return value.append(AiTemplateBuilder.LINE_SEPARATOR).append("}").toString();
	}

	@Override
	public boolean contains(final TableStats table) {
		return this.getTables().contains(table);
	}

	@Override
	public Set<TableColumn> getRangeColumnsFor(final TableStats table) {
		final MultiMap<TableColumn, ScoreBonus> tableColumns = this.tables.get(table);
		return (tableColumns != null) ? tableColumns.keySet() : null;
	}

	@Override
	protected String getFlvName() {
		return "CommonRange";
	}

	protected void addUserDefinedDistribution(final TableStats table, final Set<String> dv) throws PEException {
		this.addTable(table, table.getColumns(dv));
	}

	private void addTable(final TableStats table, final Set<TableColumn> dv) {
		for (final TableColumn column : dv) {
			this.tables.putAll(table, column, AiTemplateBuilder.getBonusesForColumn(column, this.isSafeMode));
		}
	}

	/**
	 * Get bonus score for this range.
	 */
	private double getBonusScore() {
		final float bonus = computeBonusFactor();
		return (bonus * this.score);
	}

	/**
	 * The range bonus factor is the sum of the upper (best) and lower (worst)
	 * per-table bonuses in this range.
	 */
	private float computeBonusFactor() {
		final List<Float> bonuses = new ArrayList<Float>();
		for (final TableStats table : this.tables.keySet()) {
			float netBonusFactor = 1.0f;
			for (final ScoreBonus bonus : this.tables.get(table).values()) {
				if (bonus.isPerRangeOnly()) {
					netBonusFactor += bonus.getBonusFactor();
				}
			}
			bonuses.add(netBonusFactor);
		}

		final float high = Math.max(0.0f, Collections.max(bonuses));
		final float low = Math.min(0.0f, Collections.min(bonuses));

		return (high + low);
	}

	/**
	 * Each of the relationships/joins within the range can have it's own score
	 * bonus factor. It's the sum of all bonus factors just on that
	 * relationship.
	 */
	private float computeBonusFactor(final Relationship relationship) {
		float netBonusFactor = 1.0f;
		final MultiMap<TableColumn, ScoreBonus> relColumnBonuses = this.getColumnBonusesFor(relationship);
		for (final ScoreBonus bonus : relColumnBonuses.values()) {
			if (!bonus.isPerRangeOnly()) {
				netBonusFactor += bonus.getBonusFactor();
			}
		}

		return netBonusFactor;
	}

	private MultiMap<TableColumn, ScoreBonus> getColumnBonusesFor(final Relationship relationship) {
		final MultiMap<TableColumn, ScoreBonus> relColumnBonuses = new MultiMap<TableColumn, ScoreBonus>(
				new LinkedHashMapFactory<TableColumn, Collection<ScoreBonus>>(),
				new HashSetFactory<ScoreBonus>());

		relColumnBonuses.putAll(this.tables.get(relationship.getLHS()));
		relColumnBonuses.putAll(this.tables.get(relationship.getRHS()));

		return relColumnBonuses;
	}

	private boolean isSuitableForHelper(final Set<TableColumn> columns, final Relationship join) {
		return ((columns != null) && (columns.equals(join.getLeftColumns()) || columns.equals(join.getRightColumns())));
	}

	/**
	 * Get a set of largest compatible types while preserving the original
	 * order.
	 */
	private Set<Type> getLargest(final Set<Type> a, final Set<Type> b)
			throws PEException {
		assert (a.size() == b.size());

		final Set<Type> largestTypes = new LinkedHashSet<Type>();

		final Iterator<Type> aType = a.iterator();
		final Iterator<Type> bType = b.iterator();
		while (aType.hasNext()) {
			largestTypes.add(this.getLarger(aType.next(), bType.next()));
		}

		return largestTypes;
	}

	private Type getLarger(final Type a, final Type b) throws PEException {
		if (isRangeCompatibleNumeric(a, b)) {
			a.normalize();
			b.normalize();
			return (a.getSize() >= b.getSize()) ? a : b;
		}

		if (isRangeCompatibleGeneric(a, b)) {
			return a;
		}

		throw new PEException("Incompatible column types in range '"
				+ this.getTemplateItemName() + "'.");
	}
}