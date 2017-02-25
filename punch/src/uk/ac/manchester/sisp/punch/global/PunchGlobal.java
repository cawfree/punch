package uk.ac.manchester.sisp.punch.global;

import java.util.List;

import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.core.IGroup;

public final class PunchGlobal {
	
	/* Application Constants. */
	public static final String APPLICATION_TITLE       = "Punch";
	public static final String APPLICATION_DESCRIPTION = "An editor for the Punch Graphical Programming Language";
	
	/* Recurses through an entire Group structure and compiles a list of every UIElement residing within. */
	@SuppressWarnings("unchecked") public static final <T extends IUIElement> void onAccumulateRecursive(final List<IUIElement> pUIElements, final IUIElement pUIElement) {
		/* Add the UIElement. */
		pUIElements.add(pUIElement);
		/* Determine if the UIElement is a group. */
		if(pUIElement instanceof IGroup<?>) {
			/* Cast accordingly. */
			final IGroup<T> lGroup = (IGroup<T>)pUIElement;
			/* Synchronize along the Group. */
			synchronized(lGroup) {
				/* Fetch the UIElements. */
				final List<T> lUIElements = lGroup.getUIElements();
				/* Iterate across the UIElements. */
				for(final IUIElement lUIElement : lUIElements) {
					/* Recursively process the UIElement. */
					PunchGlobal.onAccumulateRecursive(pUIElements, lUIElement);
				}
			}
		}
	}
	
	/* Returns the index at which events may be delegated. */
	public static final int onFetchDelegationIndex(final List<IUIElement> pHierarchy) {
		/* Allocate the DelegationBuffer. */
		final boolean[] lDelegationBuffer = new boolean[]{ true };
		/* Declare the iteration index. */
		int i;
		/* Iterate the CollisionResults from the top-down. */
		for(i = 0; i < pHierarchy.size() && (lDelegationBuffer[0]); i++) {
			/* Fetch the UIElement. */
			final IUIElement lUIElement = pHierarchy.get(i);
			/* Determine if we're handling a Group. */
			if(lUIElement instanceof IGroup<?>) { /** TODO: Use a courier approach! **/
				/* Cast accordingly. */
				final IGroup<?> lGroup = (IGroup<?>)lUIElement;
				/* Update the DelegationBuffer. */
				lDelegationBuffer[0] = lGroup.isInternalsDelegation(pHierarchy);
			}
		}
		/* Decrement the iteration index. */
		i--;
		/* Return the index. */
		return i;
	}
	
	/* Prevent instantiation of this class. */
	private PunchGlobal() {}
	
}