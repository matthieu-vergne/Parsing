package fr.vergne.parsing.samples.xml.layer;

import java.util.Iterator;
import java.util.List;

import fr.vergne.parsing.layer.impl.Atom;
import fr.vergne.parsing.layer.impl.Suite;

public class XmlTree extends Suite implements Iterable<Node> {

	public XmlTree() {
		super(new Atom("<tree>"), new NodeList(), new Blank(), new Atom(
				"</tree>"));
	}

	private NodeList getNodeList() {
		return get(1);
	}

	public List<Node> getNodes() {
		return getNodeList().getNodes();
	}

	@Override
	public Iterator<Node> iterator() {
		return getNodeList().iterator();
	}
}
