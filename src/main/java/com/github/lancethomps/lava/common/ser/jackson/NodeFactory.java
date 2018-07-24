package com.github.lancethomps.lava.common.ser.jackson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A factory for creating Node objects.
 */
public class NodeFactory {

	/**
	 * Find circular references.
	 *
	 * @param node the node
	 * @return the sets the
	 */
	public static Set<String> findCircularReferences(JsonNode node) {
		return findCircularReferences(node, "root", new ArrayList<>(), new LinkedHashSet<>());
	}

	/**
	 * Find circular references.
	 *
	 * @param node the node
	 * @param fullKey the full key
	 * @param foundNodes the found nodes
	 * @param currentCircular the current circular
	 * @return the sets the
	 */
	public static Set<String> findCircularReferences(JsonNode node, String fullKey, List<Pair<String, JsonNode>> foundNodes, Set<String> currentCircular) {
		if (!node.isContainerNode()) {
			return currentCircular;
		}
		Pair<String, JsonNode> foundNode = foundNodes.stream().filter(found -> found.getRight() == node).findFirst().orElse(null);
		if (foundNode != null) {
			currentCircular.add(foundNode.getLeft());
			currentCircular.add(fullKey);
			return currentCircular;
		}
		foundNodes.add(Pair.of(fullKey, node));
		if (node.isObject()) {
			Iterator<Entry<String, JsonNode>> iterator = ((ObjectNode) node).fields();
			while (iterator.hasNext()) {
				Entry<String, JsonNode> field = iterator.next();
				findCircularReferences(field.getValue(), fullKey + '.' + field.getKey(), foundNodes, currentCircular);
			}
		} else if (node.isArray()) {
			ArrayNode arrayNode = (ArrayNode) node;
			int size = arrayNode.size();
			for (int index = 0; index < size; index++) {
				findCircularReferences(arrayNode.get(index), fullKey + '[' + index + ']', foundNodes, currentCircular);
			}
		}
		return currentCircular;
	}

}
