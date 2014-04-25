package fr.vergne.parsing.layer.impl;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.vergne.parsing.layer.exception.ParsingException;

public class FormulaTest {

	@Test
	public void testSetGetContent() {
		{
			String content = "test";
			Formula formula = new Formula("[a-z]{4}");
			formula.setContent(content);
			assertEquals(content, formula.getContent());
		}
		{
			String content = "test\ntest";
			Formula formula = new Formula("[a-z\n]{9}");
			formula.setContent(content);
			assertEquals(content, formula.getContent());
		}
		{
			String content = "";
			Formula formula = new Formula("");
			formula.setContent(content);
			assertEquals(content, formula.getContent());
		}
	}

	@Test
	public void testDifferent() {
		{
			Formula formula = new Formula("[a-z]{4}");
			try {
				formula.setContent("abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Incompatible regex \"[a-z]{4}\" for content \"abc\"",
						e.getMessage());
			}
		}
		{
			Formula formula = new Formula("[a-z\n]{9}");
			try {
				formula.setContent("abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Incompatible regex \"[a-z\\n]{9}\" for content \"abc\"",
						e.getMessage());
			}
		}
		{
			Formula formula = new Formula("");
			try {
				formula.setContent("abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\" for content \"abc\"",
						e.getMessage());
			}
		}
	}

	@Test
	public void testTooLongOnRight() {
		{
			String content = "test";
			Formula formula = new Formula("[a-z]{4}");
			try {
				formula.setContent(content + "abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Incompatible regex \"[a-z]{4}\" for content \"testabc\"",
						e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Formula formula = new Formula("[a-z\n]{9}");
			try {
				formula.setContent(content + "abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Incompatible regex \"[a-z\\n]{9}\" for content \"test\\ntestabc\"",
						e.getMessage());
			}
		}
		{
			String content = "";
			Formula formula = new Formula("");
			try {
				formula.setContent(content + "abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\" for content \"abc\"",
						e.getMessage());
			}
		}
	}

	@Test
	public void testTooLongOnLeft() {
		{
			String content = "test";
			Formula formula = new Formula("[a-z]{4}");
			try {
				formula.setContent("abc" + content);
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Incompatible regex \"[a-z]{4}\" for content \"abctest\"",
						e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Formula formula = new Formula("[a-z\n]{9}");
			try {
				formula.setContent("abc" + content);
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Incompatible regex \"[a-z\\n]{9}\" for content \"abctest\\ntest\"",
						e.getMessage());
			}
		}
		{
			String content = "";
			Formula formula = new Formula("");
			try {
				formula.setContent("abc" + content);
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\" for content \"abc\"",
						e.getMessage());
			}
		}
	}

	@Test
	public void testTooLongOnMiddle() {
		{
			String content = "test";
			Formula formula = new Formula("[a-z]{4}");
			try {
				formula.setContent(content.substring(0, 2) + "abc"
						+ content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Incompatible regex \"[a-z]{4}\" for content \"teabcst\"",
						e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Formula formula = new Formula("[a-z\n]{9}");
			try {
				formula.setContent(content.substring(0, 2) + "abc"
						+ content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Incompatible regex \"[a-z\\n]{9}\" for content \"teabcst\\ntest\"",
						e.getMessage());
			}
			try {
				formula.setContent(content.substring(0, 6) + "abc"
						+ content.substring(6));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Incompatible regex \"[a-z\\n]{9}\" for content \"test\\ntabcest\"",
						e.getMessage());
			}
		}
	}

	@Test
	public void testTooShortOnRight() {
		{
			String content = "test";
			Formula formula = new Formula("[a-z]{4}");
			try {
				formula.setContent(content.substring(0, 2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Incompatible regex \"[a-z]{4}\" for content \"te\"",
						e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Formula formula = new Formula("[a-z\n]{9}");
			try {
				formula.setContent(content.substring(0, 2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Incompatible regex \"[a-z\\n]{9}\" for content \"te\"",
						e.getMessage());
			}
			try {
				formula.setContent(content.substring(0, 4));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Incompatible regex \"[a-z\\n]{9}\" for content \"test\"",
						e.getMessage());
			}
		}
	}

	@Test
	public void testTooShortOnLeft() {
		{
			String content = "test";
			Formula formula = new Formula("[a-z]{4}");
			try {
				formula.setContent(content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Incompatible regex \"[a-z]{4}\" for content \"st\"",
						e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Formula formula = new Formula("[a-z\n]{9}");
			try {
				formula.setContent(content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Incompatible regex \"[a-z\\n]{9}\" for content \"st\\ntest\"",
						e.getMessage());
			}
			try {
				formula.setContent(content.substring(5));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Incompatible regex \"[a-z\\n]{9}\" for content \"test\"",
						e.getMessage());
			}
		}
	}

	@Test
	public void testTooShortOnMiddle() {
		{
			String content = "test";
			Formula formula = new Formula("[a-z]{4}");
			try {
				formula.setContent(content.substring(0, 1)
						+ content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Incompatible regex \"[a-z]{4}\" for content \"tst\"",
						e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Formula formula = new Formula("[a-z\n]{9}");
			try {
				formula.setContent(content.substring(0, 2)
						+ content.substring(3));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Incompatible regex \"[a-z\\n]{9}\" for content \"tet\\ntest\"",
						e.getMessage());
			}
			try {
				formula.setContent(content.substring(0, 2)
						+ content.substring(6));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Incompatible regex \"[a-z\\n]{9}\" for content \"teest\"",
						e.getMessage());
			}
			try {
				formula.setContent(content.substring(0, 6)
						+ content.substring(7));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Incompatible regex \"[a-z\\n]{9}\" for content \"test\\ntst\"",
						e.getMessage());
			}
		}
	}

	@Test
	public void testRegex() {
		{
			String regex = "[abc]{2}";
			assertEquals(regex, new Formula(regex).getRegex());
		}
		{
			String regex = "test+";
			assertEquals(regex, new Formula(regex).getRegex());
		}
		{
			String regex = "([a-z]+|[0-9]{3,5})";
			assertEquals(regex, new Formula(regex).getRegex());
		}
	}

}
