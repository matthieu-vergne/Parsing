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

	@Override
	public Object clone() {
		Newline clone = new Newline();
		String content = getContent();
		if (content != null) {
			clone.setContent(content);
		} else {
			// no content to set
		}
		return clone;
	}
}
