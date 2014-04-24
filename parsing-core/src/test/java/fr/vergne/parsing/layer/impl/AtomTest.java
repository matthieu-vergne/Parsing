package fr.vergne.parsing.layer.impl;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.vergne.parsing.layer.exception.ParsingException;

public class AtomTest {

	@Test
	public void testSetGetContent() {
		{
			String content = "test";
			Atom atom = new Atom(content);
			atom.setContent(content);
			assertEquals(content, atom.getContent());
		}
		{
			String content = "test\ntest";
			Atom atom = new Atom(content);
			atom.setContent(content);
			assertEquals(content, atom.getContent());
		}
		{
			String content = "";
			Atom atom = new Atom(content);
			atom.setContent(content);
			assertEquals(content, atom.getContent());
		}
	}

	@Test
	public void testDifferent() {
		{
			String content = "test";
			Atom atom = new Atom(content);
			try {
				atom.setContent("abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Unable to parse from 0: \"abc\" incompatible with \"\\Qtest\\E\"",
						e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Atom atom = new Atom(content);
			try {
				atom.setContent("abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Unable to parse from 0: \"abc\" incompatible with \"\\Qtest\\ntest\\E\"",
						e.getMessage());
			}
		}
		{
			String content = "";
			Atom atom = new Atom(content);
			try {
				atom.setContent("abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Unable to parse from 0: \"abc\" incompatible with \"\\Q\\E\"",
						e.getMessage());
			}
		}
	}

	@Test
	public void testTooLongOnRight() {
		{
			String content = "test";
			Atom atom = new Atom(content);
			try {
				atom.setContent(content + "abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Unable to parse from 0: \"testabc\" incompatible with \"\\Qtest\\E\"",
						e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Atom atom = new Atom(content);
			try {
				atom.setContent(content + "abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Unable to parse from 0: \"test\\ntestabc\" incompatible with \"\\Qtest\\ntest\\E\"",
						e.getMessage());
			}
		}
		{
			String content = "";
			Atom atom = new Atom(content);
			try {
				atom.setContent(content + "abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Unable to parse from 0: \"abc\" incompatible with \"\\Q\\E\"",
						e.getMessage());
			}
		}
	}

	@Test
	public void testTooLongOnLeft() {
		{
			String content = "test";
			Atom atom = new Atom(content);
			try {
				atom.setContent("abc" + content);
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Unable to parse from 0: \"abctest\" incompatible with \"\\Qtest\\E\"",
						e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Atom atom = new Atom(content);
			try {
				atom.setContent("abc" + content);
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Unable to parse from 0: \"abctest\\ntest\" incompatible with \"\\Qtest\\ntest\\E\"",
						e.getMessage());
			}
		}
		{
			String content = "";
			Atom atom = new Atom(content);
			try {
				atom.setContent("abc" + content);
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Unable to parse from 0: \"abc\" incompatible with \"\\Q\\E\"",
						e.getMessage());
			}
		}
	}

	@Test
	public void testTooLongOnMiddle() {
		{
			String content = "test";
			Atom atom = new Atom(content);
			try {
				atom.setContent(content.substring(0, 2) + "abc"
						+ content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Unable to parse from 0: \"teabcst\" incompatible with \"\\Qtest\\E\"",
						e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Atom atom = new Atom(content);
			try {
				atom.setContent(content.substring(0, 2) + "abc"
						+ content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Unable to parse from 0: \"teabcst\\ntest\" incompatible with \"\\Qtest\\ntest\\E\"",
						e.getMessage());
			}
			try {
				atom.setContent(content.substring(0, 6) + "abc"
						+ content.substring(6));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Unable to parse from 0: \"test\\ntabcest\" incompatible with \"\\Qtest\\ntest\\E\"",
						e.getMessage());
			}
		}
	}

	@Test
	public void testTooShortOnRight() {
		{
			String content = "test";
			Atom atom = new Atom(content);
			try {
				atom.setContent(content.substring(0, 2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Unable to parse from 0: \"te\" incompatible with \"\\Qtest\\E\"",
						e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Atom atom = new Atom(content);
			try {
				atom.setContent(content.substring(0, 2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Unable to parse from 0: \"te\" incompatible with \"\\Qtest\\ntest\\E\"",
						e.getMessage());
			}
			try {
				atom.setContent(content.substring(0, 4));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Unable to parse from 0: \"test\" incompatible with \"\\Qtest\\ntest\\E\"",
						e.getMessage());
			}
		}
	}

	@Test
	public void testTooShortOnLeft() {
		{
			String content = "test";
			Atom atom = new Atom(content);
			try {
				atom.setContent(content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Unable to parse from 0: \"st\" incompatible with \"\\Qtest\\E\"",
						e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Atom atom = new Atom(content);
			try {
				atom.setContent(content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Unable to parse from 0: \"st\\ntest\" incompatible with \"\\Qtest\\ntest\\E\"",
						e.getMessage());
			}
			try {
				atom.setContent(content.substring(5));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Unable to parse from 0: \"test\" incompatible with \"\\Qtest\\ntest\\E\"",
						e.getMessage());
			}
		}
	}

	@Test
	public void testTooShortOnMiddle() {
		{
			String content = "test";
			Atom atom = new Atom(content);
			try {
				atom.setContent(content.substring(0, 1) + content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Unable to parse from 0: \"tst\" incompatible with \"\\Qtest\\E\"",
						e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Atom atom = new Atom(content);
			try {
				atom.setContent(content.substring(0, 2) + content.substring(3));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Unable to parse from 0: \"tet\\ntest\" incompatible with \"\\Qtest\\ntest\\E\"",
						e.getMessage());
			}
			try {
				atom.setContent(content.substring(0, 2) + content.substring(6));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Unable to parse from 0: \"teest\" incompatible with \"\\Qtest\\ntest\\E\"",
						e.getMessage());
			}
			try {
				atom.setContent(content.substring(0, 6) + content.substring(7));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Unable to parse from 0: \"test\\ntst\" incompatible with \"\\Qtest\\ntest\\E\"",
						e.getMessage());
			}
		}
	}
}
