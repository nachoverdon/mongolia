package com.nachoverdon.mongolia.utils;

import info.magnolia.cms.util.QueryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.Query;

public class QueryUtils extends QueryUtil {

    private static Logger log = LoggerFactory.getLogger(QueryUtils.class);

    /**
     * Wraps a String value between quotes
     *
     * @param value
     * @return
     */
    public static String quoteSQL2Value(String value) {
        return "'" + value.replaceAll("'", "''") + "'";
    }

    /**
     * Refer to {@link #quoteSQL2Value(String)}
     *
     * @param value
     * @return
     */
    public static String quoteSQL2Value(int value) {
        return quoteSQL2Value(String.valueOf(value));
    }

    /**
     * Refer to {@link #quoteSQL2Value(String)}
     *
     * @param value
     * @return
     */
    public static String quoteSQL2Value(Object value) {
        return quoteSQL2Value(String.valueOf(value));
    }

    /**
     * Performs a query and retrieves the first available Node. It basically uses
     * {@link #search(String, String, String, String, boolean)} in combination with {@link NodeUtils#getAnyOrNull(NodeIterator)}
     *
     * @param workspace The desired workspace. Ex: "website"
     * @param statement The SQL/xpath statement that will be executed
     * @param language The language that will be used. Ex: "JCR-SQL2" {@link javax.jcr.query.Query}
     * @param returnItemType
     * @param isSelector
     * @return
     */
    public static Node searchSingle(String workspace, String statement, String language, String returnItemType, boolean isSelector) {
        try {
            return NodeUtils.getAnyOrNull(search(workspace, statement, language, returnItemType, isSelector));
        } catch (Exception e) {
            log.error("Query failed for workspace [" + workspace + "] and language [" + language
                    + "] with returnItemType [" + returnItemType + "]" + (isSelector ? " as selector" : "")
                    + " for statement:\n" +  statement, e);
        }

        return null;
    }

    /**
     * Refer to {@link #searchSingle(String, String, String, String, boolean)}
     *
     * @param workspace
     * @param statement
     * @param language
     * @param returnItemType
     * @return
     */
    public static Node searchSingle(String workspace, String statement, String language, String returnItemType) {
        return searchSingle(workspace, statement, language, returnItemType, false);
    }

    /**
     * Refer to {@link #searchSingle(String, String, String, String, boolean)}
     *
     * @param workspace
     * @param statement
     * @param language
     * @return
     */
    public static Node searchSingle(String workspace, String statement, String language) {
        try {
            return NodeUtils.getAnyOrNull(search(workspace, statement, language));
        } catch (Exception e) {
            log.error("Query failed for workspace [" + workspace + "] and language [" + language + "] for statement:\n"
                    +  statement, e);
        }

        return null;
    }

    /**
     * Refer to {@link #searchSingle(String, String, String, String, boolean)}
     *
     * @param workspace
     * @param statement
     * @return
     */
    public static Node searchSingle(String workspace, String statement) {
        return searchSingle(workspace, statement, Query.JCR_SQL2);
    }

}
