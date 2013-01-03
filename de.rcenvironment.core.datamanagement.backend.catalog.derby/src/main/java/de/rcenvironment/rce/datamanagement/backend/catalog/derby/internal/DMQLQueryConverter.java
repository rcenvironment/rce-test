/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.backend.catalog.derby.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.rcenvironment.commons.ArrayUtils;
import de.rcenvironment.commons.StringUtils;
import de.rcenvironment.rce.datamanagement.commons.DMQLQuery;

/*
 * Suggestions for grammar:
 * 
 * select *
 * --> select key_, value
 * 
 * select * where a is [ '1' ]
 * --> select key_, value where guid in
 *          (select distinct guid
 *              where count(guid) = 1 and key_ = 'a' and value = '1'
 *              group by guid, key_)
 *              
 * select * where a is [ '1', '2' ]
 * --> select key_, value where guid in
 *          (select distinct guid
 *              join on m0.guid = m1.guid and m0.value != m1.value
 *              where count(guid) = 1 and m0.key_ = 'a' and m0.value = '1' and m1.key = 'a' and m1.value = '2'
 *              group by guid, m0.key_, m1.key_)
 *              
 * select * where a contains [ '1' ]
 * --> select key_, value where guid in
 *          (select distinct guid
 *              where key_ = 'a' and value = '1'
 *              group by guid, key_)
 *              
 * select * where a contains [ '1', '2' ]
 * --> select key_, value where guid in
 *          (select distinct guid
 *              join on m0.guid = m1.guid and m0.value != m1.value
 *              where m0.key_ = 'a' and m0.value = '1' and m1.key = 'a' and m1.value = '2'
 *              group by guid)
 */

/**
 * Converter from {@link DMQLQuery} to SQL query string.
 * 
 * @author Christian Weiss
 */
class DMQLQueryConverter {
    
    private static final String DOT = ".";

    private static final String TABLE_PREFIX = "m";
    
    private static final String ROOT_TABLE = TABLE_PREFIX + 0;
    
    private final DMQLQuery query;
    
    // FIXME static instance var - constant
    private Pattern patternInstance;
    
    private List<String> propertyKeysSet;
    
    private List<String> groupingKeysSet;
    
    private List<String> havingExpressionSet;
    
    DMQLQueryConverter(final DMQLQuery query) {
        this.query = query;
        parse(query);
    }
    
    private Pattern createPattern() {
        if (patternInstance == null) {
            final String propertyKeyPattern = "\\s*[-_a-zA-Z0-9\\.]+\\s*";
            final String propertyPattern = "\\s*(?:(?:MIN|MAX)\\s*\\(\\s*" + propertyKeyPattern + "\\s*\\)|" + propertyKeyPattern + ")\\s*";
            final String propertyListPattern = propertyPattern + "(?:," + propertyPattern + ")*";
            final String apkp = "\\s*(?:MIN|MAX)\\s*\\(\\s*[-_a-zA-Z0-9\\.]+\\s*\\)\\s*"; // aggregated property key pattern
            final String apklp = apkp + "(?:," + apkp + ")*";
            final String patternString = "^(SELECT\\s+(?:\\*|" + propertyListPattern + "))"
                    + "(?:\\s+(GROUP BY " + propertyListPattern + "))?"
                    + "(?:\\s+(HAVING " + apklp + "))?\\s*$";
            patternInstance = Pattern.compile(patternString);
        }
        return patternInstance;
    }
    
