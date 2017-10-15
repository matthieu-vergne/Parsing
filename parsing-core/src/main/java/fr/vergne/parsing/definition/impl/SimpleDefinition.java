package fr.vergne.parsing.definition.impl;

import java.util.function.Function;
import java.util.function.Supplier;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.Layer;

public class SimpleDefinition<T extends Layer> implements Definition<T> {
	
	private final Supplier<T> instantiator;
	private final Function<T, String> regexProvider;

	public SimpleDefinition(Supplier<T> instantiator, Function<T, String> regexProvider) {
		this.instantiator = instantiator;
		this.regexProvider = regexProvider;
	}
	
	@Override
	public String getRegex() {
		return regexProvider.apply(create());
	}

	@Override
	public T create() {
		return instantiator.get();
	}

	@Override
	public boolean isCompatibleWith(T layer) {
		return getRegex().equals(regexProvider.apply(layer));
	}

}
