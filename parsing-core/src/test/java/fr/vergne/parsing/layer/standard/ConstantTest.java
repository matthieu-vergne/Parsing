package fr.vergne.parsing.layer.standard;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import fr.vergne.parsing.layer.LayerTest;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.standard.Constant;

@RunWith(JUnitPlatform.class)
public interface ConstantTest extends LayerTest<Constant> {
	
	Constant instantiateConstant(String content);

	@Override
	default Map<String, Constant> instantiateLayers(Collection<String> specialCharacters) {
		Map<String, Constant> map = new HashMap<>();
		for (String content : specialCharacters) {
			map.put(content, instantiateConstant(content));
		}
		return map;
	}

	@Test
	default void testSetGetContent() {
		{
			String content = "test";
			Constant atom = instantiateConstant(content);
			atom.setContent(content);
			assertEquals(content, atom.getContent());
		}
		{
			String content = "test\ntest";
			Constant atom = instantiateConstant(content);
			atom.setContent(content);
			assertEquals(content, atom.getContent());
		}
		{
			String content = "";
			Constant atom = instantiateConstant(content);
			atom.setContent(content);
			assertEquals(content, atom.getContent());
		}
	}

	@Test
	default void testDifferent() {
		{
			String content = "test";
			Constant atom = instantiateConstant(content);
			try {
				atom.setContent("abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\\Qtest\\E\" for content \"abc\"", e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Constant atom = instantiateConstant(content);
			try {
				atom.setContent("abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\\Qtest\\ntest\\E\" for content \"abc\"", e.getMessage());
			}
		}
		{
			String content = "";
			Constant atom = instantiateConstant(content);
			try {
				atom.setContent("abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\\Q\\E\" for content \"abc\"", e.getMessage());
			}
		}
	}

	@Test
	default void testTooLongOnRight() {
		{
			String content = "test";
			Constant atom = instantiateConstant(content);
			try {
				atom.setContent(content + "abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\\Qtest\\E\" for content \"testabc\"", e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Constant atom = instantiateConstant(content);
			try {
				atom.setContent(content + "abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\\Qtest\\ntest\\E\" for content \"test\\ntestabc\"", e.getMessage());
			}
		}
		{
			String content = "";
			Constant atom = instantiateConstant(content);
			try {
				atom.setContent(content + "abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\\Q\\E\" for content \"abc\"", e.getMessage());
			}
		}
	}

	@Test
	default void testTooLongOnLeft() {
		{
			String content = "test";
			Constant atom = instantiateConstant(content);
			try {
				atom.setContent("abc" + content);
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\\Qtest\\E\" for content \"abctest\"", e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Constant atom = instantiateConstant(content);
			try {
				atom.setContent("abc" + content);
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\\Qtest\\ntest\\E\" for content \"abctest\\ntest\"", e.getMessage());
			}
		}
		{
			String content = "";
			Constant atom = instantiateConstant(content);
			try {
				atom.setContent("abc" + content);
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\\Q\\E\" for content \"abc\"", e.getMessage());
			}
		}
	}

	@Test
	default void testTooLongOnMiddle() {
		{
			String content = "test";
			Constant atom = instantiateConstant(content);
			try {
				atom.setContent(content.substring(0, 2) + "abc" + content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\\Qtest\\E\" for content \"teabcst\"", e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Constant atom = instantiateConstant(content);
			try {
				atom.setContent(content.substring(0, 2) + "abc" + content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\\Qtest\\ntest\\E\" for content \"teabcst\\ntest\"", e.getMessage());
			}
			try {
				atom.setContent(content.substring(0, 6) + "abc" + content.substring(6));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\\Qtest\\ntest\\E\" for content \"test\\ntabcest\"", e.getMessage());
			}
		}
	}

	@Test
	default void testTooShortOnRight() {
		{
			String content = "test";
			Constant atom = instantiateConstant(content);
			try {
				atom.setContent(content.substring(0, 2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\\Qtest\\E\" for content \"te\"", e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Constant atom = instantiateConstant(content);
			try {
				atom.setContent(content.substring(0, 2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\\Qtest\\ntest\\E\" for content \"te\"", e.getMessage());
			}
			try {
				atom.setContent(content.substring(0, 4));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\\Qtest\\ntest\\E\" for content \"test\"", e.getMessage());
			}
		}
	}

	@Test
	default void testTooShortOnLeft() {
		{
			String content = "test";
			Constant atom = instantiateConstant(content);
			try {
				atom.setContent(content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\\Qtest\\E\" for content \"st\"", e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Constant atom = instantiateConstant(content);
			try {
				atom.setContent(content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\\Qtest\\ntest\\E\" for content \"st\\ntest\"", e.getMessage());
			}
			try {
				atom.setContent(content.substring(5));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\\Qtest\\ntest\\E\" for content \"test\"", e.getMessage());
			}
		}
	}

	@Test
	default void testTooShortOnMiddle() {
		{
			String content = "test";
			Constant atom = instantiateConstant(content);
			try {
				atom.setContent(content.substring(0, 1) + content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\\Qtest\\E\" for content \"tst\"", e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Constant atom = instantiateConstant(content);
			try {
				atom.setContent(content.substring(0, 2) + content.substring(3));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\\Qtest\\ntest\\E\" for content \"tet\\ntest\"", e.getMessage());
			}
			try {
				atom.setContent(content.substring(0, 2) + content.substring(6));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\\Qtest\\ntest\\E\" for content \"teest\"", e.getMessage());
			}
			try {
				atom.setContent(content.substring(0, 6) + content.substring(7));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"\\Qtest\\ntest\\E\" for content \"test\\ntst\"", e.getMessage());
			}
		}
	}
}
