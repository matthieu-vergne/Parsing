package fr.vergne.parsing.layer.util;

import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.standard.Formula;

/**
 * {@link Any} represents any kind of string. Empty, newlines, any kind of
 * string matches with this {@link Layer}. This is a generic-purpose
 * {@link Layer} to grasp any content which is not supposed to be constrained.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
public class Any extends Formula {

	public Any() {
		super("[\\s\\S]*");
	}

}
