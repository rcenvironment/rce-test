/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.backend.catalog.derby.internal;

import junit.framework.Assert;

import org.junit.Test;

import de.rcenvironment.rce.datamanagement.commons.DMQLQuery;

/**
 * Test for conversion of {@link DMQLQuery}.
 * 
 * @author Christian Weiss
 */
public class DMQLQueryConversionTest {
    
    private static final String SINGLE_WHITESPACE = " ";

    private static final String MULTIPLE_WHITESPACES = "\\s+";

    /** Test. */
    @Test
    public void testSelectAll() {
        final DMQLQuery query = new DMQLQuery("SELECT *");
        final DMQLQueryConverter converter = new DMQLQueryConverter(query);
        final String actualSql = converter.getSqlQuery().toLowerCase();
        final String expectedSql = ("select m0.key_, m0.value "
                    + "FROM all_metadata m0  "
                    + "").trim().toLowerCase().replaceAll(MULTIPLE_WHITESPACES, SINGLE_WHITESPACE);
        Assert.assertEquals(expectedSql.toLowerCase(), actualSql.toLowerCase());
    }

    /** Test. */
    @Test
    public void testSelect() {
        final DMQLQuery query = new DMQLQuery("SELECT a.b1, a.b2");
        final DMQLQueryConverter converter = new DMQLQueryConverter(query);
        final String actualSql = converter.getSqlQuery().toLowerCase();
        final String expectedSql = ("select m0.value, m1.value "
                    + "FROM all_metadata m0  "
                    + "join all_metadata m1 on m0.datareference_guid = m1.datareference_guid  "
                    + "where m0.key_ = 'a.b1' and m1.key_ = 'a.b2'  "
                    + "").trim().toLowerCase().replaceAll(MULTIPLE_WHITESPACES, SINGLE_WHITESPACE);
        Assert.assertEquals(expectedSql.toLowerCase(), actualSql.toLowerCase());
    }

    /** Test. */
    @Test
    public void testSelectGroupBy1() {
        final DMQLQuery query = new DMQLQuery("SELECT a.b1 GROUP BY a.b1");
        final DMQLQueryConverter converter = new DMQLQueryConverter(query);
        final String actualSql = converter.getSqlQuery().toLowerCase();
        final String expectedSql = ("select m0.value  "
                    + "FROM all_metadata m0 "
                    + "where m0.key_ = 'a.b1' "
                    + "group by m0.value"
                    + "").trim().toLowerCase().replaceAll(MULTIPLE_WHITESPACES, SINGLE_WHITESPACE);
        Assert.assertEquals(expectedSql.toLowerCase(), actualSql.toLowerCase());
    }

    /** Test. */
    @Test
    public void testSelectGroupBy2() {
        final DMQLQuery query = new DMQLQuery("SELECT a.b1, a.b2 GROUP BY a.b1, a.b2");
        final DMQLQueryConverter converter = new DMQLQueryConverter(query);
        final String actualSql = converter.getSqlQuery().toLowerCase();
        final String expectedSql = ("select m0.value, m1.value  "
                    + "FROM all_metadata m0 "
                    + "join all_metadata m1 on m0.datareference_guid = m1.datareference_guid "
                    + "where m0.key_ = 'a.b1' and m1.key_ = 'a.b2' "
                    + "group by m0.value, m1.value"
                    + "").trim().toLowerCase().replaceAll(MULTIPLE_WHITESPACES, SINGLE_WHITESPACE);
        Assert.assertEquals(expectedSql.toLowerCase(), actualSql.toLowerCase());
    }

    /** Test. */
    @Test
    public void testSelectHaving() {
        final DMQLQuery query = new DMQLQuery("SELECT a.b1, a.b2, a.b3 HAVING MIN(a.b3)");
        final DMQLQueryConverter converter = new DMQLQueryConverter(query);
        final String actualSql = converter.getSqlQuery().toLowerCase();
        final String expectedSql = ("select m0.value, m1.value, m2.value "
                    + "FROM all_metadata m0    "
                    + "join all_metadata m1 on m0.datareference_guid = m1.datareference_guid     "
                    + "join all_metadata m2 on m0.datareference_guid = m2.datareference_guid      "
                    + "where m0.key_ = 'a.b1' and m1.key_ = 'a.b2' and m2.key_ = 'a.b3' "
                    + "having min(m2.value)"
                    + "").trim().toLowerCase().replaceAll(MULTIPLE_WHITESPACES, SINGLE_WHITESPACE);
        Assert.assertEquals(expectedSql.toLowerCase(), actualSql.toLowerCase());
    }

    /** Test. */
    @Test
    public void testSelectMin() {
        final DMQLQuery query = new DMQLQuery("SELECT a.b1, a.b2, MIN(a.b3) GROUP BY a.b1, a.b2");
        final DMQLQueryConverter converter = new DMQLQueryConverter(query);
        final String actualSql = converter.getSqlQuery().toLowerCase();
        final String expectedSql = ("select m0.value, m1.value, min(m2.value) "
                    + "FROM all_metadata m0    "
                    + "join all_metadata m1 on m0.datareference_guid = m1.datareference_guid     "
                    + "join all_metadata m2 on m0.datareference_guid = m2.datareference_guid      "
                    + "where m0.key_ = 'a.b1' and m1.key_ = 'a.b2' and m2.key_ = 'a.b3' "
                    + "group by m0.value, m1.value"
                    + "").trim().toLowerCase().replaceAll(MULTIPLE_WHITESPACES, SINGLE_WHITESPACE);
        Assert.assertEquals(expectedSql.toLowerCase(), actualSql.toLowerCase());
    }
    
}
