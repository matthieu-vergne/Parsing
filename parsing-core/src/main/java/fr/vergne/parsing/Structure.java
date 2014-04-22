package fr.vergne.parsing;

public interface Structure {

	/**
	 * This method aims at providing the complete regular expression which
	 * represents this {@link Structure}. However, grouping parenthesis are
	 * forbidden to avoid conflict with specific uses of this regular expression
	 * (but using "(?...)" is allowed, as it does not conflict).
	 * 
	 * @return the undecorated regular expression representing this
	 *         {@link Structure}
	 */
	public String getRegex();

	public String getContent();

	public void setContent(String content);

}
