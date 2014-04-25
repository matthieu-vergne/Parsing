package fr.vergne.parsing.samples.xml.layer;

import fr.vergne.parsing.layer.impl.Atom;
import fr.vergne.parsing.layer.impl.Formula;
import fr.vergne.parsing.layer.impl.Suite;

public class Field extends Suite {

	public Field() {
		super(new Blank(), new Formula("[a-z]+"), new Atom("=\""), new Formula(
				"[^\"]*"), new Atom("\""), new Blank());
	}

	public Formula getName() {
		return get(1);
	}

	public Formula getValue() {
		return get(3);
	}
}
