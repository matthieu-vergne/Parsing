package fr.vergne.parsing.samples.xml.layer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import fr.vergne.parsing.layer.impl.Loop;

public class NodeList extends Loop<Node> {

	public NodeList() {
		super(new Generator<Node>() {

			@Override
			public Node generates() {
				return new Node();
			}
		});
	}

	public List<Node> getNodes() {
		List<Node> nodes = new LinkedList<Node>();
		for (Node node : this) {
			nodes.add(node);
		}
		return Collections.unmodifiableList(nodes);
	}

}
