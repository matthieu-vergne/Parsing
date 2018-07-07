package fr.vergne.parsing.layer.standard;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.impl.AbstractLayer;
import fr.vergne.parsing.util.Named;
import fr.vergne.parsing.util.RegexRecursivityLimiter;

/**
 * A {@link Sequence} is a {@link Layer} representing an ordered sequence of
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
public class Sequence extends AbstractLayer implements Named {

	private final List<Definition<? extends Layer>> definitions;
	private final List<Layer> layers;
	private ContentListener deepListener = new ContentListener() {

		@Override
		public void contentSet(String newContent) {
			try {
				fireContentUpdate();
			} catch (NoContentException e) {
				// sequence not complete, nothing to notify
			}
		}
	};

	public Sequence(List<Definition<? extends Layer>> definitions) {
		if (definitions == null || definitions.isEmpty()) {
			throw new IllegalArgumentException("No definition provided: " + definitions);
		} else {
			this.definitions = Collections.unmodifiableList(new ArrayList<>(definitions));
			this.layers = new ArrayList<>(definitions.size());
		}
	}

	@SafeVarargs
	public Sequence(Definition<? extends Layer>... definitions) {
		this(Arrays.asList(definitions));
	}

	@Override
	public InputStream getInputStream() {
		/*
		 * We first store the InputStreams to ensure that all of them are available.
		 * This allows to throw NoContentException immediately if the sequence is not
		 * complete.
		 */
		final List<InputStream> streams = new LinkedList<InputStream>();
		for (Layer sublayer : layers) {
			streams.add(sublayer.getInputStream());
		}
		return new InputStream() {
			private InputStream reader = new InputStream() {

				@Override
				public int read() throws IOException {
					return -1;
				}
			};
			private final Iterator<InputStream> iterator = streams.iterator();

			@Override
			public int read() throws IOException {
				int character = reader.read();
				while (character == -1 && iterator.hasNext()) {
					reader.close();
					reader = iterator.next();
					character = reader.read();
				}
				return character;
			}

			@Override
			public void close() throws IOException {
				reader.close();
				super.close();
			}
		};
	}

	@Override
	protected void setInternalContent(String content) {
		Matcher matcher = Pattern.compile("^" + buildCapturingRegex(definitions) + "$").matcher(content);
		if (matcher.find()) {
			int deltaCapture = matcher.groupCount() - definitions.size();
			if (deltaCapture > 0) {
				throw new RuntimeException(
						deltaCapture + " more captures, check the definitions of the items do not add captures.");
			} else {
				int delta = 0;
				for (int matchIndex = 1; matchIndex <= matcher.groupCount(); matchIndex++) {
					int layerIndex = matchIndex - 1;
					String match = matcher.group(matchIndex);
					int subStart = delta;
					int subEnd = subStart + match.length();
					Layer item;
					if (layers.size() == layerIndex) {
						item = definitions.get(layerIndex).create();
						layers.add(item);
					} else {
						item = layers.get(layerIndex);
						item.removeContentListener(deepListener);
					}
					try {
						item.setContent(match);
					} catch (ParsingException e) {
						throw new ParsingException(this, definitions.get(layerIndex), content, subStart + e.getStart(),
								subEnd, e);
					} finally {
						item.addContentListener(deepListener);
					}
					delta += match.length();
				}
			}
		} else {
			LinkedList<Definition<?>> preOk = new LinkedList<>(definitions);
			LinkedList<Definition<?>> innerKo = new LinkedList<>();
			do {
				innerKo.addFirst(preOk.removeLast());
				String regex = buildCapturingRegex(preOk);
				matcher = Pattern.compile("^" + regex).matcher(content);
			} while (!matcher.find());

			Definition<Regex> remaining = Regex.define("[\\s\\S]*");
			preOk.add(remaining);
			Sequence sequence = new Sequence(preOk);
			sequence.setContent(content);
			String incompatible = sequence.get(remaining).getContent();
			try {
				innerKo.getFirst().create().setContent(incompatible);
			} catch (ParsingException e) {
				preOk.remove(remaining);
				int start = 0;
				for (Definition<? extends Layer> def : preOk) {
					start += sequence.get(def).getContent().length();
				}
				throw new ParsingException(this, definitions.get(preOk.size()), content, start + e.getStart(),
						start + e.getEnd(), e);
			}
			throw new IllegalStateException("No exception thrown while it should not be parsable.");
		}
	}

	private String buildCapturingRegex(List<Definition<?>> sequence) {
		StringBuilder regex = new StringBuilder();
		for (Definition<?> definition : sequence) {
			regex.append("(" + definition.getRegex() + ")");
		}
		return regex.toString();
	}

	@SuppressWarnings("unchecked")
	public <CLayer extends Layer> CLayer get(Definition<CLayer> definition) {
		if (!definitions.contains(definition)) {
			throw new IllegalArgumentException("Unknown definition: " + definition);
		} else {
			if (layers.isEmpty()) {
				throw new NoContentException();
			} else {
				return (CLayer) layers.get(definitions.indexOf(definition));
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <CLayer extends Layer> CLayer get(int index) {
		return (CLayer) layers.get(index);
	}

	public Definition<?> getDefinition(int index) {
		return definitions.get(index);
	}

	public int size() {
		return definitions.size();
	}

	public <Item extends Layer> Item set(Definition<Item> definition, Item item) {
		if (!definitions.contains(definition)) {
			throw new IllegalArgumentException("Unknown definition: " + definition);
		} else if (definitions.indexOf(definition) != definitions.lastIndexOf(definition)) {
			throw new IllegalArgumentException("Ambiguous definition: " + definition);
		} else if (item == null) {
			throw new IllegalArgumentException("No item provided: " + item);
		} else if (!definition.isCompatibleWith(item)) {
			throw new IllegalArgumentException("Invalid item provided: " + item);
		} else if (item.getContent().isEmpty()) {
			throw new IllegalArgumentException("Empty item provided: " + item);
		} else {
			@SuppressWarnings("unchecked")
			Item oldItem = (Item) layers.set(definitions.indexOf(definition), item);
			oldItem.removeContentListener(deepListener);
			item.addContentListener(deepListener);
			fireContentUpdate();
			return oldItem;
		}
	}

	public <Item extends Layer> String set(Definition<Item> definition, String content) {
		if (definition == null) {
			throw new IllegalArgumentException("No definition provided.");
		} else {
			Item item = definition.create();
			try {
				item.setContent(content);
			} catch (NullPointerException cause) {
				throw new IllegalArgumentException(cause);
			}
			return set(definition, item).getContent();
		}
	}

	@SuppressWarnings("unchecked")
	public <Item extends Layer> Item set(int index, Item item) {
		return set((Definition<Item>) definitions.get(index), item);
	}

	public String set(int index, String content) {
		return set(definitions.get(index), content);
	}

	public List<Definition<? extends Layer>> getDefinitions() {
		return Collections.unmodifiableList(definitions);
	}

	@Override
	public String getName() {
		return "SEQ";
	}

	@Override
	public String toString() {
		List<String> suite = new LinkedList<String>();
		for (Definition<? extends Layer> definition : definitions) {
			suite.add(Named.name(definition.create()));
		}
		return getName() + suite;
	}

	public static Definition<Sequence> define(List<Definition<? extends Layer>> items) {
		return new Definition<Sequence>() {

			private final RegexRecursivityLimiter regexComputer = new RegexRecursivityLimiter(() -> {
				StringBuilder regex = new StringBuilder();
				for (Definition<?> item : items) {
					regex.append("(?:" + item.getRegex() + ")");
				}
				return regex.toString();
			});

			@Override
			public String getRegex() {
				return regexComputer.generate();
			}

			@Override
			public Sequence create() {
				return new Sequence(items);
			}

			@Override
			public boolean isCompatibleWith(Sequence sequence) {
				List<Definition<? extends Layer>> otherItems = sequence.getDefinitions();
				return otherItems.containsAll(items) && items.containsAll(otherItems);
			}
		};
	}

	@SafeVarargs
	public static final Definition<Sequence> define(Definition<? extends Layer>... items) {
		return define(Arrays.asList(items));
	}
}
