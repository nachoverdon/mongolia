package com.nachoverdon.mongolia.utils;

import info.magnolia.cms.util.QueryUtil;
import info.magnolia.context.MgnlContext;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import java.util.Collection;
import java.util.function.Predicate;

@Slf4j
public class QueryUtils extends QueryUtil {

    /**
     * Wraps a String value between quotes
     *
     * @param value The value to quote
     * @return The quoted value
     */
    public static String quoteSQL2Value(String value) {
        return "'" + value.replaceAll("'", "''") + "'";
    }

    /**
     * Refer to {@link #quoteSQL2Value(String)}
     *
     * @param value The value to quote
     * @return The quoted value
     */
    public static String quoteSQL2Value(int value) {
        return quoteSQL2Value(String.valueOf(value));
    }

    /**
     * Refer to {@link #quoteSQL2Value(String)}
     *
     * @param value The value to quote
     * @return The quoted value
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
     * @param returnItemType TODO
     * @param isSelector TODO
     * @return First available Node
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
     * @param workspace The desired workspace. Ex: "website"
     * @param statement The SQL/xpath statement that will be executed
     * @param language The language that will be used. Ex: "JCR-SQL2" {@link javax.jcr.query.Query}
     * @param returnItemType TODO
     * @return First available Node
     */
    public static Node searchSingle(String workspace, String statement, String language, String returnItemType) {
        return searchSingle(workspace, statement, language, returnItemType, false);
    }

    /**
     * Refer to {@link #searchSingle(String, String, String, String, boolean)}
     *
     * @param workspace The desired workspace. Ex: "website"
     * @param statement The SQL/xpath statement that will be executed
     * @param language The language that will be used. Ex: "JCR-SQL2" {@link javax.jcr.query.Query}
     * @return First available Node
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
     * @param workspace The desired workspace. Ex: "website"
     * @param statement The SQL/xpath statement that will be executed
     * @return First available Node
     */
    public static Node searchSingle(String workspace, String statement) {
        return searchSingle(workspace, statement, Query.JCR_SQL2);
    }

    /**
     * Gets the count of all the nodes for a given workspace and statement.
     * You can optionally pass it a filter to remove nodes that meet a certain condition. It takes a Predicate, so it
     * works the same way as a Collection.removeIf.
     *
     * @param statement The statement
     * @param workspace The workspace where it should perform the query
     * @param language The query language to use
     * @param filter An optional custom filter to remove Nodes.
     * @return The count
     */
    public static Long getNodesCount(String statement, String workspace, String language, Predicate<Node> filter) {
        try {
            if (filter == null) {
                return MgnlContext.getJCRSession(workspace).getWorkspace().getQueryManager()
                        .createQuery(statement, language).execute().getRows().getSize();
            }

            Collection<Node> nodes = NodeUtils.getCollectionFromNodeIterator(
                    MgnlContext.getJCRSession(workspace).getWorkspace()
                    .getQueryManager().createQuery(statement, language).execute().getNodes()
            );

            nodes.removeIf(filter);

            return (long) nodes.size();
        } catch (RepositoryException e) {
            log.error("Could not retrieve total on workspace [" + workspace + "]: " + statement, e);
        }

        return 0L;
    }

    /**
     * Refer to {@link #getNodesCount(String, String, String, Predicate)}
     *
     * @param statement The statement
     * @param workspace The workspace where it should perform the query
     * @param language The query language to use
     * @return The count
     */
    public static Long getNodesCount(String statement, String workspace, String language) {
        return getNodesCount(statement, workspace, language, null);
    }

    /**
     * Refer to {@link #getNodesCount(String, String, String, Predicate)}
     * Uses JCR-SQL2 as default query language.
     *
     * @param statement The statement
     * @param workspace The workspace where it should perform the query
     * @param filter An optional custom filter to remove Nodes.
     * @return The count
     */
    public static Long getNodesCount(String statement, String workspace, Predicate<Node> filter) {
        return getNodesCount(statement, workspace, Query.JCR_SQL2, null);
    }

    /**
     * Refer to {@link #getNodesCount(String, String, String, Predicate)}
     * Uses JCR-SQL2 as default query language.
     *
     * @param statement The statement
     * @param workspace The workspace where it should perform the query
     * @return The count
     */
    public static Long getNodesCount(String statement, String workspace) {
        return getNodesCount(statement, workspace, Query.JCR_SQL2);
    }

}
