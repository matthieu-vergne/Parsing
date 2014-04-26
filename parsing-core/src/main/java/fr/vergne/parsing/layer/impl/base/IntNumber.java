package fr.vergne.parsing.layer.impl.base;

import fr.vergne.parsing.layer.impl.Atom;
import fr.vergne.parsing.layer.impl.Formula;
import fr.vergne.parsing.layer.impl.Suite;

/**
 * An {@link IntNumber} represents an integer. It can accept or not negative
 * values depending on the parameters. It does not accept leading zeros like in
 * "007", thus a proper "7" should be provided in this case.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
public class IntNumber extends Formula {

	/**
	 * Instantiate an {@link IntNumber} which can have a sign or not, depending
	 * on the provided arguments. If negative integers are allowed, a sign (+ or
	 * -) will be considered, but optional for positive ones. Otherwise, no sign
	 * will be accepted and all values will be considered as positive integers.
	 * If you want to parse integers but force a given sign, you can use a
	 * {@link Suite} which takes the sign that you want as an {@link Atom}
	 * followed by an {@link IntNumber} which does not allow negative values.
	 * 
	 * @param allowNegative
	 *            <code>true</code> if negative integers should be accepted
	 */
	public IntNumber(boolean allowNegative) {
		super((allowNegative ? "[+-]?(?:" : "") + "0|(?:[1-9][0-9]*)"
				+ (allowNegative ? ")" : ""));
	}

	/**
	 * Same as {@link #Integer(boolean)} with negative values not allowed.
	 */
	public IntNumber() {
		this(false);
	}

	public int getValue() {
		return java.lang.Integer.parseInt(getContent());
	}

}
