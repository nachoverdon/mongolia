package com.nachoverdon.mongolia.utils;

import info.magnolia.jcr.util.NodeUtil;

import javax.annotation.Nullable;
import javax.jcr.Node;
import javax.jcr.NodeIterator;

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
}
