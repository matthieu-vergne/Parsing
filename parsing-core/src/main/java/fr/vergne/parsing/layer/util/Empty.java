package fr.vergne.parsing.layer.util;

import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.standard.Atom;

/**
 * {@link Empty} is a generic-purpose {@link Layer} which matches only empty
 * strings. Not a single character should be present, even invisible characters
 * like newlines.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
public class Empty extends Atom {

	public Empty() {
		super("");
	}

}
