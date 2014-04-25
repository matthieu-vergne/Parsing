package fr.vergne.parsing.samples.xml.layer;

import fr.vergne.parsing.layer.impl.Atom;
import fr.vergne.parsing.layer.impl.Suite;

public class XmlHeader extends Suite {

	public XmlHeader() {
		super(new Atom("<?xml"), new Fields(), new Atom("?>"));
	}

	public Fields getFields() {
		return get(1);
	}

	public Field getField(String name) {
		return getFields().getField(name);
	}

	public Field getVersion() {
		return getField("version");
	}

	public Field getEncoding() {
		return getField("encoding");
	}
}
