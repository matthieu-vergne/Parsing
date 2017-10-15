package fr.vergne.parsing.definition.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.impl.RecursivityLimiter;
import fr.vergne.parsing.layer.standard.Quantifier;
import fr.vergne.parsing.layer.standard.impl.Choice;
import fr.vergne.parsing.layer.standard.impl.Constant;
import fr.vergne.parsing.layer.standard.impl.Loop;
import fr.vergne.parsing.layer.standard.impl.Option;
import fr.vergne.parsing.layer.standard.impl.Regex;
import fr.vergne.parsing.layer.standard.impl.SeparatedLoop;
import fr.vergne.parsing.layer.standard.impl.Sequence;

public class StandardDefinitionFactory {

	public Definition<Regex> defineRegex(String regex) {
		return new Definition<Regex>() {

			@Override
			public String getRegex() {
				return regex;
			}

			@Override
			public Regex create() {
				return new Regex(regex);
			}

			@Override
			public boolean isCompatibleWith(Regex layer) {
				return layer.getRegex().equals(regex);
			}
		};
	}

	public Definition<Constant> defineConstant(String content) {
		return new Definition<Constant>() {

			@Override
			public String getRegex() {
				return create().getRegex();
			}

			@Override
			public Constant create() {
				return new Constant(content);
			}

			@Override
			public boolean isCompatibleWith(Constant layer) {
				return layer.getRegex().equals(create().getRegex());
			}
		};
	}

	public <T extends Layer> Definition<Option<T>> defineOptional(Definition<T> definition) {
		return defineOptional(definition, Quantifier.GREEDY);
	}

	public <T extends Layer> Definition<Option<T>> defineOptional(Definition<T> definition, Quantifier quantifier) {
		return new Definition<Option<T>>() {

			@Override
			public String getRegex() {
				return create().getRegex();
			}

			@Override
			public Option<T> create() {
				return new Option<T>(definition, quantifier);
			}

			@Override
			public boolean isCompatibleWith(Option<T> layer) {
				return layer.getRegex().equals(create().getRegex());
			}
		};
	}

	@SafeVarargs
	public final Definition<Sequence> defineSequence(Definition<? extends Layer>... items) {
		return defineSequence(Arrays.asList(items));
	}

