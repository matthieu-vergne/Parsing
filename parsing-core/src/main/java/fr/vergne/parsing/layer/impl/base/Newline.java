package fr.vergne.parsing.layer.impl.base;

import fr.vergne.parsing.layer.impl.Formula;

public class Newline extends Formula {

	public Newline() {
		super("(?:(?:\r\n)|(?:\n\r)|(?:(?<!\n)\r(?!\n))|(?:(?<!\r)\n(?!\r)))");
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
