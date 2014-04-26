package fr.vergne.parsing.layer.impl.base;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.vergne.parsing.layer.exception.ParsingException;

public class EmptyTest {

	@Test
	public void testSetGetContent() {
		Empty empty = new Empty();
		String content = "";
		empty.setContent(content);
		assertEquals(content, empty.getContent());
	}

	@Test
	public void testNotEmpty() {
		Empty empty = new Empty();
		try {
			empty.setContent("a");
			fail("Character considered as empty.");
		} catch (ParsingException e) {
		}
		try {
			empty.setContent(" ");
			fail("Space considered as empty.");
		} catch (ParsingException e) {
		}
		try {
			empty.setContent("\n");
			fail("\\n considered as empty.");
		} catch (ParsingException e) {
		}
		try {
			empty.setContent("\r");
			fail("\\r considered as empty.");
		} catch (ParsingException e) {
		}
	}

}
