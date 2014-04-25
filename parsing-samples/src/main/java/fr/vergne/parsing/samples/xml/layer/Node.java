package fr.vergne.parsing.samples.xml.layer;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.impl.Choice;

public class Node extends Choice {

	public Node() {
		super(new LeafNode(), new RecursiveNode());
	}

	public Fields getFields() {
		Layer node = getCurrent();
		if (node instanceof LeafNode) {
			return ((LeafNode) node).getFields();
		} else {
			return ((RecursiveNode) node).getFields();
		}
	}

	public Field getField(String name) {
		return getFields().getField(name);
	}

	public boolean hasChildren() {
		if (getCurrent() instanceof LeafNode) {
			return false;
		} else {
			return !((RecursiveNode) getCurrent()).getNodeList().isEmpty();
		}
	}

	public List<Node> getChildren() {
		if (getCurrent() instanceof LeafNode) {
			return Collections.emptyList();
		} else {
			return ((RecursiveNode) getCurrent()).getNodeList().getNodes();
		}
	}

	public NodeList getChildrenManager() {
		if (getCurrent() instanceof LeafNode) {
			throw new NoSuchElementException();
		} else {
			return ((RecursiveNode) getCurrent()).getNodeList();
		}
	}
}
