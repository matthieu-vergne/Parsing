package fr.vergne.parsing.layer.util;

import fr.vergne.parsing.layer.standard.Atom;
import fr.vergne.parsing.layer.standard.Formula;
import fr.vergne.parsing.layer.standard.Option;
import fr.vergne.parsing.layer.standard.Suite;

public class Time extends Suite {

	public Time() {
		super(new Formula("[0-9]{1,2}"), new Atom(":"), new Formula(
				"[0-9]{1,2}"), new Option<Suite>(new Suite(new Atom(":"),
				new Formula("[0-9]{1,2}"), new Option<Suite>(new Suite(
						new Atom("."), new Formula("[0-9]{1,3}"))))));
	}

	public Integer getHours() {
		Formula formula = get(0);
		return Integer.parseInt(formula.getContent());
	}

	public Integer getMinutes() {
		Formula formula = get(2);
		return Integer.parseInt(formula.getContent());
	}

	public Integer getSeconds() {
		Option<Suite> option = get(3);
		if (option.isPresent()) {
			Formula formula = option.getOption().get(1);
			return Integer.parseInt(formula.getContent());
		} else {
			return null;
		}
	}

	public Integer getMilliseconds() {
		Option<Suite> option = get(3);
		if (option.isPresent()) {
			Option<Suite> option2 = option.getOption().get(2);
			if (option2.isPresent()) {
				Formula formula = option2.getOption().get(1);
				String content = formula.getContent();
				while (content.length() < 3) {
					content += "0";
				}
				return Integer.parseInt(content);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
}
