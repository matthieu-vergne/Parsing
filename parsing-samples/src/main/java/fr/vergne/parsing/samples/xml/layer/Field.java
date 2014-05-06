package fr.vergne.parsing.samples.xml.layer;

import fr.vergne.parsing.layer.standard.Atom;
import fr.vergne.parsing.layer.standard.Formula;
import fr.vergne.parsing.layer.standard.Suite;

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
