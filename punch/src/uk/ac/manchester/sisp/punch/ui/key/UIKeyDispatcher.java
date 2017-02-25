package uk.ac.manchester.sisp.punch.ui.key;

import uk.ac.manchester.sisp.ribbon.event.filter.EventDispatcher;

public class UIKeyDispatcher extends EventDispatcher<UIKeyEvent, UIKeyDispatcher> {
	
	/* Member Variables. */
	
	public UIKeyDispatcher(final boolean pIsEnabled) {
		super(pIsEnabled);
		/* Initialize Member Variables. */
	}

	@Override
	protected final UIKeyEvent onGenerateImmutableEvent(final UIKeyEvent pUIKeyEvent) {
		return new UIKeyEvent(pUIKeyEvent);
	}

}
