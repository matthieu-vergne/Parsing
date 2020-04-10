package fr.vergne.parsing.definition;

import java.util.function.Function;

import fr.vergne.parsing.layer.Layer;

/**
 * A {@link Definition} defines a {@link Layer} in order to be able to
 * instantiate it through {@link #create()}.
 * 
 * @param <T>
 */
// TODO Import quantifier here? Should it go together with regex?
public interface Definition<T extends Layer> {
	/**
	 * This method provides the complete regular expression which represents the
	 * {@link Layer}s we instantiate. If capturing parenthesis are used, they are
	 * not capturing ("(?:...)") to not interfere with parsing processes.
	 * 
	 * @return the regular expression describing the {@link Layer}s instantiated by
	 *         this {@link Definition}
	 */
	public String getRegex();

	/**
	 * Each call to this method instantiates a {@link Layer} corresponding to this
	 * {@link Definition}.
	 * 
	 * @return a new {@link Layer} instance
	 */
	public T create();

	/**
	 * Create an alias to an existing {@link Definition}. The source
	 * {@link Definition} and the alias are not the same (different instances), but
	 * they build on the same rules (can parse the same content). If you need it to
	 * have also a different {@link Class}, you must use
	 * {@link #like(Definition, Function)} instead.
	 * 
	 * @param source
	 *            the {@link Definition} to mimic
	 * @return a new {@link Definition} equivalent to the one provided
	 */
	// TODO test
	public static <T extends Layer> Definition<T> like(Definition<T> source) {
		return like(source, Function.identity());
	}

	/**
	 * Create an alias to an existing {@link Definition}. The source
	 * {@link Definition} and the alias are not the same (different instances), but
	 * they build on the same rules (can parse the same content). If they have the
	 * same {@link Class}, you can use {@link #like(Definition)} instead.
	 * 
	 * @param source
	 *            the {@link Definition} to use as reference
	 * @param translater
	 *            the {@link Function} which creates a target {@link Layer} based on
	 *            a source {@link Layer}
	 * @return the target {@link Definition}
	 */
	// TODO test
	public static <L1 extends Layer, L2 extends Layer> Definition<L2> like(Definition<L1> source,
			Function<L1, L2> translater) {
		return new Definition<L2>() {

			@Override
			public String getRegex() {
				return source.getRegex();
			}

			@Override
			public L2 create() {
				return translater.apply(source.create());
			}
		};
	}

	/**
	 * A {@link DefinitionProxy} allows to obtain a {@link Definition} for which we
	 * want to delay the specification. When having a {@link DefinitionProxy}, one
	 * can use it as any {@link Definition} instance. Once the specification is
	 * known, it should be provided to {@link #setDelegate(Definition)}. Only then
	 * the {@link DefinitionProxy} can create instances through
	 * {@link DefinitionProxy#create()}.
	 * 
	 * @param <L>
	 *            The type of {@link Layer} to delegate to
	 */
	public static interface DefinitionProxy<L extends Layer> extends Definition<L> {
		public void setDelegate(Definition<L> definition);
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
	// TODO test
	public static <T extends Layer> DefinitionProxy<T> prepare() {
		return new DefinitionProxy<T>() {

			private Definition<T> definition;

			@Override
			public String getRegex() {
				return definition.getRegex();
			}

			@Override
			public T create() {
				return definition.create();
			}

			@Override
			public void setDelegate(Definition<T> definition) {
				this.definition = definition;
			}
		};
	}

	/**
	 * Create a new {@link Definition} where any new {@link Layer} instance is
	 * created with the given default content.
	 * 
	 * @param content
	 *            the default content to use
	 * @param definition
	 *            the {@link Definition} to enrich with a default content
	 * @return the new, default-enriched {@link Definition}
	 */
	// TODO test
	public static <L extends Layer> Definition<L> withDefault(String content, Definition<L> definition) {
		return new Definition<L>() {

			@Override
			public String getRegex() {
				return definition.getRegex();
			}

			@Override
			public L create() {
				L layer = definition.create();
				layer.setContent(content);
				return layer;
			}
		};
	}
}
