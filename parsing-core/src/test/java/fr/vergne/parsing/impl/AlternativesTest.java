package fr.vergne.parsing.impl;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import fr.vergne.parsing.exception.IncompatibilityException;

public class AlternativesTest {

	@Test
	public void testGetContent() {
		Atom lower = new Atom("[a-z]+");
		Atom upper = new Atom("[A-Z]+");
		Atom number = new Atom("[0-9]+");
		StaticAtom test = new StaticAtom("T35T");
		Alternatives alternatives = new Alternatives(Arrays.asList(lower, upper, number, test));
		
		String content = "abc";
		alternatives.setContent(content);
		assertEquals(content, alternatives.getContent());
		
		content = "ABC";
		alternatives.setContent(content);
		assertEquals(content, alternatives.getContent());
		
		content = "123";
		alternatives.setContent(content);
		assertEquals(content, alternatives.getContent());
	}

	@Test
	public void testInnerContentSynchronization() {
		Atom lower = new Atom("[a-z]+");
		Atom upper = new Atom("[A-Z]+");
		Atom number = new Atom("[0-9]+");
		StaticAtom test = new StaticAtom("T35T");
		Alternatives alternatives = new Alternatives(Arrays.asList(lower, upper, number, test));
		
		String content = "abc";
		alternatives.setContent(content);
		assertEquals(content, lower.getContent());
		
		content = "ABC";
		alternatives.setContent(content);
		assertEquals(content, upper.getContent());
		
		content = "321";
		alternatives.setContent(content);
		assertEquals(content, number.getContent());
		
		content = "T35T";
		alternatives.setContent(content);
		assertEquals(content, test.getContent());
	}

	@Test
	public void testSetContent() {
		Atom lower = new Atom("[a-z]+");
		Atom upper = new Atom("[A-Z]+");
		Atom number = new Atom("[0-9]+");
		StaticAtom test = new StaticAtom("T35T");
		Alternatives alternatives = new Alternatives(Arrays.asList(lower, upper, number, test));
		
		String content = "abc";
		alternatives.setContent(content);

		content = "654";
		alternatives.setContent(content);
		assertEquals(content, alternatives.getContent());

		try {
			alternatives.setContent("123abc");
			fail("Exception not thrown.");
		} catch (IncompatibilityException e) {
		}
	}

	@Test
	public void testIncompatibilityException() {
		Atom lower = new Atom("[a-z]+");
		Atom upper = new Atom("[A-Z]+");
		Atom number = new Atom("[0-9]+");
		StaticAtom test = new StaticAtom("T35T");
		Alternatives alternatives = new Alternatives(Arrays.asList(lower, upper, number, test));

		try {
			alternatives.setContent("abc12");
			fail("Exception not thrown");
		} catch (IncompatibilityException e) {
			assertEquals("Incompatible format \"(?:(?:[a-z]+)|(?:[A-Z]+)|(?:[0-9]+)|(?:\\QT35T\\E))\" at position 0: \"abc12\"",
					e.getMessage());
		}
	}

}
