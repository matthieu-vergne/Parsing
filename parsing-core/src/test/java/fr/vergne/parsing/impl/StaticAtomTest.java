package fr.vergne.parsing.impl;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.vergne.parsing.exception.IncompatibilityException;

public class StaticAtomTest {

	@Test
	public void testGetContent() {
		String content = "test";
		StaticAtom structure = new StaticAtom(content);

		structure.setContent(content);
		assertEquals(content, structure.getContent());
	}

	@Test
	public void testSetContent() {
		String content = "test";
		StaticAtom structure = new StaticAtom(content);

		structure.setContent(content);

		try {
			structure.setContent(content + "abc");
			fail("Exception not thrown.");
		} catch (IncompatibilityException e) {
		}
	}

}
