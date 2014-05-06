package fr.vergne.parsing.samples.xml.layer;

import fr.vergne.parsing.layer.standard.Atom;
import fr.vergne.parsing.layer.standard.Suite;

public class LeafNode extends Suite {

	public LeafNode() {
		super(new Blank(), new Atom("<node"), new Fields(), new Atom("/>"));
	}

	public Fields getFields() {
		return get(2);
	}
}
