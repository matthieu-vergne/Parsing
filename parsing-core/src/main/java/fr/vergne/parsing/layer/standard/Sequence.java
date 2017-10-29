package fr.vergne.parsing.layer.standard;

import java.util.List;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.util.Named;

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
public interface Sequence extends Layer, Named {

	public <CLayer extends Layer> CLayer get(Definition<CLayer> definition);

	public <CLayer extends Layer> CLayer get(int index);

	public Definition<?> getDefinition(int index);

	public int size();

	public <Item extends Layer> Item set(Definition<Item> definition, Item item);

	public <Item extends Layer> String set(Definition<Item> definition, String content);

	public <Item extends Layer> Item set(int index, Item item);

	public String set(int index, String content);

	public List<Definition<? extends Layer>> getDefinitions();

	@Override
	default String getName() {
		return "SEQ";
	}
}
