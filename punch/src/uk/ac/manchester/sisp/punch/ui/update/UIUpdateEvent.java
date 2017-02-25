package uk.ac.manchester.sisp.punch.ui.update;

import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.ribbon.event.IEvent;

public class UIUpdateEvent implements IEvent {
	
	/* Member Variables. */
	private final float        mEventTimeSeconds;
	private final EUICommand   mUICommand;
	private       IUIElement[] mUIElements;
	
	public UIUpdateEvent(final UIUpdateEvent pUIUpdateEvent) {
		this(pUIUpdateEvent.getObjectTimeSeconds(), pUIUpdateEvent.getUICommand(), pUIUpdateEvent.getUIElements());
	}
	
	/* All UIElements scheduled for graphical context initialization must be visible, else they will be exempt from the GPU instantiation process. */
	public UIUpdateEvent(final float pEventTimeSeconds, final EUICommand pUICommand, final IUIElement ... pUIElements) {
		/* Initialize Member Variables. */
		this.mEventTimeSeconds = pEventTimeSeconds;
		this.mUICommand        = pUICommand;
		this.mUIElements       = pUIElements;
	}

	@Override
	public final float getObjectTimeSeconds() {
		return this.mEventTimeSeconds;
	}
	
	public final EUICommand getUICommand() {
		return this.mUICommand;
	}
	
	protected final void setUIElements(final IUIElement[] pUIElements) {
		this.mUIElements = pUIElements;
	}
	
	public final IUIElement[] getUIElements() {
		return this.mUIElements;
	}

	@Override public void dispose() { }

}