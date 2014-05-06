package fr.vergne.parsing.layer.util;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.standard.Formula;
import fr.vergne.parsing.layer.standard.Suite;

public class NewlineTest {

	@Test
	public void testNewlineUnits() {
		Newline newline = new Newline();
		newline.setContent("\r");
		newline.setContent("\n");
		newline.setContent("\r\n");
		newline.setContent("\n\r");
		try {
			newline.setContent("");
			fail("Empty line recognised as single newline.");
		} catch (ParsingException e) {
		}
		try {
			newline.setContent("\r\r");
			fail("Double \\r recognised as single newline.");
		} catch (ParsingException e) {
		}
		try {
			newline.setContent("\n\n");
			fail("Double \\n recognised as single newline.");
		} catch (ParsingException e) {
		}
		try {
			newline.setContent("\r\n\r");
			fail("Excessive \\r considered in the newline.");
		} catch (ParsingException e) {
		}
		try {
			newline.setContent("\n\r\n");
			fail("Excessive \\n considered in the newline.");
		} catch (ParsingException e) {
		}
	}

	@Test
	public void testNewlineEnclosed() {
		Formula leftPart = new Formula(".*");
		Formula rightPart = new Formula(".*");
		Suite suite = new Suite(leftPart, new Newline(), rightPart);
		for (String newline : Arrays.asList("\r", "\n", "\r\n", "\n\r")) {
			{
				String left = "left";
				String right = "right";
				suite.setContent(left + newline + right);
				assertEquals(left, leftPart.getContent());
				assertEquals(right, rightPart.getContent());
			}
			{
				String left = "if face)";
				String right = "ライム";
				suite.setContent(left + newline + right);
				assertEquals(left, leftPart.getContent());
				assertEquals(right, rightPart.getContent());
			}
		}
	}

	@Test
	public void testNewlineSequence() {
		for (String newline : Arrays.asList("\r", "\n", "\r\n", "\n\r")) {
			Suite suite = new Suite(new Newline(), new Newline(), new Newline());
			suite.setContent(newline + newline + newline);
			try {
				suite.setContent(newline + newline);
			} catch (ParsingException e) {
			}
			try {
				suite.setContent(newline + newline + newline + newline);
			} catch (ParsingException e) {
			}
		}
	}
}
