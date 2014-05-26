package fr.vergne.parsing.layer.util;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.LayerTest;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.standard.Atom;
import fr.vergne.parsing.layer.standard.Loop.Generator;

public class SeparatedLoopTest extends LayerTest {

	@Override
	protected Layer instantiateFilledLayer() {
		SeparatedLoop<IntNumber, Atom> separatedLoop = new SeparatedLoop<IntNumber, Atom>(
				new Generator<IntNumber>() {

					@Override
					public IntNumber generates() {
						return new IntNumber();
					}
				}, new Generator<Atom>() {

					@Override
					public Atom generates() {
						return new Atom("-");
					}
				});
		separatedLoop.setContent("9-8-7");
		return separatedLoop;
	}

	@Test
	public void testSize() {
		SeparatedLoop<IntNumber, Atom> separatedLoop = new SeparatedLoop<IntNumber, Atom>(
				new Generator<IntNumber>() {

					@Override
					public IntNumber generates() {
						return new IntNumber();
					}
				}, new Generator<Atom>() {

					@Override
					public Atom generates() {
						return new Atom("-");
					}
				});

		separatedLoop.setContent("");
		assertEquals(0, separatedLoop.size());

		separatedLoop.setContent("1");
		assertEquals(1, separatedLoop.size());

		separatedLoop.setContent("1-2");
		assertEquals(2, separatedLoop.size());

		separatedLoop.setContent("9-8-7");
		assertEquals(3, separatedLoop.size());
	}

	@Test
	public void testGetIndex() {
		SeparatedLoop<IntNumber, Atom> separatedLoop = new SeparatedLoop<IntNumber, Atom>(
				new Generator<IntNumber>() {

					@Override
					public IntNumber generates() {
						return new IntNumber();
					}
				}, new Generator<Atom>() {

					@Override
					public Atom generates() {
						return new Atom("-");
					}
				});

		separatedLoop.setContent("1");
		assertEquals(1, separatedLoop.get(0).getValue());

		separatedLoop.setContent("1-2");
		assertEquals(1, separatedLoop.get(0).getValue());
		assertEquals(2, separatedLoop.get(1).getValue());

		separatedLoop.setContent("9-8-7");
		assertEquals(9, separatedLoop.get(0).getValue());
		assertEquals(8, separatedLoop.get(1).getValue());
		assertEquals(7, separatedLoop.get(2).getValue());
	}

	@Test
	public void testGetWrongIndex() {
		SeparatedLoop<IntNumber, Atom> separatedLoop = new SeparatedLoop<IntNumber, Atom>(
				new Generator<IntNumber>() {

					@Override
					public IntNumber generates() {
						return new IntNumber();
					}
				}, new Generator<Atom>() {

					@Override
					public Atom generates() {
						return new Atom("-");
					}
				});

		separatedLoop.setContent("");
		try {
			separatedLoop.get(-1);
		} catch (IndexOutOfBoundsException e) {
		}
		try {
			separatedLoop.get(0);
		} catch (IndexOutOfBoundsException e) {
		}

		separatedLoop.setContent("1");
		try {
			separatedLoop.get(-1);
		} catch (IndexOutOfBoundsException e) {
		}
		try {
			separatedLoop.get(1);
		} catch (IndexOutOfBoundsException e) {
		}

		separatedLoop.setContent("1-2");
		try {
			separatedLoop.get(-1);
		} catch (IndexOutOfBoundsException e) {
		}
		try {
			separatedLoop.get(2);
		} catch (IndexOutOfBoundsException e) {
		}

		separatedLoop.setContent("9-8-7");
		try {
			separatedLoop.get(-1);
		} catch (IndexOutOfBoundsException e) {
		}
		try {
			separatedLoop.get(3);
		} catch (IndexOutOfBoundsException e) {
		}
	}

	@Test
	public void testParsingException() {
		SeparatedLoop<IntNumber, Atom> separatedLoop = new SeparatedLoop<IntNumber, Atom>(
				new Generator<IntNumber>() {

					@Override
					public IntNumber generates() {
						return new IntNumber();
					}
				}, new Generator<Atom>() {

					@Override
					public Atom generates() {
						return new Atom("-");
					}
				});

		try {
			separatedLoop.setContent("-");
			fail("No exception thrown.");
		} catch (ParsingException e) {
		}

		try {
			separatedLoop.setContent("-1");
			fail("No exception thrown.");
		} catch (ParsingException e) {
		}

		try {
			separatedLoop.setContent("1-");
			fail("No exception thrown.");
		} catch (ParsingException e) {
		}

		try {
			separatedLoop.setContent("-1-");
			fail("No exception thrown.");
		} catch (ParsingException e) {
		}

		try {
			separatedLoop.setContent("1x2");
			fail("No exception thrown.");
		} catch (ParsingException e) {
		}
	}

	@Test
	public void testIterator() {
		SeparatedLoop<IntNumber, Atom> separatedLoop = new SeparatedLoop<IntNumber, Atom>(
				new Generator<IntNumber>() {

					@Override
					public IntNumber generates() {
						return new IntNumber();
					}
				}, new Generator<Atom>() {

					@Override
					public Atom generates() {
						return new Atom("-");
					}
				});

		separatedLoop.setContent("1-2-3-4-5");
		Iterator<IntNumber> iterator = separatedLoop.iterator();
		assertTrue(iterator.hasNext());
		assertEquals("1", iterator.next().getContent());
		assertTrue(iterator.hasNext());
		assertEquals("2", iterator.next().getContent());
		assertTrue(iterator.hasNext());
		assertEquals("3", iterator.next().getContent());
		assertTrue(iterator.hasNext());
		assertEquals("4", iterator.next().getContent());
		assertTrue(iterator.hasNext());
		assertEquals("5", iterator.next().getContent());
		assertFalse(iterator.hasNext());
	}

}
