package fr.vergne.parsing.layer.standard.impl;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import fr.vergne.parsing.layer.standard.Regex;
import fr.vergne.parsing.layer.standard.RegexTest;

@RunWith(JUnitPlatform.class)
public class JavaPatternRegexTest implements RegexTest {

	@Override
	public Regex instantiateRegex(String regex) {
		return new JavaPatternRegex(regex);
	}

}
