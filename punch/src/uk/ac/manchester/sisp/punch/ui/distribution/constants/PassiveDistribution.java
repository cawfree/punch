package uk.ac.manchester.sisp.punch.ui.distribution.constants;

import java.util.List;

import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.IUIPadding;
import uk.ac.manchester.sisp.punch.ui.distribution.IDistribution;
import uk.ac.manchester.sisp.punch.ui.distribution.global.DistributionGlobal;
import uk.ac.manchester.sisp.punch.ui.update.easing.ui.UIEasingGroup;
import uk.ac.manchester.sisp.punch.ui.update.easing.ui.UIElementPacket;
import uk.ac.manchester.sisp.ribbon.common.IBounds2;

/* Distributes the UIElements in a way that ensures that Container tightly encloses all UIElements. The locations of the UIElements themselves *appear* unchanged. */
public final class PassiveDistribution <T extends IUIElement> implements IDistribution {
	
	/* Member Variables. */
	private final List<T> mUIElements;
	
	public PassiveDistribution(final List<T> pUIElements) {
		/* Initialize Member Variables. */
		this.mUIElements = pUIElements;
	}
	
	private final List<T> getUIElements() {
		return this.mUIElements;
	}

	@Override
	public final void onSupplyDistributables(final List<IUIElement> pUIElements) {
		/* Supply all the provided UIElements. */
		pUIElements.addAll(this.getUIElements());
	}

	@Override
	public final void onDistributeElements(final UIEasingGroup pUIEasingGroup, final IUIPadding pUIPadding, final IBounds2.I.W pResultingBounds) {
		/* Ensure we've UIElements to handle. */
		if(!this.getUIElements().isEmpty()) {
			/* Initialize the Bounds to ensure accurate detection. */
			pResultingBounds.setMinimumX(Integer.MAX_VALUE);
			pResultingBounds.setMinimumY(Integer.MAX_VALUE);
			pResultingBounds.setMaximumX(Integer.MIN_VALUE);
			pResultingBounds.setMaximumY(Integer.MIN_VALUE);
		}
		/* Accumulate the total bounds of the UIElements. */
		for(int i = 0; i < this.getUIElements().size(); i++) {
			/* Fetch the UIElement. */
			final IUIElement      lUIElement       = this.getUIElements().get(i);
			/* Fetch the corresponding UIElementPacket. */
			final UIElementPacket lUIElementPacket = DistributionGlobal.onFetchPacket(pUIEasingGroup, lUIElement);
			/* Update the minima of the ResultingBounds. */
			pResultingBounds.setMinimumX(Math.min(pResultingBounds.getMinimumX(), lUIElementPacket.getX()));
			pResultingBounds.setMinimumY(Math.min(pResultingBounds.getMinimumY(), lUIElementPacket.getY()));
			/* Update the maxima of the ResultingBounds. (Use the form [X + W], [Y + H]. */
			pResultingBounds.setMaximumX(Math.max(pResultingBounds.getMaximumX(), lUIElementPacket.getX() + lUIElementPacket.getWidth()));
			pResultingBounds.setMaximumY(Math.max(pResultingBounds.getMaximumY(), lUIElementPacket.getY() + lUIElementPacket.getHeight()));
		}
	}
	
	@Override
	public final boolean isAnchored() {
		/* Assert that we wish for the distribution to merely encapsulate UIElements. Maintaining a fixed location for the container is non-essential. */
		return false;
	}

	@Override
	public final void dispose() {
		/* Clear the UIElements. */
		this.getUIElements().clear();
	}
	
}