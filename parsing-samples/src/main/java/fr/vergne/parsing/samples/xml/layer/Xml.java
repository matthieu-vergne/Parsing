package fr.vergne.parsing.samples.xml.layer;

import fr.vergne.parsing.layer.standard.Suite;

public class Xml extends Suite {

	public Xml() {
		super(new XmlHeader(), new Blank(), new XmlTree());
	}
	
	public XmlHeader getHeader() {
		return get(0);
	}

	public XmlTree getTree() {
		return get(2);
	}

}
