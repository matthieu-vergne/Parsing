package fr.vergne.parsing.layer.exception;

import fr.vergne.parsing.layer.Layer;

@SuppressWarnings("serial")
public class ParsingException extends IllegalArgumentException {

	private final String regex;
	private final String content;
	private final int start;
	private final int end;

	public ParsingException(String regex, String content) {
		super(
				"Incompatible regex "
						+ (regex == null ? "(empty)" : "\""
								+ formatRegex(regex) + "\"")
						+ " for content \"" + format(content) + "\"");
		this.regex = regex;
		this.content = content;
		this.start = 0;
		this.end = content.length();
	}

	public ParsingException(Layer parent, Layer blocker, String content,
			int start, int end) {
		this(parent, blocker, content, start, end, null);
	}

	public ParsingException(Layer parent, Layer blocker, String content,
			int start, int end, Throwable cause) {
		super((blocker == null ? "Nothing expected" : "Unable to parse "
				+ format(blocker.toString()))
				+ " for "
				+ format(parent.toString())
				+ " from "
				+ formatStart(content, start)
				+ ": \""
				+ format(content.substring(start, end)) + "\"", cause);
		this.regex = blocker == null ? null : blocker.getRegex();
		this.content = content;
		this.start = start;
		this.end = end;
	}

	public static String formatStart(String content, int start) {
		String prefix = content.substring(0, start);
		prefix = prefix.replaceAll("(\n\r?)|(\r\n?)", "\n");
		int line = prefix.replaceAll("[^\n]", "").length() + 1;
		int position = prefix.replaceAll(".*\n", "").length() + 1;
		return "(" + line + "," + position + ")";
	}

	public static String formatRegex(String regex) {
		regex = regex.replaceAll("\\\\\\\\", "^^^\\\\^^^");
		regex = regex.replaceAll("(?<!\\\\)\\(\\?:", "(");
		regex = regex.replaceAll("\\^\\^\\^\\\\\\\\\\^\\^\\^", "\\\\");
		return format(regex);
	}

	public static String format(String string) {
		string = string.replaceAll("\n", "\\\\n");
		string = string.replaceAll("\r", "\\\\r");
		string = string.length() > 50 ? string.substring(0, 23) + "..."
				+ string.substring(string.length() - 23, string.length())
				: string;
		return string;
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
