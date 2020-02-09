package com.nachoverdon.mongolia.utils;

import com.nachoverdon.mongolia.annotations.Children;
import com.nachoverdon.mongolia.annotations.Translatable;
import info.magnolia.cms.i18n.I18nContentSupportFactory;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.context.MgnlContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
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
     * @param returnItemType Searches for statement and then pops-up in the node hierarchy until returnItemType is
     *                       found. If the result is not returnItemType or none of its parents are then next node in
     *                       result is checked. Duplicate nodes are removed from result.
     * @param isSelector If isSelector is set to true then returnItemType will be used as the selector and result
     *                   will contain only nodes that are marked by this selector.
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
     * @param returnItemType Searches for statement and then pops-up in the node hierarchy until returnItemType is
     *                       found. If the result is not returnItemType or none of its parents are then next node in
     *                       result is checked. Duplicate nodes are removed from result.
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

    /**
     * Create a JCR-SQL2 query condition to search on fields.
     * It will produce something like:
     * <pre>{@code
     *     setSearchableFields(Book.class, "Cinderella", "book");
     *     // => " LOWER(book.title_en) LIKE '%%cinderella%%' OR LOWER(book.title_es) LIKE '%%cinderella%%' "
     * }</pre>
     *
     * @param clazz The class of the JavaBean to build the query for
     * @param searchTerm The search term
     * @param as The variable name of the node
     * @return The condition of the query
     */
    public static String setSearchableFields(Class clazz, String searchTerm, String as) {
        String currentLang = LangUtils.getLanguage();
        Constructor<?> constructor = ReflectionUtils.getEmptyConstructor(clazz);
        StringBuilder addedQuerySB = new StringBuilder();

        try {
            if (constructor != null)  {
                Object object = constructor.newInstance();
                List<Field> fieldList = ReflectionUtils.getAllPublicAndProtectedFields(object.getClass());
                int i = 0;

                for (Field field : fieldList) {
                    String fieldName = field.getName();

                    // Check if is a translatable field
                    if (field.getDeclaredAnnotation(Translatable.class) != null) {
                        String defaultLang = MgnlContext.isWebContext()
                                ? I18nContentSupportFactory.getI18nSupport().getFallbackLocale().getLanguage()
                                : LangUtils.DEFAULT_LANG;

                        fieldName += !currentLang.equals(defaultLang) ? "_" + currentLang : "";
                    }

                    // Prevent children to destroy the query
                    if (field.getDeclaredAnnotation(Children.class) == null) {
                        addedQuerySB.append(" LOWER(")
                                .append(as)
                                .append(".")
                                .append(fieldName)
                                .append(") LIKE '%%")
                                .append(StringUtils.replace(searchTerm.toLowerCase(), "'", "''"))
                                .append("%%' ");

                        // Append 'OR' unless it's the last element
                        if (i != fieldList.size() - 1)
                            addedQuerySB.append(" OR ");
                    }

                    i++;
                }
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            log.error(e.getMessage(), e);
        } catch (InstantiationException e) {
            log.error("Cannot instantiate object", e);
        }

        return addedQuerySB.toString();
    }
}
