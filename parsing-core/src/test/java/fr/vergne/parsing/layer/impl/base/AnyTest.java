package fr.vergne.parsing.layer.impl.base;

import static org.junit.Assert.*;

import org.junit.Test;

public class AnyTest {

	@Test
	public void testSetGetContent() {
		Any any = new Any();
		{
			String content = "";
			any.setContent(content);
			assertEquals(content, any.getContent());
		}
		{
			String content = "test";
			any.setContent(content);
			assertEquals(content, any.getContent());
		}
		{
			String content = "\n\n\n";
			any.setContent(content);
			assertEquals(content, any.getContent());
		}
		{
			String content = "\r\r\r";
			any.setContent(content);
			assertEquals(content, any.getContent());
		}
	}

}
