package fr.vergne.parsing.layer.standard.impl;

import java.util.regex.Pattern;

import fr.vergne.parsing.layer.standard.Constant;

// TODO remove (definition factory uses its own, test this one)
// TODO Doc
public class RegexBasedConstant extends JavaPatternRegex implements Constant {

	public RegexBasedConstant(String content) {
		super(Pattern.quote(content));
		setContent(content);
	}

	@Override
	public String getConstant() {
		return getContent();
	}

	@Override
	public String getName() {
		return Constant.super.getName();
	}

	@Override
	public String toString() {
		return getName() + "[" + getContent() + "]";
	}
}
