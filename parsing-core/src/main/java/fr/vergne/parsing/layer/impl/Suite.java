package fr.vergne.parsing.layer.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.exception.ParsingException;

/**
 * A {@link Suite} is a {@link Layer} representing an ordered sequence of
 * elements. This is particularly suited for structure templates like in
 * C/C++/Java/...:
 * <ol>
 * <li>"if ("</li>
 * <li>condition</li>
 * <li>") {"</li>
 * <li>block</li>
 * <li>"}"</li>
 * </ol>
 * or in HTML:
 * <ol>
 * <li>"&lt;a href='"</li>
 * <li>url</li>
 * <li>"'>"</li>
 * <li>content</li>
 * <li>"&lt;/a>"</li>
 * </ol>
 * At a higher level, it also fits global structures like the architecture of a
 * scientific paper for instance:
 * <ol>
 * <li>title</li>
 * <li>authors</li>
 * <li>abstract</li>
 * <li>introduction</li>
 * <li>problem</li>
 * <li>solution</li>
 * <li>discussion</li>
 * <li>conclusion</li>
 * </ol>
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
public class Suite extends AbstractLayer {

	private final List<? extends Layer> sequence;

	public Suite(List<? extends Layer> sequence) {
		this.sequence = Collections.unmodifiableList(sequence);
	}

	@Override
	public String getRegex() {
		String regex = "";
		for (Layer layer : sequence) {
			regex += layer.getRegex();
		}
		return regex;
	}

	@Override
	public String getContent() {
		String content = "";
		for (Layer layer : sequence) {
			content += layer.getContent();
		}
		return content;
	}

	@Override
	protected void setInternalContent(String content) {
		String regex = buildCapturingRegex(sequence);
		Matcher matcher = Pattern.compile("^" + regex + "$").matcher(content);
		if (matcher.find()) {
			int delta = 0;
			for (int i = 1; i <= matcher.groupCount(); i++) {
				String match = matcher.group(i);
				int subStart = delta;
				int subEnd = subStart + match.length();
				sequence.get(i - 1).setContent(
						content.substring(subStart, subEnd));
				delta += match.length();
			}
		} else {
			LinkedList<Layer> compatibleBeginning = new LinkedList<Layer>();
			do {
				compatibleBeginning.addLast(sequence.get(compatibleBeginning
						.size()));
				regex = buildCapturingRegex(compatibleBeginning);
				matcher = Pattern.compile("^" + regex).matcher(content);
			} while (matcher.find());
			compatibleBeginning.removeLast();

			LinkedList<Layer> compatibleEnding = new LinkedList<Layer>();
			do {
				compatibleEnding.addFirst(sequence.get(sequence.size()
						- compatibleEnding.size() - 1));
				regex = buildCapturingRegex(compatibleEnding);
				matcher = Pattern.compile(regex + "$").matcher(content);
			} while (matcher.find()
					&& compatibleEnding.size() + compatibleBeginning.size() < sequence
							.size() + 2);
			compatibleEnding.removeFirst();

			if (compatibleBeginning.size() + compatibleEnding.size() == sequence
					.size() + 1) {
				compatibleBeginning.removeLast();
				compatibleEnding.removeFirst();
			} else {
				// nothing to adapt
			}
			List<Layer> fakeSequence = new LinkedList<Layer>();
			fakeSequence.addAll(compatibleBeginning);
			Formula atom = new Formula(".*");
			fakeSequence.add(atom);
			fakeSequence.addAll(compatibleEnding);
			Suite s = new Suite(fakeSequence);
			s.setContent(content);
			String content2 = atom.getContent();
			String prefix = new Suite(compatibleBeginning).getContent();
			try {
				sequence.get(compatibleBeginning.size()).setContent(content2);
			} catch (ParsingException e) {
				throw new ParsingException(e.getRegex(), content,
						prefix.length() + e.getStart(), prefix.length()
								+ e.getEnd());
			}
		}
	}

	private String buildCapturingRegex(List<? extends Layer> compatibleEnding) {
		String regex;
		regex = "";
		for (Layer layer : compatibleEnding) {
			regex += "(" + layer.getRegex() + ")";
		}
		return regex;
	}

	@Override
	public String toString() {
		return sequence.toString();
	}
}
