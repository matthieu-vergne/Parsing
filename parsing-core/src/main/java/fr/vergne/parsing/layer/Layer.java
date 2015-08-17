package fr.vergne.parsing.layer;

import java.io.InputStream;

import javax.sound.midi.Sequence;

import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.standard.Choice;

/**
 * A {@link Layer} aims at representing the semantics of a piece of text. By
 * using its {@link #setContent(String)} method, a content is assigned to a
 * {@link Layer}. This content must fit to the syntaxic rule provided by
 * {@link #getRegex()}. In the case of a composite {@link Layer}, like
 * {@link Choice} or {@link Sequence}, the content is distributed among the
 * composing {@link Layer}s. All this architecture must be synchronized, in the
 * sense that if a {@link Layer} A contains a {@link Layer} B, changing the
 * content of B should change correspondingly the content of A. This is how
 * these elements act as different semantical "layers" over a specific content.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
public interface Layer {

	/**
	 * This method should provide the complete regular expression which
	 * represents this {@link Layer}. However, capturing parenthesis are
	 * forbidden to avoid conflict with future uses of such captures (but using
	 * "(?:...)" is allowed, as it does not conflict).
	 * 
	 * @return the undecorated regular expression representing this
	 *         {@link Layer}
	 */
	public String getRegex();

	/**
	 * This method should apply the given content to the current {@link Layer}.
	 * In particular, if this is a composite {@link Layer}, the content should
	 * be correspondingly applied to the {@link Layer}s which compose it. This
	 * way, the content corresponding to each of these elements can be retrieved
	 * through their own {@link #getContent()} method.
	 * 
	 * @param content
	 *            the content this {@link Layer} should represent
	 * @throws ParsingException
	 *             if the content does not fit the {@link Layer}'s syntax as
	 *             returned by {@link #getRegex()}
	 */
	public void setContent(String content);

	/**
	 * This method should provide the content as it was provided to the
	 * {@link #setContent(String)} method.
	 * 
	 * @return the current content of this {@link Layer}, <code>null</code> if
	 *         there is not content yet
	 */
	public String getContent();

	/**
	 * This method should provide an {@link InputStream} which retrieves the
	 * current content of this {@link Layer}. This method is intended to fasten
	 * the writing of the content, avoiding the need to rebuild and keep in
	 * memory the full content.
	 * 
	 * @return an {@link InputStream} on this {@link Layer}
	 * @throws NoContentException
	 *             when no content is available, so requesting an
	 *             {@link InputStream} makes no sense
	 */
	public InputStream getInputStream() throws NoContentException;

	/**
	 * 
	 * @param listener
	 *            the listener to add
	 */
	public void addContentListener(ContentListener listener);

	/**
	 * 
	 * @param listener
	 *            the listener to remove
	 */
	public void removeContentListener(ContentListener listener);

	/**
	 * A {@link ContentListener} allows to notify other elements when a
	 * {@link Layer} sees its contents modified. It does not necessarily reduce
	 * the to the call of {@link Layer#setContent(String)}, but can come from
	 * other methods which modifies part of the content.
	 * 
	 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
	 * 
	 */
	public static interface ContentListener {

		/**
		 * This method is called just after the modification of the content.
		 * Thus, the content is already changed when this method is called.
		 * 
		 * @param newContent
		 *            the new, current content of the {@link Layer}
		 */
		public void contentSet(String newContent);
	}

	@SuppressWarnings("serial")
	public static class NoContentException extends RuntimeException {
		public NoContentException() {
			super();
		}

		public NoContentException(String message) {
			super(message);
		}

		public NoContentException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
