package fr.vergne.parsing.impl;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.vergne.parsing.exception.IncompatibilityException;

public class AtomTest {

	@Test
	public void testGetContent() {
		String content = "ca";
		Atom structure = new Atom("[abc]{2}");

		structure.setContent(content);
		assertEquals(content, structure.getContent());
	}

	@Test
	public void testSetContent() {
		Atom structure = new Atom("[a-z]{2,4}");

		String content = "run";
		structure.setContent(content);
		assertEquals(content, structure.getContent());

		try {
			structure.setContent("BOOM");
			fail("Exception not thrown.");
		} catch (IncompatibilityException e) {
		}
	}

}
