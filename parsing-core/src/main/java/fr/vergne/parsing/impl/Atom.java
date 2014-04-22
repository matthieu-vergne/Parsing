package fr.vergne.parsing.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.vergne.parsing.Structure;
import fr.vergne.parsing.exception.IncompatibilityException;

public class Atom implements Structure {

	private final String regex;
	private String content;

	public Atom(String regex) {
		this.regex = regex;
	}

	@Override
	public String getRegex() {
		return regex;
	}

	@Override
	public String getContent() {
		return content;
	}

	@Override
	public void setContent(String content) {
		Matcher matcher = Pattern.compile("^" + regex + "$").matcher(content);
		if (matcher.find()) {
			this.content = matcher.group();
		} else {
			throw new IncompatibilityException(regex, content, 0,
					content.length());
		}
	}

	@Override
	public String toString() {
		return "{" + regex + "=" + content + "}";
	}
}
