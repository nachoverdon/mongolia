package com.nachoverdon.mongolia.utils;

import com.nachoverdon.mongolia.annotations.Children;
import com.nachoverdon.mongolia.annotations.Translatable;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.Components;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Workspace;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class QueryUtils extends QueryUtil {

  /**
   * Wraps a String value between quotes.
   *
   * @param value The value to quote
   * @return The quoted value
   */
  public static String quote(String value) {
    return "'" + value.replaceAll("'", "''") + "'";
  }

  /**
   * Refer to {@link #quote(String)}.
   *
   * @param value The value to quote
   * @return The quoted value
   */
  public static String quote(int value) {
    return quote(String.valueOf(value));
  }

  /**
   * Refer to {@link #quote(String)}.
   *
   * @param value The value to quote
   * @return The quoted value
   */
  public static String quote(Object value) {
    return quote(String.valueOf(value));
  }

  /**
   * Get a workspace by its name.
   *
   * @param workspace The desired workspace. Ex: "website"
   * @return Workspace
   * @throws RepositoryException If the Workspace doesn't exist or can't be accessed.
   */
  @SuppressWarnings("deprecation")
  public static Workspace getWorkspace(String workspace) throws RepositoryException {
    return MgnlContext.getSystemContext().getJCRSession(workspace).getWorkspace();
  }

  /**
   * Builds a query for the given statement, workspace and language, setting the limit and the
   * offset of the query.
   *
   * @param statement The SQL/xpath statement that will be executed
   * @param workspace The desired workspace. Ex: "website"
   * @param language The language that will be used. Ex: "JCR-SQL2" {@link javax.jcr.query.Query}
   * @param limit The maximum amount of nodes to retrieve
   * @param offset The offset of the query
   * @return a Query with the given parameters
   * @throws RepositoryException If the Query cannot be created
   */
  public static Query getQuery(String statement, String workspace, String language, long limit,
                               long offset) throws RepositoryException {
    Query query = getWorkspace(workspace)
        .getQueryManager().createQuery(statement, language);

    if (limit > 0) {
      query.setLimit(limit);
    }

    if (offset > 0) {
      query.setOffset(offset);
    }

    return query;
  }

  /**
   * Refer to {@link #getQuery(String, String, String, long, long)}.
   *
   *
   * @param statement The SQL/xpath statement that will be executed
   * @param workspace The desired workspace. Ex: "website"
   * @param language The language that will be used. Ex: "JCR-SQL2" {@link javax.jcr.query.Query}
   * @return a Query with the given parameters
   * @throws RepositoryException If the Query cannot be created
   */
  public static Query getQuery(String statement, String workspace, String language)
      throws RepositoryException {
    return getQuery(statement, workspace, language, -1, 0);
  }

  /**
   * Builds a query for the given statement, workspace and language, setting the limit and the
   * offset of the query.
   *
   *
   * @param statement The SQL/xpath statement that will be executed
   * @param workspace The desired workspace. Ex: "website"
   * @return a Query with the given parameters
   * @throws RepositoryException If the Query cannot be created
   */
  public static Query getQuery(String statement, String workspace) throws RepositoryException {
    return getQuery(statement, workspace, Query.JCR_SQL2);
  }

  /**
   * Performs a query and retrieves the first available Node. It basically uses
   * {@link #search(String, String, String, String, boolean)} in combination with
   * {@link NodeUtils#getAnyOrNull(NodeIterator)}
   *
   * @param statement The SQL/xpath statement that will be executed
   * @param workspace The desired workspace. Ex: "website"
   * @param language The language that will be used. Ex: "JCR-SQL2" {@link javax.jcr.query.Query}
   * @param returnItemType Searches for statement and then pops-up in the node hierarchy until
   *                       returnItemType is found. If the result is not returnItemType or none of
   *                       its parents are then next node in result is checked. Duplicate nodes are
   *                       removed from result.
   * @param isSelector If isSelector is set to true then returnItemType will be used as the selector
   *                  and result will contain only nodes that are marked by this selector.
   * @return First available Node
   */
  @Nullable
  public static Node getNode(String statement, String workspace, String language,
                             String returnItemType, boolean isSelector) {
    try {
      return NodeUtils.getAnyOrNull(search(workspace, statement, language, returnItemType,
          isSelector, 1, 0));
    } catch (Exception e) {
      log.error("Query failed for workspace [" + workspace + "] and language [" + language
          + "] with returnItemType [" + returnItemType + "]" + (isSelector ? " as selector" : "")
          + " for statement:\n" +  statement, e);
    }

    return null;
  }

  /**
   * Refer to {@link #getNode(String, String, String, String, boolean)}.
   *
   * @param statement The SQL/xpath statement that will be executed
   * @param workspace The desired workspace. Ex: "website"
   * @param language The language that will be used. Ex: "JCR-SQL2" {@link javax.jcr.query.Query}
   * @param returnItemType Searches for statement and then pops-up in the node hierarchy until
   *                       returnItemType is found. If the result is not returnItemType or none of
   *                       its parents are then next node in result is checked. Duplicate nodes are
   *                       removed from result.
   * @return First available Node
   */
  public static Node getNode(String statement, String workspace, String language,
                             String returnItemType) {
    return getNode(statement, workspace, language, returnItemType, false);
  }

  /**
   * Refer to {@link #getNode(String, String, String, String, boolean)}.
   *
   * @param statement The SQL/xpath statement that will be executed
   * @param workspace The desired workspace. Ex: "website"
   * @param language The language that will be used. Ex: "JCR-SQL2" {@link javax.jcr.query.Query}
   * @return First available Node
   */
  @Nullable
  public static Node getNode(String statement, String workspace, String language) {
    try {
      return NodeUtils.getAnyOrNull(search(workspace, statement, language, 1));
    } catch (Exception e) {
      log.error("Query failed for workspace [" + workspace + "] and language [" + language
          + "] for statement:\n" +  statement, e);
    }

    return null;
  }

  /**
   * Refer to {@link #getNode(String, String, String, String, boolean)}.
   *
   * @param statement The JCR-SQL2 statement that will be executed
   * @param workspace The desired workspace. Ex: "website"
   * @return First available Node
   */
  public static Node getNode(String statement, String workspace) {
    return getNode(statement, workspace, Query.JCR_SQL2);
  }

  /**
   * Gets a limited collection of nodes starting at the given offset, optionally filtered with a
   * custom filter. Note that, if a custom filter is selected, the query will retrieve ALL nodes and
   * then it will apply the filter and the limit.
   *
   * @param statement The SQL/xpath statement that will be executed
   * @param workspace The desired workspace. Ex: "website"
   * @param language The language that will be used. Ex: "JCR-SQL2" {@link javax.jcr.query.Query}
   * @param limit The maximum amount of nodes to retrieve
   * @param offset The offset of the query
   * @param filter A custom node filter
   * @return A collection of nodes
   */
  public static Collection<Node> getNodesPaginated(String statement, String workspace,
                                                   String language, long limit, long offset,
                                                   Predicate<Node> filter) {
    try {
      Query query = getQuery(statement, workspace, language, limit, offset);

      if (query == null) {
        return Collections.emptyList();
      }

      if (filter != null) {
        query.setLimit(-1);
      }

      Collection<Node> nodes = NodeUtils.getCollectionFromNodeIterator(query.execute().getNodes());

      if (filter == null) {
        return nodes;
      }

      nodes.removeIf(filter);

      return limitNodes(nodes, limit);
    } catch (RepositoryException e) {
      log.error("Could not retrieve nodes with SQL on workspace [" + workspace + "]: " + statement,
          e);
    }

    return Collections.emptyList();
  }

  /**
   * Gets a collection of nodes, optionally filtered with a custom filter. To support queries with
   * INNER JOIN, a selector must be specified.
   *
   * @param statement The SQL/xpath statement that will be executed
   * @param workspace The desired workspace. Ex: "website"
   * @param language The language that will be used. Ex: "JCR-SQL2" {@link javax.jcr.query.Query}
   * @param selectorName The selector name
   * @param filter A custom node filter
   * @return A collection of nodes
   */
  public static Collection<Node> getNodes(String statement, String workspace, String language,
                                          String selectorName, Predicate<Node> filter) {
    try {
      Collection<Node> nodes = NodeUtils.getCollectionFromNodeIterator(
          StringUtils.isEmpty(selectorName)
              ? search(statement, workspace, language)
              : search(statement, workspace, language, selectorName, true)
      );

      if (filter != null) {
        nodes.removeIf(filter);
      }

      return nodes;
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  /**
   * Refer to {@link #getNodes(String, String, String, String, Predicate)}.
   *
   * @param statement The SQL/xpath statement that will be executed
   * @param workspace The desired workspace. Ex: "website"
   * @param language The language that will be used. Ex: "JCR-SQL2" {@link javax.jcr.query.Query}
   * @param filter A custom node filter
   * @return A collection of nodes
   */
  public static Collection<Node> getNodes(String statement, String workspace, String language,
                                          Predicate<Node> filter) {
    return getNodes(statement, workspace, language, null, filter);
  }

  /**
   * Refer to {@link #getNodes(String, String, String, String, Predicate)}.
   *
   * @param statement The SQL/xpath statement that will be executed
   * @param workspace The desired workspace. Ex: "website"
   * @param language The language that will be used. Ex: "JCR-SQL2" {@link javax.jcr.query.Query}
   * @param selectorName The selector name
   * @return A collection of nodes
   */
  public static Collection<Node> getNodes(String statement, String workspace, String language,
                                          String selectorName) {
    return getNodes(statement, workspace, language, selectorName, null);
  }

  /**
   * Refer to {@link #getNodes(String, String, String, Predicate)}.
   *
   * @param statement The SQL/xpath statement that will be executed
   * @param workspace The desired workspace. Ex: "website"
   * @param language The language that will be used. Ex: "JCR-SQL2" {@link javax.jcr.query.Query}
   * @return A collection of nodes
   */
  public static Collection<Node> getNodes(String statement, String workspace, String language) {
    return getNodes(statement, workspace, language, "");
  }

  /**
   * Refer to {@link #getNodes(String, String, String, Predicate)}.
   *
   * @param statement The SQL/xpath statement that will be executed
   * @param workspace The desired workspace. Ex: "website"
   * @param filter A custom node filter
   * @return A collection of nodes
   */
  public static Collection<Node> getNodes(String statement, String workspace,
                                          Predicate<Node> filter) {
    return getNodes(statement, workspace, Query.JCR_SQL2, filter);
  }

  /**
   * Refer to {@link #getNodes(String, String, String, Predicate)}.
   *
   * @param statement The SQL/xpath statement that will be executed
   * @param workspace The desired workspace. Ex: "website"
   * @return A collection of nodes
   */
  public static Collection<Node> getNodes(String statement, String workspace) {
    return getNodes(statement, workspace, Query.JCR_SQL2);
  }

  /**
   * Gets the count of all the nodes for a given workspace and statement.
   * You can optionally pass it a filter to remove nodes that meet a certain condition. It takes a
   * Predicate, so it works the same way as a Collection.removeIf.
   *
   * @param statement The SQL/xpath statement that will be executed
   * @param workspace The workspace where it should perform the query
   * @param language The query language to use
   * @param filter An optional custom filter to remove Nodes.
   * @return The count
   */
  public static long getNodesCount(String statement, String workspace, String language,
                                   Predicate<Node> filter) {
    try {
      Query query = getQuery(statement, workspace, language);

      if (filter == null) {
        return query.execute().getRows().getSize();
      }

      Collection<Node> nodes = NodeUtils.getCollectionFromNodeIterator(query.execute().getNodes());

      nodes.removeIf(filter);

      return nodes.size();
    } catch (RepositoryException e) {
      log.error("Could not retrieve total on workspace [" + workspace + "]: " + statement, e);
    }

    return 0L;
  }

  /**
   * Refer to {@link #getNodesCount(String, String, String, Predicate)}.
   *
   * @param statement The SQL/xpath statement that will be executed
   * @param workspace The workspace where it should perform the query
   * @param language The query language to use
   * @return The count
   */
  public static long getNodesCount(String statement, String workspace, String language) {
    return getNodesCount(statement, workspace, language, null);
  }

  /**
   * Refer to {@link #getNodesCount(String, String, String, Predicate)}
   * Uses JCR-SQL2 as default query language.
   *
   * @param statement The SQL/xpath statement that will be executed
   * @param workspace The workspace where it should perform the query
   * @param filter An optional custom filter to remove Nodes.
   * @return The count
   */
  public static long getNodesCount(String statement, String workspace, Predicate<Node> filter) {
    return getNodesCount(statement, workspace, Query.JCR_SQL2, filter);
  }

  /**
   * Refer to {@link #getNodesCount(String, String, String, Predicate)}
   * Uses JCR-SQL2 as default query language.
   *
   * @param statement The SQL/xpath statement that will be executed
   * @param workspace The workspace where it should perform the query
   * @return The count
   */
  public static long getNodesCount(String statement, String workspace) {
    return getNodesCount(statement, workspace, Query.JCR_SQL2);
  }

  /**
   * Create a JCR-SQL2 query condition to search on fields.
   * It will produce something like:
   * <pre>{@code
   *     setSearchableFields(Book.class, "Cinderella", "book");
   *     // => " LOWER(book.title_en) LIKE '%%cinderella%%' OR LOWER(book.title_es) LIKE
   *     // => '%%cinderella%%' "
   * }</pre>
   *
   * @param clazz The class of the JavaBean to build the query for
   * @param searchTerm The search term
   * @param as The variable name of the node
   * @return The condition of the query
   */
  public static String setSearchableFields(Class<?> clazz, String searchTerm, String as) {
    I18nContentSupport i18nContentSupport = Components.getComponent(I18nContentSupport.class);
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
                ? i18nContentSupport.getFallbackLocale().getLanguage()
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
            if (i != fieldList.size() - 1) {
              addedQuerySB.append(" OR ");
            }
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

  /**
   * Filters a collection of nodes to limit the amount.
   *
   * @param nodes The collection of nodes to limit
   * @param limit The maximum amount of nodes that the collection will have
   * @return A collection of nodes limited or empty.
   */
  public static Collection<Node> limitNodes(Collection<Node> nodes, long limit) {
    if (nodes == null) {
      return Collections.emptyList();
    }

    return nodes.stream().limit(limit).collect(Collectors.toList());
  }

  /**
   * Builds a simple JCR-SQL2 SELECT query. Usage example:
   * buildSqlStatement("mgnl:page", "AND", "title = 'Home'", "lang = 'es'")
   * // "SELECT * FROM [mgnl:page] WHERE title = 'Home' AND lang = 'es'"
   *
   * @param nodeType The desired node type to be selected
   * @param conditionType An optional condition. Must be "AND" or "OR".
   * @param conditions An optional condition. If omitted, it will return a select all query.
   * @return The query statement
   */
  public static String buildSqlStatement(String nodeType, @Nullable String conditionType,
                                         @Nullable String...conditions) {
    final StringBuilder statement = new StringBuilder("SELECT * FROM [" + nodeType + "] ");

    if (ArrayUtils.isEmpty(conditions)) {
      return statement.toString();
    }

    statement.append(" WHERE ");

    int index = 0;

    for (String condition: conditions) {
      if (StringUtils.isNotEmpty(condition)) {
        statement.append(" (").append(condition).append(") ");
      }

      index++;

      if (index != conditions.length) {
        statement.append(conditionType);
      }

    }

    return statement.toString();
  }

  /**
   * Builds a simple JCR-SQL2 SELECT query. Usage example:
   * buildSqlStatement("mgnl:page", "title = 'Home'")
   * // "SELECT * FROM [mgnl:page] WHERE title = 'Home'"
   *
   * @param nodeType The desired node type to be selected
   * @param condition An optional condition. If omitted, it will return a select all query.
   * @return The query statement
   */
  public static String buildSqlStatement(String nodeType, @Nullable String condition) {
    return buildSqlStatement(nodeType, null, condition);
  }

  /**
   * Refer to {@link #buildSqlStatement(String, String)}.
   *
   * @param nodeType The desired node type to be selected
   * @return The query statement
   */
  public static String buildSqlStatement(String nodeType) {
    return buildSqlStatement(nodeType, null);
  }

  /**
   * Performs a query using a simple SELECT statement.
   *
   * <pre>{@code
   *   SELECT * FROM [nodeType] WHERE (condition)
   * }</pre>
   * <p>
   * Optionally filtering the result.
   * </p>
   *
   * @param nodeType The desired node type to be selected
   * @param condition An optional condition. If omitted, it will return a select all query.
   * @param workspace The workspace where it should perform the query
   * @param filter An optional custom filter to remove Nodes.
   * @return A collection of nodes
   */
  public static Collection<Node> selectFrom(String nodeType, String condition, String workspace,
                                            Predicate<Node> filter) {
    String statement = buildSqlStatement(nodeType, condition);

    return getNodes(statement, workspace, filter);
  }

  /**
   * Refer to {@link #selectFrom(String, String, String, Predicate)}.
   *
   * @param nodeType The desired node type to be selected
   * @param workspace The workspace where it should perform the query
   * @param filter An optional custom filter to remove Nodes.
   * @return A collection of nodes
   */
  public static Collection<Node> selectFrom(String nodeType, String workspace,
                                            Predicate<Node> filter) {
    return selectFrom(nodeType, null, workspace, filter);
  }

  /**
   * Refer to {@link #selectFrom(String, String, String, Predicate)}.
   *
   * @param nodeType The desired node type to be selected
   * @param workspace The workspace where it should perform the query
   * @return A collection of nodes
   */
  public static Collection<Node> selectFrom(String nodeType, String workspace) {
    return selectFrom(nodeType, workspace, null);
  }

  /**
   * Performs {@link QueryUtil#search(String, String, String, String, boolean)} with a Query from
   * {@link #getQuery(String, String, String, long, long)}.
   *
   * @param workspace The desired workspace. Ex: "website"
   * @param statement The SQL/xpath statement that will be executed
   * @param language The language that will be used. Ex: "JCR-SQL2" {@link javax.jcr.query.Query}
   * @param returnItemType Searches for statement and then pops-up in the node hierarchy until
   *                       returnItemType is found. If the result is not returnItemType or none of
   *                       its parents are then next node in result is checked. Duplicate nodes are
   *                       removed from result.
   * @param isSelector If isSelector is set to true then returnItemType will be used as the selector
   *                  and result will contain only nodes that are marked by this selector.
   * @param limit The maximum amount of nodes to retrieve
   * @param offset The offset of the query
   * @return A collection of nodes
   * @throws RepositoryException Possible exceptions related to the JCR repository
   */
  public static NodeIterator search(String workspace, String statement, String language,
                                    String returnItemType, boolean isSelector, long limit,
                                    long offset) throws RepositoryException {
    QueryResult result = getQuery(statement, workspace, language, limit, offset).execute();

    if (isSelector) {
      return NodeUtil.filterDuplicates(
          NodeUtil.filterParentNodeType(result.getRows(), returnItemType)
      );
    }

    return NodeUtil.filterDuplicates(
        NodeUtil.filterParentNodeType(result.getNodes(), returnItemType)
    );
  }

  /**
   * Executes a query limiting its results.
   *
   * @param workspace The desired workspace. Ex: "website"
   * @param statement The SQL/xpath statement that will be executed
   * @param language The language that will be used. Ex: "JCR-SQL2" {@link javax.jcr.query.Query}
   * @param limit The maximum amount of nodes to retrieve
   * @param offset The offset of the query
   * @return A collection of nodes
   * @throws RepositoryException Possible exceptions related to the JCR repository
   */
  public static NodeIterator search(String workspace, String statement, String language, long limit,
                                    long offset) throws RepositoryException {
    QueryResult result = getQuery(statement, workspace, language, limit, offset).execute();

    return NodeUtil.filterDuplicates(result.getNodes());
  }

  /**
   * Refer to {@link #search(String, String, String, long, long)}.
   *
   * @param workspace The desired workspace. Ex: "website"
   * @param statement The SQL/xpath statement that will be executed
   * @param language The language that will be used. Ex: "JCR-SQL2" {@link javax.jcr.query.Query}
   * @param limit The maximum amount of nodes to retrieve
   * @return A collection of nodes
   * @throws RepositoryException Possible exceptions related to the JCR repository
   */
  public static NodeIterator search(String workspace, String statement, String language, long limit)
      throws RepositoryException {
    return search(statement, workspace, language, limit, 0);
  }

  /**
   * Refer to {@link #search(String, String, String, long, long)}.
   *
   * @param workspace The desired workspace. Ex: "website"
   * @param statement The SQL/xpath statement that will be executed
   * @param limit The maximum amount of nodes to retrieve
   * @return A collection of nodes
   * @throws RepositoryException Possible exceptions related to the JCR repository
   */
  public static NodeIterator search(String workspace, String statement, long limit)
      throws RepositoryException {
    return search(statement, workspace, Query.JCR_SQL2, limit, 0);
  }
}
