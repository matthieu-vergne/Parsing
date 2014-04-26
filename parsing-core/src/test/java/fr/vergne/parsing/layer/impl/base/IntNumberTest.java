package fr.vergne.parsing.layer.impl.base;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import fr.vergne.parsing.layer.exception.ParsingException;

public class IntNumberTest {

	@Test
	public void testSetGetContentPositiveOnly() {
		IntNumber integer = new IntNumber(false);
		for (String num : Arrays.asList("0", "90", "2094")) {
			integer.setContent(num);
			assertEquals(num, integer.getContent());
		}
	}

	@Test
	public void testSetGetContentPositiveOrNegative() {
		IntNumber integer = new IntNumber(true);
		for (String sign : Arrays.asList("", "+", "-")) {
			for (String num : Arrays.asList("0", "90", "2094")) {
				String content = sign + num;
				integer.setContent(content);
				assertEquals(content, integer.getContent());
			}
		}
	}

	@Test
	public void testNoSignForPositiveOnly() {
		IntNumber integer = new IntNumber(false);
		for (String sign : Arrays.asList("", "+", "-")) {
			for (String num : Arrays.asList("0", "90", "2094")) {
				String content = sign + num;
				if (sign.isEmpty()) {
					integer.setContent(content);
					assertEquals(content, integer.getContent());
				} else {
					try {
						integer.setContent(content);
						fail(content + " accepted as positive-only integer.");
					} catch (ParsingException e) {
					}
				}
			}
		}
	}

	@Test
	public void testNoExcessiveZeros() {
		IntNumber integer = new IntNumber(true);
		for (String sign : Arrays.asList("", "+", "-")) {
			for (String num : Arrays.asList("00", "090", "002094")) {
				String content = sign + num;
				try {
					integer.setContent(content);
					fail(content
							+ " accepted as integer while has excessive zeros.");
				} catch (ParsingException e) {
				}
			}
		}
	}

	@Test
	public void testNofractionalPart() {
		IntNumber integer = new IntNumber(true);
		for (String sign : Arrays.asList("", "+", "-")) {
			for (String num : Arrays.asList("0.", "0.0", "90.1", "2094.321")) {
				String content = sign + num;
				try {
					integer.setContent(content);
					fail(content
							+ " accepted as integer while has fractional part.");
				} catch (ParsingException e) {
				}
			}
		}
	}

}
