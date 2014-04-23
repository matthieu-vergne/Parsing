package fr.vergne.parsing.layer.impl;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.vergne.parsing.layer.exception.ParsingException;

public class FormulaTest {

	@Test
	public void testRegex() {
		String regex = "[abc]{2}";
		Formula formula = new Formula(regex);
		assertEquals(regex, formula.getRegex());
	}

	@Test
	public void testGetContent() {
		String content = "ca";
		Formula formula = new Formula("[abc]{2}");

		formula.setContent(content);
		assertEquals(content, formula.getContent());
	}

	@Test
	public void testSetContent() {
		Formula formula = new Formula("[a-z]{2,4}");

		String content = "run";
		formula.setContent(content);
		assertEquals(content, formula.getContent());

		try {
			formula.setContent("BOOM");
			fail("Exception not thrown.");
		} catch (ParsingException e) {
		}
	}

}
