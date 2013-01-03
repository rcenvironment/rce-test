/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.commons;

import static de.rcenvironment.rce.datamanagement.commons.CatalogConstants.ALL_METADATA;
import static de.rcenvironment.rce.datamanagement.commons.CatalogConstants.DATAREFERENCE_GUID;
import static de.rcenvironment.rce.datamanagement.commons.CatalogConstants.HEAD_REVISION;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import de.rcenvironment.commons.Assertions;
import de.rcenvironment.rce.datamanagement.commons.DataReference.DataReferenceType;

/**
 * Class to create meta data queries.
 * 
 * FIXME: remove SQL logic
 * 
 * @author Dirk Rossow
 * @author Juergen Klein
 */
public class MetaDataQuery implements Query {

    /**
     * First possible revision number.
     */
    public static final int REVISION_FIRST = DataReference.FIRST_REVISION;

    /**
     * Last possible revision number.
     */
    public static final int REVISION_LAST = Integer.MAX_VALUE;

    /**
     * Interpreted as the actual head revision.
     */
    public static final int REVISION_HEAD = DataReference.HEAD_REVISION;
    
    /**
     * Assertion failed message.
     */
    private static final String PLEASE_PROVIDE_A_VALID_META_DATA_OBJECT = "Please provide a valid MetaData object.";

    /**
     * Serial uid.
     */
    private static final long serialVersionUID = -7732091754429656343L;

    /**
     * String constant.
     */
    private static final String AND = " AND ";

    /**
     * String constant.
     */
    private static final String BETWEEN = " BETWEEN ";
    
    /**
     * String constant.
     */
    private static final String APO = "'";

    /**
     * String constant.
     */
    private static final String IN = " IN ";

    /**
     * String constant.
     */
    private static final String EQ = " = ";

    private static final String TABLE_PLACEHOLDER = "<T>";

    private final AtomicInteger clauseCount = new AtomicInteger(0);
    
    private final String[] clauseParts = new String[] { "SELECT DISTINCT am0." + DATAREFERENCE_GUID + " FROM " + ALL_METADATA + " am0 "
            + "LEFT OUTER JOIN " + HEAD_REVISION + " head ON am0." + DATAREFERENCE_GUID + " = head." + DATAREFERENCE_GUID, "", "" };
    
    // /**
    // * List of intersectable queries.
    // */
    // private List<String> myIntersectableQueries = new ArrayList<String>();

    /**
     * List of or clauses.
     */
    private final Map<String, List<String>> myConditionalClauses = new HashMap<String, List<String>>();

    /**
     * 
     */
    private String myMasterQueryWherePart = "";
    
    private String query = "";

