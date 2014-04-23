package fr.vergne.parsing.layer.exception;

@SuppressWarnings("serial")
public class ParsingException extends IllegalArgumentException {

	private final String regex;
	private final String content;
	private final int start;
	private final int end;

	public ParsingException(String regex, String content, int start,
			int end) {
		super("Incompatible format "
				+ (regex == null ? "(empty)" : "\"" + regex + "\"")
				+ " at position " + start + ": \""
				+ content.substring(start, end) + "\"");
		this.regex = regex;
		this.content = content;
		this.start = start;
		this.end = end;
	}

	public String getRegex() {
		return regex;
	}

	public String getContent() {
		return content;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}
}
