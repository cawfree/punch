package uk.ac.manchester.sisp.punch.ui.key;

import uk.ac.manchester.sisp.ribbon.event.IEvent;
import uk.ac.manchester.sisp.ribbon.utils.DataUtils;

public final class UIKeyEvent implements IEvent {
	
	/** TODO: Move all of these operations to a Locale type implementation... **/
	/* Punctuation Definitions. */
	private static final String CHARSET_PUNCTUATION    = "-=!\"£$%^&*()_+[]{};':@#~,./<>? \u00BB©\\\r";/* Charset Definitions. */
	public  static final String CHARSET_SIMPLE_ENGLISH = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890" + UIKeyEvent.CHARSET_PUNCTUATION;
	
	
	/* Static KeyCode Definitions. */
	public static final char KEY_CODE_SPACE     = '\u0020';
	public static final char KEY_CODE_BACKSPACE = 8;
	public static final char KEY_CODE_RETURN    = 13;
	public static final char KEY_CODE_DELETE    = 147;
	
	public static final boolean isSpace(final short pKeyCode) {
		return pKeyCode == UIKeyEvent.KEY_CODE_SPACE;
	}
	
	public static final boolean isBackSpace(final short pKeyCode) {
		return pKeyCode == UIKeyEvent.KEY_CODE_BACKSPACE;
	}
	
	public static final boolean isReturn(final short pKeyCode) {
		return pKeyCode == UIKeyEvent.KEY_CODE_RETURN;
	}
	
	public static final boolean isPunctuation(final char pChar) {
		return UIKeyEvent.CHARSET_PUNCTUATION.indexOf(pChar) != DataUtils.JAVA_NULL_INDEX;
	}
//	
	public static final boolean isAlphabet(final char pChar) {
		return UIKeyEvent.CHARSET_SIMPLE_ENGLISH.indexOf(pChar) != DataUtils.JAVA_NULL_INDEX;
	}
	
	/* Static Flag Declarations. */
	public static final int FLAG_KEY_SHIFT = 1 << 0;
	public static final int FLAG_KEY_CTRL  = 1 << 1;
	public static final int FLAG_KEY_ALT   = 1 << 2;
	
	/* Member Variables. */
	private final float mEventTimeSeconds;
	private final char  mKeyChar;
	private final short mKeyCode;
	private final int   mFlags;
	
	protected UIKeyEvent(final UIKeyEvent pUIKeyEvent) {
		this(pUIKeyEvent.getObjectTimeSeconds(), pUIKeyEvent.getKeyChar(), pUIKeyEvent.getKeyCode(), pUIKeyEvent.getFlags());
	}
	
	public UIKeyEvent(final float pEventTimeSeconds, final char pKeyChar, final short pKeyCode, final int pFlags) {
		/* Initialize Member Variables. */
		this.mEventTimeSeconds = pEventTimeSeconds;
		this.mKeyChar          = pKeyChar;
		this.mKeyCode          = pKeyCode;
		this.mFlags            = pFlags;
	}

	@Override public final float getObjectTimeSeconds() {
		return this.mEventTimeSeconds;
	}

	@Override public void dispose() { }
	
	public final char getKeyChar() {
		return this.mKeyChar;
	}
	
	public final short getKeyCode() {
		return this.mKeyCode;
	}
	
	public final boolean isShiftDown() {
		return DataUtils.isFlagSet(this.getFlags(), UIKeyEvent.FLAG_KEY_SHIFT);
	}
	
	public final boolean isControlDown() {
		return DataUtils.isFlagSet(this.getFlags(), UIKeyEvent.FLAG_KEY_CTRL);
	}
	
	public final boolean isAltDown() {
		return DataUtils.isFlagSet(this.getFlags(), UIKeyEvent.FLAG_KEY_ALT);
	}
	
	private final int getFlags() {
		return this.mFlags;
	}

}
