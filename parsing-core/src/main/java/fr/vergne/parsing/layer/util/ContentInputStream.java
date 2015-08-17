package fr.vergne.parsing.layer.util;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

public class ContentInputStream extends ByteArrayInputStream {

	private final static Charset ENCODING = Charset.forName("UTF-8");

	public ContentInputStream(String content) {
		super(content.getBytes(ENCODING));
	}

}
