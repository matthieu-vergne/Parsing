package fr.vergne.parsing.layer.standard;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.util.Named;

/**
 * An {@link Option} make a {@link Layer} optional. Thus, a compatible content
 * is one which fits the optional {@link Layer} or an empty one.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 * @param <CLayer>
 */
public interface Option<CLayer extends Layer> extends Layer, Named {

	/**
	 * 
	 * @return the {@link Definition} of the {@link Layer} used in this
	 *         {@link Option}
	 */
	public Definition<CLayer> getOptionalDefinition();

	public Quantifier getQuantifier();

	/**
	 * 
	 * @return <code>true</code> if the current content fits the option,
	 *         <code>false</code> otherwise (empty content)
	 */
	public boolean isPresent();

	/**
	 * 
	 * @return the {@link Layer} wrapped by this {@link Option}
	 * @throws RuntimeException
	 *             if the option is not present
	 */
	public CLayer getOption();

	public void setOption(CLayer layer);

	@Override
	default String getName() {
		return "OPT";
	}
}
