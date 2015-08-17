package fr.vergne.parsing.layer.standard;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.Layer.ContentListener;
import fr.vergne.parsing.layer.LayerTest;
import fr.vergne.parsing.layer.exception.ParsingException;

public class SuiteTest extends LayerTest {

	@Override
	protected Map<String, Layer> instantiateLayers(
			Collection<String> specialCharacters) {
		StringBuilder builder = new StringBuilder();
		List<Layer> sequence = new LinkedList<Layer>();
		for (String character : specialCharacters) {
			builder.append(character);
			sequence.add(new Atom(character));
		}
		Suite suite = new Suite(sequence);

		Map<String, Layer> map = new HashMap<String, Layer>();
		map.put(builder.toString(), suite);
		return map;
	}

	@Test
	public void testSetGetContent() {
		{
			String content = "test";
			Formula letter1 = new Formula("[a-zA-Z]");
			Atom letter2 = new Atom("e");
			Atom letter3 = new Atom("s");
			Formula letter4 = new Formula("[a-zA-Z]");
			Suite suite = new Suite(letter1, letter2, letter3, letter4);
			suite.setContent(content);
			assertEquals(content, suite.getContent());
		}
		{
			String content = "test\ntest";
			Formula word1 = new Formula("[a-z]{4}");
			Formula space = new Formula("\\s+");
			Formula word2 = new Formula("[a-z]{4}");
			Suite suite = new Suite(word1, space, word2);
			suite.setContent(content);
			assertEquals(content, suite.getContent());
		}
		{
			String content = "test";
			Formula word1 = new Formula("");
			Atom word2 = new Atom("test");
			Formula word3 = new Formula("");
			Suite suite = new Suite(word1, word2, word3);
			suite.setContent(content);
			assertEquals(content, suite.getContent());
		}
	}

