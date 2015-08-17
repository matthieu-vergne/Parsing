package fr.vergne.parsing.layer.standard;

import java.io.InputStream;

import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.standard.Loop.Generator;
import fr.vergne.parsing.layer.util.ContentInputStream;

/**
 * An {@link Option} make a {@link Layer} optional. Thus, a compatible content
 * is one which fits the optional {@link Layer} or an empty one.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 * @param <CLayer>
 */
public class Option<CLayer extends Layer> extends AbstractLayer implements
		Layer {

	private final CLayer option;
	private boolean isPresent = false;
	private final Quantifier quantifier;

	public Option(CLayer layer, Quantifier quantifier) {
		this.option = layer;
		setContent("");
		this.option.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				if (isPresent) {
					fireContentUpdate(newContent);
				} else {
					// don't care
				}
			}
		});
		this.quantifier = quantifier;
	}

	public Option(CLayer layer) {
		this(layer, Quantifier.GREEDY);
	}

	public Quantifier getQuantifier() {
		return quantifier;
	}

	@Override
	protected String buildRegex() {
		return "(?:" + option.getRegex() + ")?" + quantifier.getDecorator();
	}

	@Override
	public InputStream getInputStream() {
		if (isPresent()) {
			return option.getInputStream();
		} else {
			return new ContentInputStream("");
		}
	}

	@Override
	protected void setInternalContent(String content) {
		if (content.isEmpty()) {
			isPresent = false;
		} else {
			try {
				isPresent = true;
				option.setContent(content);
			} catch (ParsingException e) {
				throw new ParsingException(this, option, content, 0,
						content.length(), e);
			}
		}
	}

	/**
	 * 
	 * @return <code>true</code> if the current content fits the option,
	 *         <code>false</code> otherwise (empty content)
	 */
	public boolean isPresent() {
		return isPresent;
	}

	/**
	 * This method is an indirect way to set the content of this {@link Option}
	 * by switching between an empty content (<code>false</code>) and a
	 * non-empty one (<code>true</code>). Notice that it is always possible to
	 * switch to an empty content, but you can switch to a non-empty one
	 * <u>only</u> if the {@link Layer} returned by {@link #getOption()} has a
	 * content (not <code>null</code>). If it is not the case, calling this
	 * method to activate the option will throw an exception.
	 * 
	 * @param isPresent
	 *            <code>true</code> to activate the option returned by
	 *            {@link #getOption()}, <code>false</code> to deactivate it
	 */
	public void setPresent(boolean isPresent) {
		if (isPresent && getOption().getContent() == null) {
			throw new RuntimeException(
					"Impossible to activate the option: it has no content");
		} else if (this.isPresent != isPresent) {
			this.isPresent = isPresent;
			fireContentUpdate();
		} else {
			// no difference, don't change anything
		}
	}

	/**
	 * 
	 * @return the option wrapped by this {@link Option}, whether it is used or
	 *         not
	 */
	public CLayer getOption() {
		return option;
	}

	@Override
	public String toString() {
		return option.toString() + "(opt)";
	}

	@Override
	public Object clone() {
		Generator<CLayer> generator = Loop.createGeneratorFromTemplate(option);
		CLayer clone = generator.generates();
		Option<CLayer> option = new Option<CLayer>(clone, quantifier);
		String content = getContent();
		if (content != null) {
			option.setContent(content);
		} else {
			// keep it not filled
		}
		return option;
	}
}
