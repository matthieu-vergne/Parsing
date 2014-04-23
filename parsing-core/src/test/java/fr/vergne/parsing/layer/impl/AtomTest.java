package fr.vergne.parsing.layer.impl;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.vergne.parsing.layer.exception.ParsingException;

public class AtomTest {

	@Test
	public void testGetContent() {
		String content = "test";
		Atom atom = new Atom(content);

		atom.setContent(content);
		assertEquals(content, atom.getContent());
	}

	@Test
	public void testSetContent() {
		String content = "test";
		Atom atom = new Atom(content);

		atom.setContent(content);

		try {
			atom.setContent(content + "abc");
			fail("Exception not thrown.");
		} catch (ParsingException e) {
		}
	}

}
