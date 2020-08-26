package com.nachoverdon.mongolia.bean2node;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import lombok.extern.slf4j.Slf4j;
import org.jcrom.Jcrom;

@Slf4j
public class Bean2Node {

  private static final Set<Class<?>> classes = new HashSet<>();
  private static Jcrom mapper;
  private static boolean isInit = false;

  /**
   * Adds a class to the mapper.
   *
   * @param clazz The class to map.
   */
  public static void addClass(Class<?> clazz) {
    classes.add(clazz);
    isInit = false;
    init();
  }

  private static void init() {
    if (!isInit) {
      mapper = new Jcrom(classes);
    }
  }

  /**
   * Transforms a Java bean to a JCR Node.
   *
   * @param obj The object to transform.
   * @param parentNode The parent Node that will hold the Node.
   * @param nodePath The name (path) of the Node, relative to the parent Node.
   * @param <T> The type of the object to transform.
   * @return Node The transformed node.
   */
  @Nullable
  public static <T> Node toNode(T obj, Node parentNode, String nodePath, String nodeType) {
    init();

    Node node = null;

    try {
      // If the Node exists, just update it, don't add it.
      if (parentNode.hasNode(nodePath)) {
        node = mapper.updateNode(parentNode.getNode(nodePath), obj);
      } else {
        node = mapper.addNode(parentNode, obj);
        node.setPrimaryType(nodeType);
      }

    } catch (RepositoryException e) {
      log.error("Unable to create node " + nodePath, e);
    }

    return node;
  }
}
