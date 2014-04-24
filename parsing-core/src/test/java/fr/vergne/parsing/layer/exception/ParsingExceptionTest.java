package fr.vergne.parsing.layer.exception;

import static org.junit.Assert.*;

import org.junit.Test;

public class ParsingExceptionTest {

	@Test
	public void testGetters() {
		String regex = "abc";
		String content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
		int start = 10;
		int end = 20;
		ParsingException ex = new ParsingException(regex, content, start, end);
		assertEquals(regex, ex.getRegex());
		assertEquals(content, ex.getContent());
		assertEquals(start, ex.getStart());
		assertEquals(end, ex.getEnd());
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

}
