package fr.vergne.parsing.layer.standard;

import java.util.Collection;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.util.Named;

/**
 * A {@link SeparatedLoop} provides, rather than a sequence of adjacent
 * {@link Item}s (e.g. AAAAA) like a classical {@link Loop}, a sequence of
 * {@link Item}s is separated by {@link Separator}s (e.g. AXAXAXAXA).
 * Consequently, the number of {@link Separator}s is always equal to the number
 * of {@link Item}s - 1.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 * @param <Item>
 * @param <Separator>
 */
// TODO Doc
// TODO Revise exceptions (inspire from Loop)
// TODO Transform standards tests into interfaces + create impl tests
public interface SeparatedLoop<Item extends Layer, Separator extends Layer> extends Layer, Iterable<Item>, Named {

	public Definition<Item> getItemDefinition();

	public Definition<Separator> getSeparatorDefinition();

	public int getMin();

	public int getMax();

	public Quantifier getQuantifier();

	/**
	 * 
	 * @return the number of {@link Item}s of this {@link SeparatedLoop}
	 */
	public int size();

	public boolean isEmpty();

	/**
	 * 
	 * @param index
	 *            the index of a parsed {@link Item}
	 * @return the {@link Item} requested
	 * @throws IndexOutOfBoundsException
	 *             if the index relate to an inexistent {@link Item}
	 */
	public Item get(int index) throws IndexOutOfBoundsException;

	public Separator getSeparator(int index);

	public <CLayer extends Layer> CLayer get(Definition<CLayer> definition, int index);

	public void setDefaultSeparator(String separator);

	public String getDefaultSeparator();

	public void add(int index, Item item);

	public Item add(int index, String content);

	public Collection<Item> addAllContents(int index, Collection<String> contents);

	public void addAll(int index, Collection<Item> items);

	public Item remove(int index);

	public void clear();

	// TODO Remove
	public String getRegex();

	@Override
	default String getName() {
		return "SEPLOOP";
	}

}
