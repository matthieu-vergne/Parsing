package fr.vergne.parsing.layer.standard.impl;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import fr.vergne.parsing.layer.LayerTest;
import fr.vergne.parsing.layer.exception.ParsingException;

@RunWith(JUnitPlatform.class)
public class RegexTest implements LayerTest<Regex> {

	@Override
	public Map<String, Regex> instantiateLayers(Collection<String> specialCharacters) {
		Regex formula = new Regex("(?s:.+)");
		Map<String, Regex> map = new HashMap<>();
		for (String content : specialCharacters) {
			map.put(content, formula);
		}
		return map;
	}

	@Test
	public void testSetGetContent() {
		{
			String content = "test";
			Regex formula = new Regex("[a-z]{4}");
			formula.setContent(content);
			assertEquals(content, formula.getContent());
		}
		{
			String content = "test\ntest";
			Regex formula = new Regex("[a-z\n]{9}");
			formula.setContent(content);
			assertEquals(content, formula.getContent());
		}
		{
			String content = "";
			Regex formula = new Regex("");
			formula.setContent(content);
			assertEquals(content, formula.getContent());
		}
	}

	@Test
	public void testDifferent() {
		{
			Regex formula = new Regex("[a-z]{4}");
			try {
				formula.setContent("abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"[a-z]{4}\" for content \"abc\"", e.getMessage());
			}
		}
		{
			Regex formula = new Regex("[a-z\n]{9}");
			try {
				formula.setContent("abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"[a-z\\n]{9}\" for content \"abc\"", e.getMessage());
			}
		}
		{
			Regex formula = new Regex("");
			try {
				formula.setContent("abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\" for content \"abc\"", e.getMessage());
			}
		}
	}

	@Test
	public void testTooLongOnRight() {
		{
			String content = "test";
			Regex formula = new Regex("[a-z]{4}");
			try {
				formula.setContent(content + "abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"[a-z]{4}\" for content \"testabc\"", e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Regex formula = new Regex("[a-z\n]{9}");
			try {
				formula.setContent(content + "abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"[a-z\\n]{9}\" for content \"test\\ntestabc\"", e.getMessage());
			}
		}
		{
			String content = "";
			Regex formula = new Regex("");
			try {
				formula.setContent(content + "abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\" for content \"abc\"", e.getMessage());
			}
		}
	}

	@Test
	public void testTooLongOnLeft() {
		{
			String content = "test";
			Regex formula = new Regex("[a-z]{4}");
			try {
				formula.setContent("abc" + content);
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"[a-z]{4}\" for content \"abctest\"", e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Regex formula = new Regex("[a-z\n]{9}");
			try {
				formula.setContent("abc" + content);
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"[a-z\\n]{9}\" for content \"abctest\\ntest\"", e.getMessage());
			}
		}
		{
			String content = "";
			Regex formula = new Regex("");
			try {
				formula.setContent("abc" + content);
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\" for content \"abc\"", e.getMessage());
			}
		}
	}

	@Test
	public void testTooLongOnMiddle() {
		{
			String content = "test";
			Regex formula = new Regex("[a-z]{4}");
			try {
				formula.setContent(content.substring(0, 2) + "abc" + content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"[a-z]{4}\" for content \"teabcst\"", e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Regex formula = new Regex("[a-z\n]{9}");
			try {
				formula.setContent(content.substring(0, 2) + "abc" + content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"[a-z\\n]{9}\" for content \"teabcst\\ntest\"", e.getMessage());
			}
			try {
				formula.setContent(content.substring(0, 6) + "abc" + content.substring(6));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"[a-z\\n]{9}\" for content \"test\\ntabcest\"", e.getMessage());
			}
		}
	}

	@Test
	public void testTooShortOnRight() {
		{
			String content = "test";
			Regex formula = new Regex("[a-z]{4}");
			try {
				formula.setContent(content.substring(0, 2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"[a-z]{4}\" for content \"te\"", e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Regex formula = new Regex("[a-z\n]{9}");
			try {
				formula.setContent(content.substring(0, 2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"[a-z\\n]{9}\" for content \"te\"", e.getMessage());
			}
			try {
				formula.setContent(content.substring(0, 4));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"[a-z\\n]{9}\" for content \"test\"", e.getMessage());
			}
		}
	}

	@Test
	public void testTooShortOnLeft() {
		{
			String content = "test";
			Regex formula = new Regex("[a-z]{4}");
			try {
				formula.setContent(content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"[a-z]{4}\" for content \"st\"", e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Regex formula = new Regex("[a-z\n]{9}");
			try {
				formula.setContent(content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"[a-z\\n]{9}\" for content \"st\\ntest\"", e.getMessage());
			}
			try {
				formula.setContent(content.substring(5));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"[a-z\\n]{9}\" for content \"test\"", e.getMessage());
			}
		}
	}

	@Test
	public void testTooShortOnMiddle() {
		{
			String content = "test";
			Regex formula = new Regex("[a-z]{4}");
			try {
				formula.setContent(content.substring(0, 1) + content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"[a-z]{4}\" for content \"tst\"", e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Regex formula = new Regex("[a-z\n]{9}");
			try {
				formula.setContent(content.substring(0, 2) + content.substring(3));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"[a-z\\n]{9}\" for content \"tet\\ntest\"", e.getMessage());
			}
			try {
				formula.setContent(content.substring(0, 2) + content.substring(6));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"[a-z\\n]{9}\" for content \"teest\"", e.getMessage());
			}
			try {
				formula.setContent(content.substring(0, 6) + content.substring(7));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"[a-z\\n]{9}\" for content \"test\\ntst\"", e.getMessage());
			}
		}
	}

	@Test
	public void testRegex() {
		{
			String regex = "[abc]{2}";
			assertEquals(regex, new Regex(regex).getRegex());
		}
		{
			String regex = "test+";
			assertEquals(regex, new Regex(regex).getRegex());
		}
		{
			String regex = "([a-z]+|[0-9]{3,5})";
			assertEquals(regex, new Regex(regex).getRegex());
		}
	}
}
