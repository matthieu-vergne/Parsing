package fr.vergne.parsing.layer.exception;

import static org.junit.Assert.*;

import org.junit.Test;

public class ParsingExceptionTest {

	@Test
	public void testGetters() {
		String regex = "abc";
		String content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
		ParsingException ex = new ParsingException(regex, content);
		assertEquals(regex, ex.getRegex());
		assertEquals(content, ex.getContent());
		assertEquals(0, ex.getStart());
		assertEquals(content.length(), ex.getEnd());
	}

	@Test
	public void testParenthesisReduction() {
		assertEquals("(abc)", ParsingException.formatRegex("(abc)"));
		assertEquals("(abc)", ParsingException.formatRegex("(?:abc)"));
		assertEquals("((abc)|(def))",
				ParsingException.formatRegex("(?:(?:abc)|(?:def))"));
		assertEquals("((abc)|(\\(?:)|(def))",
				ParsingException.formatRegex("(?:(?:abc)|(\\(?:)|(?:def))"));
	}

	@Test
	public void testPositionFormat() {
		String content = "012345678\n012345678\n0123456789";
		assertEquals("(1,1)", ParsingException.formatStart(content, 0));
		assertEquals("(1,2)", ParsingException.formatStart(content, 1));
		assertEquals("(1,3)", ParsingException.formatStart(content, 2));
		assertEquals("(1,10)", ParsingException.formatStart(content, 9));
		assertEquals("(2,1)", ParsingException.formatStart(content, 10));
		assertEquals("(2,2)", ParsingException.formatStart(content, 11));
		assertEquals("(2,3)", ParsingException.formatStart(content, 12));
		assertEquals("(2,10)", ParsingException.formatStart(content, 19));
		assertEquals("(3,1)", ParsingException.formatStart(content, 20));
	}

}
