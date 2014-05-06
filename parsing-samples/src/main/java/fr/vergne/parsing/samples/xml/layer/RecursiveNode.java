package fr.vergne.parsing.samples.xml.layer;

import fr.vergne.parsing.layer.standard.Atom;
import fr.vergne.parsing.layer.standard.Suite;

public class RecursiveNode extends Suite {

	public RecursiveNode() {
		super(new Blank(), new Atom("<node"), new Fields(), new Atom(">"), new NodeList(),
				new Blank(), new Atom("</node>"));
	}

	public Fields getFields() {
		return get(2);
	}

	public NodeList getNodeList() {
		return get(4);
	}
}
