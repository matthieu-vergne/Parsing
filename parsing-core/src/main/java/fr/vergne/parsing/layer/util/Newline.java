package fr.vergne.parsing.layer.util;

import fr.vergne.parsing.layer.standard.Formula;

public class Newline extends Formula {

	public Newline() {
		super("(?:(?:\r\n)|(?:\n\r)|(?:(?<!\n)\r(?!\n))|(?:(?<!\r)\n(?!\r)))");
		setContent("\n");
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
