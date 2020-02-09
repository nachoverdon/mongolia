package com.nachoverdon.mongolia.utils;

import info.magnolia.jcr.util.NodeUtil;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import java.util.function.Consumer;

@Slf4j
public class NodeUtils extends NodeUtil {

    /**
     * Gets the first node that it finds or null
     *
     * @param nodes A NodeIterator
     * @return First available Node
     */
    @Nullable
    public static Node getAnyOrNull(NodeIterator nodes) {
        return nodes.hasNext() ? nodes.nextNode() : null;
    }

    /**
     * Performs an action for every nodes
     *
     * @param nodes A NodeIterator
     * @param action The action to perform
     */
    public static void forEach(NodeIterator nodes, Consumer<Node> action) {
        while (nodes.hasNext())
            action.accept(nodes.nextNode());
    }

}
