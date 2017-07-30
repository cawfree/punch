package uk.ac.manchester.sisp.punch.ui.update;

import java.util.ArrayList;
import java.util.List;

import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.ribbon.event.filter.EventDispatcher;

public final class UIUpdateDispatcher extends EventDispatcher<UIUpdateEvent, UIUpdateDispatcher> {
	
	public UIUpdateDispatcher(final boolean pIsEnabled) {
		super(pIsEnabled);
	}
	
	@Override
	protected final boolean onProcessEvent(final UIUpdateEvent pUIUpdateEvent) {
		/* Allocate a List to hold only the visible UIElements. */
		final List<IUIElement> lUIElements = new ArrayList<IUIElement>(0);
		/* Iterate through the UIElements. */
		for(final IUIElement lUIElement : pUIUpdateEvent.getUIElements()) {
			/* Ensure the UIElement is visible. */
			//if(lUIElement.isVisible()) {
				/* Add the current UIElement to the UIElements. */
				lUIElements.add(lUIElement);
			//}
		}
		/* Finally, convert the visible UIElements into an equivalent array and overwrite the reference in the UIUpdateEvent. */
		pUIUpdateEvent.setUIElements(lUIElements.toArray(new IUIElement[lUIElements.size()]));
		/* Unconditionally launch the event. */
		return true;
	}
	

	@Override protected final UIUpdateEvent onGenerateImmutableEvent(final UIUpdateEvent pUIUpdateEvent) {
		return new UIUpdateEvent(pUIUpdateEvent);
	}

}