    /**
     * Adds a MetaData constraint on a revision range. If MetaData is revision independent revision
     * range is ignored.
     * 
     * @param metaData
     *            MetaData to match
     * @param value
     *            Value to match
     */
    public void addMetaDataConstraint(MetaData metaData, String value) {
        addMetaDataConstraint(metaData, value, REVISION_HEAD, REVISION_HEAD);
    }
    /**
     * Adds a MetaData constraint on a revision range. If MetaData is revision independent revision
     * range is ignored.
     * 
     * @param metaData
     *            MetaData to match
     * @param value
     *            Value to match
     * @param startRevisionNr
     *            min (included) of revision range, or {@link #REVISION_FIRST},
     *            {@link #REVISION_HEAD},
     * @param endRevisionNr
     *            max (included) of revision range, or {@link #REVISION_LAST},
     *            {@link #REVISION_HEAD},
     */
    public void addMetaDataConstraint(MetaData metaData, String value, int startRevisionNr, int endRevisionNr) {
        Assertions.isDefined(metaData, PLEASE_PROVIDE_A_VALID_META_DATA_OBJECT);
        Assertions.isTrue(value == null || value.length() > 0, "Please define a value to search for in the MetaData catalogue.");

        final String tableToQuery = CatalogConstants.ALL_METADATA;
//        if (metaData.isRevisionIndependent()) {
//            tableToQuery = CatalogConstants.REFERENCE_METADATA;
//        } else {
//            tableToQuery = CatalogConstants.METADATA;
//        }

        String conditionalClause = CatalogConstants.KEY + EQ + APO + metaData.getKey() + APO;
        if (value != null) {
            conditionalClause += AND + CatalogConstants.VALUE + EQ + APO + value + APO;
        }
        // String subQuery;
        // subQuery = SELECT + CatalogConstants.DATAREFERENCE_GUID + " as  guid " + FROM +
        // tableToQuery + WHERE + conditionalClause;
        // if (!metaData.isRevisionIndependent()) {
        // // TODO add to or clause
        // subQuery = addRevisionConstraint(subQuery, startRevisionNr, endRevisionNr);
        // }
        // myIntersectableQueries.add(subQuery);
        getConditionalClauses(tableToQuery).add(conditionalClause);
        final String revisionConstraint = createRevisionConstraint(metaData.isRevisionIndependent(), startRevisionNr, endRevisionNr);
        final StringBuilder clauseBuilder = new StringBuilder();
        clauseBuilder.append("(");
        clauseBuilder.append(TABLE_PLACEHOLDER);
        clauseBuilder.append(CatalogConstants.KEY);
        clauseBuilder.append(EQ);
        clauseBuilder.append(APO).append(metaData.getKey()).append(APO);
        if (value != null) {
            clauseBuilder.append(AND);
            clauseBuilder.append(TABLE_PLACEHOLDER);
            clauseBuilder.append(CatalogConstants.VALUE);
            clauseBuilder.append(EQ);
            clauseBuilder.append(APO).append(value).append(APO);
        }
        clauseBuilder.append(AND);
        clauseBuilder.append(revisionConstraint);
        clauseBuilder.append(")");
        final String clause = clauseBuilder.toString();
        addClause(clause);
    }

    protected String createRevisionConstraint(final boolean revisionIndependant, final int startRevision, final int endRevision) {
        assert startRevision >= DataReference.HEAD_REVISION && endRevision >= DataReference.HEAD_REVISION;
        assert startRevision <= endRevision || startRevision == DataReference.HEAD_REVISION || endRevision == DataReference.HEAD_REVISION;
        if (!(startRevision >= DataReference.HEAD_REVISION && endRevision >= DataReference.HEAD_REVISION)
                || !(startRevision <= endRevision || startRevision == DataReference.HEAD_REVISION
                || endRevision == DataReference.HEAD_REVISION)) {
            throw new IllegalArgumentException();
        }
        final String start;
        if (startRevision > DataReference.HEAD_REVISION) {
            start = Integer.toString(startRevision);
        } else {
            start = "head." + CatalogConstants.REVISION_NUMBER;
        }
        final String end;
        if (endRevision > DataReference.HEAD_REVISION) {
            end = Integer.toString(endRevision);
        } else {
            end = "head." + CatalogConstants.REVISION_NUMBER;
        }
        final StringBuilder resultBuilder = new StringBuilder();
        // always include revision indipendent revision
        resultBuilder.append("(");
        if (revisionIndependant) {
            resultBuilder.append(TABLE_PLACEHOLDER);
            resultBuilder.append(CatalogConstants.REVISION_NUMBER);
            resultBuilder.append(EQ);
            resultBuilder.append(DataReference.REVISION_INDEPENDENT_REVISION);
        } else {
            if (startRevision == endRevision) {
                resultBuilder.append(TABLE_PLACEHOLDER);
                resultBuilder.append(CatalogConstants.REVISION_NUMBER);
                resultBuilder.append(EQ);
                resultBuilder.append(start);
            } else {
                resultBuilder.append(TABLE_PLACEHOLDER);
                resultBuilder.append(CatalogConstants.REVISION_NUMBER);
                resultBuilder.append(" BETWEEN ");
                resultBuilder.append(start);
                resultBuilder.append(AND);
                resultBuilder.append(end);
            }
        }
        resultBuilder.append(")");
        final String result = resultBuilder.toString();
        return result;
    }

    private List<String> getConditionalClauses(final String table) {
        List<String> result = myConditionalClauses.get(table);
        if (result == null) {
            result = new LinkedList<String>();
            myConditionalClauses.put(table, result);
        }
        return result;
    }

