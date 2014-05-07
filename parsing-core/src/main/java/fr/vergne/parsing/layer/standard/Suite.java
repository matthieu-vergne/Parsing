package fr.vergne.parsing.layer.standard;

import java.util.Arrays;
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

	public Suite(Layer... sequence) {
		this(Arrays.asList(sequence));
	}

	@Override
	protected String buildRegex() {
		String regex = "";
		for (Layer layer : sequence) {
			regex += "(?:" + layer.getRegex() + ")";
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
		Matcher matcher = Pattern.compile(
				"^" + buildCapturingRegex(sequence) + "$").matcher(content);
		if (matcher.find()) {
			int delta = 0;
			for (int i = 1; i <= matcher.groupCount(); i++) {
				String match = matcher.group(i);
				int subStart = delta;
				int subEnd = subStart + match.length();
				Layer item = sequence.get(i - 1);
				try {
					item.setContent(content.substring(subStart, subEnd));
				} catch (ParsingException e) {
					throw new ParsingException(this, item, content, subStart
							+ e.getStart(), subEnd, e);
				}
				delta += match.length();
			}
		} else {
			LinkedList<Layer> preOk = new LinkedList<Layer>(sequence);
			LinkedList<Layer> innerKo = new LinkedList<Layer>();
			do {
				innerKo.addFirst(preOk.removeLast());
				String regex = buildCapturingRegex(preOk);
				matcher = Pattern.compile("^" + regex).matcher(content);
			} while (!matcher.find());

			LinkedList<Layer> postOk = new LinkedList<Layer>(innerKo);
			innerKo.clear();
			do {
				innerKo.addLast(postOk.removeFirst());
				String regex = buildCapturingRegex(postOk);
				matcher = Pattern.compile(regex + "$").matcher(content);
			} while (!matcher.find());

			List<Layer> fakeSequence = new LinkedList<Layer>(preOk);
			Formula remaining = new Formula("[\\s\\S]*");
			fakeSequence.add(remaining);
			// fakeSequence.addAll(postOk);
			new Suite(fakeSequence).setContent(content);
			String incompatible = remaining.getContent();
			try {
				innerKo.getFirst().setContent(incompatible);
			} catch (ParsingException e) {
				int start = 0;
				for (Layer layer : preOk) {
					start += layer.getContent().length();
				}
				throw new ParsingException(this, innerKo.getFirst(), content,
						start + e.getStart(), start + e.getEnd(), e);
				// throw new ParsingException(this, innerKo.getFirst(), content,
				// start + e.getStart(), start + e.getEnd(), e);
			}
			throw new IllegalStateException(
					"No exception thrown while it should not be parsable.");
		}
	}

	private String buildCapturingRegex(List<? extends Layer> sequence) {
		String regex;
		regex = "";
		for (Layer layer : sequence) {
			regex += "(" + layer.getRegex() + ")";
		}
		return regex;
	}

	@Override
	public String toString() {
		List<String> suite = new LinkedList<String>();
		for (Layer layer : sequence) {
			suite.add(layer.getClass().getSimpleName());
		}
		return getClass().getSimpleName() + suite;
	}

	@SuppressWarnings("unchecked")
	public <CLayer extends Layer> CLayer get(int index) {
		return (CLayer) sequence.get(index);
	}
}
