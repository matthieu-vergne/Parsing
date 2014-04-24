package fr.vergne.parsing.layer.exception;

@SuppressWarnings("serial")
public class ParsingException extends IllegalArgumentException {

	private final String regex;
	private final String content;
	private final int start;
	private final int end;

	public ParsingException(String regex, String content, int start, int end) {
		super(
				"Unable to parse from "
						+ start
						+ ": \""
						+ format(content.substring(start, end))
						+ "\" incompatible with "
						+ (regex == null ? "(empty)" : "\""
								+ formatRegex(regex) + "\""));
		this.regex = regex;
		this.content = content;
		this.start = start;
		this.end = end;
	}

	public static String formatRegex(String regex) {
		regex = regex.replaceAll("\\\\\\\\", "^^^\\\\^^^");
		regex = regex.replaceAll("(?<!\\\\)\\(\\?:", "(");
		regex = regex.replaceAll("\\^\\^\\^\\\\\\\\\\^\\^\\^", "\\\\");
		return format(regex);
	}

	public static String format(String string) {
		string = string.replaceAll("\n", "\\\\n");
		string = string.length() > 50 ? string.substring(0, 47) + "..."
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