    private void addClause(final String clause) {
        final int clauseIndex = clauseCount.incrementAndGet() - 1;
        final String tableAlias = "am" + clauseIndex;
        if (clauseIndex == 0) {
            clauseParts[2] = clause.replaceAll(TABLE_PLACEHOLDER, tableAlias + ".");
        } else {
            clauseParts[1] += " JOIN ALL_METADATA " + tableAlias + " ON am0.DATAREFERENCE_GUID = " + tableAlias + ".DATAREFERENCE_GUID";
            clauseParts[2] += AND + clause.replaceAll(TABLE_PLACEHOLDER, tableAlias + ".");
        }
        query = clauseParts[0] + clauseParts[1] + " WHERE " + clauseParts[2] + " ORDER BY am0.DATAREFERENCE_GUID";
    }

    /**
     * Adds a constraint on a revision range which checks existance of a specified key. If MetaData
     * is revision independent revision range is ignored.
     * 
     * @param metaData
     *            MetaData to match
     */
    public void addMetaDataKeyExistsConstraint(MetaData metaData) {
        addMetaDataConstraint(metaData, null, REVISION_HEAD, REVISION_HEAD);
    }
    
    /**
     * Adds a constraint on a revision range which checks existance of a specified key. If MetaData
     * is revision independent revision range is ignored.
     * 
     * @param metaData
     *            MetaData to match
     * @param startRevisionNr
     *            min (included) of revision range, or {@link #REVISION_FIRST},
     *            {@link #REVISION_HEAD},
     * @param endRevisionNr
     *            max (included) of revision range, or {@link #REVISION_LAST},
     *            {@link #REVISION_HEAD},
     */
    public void addMetaDataKeyExistsConstraint(MetaData metaData, int startRevisionNr, int endRevisionNr) {
        addMetaDataConstraint(metaData, null, startRevisionNr, endRevisionNr);
//        final String clause = String.format("(%s = '%s' AND %s BETWEEN %d and %d)", CatalogConstants.KEY, metaData.getKey(),
//                                            CatalogConstants.REVISION_NUMBER, startRevisionNr, endRevisionNr);
//        addClause(clause);
    }

    /**
     * Adds a MetaData constraint on a revision range. MetaData Values are interpreted as
     * {@link Long}. If MetaData is revision independent revision range is ignored.
     * 
     * @param metaData
     *            MetaData to match
     * @param startValue
     *            start Longs to include in result
     * @param endValue
     *            end Longs to include in result
     * @param startRevisionNr
     *            min (included) of revision range, or {@link #REVISION_FIRST},
     *            {@link #REVISION_HEAD},
     * @param endRevisionNr
     *            max (included) of revision range, or {@link #REVISION_LAST},
     *            {@link #REVISION_HEAD},
     */
    public void addMetaDataConstraint(MetaData metaData, long startValue, long endValue, int startRevisionNr, int endRevisionNr) {
        Assertions.isDefined(metaData, PLEASE_PROVIDE_A_VALID_META_DATA_OBJECT);

        final String tableToQuery = CatalogConstants.ALL_METADATA;
//        if (metaData.isRevisionIndependent()) {
//            tableToQuery = CatalogConstants.REFERENCE_METADATA;
//        } else {
//            tableToQuery = CatalogConstants.METADATA;
//        }

        String conditionalClause = CatalogConstants.KEY + EQ + APO + metaData.getKey() + APO + AND + "BIGINT(CHAR("
                + CatalogConstants.VALUE + "))" + BETWEEN + startValue + AND + endValue;
        // String subQuery = SELECT + CatalogConstants.DATAREFERENCE_GUID + " as guid " + FROM +
        // tableToQuery + WHERE + conditionalClause;
        // if (!metaData.isRevisionIndependent()) {
        // subQuery = addRevisionConstraint(subQuery, startRevisionNr, endRevisionNr);
        // }
        // myIntersectableQueries.add(subQuery);
        getConditionalClauses(tableToQuery).add(conditionalClause);
    }

