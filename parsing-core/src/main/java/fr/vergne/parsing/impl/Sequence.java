package fr.vergne.parsing.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.vergne.parsing.Structure;
import fr.vergne.parsing.exception.IncompatibilityException;

public class Sequence implements Structure {

	private final List<? extends Structure> sequence;

	public Sequence(List<? extends Structure> structures) {
		this.sequence = Collections.unmodifiableList(structures);
	}

	@Override
	public String getRegex() {
		String regex = "";
		for (Structure structure : sequence) {
			regex += structure.getRegex();
		}
		return regex;
	}

	@Override
	public String getContent() {
		String content = "";
		for (Structure structure : sequence) {
			content += structure.getContent();
		}
		return content;
	}

	@Override
	public void setContent(String content) {
		String regex = buildGroupedRegex(sequence);
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
			LinkedList<Structure> compatibleBeginning = new LinkedList<Structure>();
			do {
				compatibleBeginning.addLast(sequence.get(compatibleBeginning
						.size()));
				regex = buildGroupedRegex(compatibleBeginning);
				matcher = Pattern.compile("^" + regex).matcher(content);
			} while (matcher.find());
			compatibleBeginning.removeLast();

			LinkedList<Structure> compatibleEnding = new LinkedList<Structure>();
			do {
				compatibleEnding.addFirst(sequence.get(sequence.size()
						- compatibleEnding.size() - 1));
				regex = buildGroupedRegex(compatibleEnding);
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
			List<Structure> fakeSequence = new LinkedList<Structure>();
			fakeSequence.addAll(compatibleBeginning);
			Atom atom = new Atom(".*");
			fakeSequence.add(atom);
			fakeSequence.addAll(compatibleEnding);
			Sequence s = new Sequence(fakeSequence);
			s.setContent(content);
			String content2 = atom.getContent();
			String prefix = new Sequence(compatibleBeginning).getContent();
			try {
				sequence.get(compatibleBeginning.size()).setContent(content2);
			} catch (IncompatibilityException e) {
				throw new IncompatibilityException(e.getRegex(), content,
						prefix.length() + e.getStart(), prefix.length()
								+ e.getEnd());
			}
		}
	}

	private String buildGroupedRegex(List<? extends Structure> compatibleEnding) {
		String regex;
		regex = "";
		for (Structure structure : compatibleEnding) {
			regex += "(" + structure.getRegex() + ")";
		}
		return regex;
	}

	@Override
	public String toString() {
		return sequence.toString();
	}
}
