package fr.vergne.parsing.layer.standard;

import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.exception.ParsingException;

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
	private GreedyMode mode = GreedyMode.GREEDY;

	public Option(CLayer layer, GreedyMode mode) {
		this(layer);
		setMode(mode);
	}

	public Option(CLayer layer) {
		this.option = layer;
		this.option.addContentListener(new ContentListener() {
			
			@Override
			public void contentSet(String newContent) {
				if (isPresent) {
					fireContentUpdate(getContent());
				} else {
					// don't care
				}
			}
		});
	}

	public void setMode(GreedyMode mode) {
		this.mode = mode;
	}

	public GreedyMode getMode() {
		return mode;
	}

	@Override
	protected String buildRegex() {
		return "(?:" + option.getRegex() + ")?" + mode.getDecorator();
	}

	@Override
	public String getContent() {
		return isPresent() ? option.getContent() : "";
	}

	@Override
	public InputStream getInputStream() {
		return IOUtils.toInputStream(getContent(), Charset.forName("UTF-8"));
	}

	@Override
	protected void setInternalContent(String content) {
		if (content.isEmpty()) {
			isPresent = false;
		} else {
			try {
				option.setContent(content);
				isPresent = true;
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
	
	@SuppressWarnings("unchecked")
	@Override
	public Object clone() {
		CLayer clone;
		try {
			clone = (CLayer) option.getClass().getMethod("clone")
					.invoke(option);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Option<CLayer> option = new Option<CLayer>(clone, mode);
		String content = getContent();
		if (content != null) {
			option.setContent(content);
		} else {
			// keep it not filled
		}
		return option;
	}
}