	@Test
	public void testDifferent() {
		{
			Formula letter1 = new Formula("[a-zA-Z]");
			Atom letter2 = new Atom("e");
			Atom letter3 = new Atom("s");
			Formula letter4 = new Formula("[a-zA-Z]");
			Suite suite = new Suite(letter1, letter2, letter3, letter4);
			try {
				suite.setContent("1esr");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + letter1 + " for " + suite
						+ " from (1,1): \"1esr\"", e.getMessage());
			}
			try {
				suite.setContent("taxi");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + letter2 + " for " + suite
						+ " from (1,2): \"axi\"", e.getMessage());
			}
			try {
				suite.setContent("text");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + letter3 + " for " + suite
						+ " from (1,3): \"xt\"", e.getMessage());
			}
			try {
				suite.setContent("tes1");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + letter4 + " for " + suite
						+ " from (1,4): \"1\"", e.getMessage());
			}
		}
		{
			Formula word1 = new Formula("[a-z]{4}");
			Formula space = new Formula("\\s+");
			Formula word2 = new Formula("[a-z]{4}");
			Suite suite = new Suite(word1, space, word2);
			try {
				suite.setContent("abc\ndef");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word1 + " for " + suite
						+ " from (1,1): \"abc\\ndef\"", e.getMessage());
			}
			try {
				suite.setContent("test-def");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + space + " for " + suite
						+ " from (1,5): \"-def\"", e.getMessage());
			}
			try {
				suite.setContent("test\ntex");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word2 + " for " + suite
						+ " from (2,1): \"tex\"", e.getMessage());
			}
		}
		{
			Formula word1 = new Formula("");
			Atom word2 = new Atom("test");
			Formula word3 = new Formula("");
			Suite suite = new Suite(word1, word2, word3);
			try {
				suite.setContent("abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word2 + " for " + suite
						+ " from (1,1): \"abc\"", e.getMessage());
			}
		}
	}

	@Test
	public void testTooLongOnRight() {
		{
			String content = "test";
			Formula letter1 = new Formula("[a-zA-Z]");
			Atom letter2 = new Atom("e");
			Atom letter3 = new Atom("s");
			Formula letter4 = new Formula("[a-zA-Z]");
			Suite suite = new Suite(letter1, letter2, letter3, letter4);
			try {
				suite.setContent(content + "abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + letter4 + " for " + suite
						+ " from (1,4): \"tabc\"", e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Formula word1 = new Formula("[a-z]{4}");
			Formula space = new Formula("\\s+");
			Formula word2 = new Formula("[a-z]{4}");
			Suite suite = new Suite(word1, space, word2);
			try {
				suite.setContent(content + "abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word2 + " for " + suite
						+ " from (2,1): \"testabc\"", e.getMessage());
			}
		}
		{
			String content = "test";
			Formula word1 = new Formula("");
			Atom word2 = new Atom("test");
			Formula word3 = new Formula("");
			Suite suite = new Suite(word1, word2, word3);
			try {
				suite.setContent(content + "abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word3 + " for " + suite
						+ " from (1,5): \"abc\"", e.getMessage());
			}
		}
	}

	@Test
	public void testTooLongOnLeft() {
		{
			String content = "test";
			Formula letter1 = new Formula("[a-zA-Z]");
			Atom letter2 = new Atom("e");
			Atom letter3 = new Atom("s");
			Formula letter4 = new Formula("[a-zA-Z]");
			Suite suite = new Suite(letter1, letter2, letter3, letter4);
			try {
				suite.setContent("abc" + content);
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + letter2 + " for " + suite
						+ " from (1,2): \"bctest\"", e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Formula word1 = new Formula("[a-z]{4}");
			Formula space = new Formula("\\s+");
			Formula word2 = new Formula("[a-z]{4}");
			Suite suite = new Suite(word1, space, word2);
			try {
				suite.setContent("abc" + content);
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + space + " for " + suite
						+ " from (1,5): \"est\\ntest\"", e.getMessage());
			}
		}
		{
			String content = "test";
			Formula word1 = new Formula("");
			Atom word2 = new Atom("test");
			Formula word3 = new Formula("");
			Suite suite = new Suite(word1, word2, word3);
			try {
				suite.setContent("abc" + content);
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word2 + " for " + suite
						+ " from (1,1): \"abctest\"", e.getMessage());
			}
		}
	}

	@Test
	public void testTooLongOnMiddle() {
		{
			Formula letter1 = new Formula("[a-zA-Z]");
			Atom letter2 = new Atom("e");
			Atom letter3 = new Atom("s");
			Formula letter4 = new Formula("[a-zA-Z]");
			Suite suite = new Suite(letter1, letter2, letter3, letter4);
			try {
				suite.setContent("teabcst");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + letter3 + " for " + suite
						+ " from (1,3): \"abcst\"", e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Formula word1 = new Formula("[a-z]{4}");
			Formula space = new Formula("\\s+");
			Formula word2 = new Formula("[a-z]{4}");
			Suite suite = new Suite(word1, space, word2);
			try {
				suite.setContent(content.substring(0, 2) + "abc"
						+ content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + space + " for " + suite
						+ " from (1,5): \"cst\\ntest\"", e.getMessage());
			}
			try {
				suite.setContent(content.substring(0, 6) + "abc"
						+ content.substring(6));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word2 + " for " + suite
						+ " from (2,1): \"tabcest\"", e.getMessage());
			}
		}
	}

	@Test
	public void testTooShortOnRight() {
		{
			String content = "test";
			Formula letter1 = new Formula("[a-zA-Z]");
			Atom letter2 = new Atom("e");
			Atom letter3 = new Atom("s");
			Formula letter4 = new Formula("[a-zA-Z]");
			Suite suite = new Suite(letter1, letter2, letter3, letter4);
			try {
				suite.setContent(content.substring(0, 2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + letter3 + " for " + suite
						+ " from (1,3): \"\"", e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Formula word1 = new Formula("[a-z]{4}");
			Formula space = new Formula("\\s+");
			Formula word2 = new Formula("[a-z]{4}");
			Suite suite = new Suite(word1, space, word2);
			try {
				suite.setContent(content.substring(0, 2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word1 + " for " + suite
						+ " from (1,1): \"te\"", e.getMessage());
			}
			try {
				suite.setContent(content.substring(0, 4));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + space + " for " + suite
						+ " from (1,5): \"\"", e.getMessage());
			}
			try {
				suite.setContent(content.substring(0, 5));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word2 + " for " + suite
						+ " from (2,1): \"\"", e.getMessage());
			}
			try {
				suite.setContent(content.substring(0, 7));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word2 + " for " + suite
						+ " from (2,1): \"te\"", e.getMessage());
			}
		}
	}

	@Test
	public void testTooShortOnLeft() {
		{
			String content = "test";
			Formula letter1 = new Formula("[a-zA-Z]");
			Atom letter2 = new Atom("e");
			Atom letter3 = new Atom("s");
			Formula letter4 = new Formula("[a-zA-Z]");
			Suite suite = new Suite(letter1, letter2, letter3, letter4);
			try {
				suite.setContent(content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + letter2 + " for " + suite
						+ " from (1,2): \"t\"", e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Formula word1 = new Formula("[a-z]{4}");
			Formula space = new Formula("\\s+");
			Formula word2 = new Formula("[a-z]{4}");
			Suite suite = new Suite(word1, space, word2);
			try {
				suite.setContent(content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word1 + " for " + suite
						+ " from (1,1): \"st\\ntest\"", e.getMessage());
			}
			try {
				suite.setContent(content.substring(4));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word1 + " for " + suite
						+ " from (1,1): \"\\ntest\"", e.getMessage());
			}
		}
	}

	@Test
	public void testTooShortOnMiddle() {
		{
			String content = "test";
			Formula letter1 = new Formula("[a-zA-Z]");
			Atom letter2 = new Atom("e");
			Atom letter3 = new Atom("s");
			Formula letter4 = new Formula("[a-zA-Z]");
			Suite suite = new Suite(letter1, letter2, letter3, letter4);
			try {
				suite.setContent(content.substring(0, 1) + content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + letter2 + " for " + suite
						+ " from (1,2): \"st\"", e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Formula word1 = new Formula("[a-z]{4}");
			Formula space = new Formula("\\s+");
			Formula word2 = new Formula("[a-z]{4}");
			Suite suite = new Suite(word1, space, word2);
			try {
				suite.setContent(content.substring(0, 2) + content.substring(3));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word1 + " for " + suite
						+ " from (1,1): \"tet\\ntest\"", e.getMessage());
			}
			try {
				suite.setContent(content.substring(0, 2) + content.substring(6));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + space + " for " + suite
						+ " from (1,5): \"t\"", e.getMessage());
			}
			try {
				suite.setContent(content.substring(0, 6) + content.substring(7));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word2 + " for " + suite
						+ " from (2,1): \"tst\"", e.getMessage());
			}
		}
	}

	@Test
	public void testInnerContentSynchronization() {
		String content = "A testing case.";
		Formula word1 = new Formula("[a-zA-Z]+");
		Formula word2 = new Formula("[a-zA-Z]+");
		Formula word3 = new Formula("[a-zA-Z]+");
		Atom space1 = new Atom(" ");
		Atom space2 = new Atom(" ");
		Atom dot = new Atom(".");
		Suite suite = new Suite(Arrays.asList(word1, space1, word2, space2,
				word3, dot));
		suite.setContent(content);

		assertEquals("A", word1.getContent());
		assertEquals("testing", word2.getContent());
		assertEquals("case", word3.getContent());

		word1.setContent("Another");
		word2.setContent("running");
		assertEquals("Another", word1.getContent());
		assertEquals("running", word2.getContent());
		assertEquals("case", word3.getContent());
		assertEquals("Another running case.", suite.getContent());
	}

	@Test
	public void testGetIndex() {
		Formula word1 = new Formula("[a-zA-Z]+");
		Formula word2 = new Formula("[a-zA-Z]+");
		Formula word3 = new Formula("[a-zA-Z]+");
		Atom space1 = new Atom(" ");
		Atom space2 = new Atom(" ");
		Atom dot = new Atom(".");
		Suite suite = new Suite(word1, space1, word2, space2, word3, dot);

		assertSame(word1, suite.get(0));
		assertSame(space1, suite.get(1));
		assertSame(word2, suite.get(2));
		assertSame(space2, suite.get(3));
		assertSame(word3, suite.get(4));
		assertSame(dot, suite.get(5));
	}

	@Test
	public void testInnerContentUpdateOfFilledSuiteNotifiesListeners() {
		Formula word1 = new Formula("[a-zA-Z]+");
		Formula word2 = new Formula("[a-zA-Z]+");
		Formula word3 = new Formula("[a-zA-Z]+");
		Atom space1 = new Atom(" ");
		Atom space2 = new Atom(" ");
		Atom dot = new Atom(".");
		Suite suite = new Suite(word1, space1, word2, space2, word3, dot);
		suite.setContent("A testing case.");

		final String[] value = new String[] { null };
		suite.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		word1.setContent("Another");
		assertEquals(suite.getContent(), value[0]);
		word2.setContent("running");
		assertEquals(suite.getContent(), value[0]);
	}

	@Test
	public void testInnerContentUpdateOfUnfilledSuiteDoesNotNotifyListeners() {
		Formula word1 = new Formula("[a-zA-Z]+");
		Formula word2 = new Formula("[a-zA-Z]+");
		Formula word3 = new Formula("[a-zA-Z]+");
		Atom space1 = new Atom(" ");
		Atom space2 = new Atom(" ");
		Atom dot = new Atom(".");
		Suite suite = new Suite(word1, space1, word2, space2, word3, dot);

		final String[] value = new String[] { null };
		suite.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		word1.setContent("A");
		assertEquals(null, value[0]);
		word2.setContent("testing");
		assertEquals(null, value[0]);
		word3.setContent("case");
		assertEquals(suite.getContent(), value[0]);
	}

	@Test
	public void testListenersNotifiedOncePerAtomicUpdate() {
		Formula word1 = new Formula("[a-zA-Z]+");
		Formula word2 = new Formula("[a-zA-Z]+");
		Formula word3 = new Formula("[a-zA-Z]+");
		Atom space1 = new Atom(" ");
		Atom space2 = new Atom(" ");
		Atom dot = new Atom(".");
		Suite suite = new Suite(word1, space1, word2, space2, word3, dot);
		final LinkedList<String> values = new LinkedList<String>();
		suite.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				values.addFirst(newContent);
			}
		});

		int operationCounter = 0;

		suite.setContent("A running test.");
		assertEquals(++operationCounter, values.size());
		assertEquals(suite.getContent(), values.getFirst());

		suite.setContent("Another testing case.");
		assertEquals(++operationCounter, values.size());
		assertEquals(suite.getContent(), values.getFirst());
	}

	@Test
	public void testNoProblemWithLazyComponents() {
		String content = "abc?def";
		Suite suite = new Suite(new Formula(".+?"), new Atom("?"), new Formula(
				".+?"));

		suite.setContent(content);
		assertEquals(content, suite.getContent());
		assertEquals("abc", suite.get(0).getContent());
		assertEquals("?", suite.get(1).getContent());
		assertEquals("def", suite.get(2).getContent());
	}
}