    /**
     * Adds a MetaData constraint on a revision range. MetaData Values are interpreted as
     * {@link Date} (milliseconds). If MetaData is revision independent revision range is ignored.
     * 
     * @param metaData
     *            MetaData to match
     * @param startDate
     *            start Date to include in result
     * @param endDate
     *            end Date to include in result
     * @param startRevisionNr
     *            min (included) of revision range, or {@link #REVISION_FIRST},
     *            {@link #REVISION_HEAD},
     * @param endRevisionNr
     *            max (included) of revision range, or {@link #REVISION_LAST},
     *            {@link #REVISION_HEAD},
     */
    public void addMetaDataConstraint(MetaData metaData, Date startDate, Date endDate, int startRevisionNr, int endRevisionNr) {
        Assertions.isDefined(metaData, PLEASE_PROVIDE_A_VALID_META_DATA_OBJECT);
        Assertions.isDefined(startDate, "Please provide a valid start date.");
        Assertions.isDefined(endDate, "Please provide a valid end date.");
        long startMillis = startDate.getTime();
        long endMillis = endDate.getTime();
        addMetaDataConstraint(metaData, startMillis, endMillis, startRevisionNr, endRevisionNr);
    }

    /**
     * Only DataReferences with specified type are included in the result.
     * 
     * @param dataReferenceType
     *            list of allowed types in result
     */
    public void addTypeConstraint(DataReferenceType[] dataReferenceType) {
        Assertions.isDefined(dataReferenceType, "Please provide a valid DataReferenceType.");
        Assertions.isBiggerThan(dataReferenceType.length, 0, "Please provide at least one valid DataReferenceType.");
        if (myMasterQueryWherePart.length() > 0) {
            myMasterQueryWherePart += AND;
        }
        myMasterQueryWherePart += CatalogConstants.TYPE + IN + "(";
        for (int i = 0; i < dataReferenceType.length; ++i) {
            // REVIEW String.join()??
            if (i != 0) {
                myMasterQueryWherePart += ",";
            }
            myMasterQueryWherePart += APO + dataReferenceType[i].toString() + APO;
        }
        myMasterQueryWherePart += ")";
    }

    /**
     * Only DataReferences with a parent DataReference (branches) are included in the result.
     * 
     */
    public void addHasParentConstraint() {
        if (myMasterQueryWherePart.length() > 0) {
            myMasterQueryWherePart += AND;
        }
        myMasterQueryWherePart += CatalogConstants.PARENT_GUID + " IS NOT NULL ";
    }

    /**
     * Only DataReferences with a specified parent DataReference (branches of given DataReference)
     * are included in the result.
     * 
     * @param parent
     *            Siblings of this DataReference are included in the result.
     * @param startParentRevisionNr
     *            only value between {@link #REVISION_FIRST} and {@link #REVISION_LAST} are allowed
     * @param endParentRevisionNr
     *            only value between {@link #REVISION_FIRST} and {@link #REVISION_LAST} are allowed
     * 
     */
    public void addParentConstraint(DataReference parent, int startParentRevisionNr, int endParentRevisionNr) {
        Assertions.isDefined(parent, "Parameter must be a valid DataReference.");
        Assertions.isTrue(startParentRevisionNr > 0, "Parameter must be bigger than '0'.");
        Assertions.isTrue(endParentRevisionNr > 0, "Parameter must be bigger than '0'.");
        if (myMasterQueryWherePart.length() > 0) {
            myMasterQueryWherePart += AND;
        }
        myMasterQueryWherePart += CatalogConstants.PARENT_GUID + EQ + APO + parent.getIdentifier().toString() + APO;

        if (startParentRevisionNr != REVISION_FIRST || endParentRevisionNr != REVISION_LAST) {
            if (endParentRevisionNr == REVISION_HEAD) {
                endParentRevisionNr = REVISION_LAST;
            }
            myMasterQueryWherePart += AND + CatalogConstants.PARENT_REVISION + BETWEEN 
                + startParentRevisionNr + AND + endParentRevisionNr;
        }
    }

    @Override
    public String getQuery() {
        final String result = this.query;
        return result;
    }

    @Override
    public String toString() {
        return getQuery();
    }

    /**
     * Resets query to inital state.
     */
    public void clear() {
        // myIntersectableQueries.clear();
        myConditionalClauses.clear();
        myMasterQueryWherePart = "";
    }

}
