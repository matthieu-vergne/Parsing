package fr.vergne.parsing.impl;

import java.util.regex.Pattern;

public class StaticAtom extends Atom {

	public StaticAtom(String content) {
		super(Pattern.quote(content));
	}

}
