package fr.vergne.parsing.util;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.standard.impl.Regex;

public class Newline extends Regex {

	public Newline() {
		super("(?:(?:\r\n)|(?:\n\r)|(?:(?<!\n)\r(?!\n))|(?:(?<!\r)\n(?!\r)))");
		setContent("\n");
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	public static Definition<Newline> DEFINITION = new Definition<Newline>() {

		@Override
		public String getRegex() {
			return create().getRegex();
		}

		@Override
		public Newline create() {
			return new Newline();
		}

		@Override
		public boolean isCompatibleWith(Newline layer) {
			return true;
		}
	};
}