    protected void parse(final DMQLQuery dmqlQuery) {
        final String queryString = dmqlQuery.getQuery();
        final Pattern pattern = createPattern();
        final Matcher matcher = pattern.matcher(queryString);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(queryString);
        }
        final String propertyKeys = extractKeys(matcher.group(1));
        final String groupingKeys = extractKeys(matcher.group(2));
        final String havingKeys = extractKeys(matcher.group(3));
        propertyKeysSet = extractKeySet(propertyKeys);
        groupingKeysSet = extractKeySet(groupingKeys);
        havingExpressionSet = extractExpressionSet(havingKeys);
//        System.err.println(String.format("%s || %s || %s", propertyKeys, groupingKeys, havingKeys));
    }
    
    private String extractKeys(final String string) {
        String result = null;
        if (string != null && !string.isEmpty()) {
            final String stringLC = string.toLowerCase();
            final boolean firstWhitespace = stringLC.startsWith("select") | stringLC.startsWith("having");
            final boolean secondWhitespace = stringLC.startsWith("group by");
            final int firstWhitespacePos = string.indexOf(" ") + 1;
            if (firstWhitespace) {
                result = string.substring(firstWhitespacePos);
            } else if (secondWhitespace) {
                result = string.substring(string.indexOf(" ", firstWhitespacePos));
            } else {
                throw new RuntimeException();
            }
        }
        return result;
    }
    
    private List<String> extractKeySet(final String string) {
        final List<String> result;
        if (string != null && !string.isEmpty()) {
            final String[] keys = StringUtils.split(string, ",", true);
            result = ArrayUtils.toList(keys);
        } else {
            result = new LinkedList<String>();
        }
        return result;
    }
    
    private List<String> extractExpressionSet(final String string) {
        final String patternString = "^(MIN|MAX)\\s*\\(([^\\)]+)\\)$";
        final Pattern pattern = Pattern.compile(patternString);
        final List<String> keys = extractKeySet(string);
        final List<String> result = new LinkedList<String>();
        for (final String key : keys) {
            final Matcher matcher = pattern.matcher(key);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(key);
            }
            final String condition = matcher.group(1);
            final String property = matcher.group(2).replaceAll("\\s*", "");
            result.add(String.format("%s:%s", condition, property));
        }
        return result;
    }
    
    /**
     * Converts a {@link DMQLQuery} to an according SQL query string.
     * 
     * @return the according SQL query string
     */
    public String getSqlQuery() {
        final Query queryInst = new Query();
        queryInst.addSelect(propertyKeysSet);
        queryInst.addGroupBy(groupingKeysSet);
        queryInst.addHaving(havingExpressionSet);
        return queryInst.getSql();
    }

    public List<String> getPropertyKeys() {
        return Collections.unmodifiableList(propertyKeysSet);
    }
    
    /**
     * The data model for a Query.
     *
     * @author Christian Weiss
     */
    private static final class Query {

        private static final String VALUE_FIELDNAME = "value";

        private static final String KEY_FIELDNAME = "key_";

        private final List<String> tablesList = new LinkedList<String>();
        
        private Boolean selectAll;

        private final Map<String, List<String>> selects = new HashMap<String, List<String>>();

        private final Set<String> groupBys = new HashSet<String>();

        private final Map<String, List<String>> havings = new HashMap<String, List<String>>();
        
        private final Map<String, String> propertyToTableMappings = new HashMap<String, String>();
        
        private final AtomicInteger propertyCount = new AtomicInteger(0);
        
        private String addProperty(final String property) {
            assert property != null && !property.isEmpty();
            final String tableAlias;
            if (propertyToTableMappings.containsKey(property)) {
                tableAlias = propertyToTableMappings.get(property);
            } else {
                final int clauseIndex = propertyCount.incrementAndGet() - 1;
                tableAlias = TABLE_PREFIX + clauseIndex;
                tablesList.add(tableAlias);
            }
            propertyToTableMappings.put(property, tableAlias);
            return tableAlias + "." + VALUE_FIELDNAME;
        }
        
        protected void addSelect(final String property) {
            assert property != null && !property.isEmpty();
            assert property.equals("*") && !selectAll;
            assert selectAll == null || !selectAll;
            final String aggregationPatternString = "^(MIN|MAX)\\s*\\(([^\\)]+)\\)$";
            final Pattern aggregationPattern = Pattern.compile(aggregationPatternString);
            final Matcher matcher = aggregationPattern.matcher(property);
            final String propertyKey;
            final String propertyPattern;
            if (!matcher.matches()) {
                propertyKey = property;
                propertyPattern = "%s";
            } else {
                propertyKey = matcher.group(2);
                propertyPattern = matcher.group(1) + "(%s)";
            }
            selectAll = property.equals("*");
            addProperty(propertyKey);
            final List<String> selectsList;
            if (selects.containsKey(propertyKey)) {
                selectsList = selects.get(propertyKey);
            } else {
                selectsList = new LinkedList<String>();
                selects.put(propertyKey, selectsList);
            }
            // 'mark' as select
            selectsList.add(propertyPattern);
        }
        
        protected void addSelect(final List<String> propertysList) {
            assert propertysList != null;
            for (final String property : propertysList) {
                addSelect(property);
            }
        }

        protected String getPropertyAlias(final String property) {
            assert propertyToTableMappings.containsKey(property);
            final String tableAlias = propertyToTableMappings.get(property);
            final String result = tableAlias + DOT + VALUE_FIELDNAME;
            return result;
        }
        
        protected void addGroupBy(final String property) {
            addProperty(property);
            groupBys.add(property);
        }

        protected void addGroupBy(final List<String> propertysList) {
            assert propertysList != null;
            for (final String property : propertysList) {
                addGroupBy(property);
            }
        }
        
        protected void addHaving(final String expression) {
            final String[] parts = expression.split(":");
            final String condition = parts[0];
            final String property = parts[1];
            addProperty(property);
            final List<String> expressions;
            if (havings.containsKey(property)) {
                expressions = havings.get(property); 
            } else {
                expressions = new LinkedList<String>();
                havings.put(property, expressions);
            }
            expressions.add(expression);
        }
        
        protected void addHaving(final List<String> expressions) {
            assert expressions != null;
            for (final String expression : expressions) {
                addHaving(expression);
            }
        }

        protected String getSql() {
            assert tablesList.size() > 0;
            String select = "SELECT ";
            if (selectAll) {
                select += "m0." + KEY_FIELDNAME + ", m0." + VALUE_FIELDNAME;
            }
            boolean firstSelect = true;
            String from = "FROM ";
            String where = "WHERE ";
            boolean hasWhere = false;
            String having = "HAVING ";
            boolean firstHaving = true;
            boolean hasHaving = havings.size() > 0;
//            /*
//             * 
//             */
//            if (hasHaving) {
//                calculateGroupBy();
//            }
            String groupBy = "GROUP BY ";
            boolean firstGroupBy = true;
            boolean hasGroupBy = groupBys.size() > 0;
            for (int index = 0; index < tablesList.size(); ++index) {
                final String tableAlias = tablesList.get(index);
                String property = null;
                for (Map.Entry<String, String> entry : propertyToTableMappings.entrySet()) {
                    if (entry.getValue().toString().equals(tableAlias)) {
                        property = entry.getKey();
                        break;
                    }
                }
                final String propertyAlias = getPropertyAlias(property);
                final String whereSeparate = tableAlias + DOT + KEY_FIELDNAME + " = " + "'" + property + "'";
                if (index == 0) {
                    from += "ALL_METADATA " + tableAlias;
                    if (!selectAll) {
                        where += whereSeparate;
                        hasWhere = true;
                    }
                } else {
                    from += " JOIN ALL_METADATA " + tableAlias + " ON "
                            + ROOT_TABLE + ".DATAREFERENCE_GUID = " + tableAlias + ".DATAREFERENCE_GUID";
                    where += " AND " + whereSeparate;
                    hasWhere = true;
                }
                if (!selectAll && selects.keySet().contains(property)) {
                    for (final String selectItemPattern : selects.get(property)) {
                        final String selectItemValue = String.format(selectItemPattern, getPropertyAlias(property));
                        if (firstSelect) {
                            select += selectItemValue;
                            firstSelect = false;
                        } else {
                            select += ", " + selectItemValue;
                        }
                    }
                }
                if (groupBys.contains(property)) {
                    if (firstGroupBy) {
                        groupBy += propertyAlias;
                        firstGroupBy = false;
                    } else {
                        groupBy += ", " + propertyAlias;
                    }
                }
                if (havings.keySet().contains(property)) {
                    final List<String> expressions = havings.get(property);
                    for (final String expression : expressions) {
                        final String[] parts = expression.split(":");
                        final String expressionCondition = parts[0];
                        final String expressionProperty = parts[1];
                        final String expressionPropertyAlias = getPropertyAlias(expressionProperty);
                        final String expressionSql = String.format("%s(%s)", expressionCondition, expressionPropertyAlias);
                        if (firstHaving) {
                            having += expressionSql;
                            firstHaving = false;
                        } else {
                            having += " AND " + expressionSql;
                        }
                    }
                }
            }
            String sql = select + " " + from;
            if (hasWhere) {
                sql += " " + where;
            }
            if (hasGroupBy) {
                sql += " " + groupBy;
            }
            if (hasHaving) {
                sql += " " + having;
            }
            return sql;
        }

    }

}
