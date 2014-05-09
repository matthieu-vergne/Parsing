package fr.vergne.parsing.layer.util;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.vergne.parsing.layer.exception.ParsingException;

public class TimeTest {

	@Test
	public void testHourMinute() {
		Time time = new Time();

		time.setContent("1:2");
		assertNotNull(time.getHours());
		assertEquals(1, (int) time.getHours());
		assertNotNull(time.getMinutes());
		assertEquals(2, (int) time.getMinutes());
		assertNull(time.getSeconds());
		assertNull(time.getMilliseconds());

		time.setContent("03:04");
		assertNotNull(time.getHours());
		assertEquals(3, (int) time.getHours());
		assertNotNull(time.getMinutes());
		assertEquals(4, (int) time.getMinutes());
		assertNull(time.getSeconds());
		assertNull(time.getMilliseconds());
	}

	@Test
	public void testHourMinuteSecond() {
		Time time = new Time();

		time.setContent("1:2:3");
		assertNotNull(time.getHours());
		assertEquals(1, (int) time.getHours());
		assertNotNull(time.getMinutes());
		assertEquals(2, (int) time.getMinutes());
		assertNotNull(time.getSeconds());
		assertEquals(3, (int) time.getSeconds());
		assertNull(time.getMilliseconds());

		time.setContent("03:04:05");
		assertNotNull(time.getHours());
		assertEquals(3, (int) time.getHours());
		assertNotNull(time.getMinutes());
		assertEquals(4, (int) time.getMinutes());
		assertNotNull(time.getSeconds());
		assertEquals(5, (int) time.getSeconds());
		assertNull(time.getMilliseconds());
	}

	@Test
	public void testHourMinuteSecondMillis() {
		Time time = new Time();

		time.setContent("1:2:3.04");
		assertNotNull(time.getHours());
		assertEquals(1, (int) time.getHours());
		assertNotNull(time.getMinutes());
		assertEquals(2, (int) time.getMinutes());
		assertNotNull(time.getSeconds());
		assertEquals(3, (int) time.getSeconds());
		assertNotNull(time.getMilliseconds());
		assertEquals(40, (int) time.getMilliseconds());

		time.setContent("03:04:05.678");
		assertNotNull(time.getHours());
		assertEquals(3, (int) time.getHours());
		assertNotNull(time.getMinutes());
		assertEquals(4, (int) time.getMinutes());
		assertNotNull(time.getSeconds());
		assertEquals(5, (int) time.getSeconds());
		assertNotNull(time.getMilliseconds());
		assertEquals(678, (int) time.getMilliseconds());
	}

	@Test
	public void testParsingException() {
		Time time = new Time();

		try {
			time.setContent("123");
			fail("No exception thrown.");
		} catch (ParsingException e) {
		}

		try {
			time.setContent("12:");
			fail("No exception thrown.");
		} catch (ParsingException e) {
		}

		try {
			time.setContent("123:12");
			fail("No exception thrown.");
		} catch (ParsingException e) {
		}

		try {
			time.setContent("12:12:");
			fail("No exception thrown.");
		} catch (ParsingException e) {
		}

		try {
			time.setContent("12:123:12");
			fail("No exception thrown.");
		} catch (ParsingException e) {
		}

		try {
			time.setContent("12:12:12.");
			fail("No exception thrown.");
		} catch (ParsingException e) {
		}

		try {
			time.setContent("12:12:12.1234");
			fail("No exception thrown.");
		} catch (ParsingException e) {
		}
	}

}