	public Definition<Sequence> defineSequence(List<Definition<? extends Layer>> items) {
		return new Definition<Sequence>() {

			RecursivityLimiter<Definition<Sequence>, String> regexComputer = new RecursivityLimiter<>((def) -> {
				String regex = "";
				for (Definition<?> item : items) {
					regex += "(?:" + item.getRegex() + ")";
				}
				return regex;
			}, (def) -> "[\\s\\S]*");

			@Override
			public String getRegex() {
				return regexComputer.callOn(this);
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

	public <Item extends Layer> Definition<Loop<Item>> defineLoop(Definition<Item> item) {
		return defineLoop(item, 0, Integer.MAX_VALUE, Quantifier.GREEDY);
	}

	public <Item extends Layer> Definition<Loop<Item>> defineLoop(Definition<Item> itemDefinition, int min, int max,
			Quantifier quantifier) {
		return new Definition<Loop<Item>>() {

			@Override
			public String getRegex() {
				return create().getRegex();
			}

			@Override
			public Loop<Item> create() {
				return new Loop<>(itemDefinition, min, max, quantifier);
			}

			@Override
			public boolean isCompatibleWith(Loop<Item> layer) {
				return layer.getItemDefinition().equals(itemDefinition) && layer.getMin() == min
						&& layer.getMax() == max && layer.getQuantifier().equals(quantifier);
			}
		};
	}

	public <Item extends Layer, Separator extends Layer> Definition<SeparatedLoop<Item, Separator>> defineSeparatedLoop(
			Definition<Item> item, Definition<Separator> separator) {
		return defineSeparatedLoop(item, separator, 0, Integer.MAX_VALUE, Quantifier.GREEDY);
	}

	public <Item extends Layer, Separator extends Layer> Definition<SeparatedLoop<Item, Separator>> defineSeparatedLoop(
			Definition<Item> item, Definition<Separator> separator, Quantifier quantifier) {
		return defineSeparatedLoop(item, separator, 0, Integer.MAX_VALUE, quantifier);
	}

	public <Item extends Layer, Separator extends Layer> Definition<SeparatedLoop<Item, Separator>> defineSeparatedLoop(
			Definition<Item> item, Definition<Separator> separator, String defaultSeparator, Quantifier quantifier) {
		return defineSeparatedLoop(item, separator, defaultSeparator, 0, Integer.MAX_VALUE, quantifier);
	}

	public <Item extends Layer, Separator extends Layer> Definition<SeparatedLoop<Item, Separator>> defineSeparatedLoop(
			Definition<Item> item, Definition<Separator> separator, int min, int max, Quantifier quantifier) {
		return new Definition<SeparatedLoop<Item, Separator>>() {

			@Override
			public String getRegex() {
				return create().getRegex();
			}

			@Override
			public SeparatedLoop<Item, Separator> create() {
				return new SeparatedLoop<>(item, separator, min, max, quantifier);
			}

			@Override
			public boolean isCompatibleWith(SeparatedLoop<Item, Separator> layer) {
				return layer.getRegex().equals(create().getRegex());
			}
		};
	}

	public <Item extends Layer, Separator extends Layer> Definition<SeparatedLoop<Item, Separator>> defineSeparatedLoop(
			Definition<Item> item, Definition<Separator> separator, String defaultSeparator, int min, int max,
			Quantifier quantifier) {
		return new Definition<SeparatedLoop<Item, Separator>>() {

			@Override
			public String getRegex() {
				return create().getRegex();
			}

			@Override
			public SeparatedLoop<Item, Separator> create() {
				SeparatedLoop<Item, Separator> loop = new SeparatedLoop<>(item, separator, min, max, quantifier);
				loop.setDefaultSeparator(defaultSeparator);
				return loop;
			}

			@Override
			public boolean isCompatibleWith(SeparatedLoop<Item, Separator> layer) {
				return layer.getRegex().equals(create().getRegex());
			}
		};
	}

	public <Item extends Layer> Definition<SeparatedLoop<Item, Constant>> defineSeparatedLoop(Definition<Item> item,
			String separator) {
		return defineSeparatedLoop(item, defineConstant(separator));
	}

	public <Item extends Layer> Definition<SeparatedLoop<Item, Constant>> defineSeparatedLoop(Definition<Item> item,
			String separator, Quantifier quantifier) {
		return defineSeparatedLoop(item, defineConstant(separator), quantifier);
	}

	public <Item extends Layer> Definition<SeparatedLoop<Item, Constant>> defineSeparatedLoop(Definition<Item> item,
			String separator, int min, int max, Quantifier quantifier) {
		return defineSeparatedLoop(item, defineConstant(separator), min, max, quantifier);
	}

	public Definition<Choice> defineChoice(Collection<Definition<? extends Layer>> definitions) {
		return new Definition<Choice>() {

			RecursivityLimiter<Definition<Choice>, String> regexComputer = new RecursivityLimiter<>((def) -> {
				String regex = "";
				for (Definition<? extends Layer> definition : definitions) {
					regex += "|(?:" + definition.getRegex() + ")";
				}
				return "(?:" + regex.substring(1) + ")";
			}, (def) -> "[\\s\\S]*");

			@Override
			public String getRegex() {
				return regexComputer.callOn(this);
			}

			@Override
			public Choice create() {
				return new Choice(definitions);
			}

			@Override
			public boolean isCompatibleWith(Choice layer) {
				return definitions.containsAll(layer.getDefinitions())
						&& layer.getDefinitions().containsAll(definitions);
			}
		};
	}

	@SafeVarargs
	public final Definition<Choice> defineChoice(Definition<? extends Layer>... definitions) {
		return defineChoice(Arrays.asList(definitions));
	}

	/**
	 * Some {@link Definition}s may be defined exactly the same way, despite
	 * representing different concepts. In order to build well named
	 * {@link Definition}s, one can create the common {@link Definition} and then
	 * use this method to create aliases. They are not equivalent, in the sense that
	 * one cannot use an alias instead of another, or an alias instead of the common
	 * {@link Definition}, or the common {@link Definition} instead of an alias.
	 * They are different {@link Definition} instances, but which will generate
	 * {@link Layer}s accepting the same content.
	 * 
	 * @param definition
	 *            the {@link Definition} to mimic
	 * @return a new {@link Definition} equivalent to the one provided
	 */
	public <T extends Layer> Definition<T> defineAs(Definition<T> definition) {
		return new Definition<T>() {

			@Override
			public String getRegex() {
				return definition.getRegex();
			}

			@Override
			public T create() {
				return definition.create();
			}

			@Override
			public boolean isCompatibleWith(T layer) {
				return definition.isCompatibleWith(layer);
			}
		};
	}

	/**
	 * Sometimes, one want to use a {@link Definition} before to know exactly what
	 * it will be. This is particularly true for recursive cases, where one needs to
	 * define some basic elements first, but already need the final one to define
	 * them. This method provides such a facility through a {@link DefinitionProxy},
	 * which allows to obtain a {@link Definition} immediately while telling later
	 * how to actually build it.
	 * 
	 * @return a {@link DefinitionProxy} for a given {@link Definition}
	 */
	public <T extends Layer> DefinitionProxy<T> prepareDefinition() {
		return new DefinitionProxy<T>();
	}

	/**
	 * A {@link DefinitionProxy} allows to obtain a {@link Definition} for which we
	 * want to delay the actual definition. When having a {@link DefinitionProxy},
	 * one can call {@link #getDefinition()} to obtain the {@link Definition}
	 * instance, which we will call <em>final def</em>. The same instance is
	 * returned by each call, so it can be called immediately and used as any
	 * {@link Definition}. Once a {@link Definition} is built and we want <em>final
	 * def</em> to represent it, it should be given to
	 * {@link #defineAs(Definition)}. Only then <em>final def</em> can create
	 * instances through {@link Definition#create()}.
	 * 
	 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
	 *
	 * @param <T>
	 */
	public class DefinitionProxy<T extends Layer> {
		private Definition<T> sourceDefinition;
		private final Definition<T> resultDefinition = new Definition<T>() {
			@Override
			public String getRegex() {
				return sourceDefinition.getRegex();
			}

			@Override
			public T create() {
				return sourceDefinition.create();
			}

			@Override
			public boolean isCompatibleWith(T layer) {
				return sourceDefinition.isCompatibleWith(layer);
			}
		};

		public void defineAs(Definition<T> definition) {
			this.sourceDefinition = definition;
		}

		public Definition<T> getDefinition() {
			return resultDefinition;
		}
	}
}
