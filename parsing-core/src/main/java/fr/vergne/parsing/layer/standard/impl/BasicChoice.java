package fr.vergne.parsing.layer.standard.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.impl.AbstractLayer;
import fr.vergne.parsing.layer.standard.Choice;
import fr.vergne.parsing.util.Named;

// TODO Replace Basic by more explicit term
// TODO Doc
public class BasicChoice extends AbstractLayer implements Choice {

	private final List<Definition<? extends Layer>> definitions;
	private Layer currentLayer = null;
	private Integer currentDefinitionIndex = null;
	private Integer referenceDefinitionIndex = null;
	private final ContentListener deepListener = new ContentListener() {

		@Override
		public void contentSet(String newContent) {
			fireContentUpdate(newContent);
		}
	};

	public BasicChoice(Collection<Definition<? extends Layer>> definitions) {
		this.definitions = Collections.unmodifiableList(new ArrayList<>(definitions));
	}

	@SafeVarargs
	public BasicChoice(Definition<? extends Layer>... definitions) {
		this(Arrays.asList(definitions));
	}

	@Override
	public InputStream getInputStream() {
		return currentLayer.getInputStream();
	}

	@Override
	protected void setInternalContent(String content) {
		if (currentLayer != null) {
			currentLayer.removeContentListener(deepListener);
		} else {
			// No listener to remove
		}

		Map<Definition<?>, ParsingException> exceptions = new HashMap<>();
		for (Definition<? extends Layer> definition : definitions) {
			try {
				Layer layer = definition.create();
				layer.setContent(content);
				currentLayer = layer;
				currentLayer.addContentListener(deepListener);
				currentDefinitionIndex = definitions.indexOf(definition);
				return;
			} catch (ParsingException e) {
				exceptions.put(definition, e);
			}
		}
		if (referenceDefinitionIndex != null) {
			Definition<?> reference = definitions.get(referenceDefinitionIndex);
			ParsingException cause = exceptions.get(reference);
			throw new ParsingException(this, reference, content, cause.getStart(), content.length(), cause);
		} else {
			throw new ParsingException(new StandardDefinitionFactory().defineChoice(definitions).getRegex(), content);
		}
	}

	@Override
	public int size() {
		return definitions.size();
	}

	@Override
	public Definition<?> getDefinition(int index) {
		return definitions.get(index);
	}

	@Override
	public Collection<Definition<?>> getDefinitions() {
		return Collections.unmodifiableCollection(definitions);
	}

	@Override
	public Definition<?> getCurrentDefinition() {
		return definitions.get(currentDefinitionIndex);
	}

	@Override
	public boolean is(Definition<?> definition) {
		return getCurrentDefinition() == definition;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <Item extends Layer> Item getAs(Definition<Item> definition) {
		Definition<? extends Layer> currentDefinition = definitions.get(currentDefinitionIndex);
		if (currentDefinition != definition) {
			throw new InvalidChoiceException(currentDefinition, definition);
		} else {
			return (Item) currentLayer;
		}
	}

	@Override
	public Layer get() {
		return currentLayer;
	}

	@Override
	public void setReferenceDefinition(Definition<?> alternative) {
		int index = definitions.indexOf(alternative);
		if (index >= 0) {
			referenceDefinitionIndex = index;
		} else {
			throw new InvalidChoiceException(alternative);
		}
	}

	@Override
	public Definition<?> getReferenceDefinition() {
		if (referenceDefinitionIndex == null) {
			throw new NoSuchElementException("No reference has been defined.");
		} else {
			return definitions.get(referenceDefinitionIndex);
		}
	}

	@Override
	public String toString() {
		List<String> choices = new LinkedList<String>();
		for (Definition<?> definition : definitions) {
			choices.add(Named.name(definition.create()));
		}
		return getName() + choices;
	}
}
