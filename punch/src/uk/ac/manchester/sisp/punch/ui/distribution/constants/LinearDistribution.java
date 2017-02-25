package uk.ac.manchester.sisp.punch.ui.distribution.constants;

import java.util.Collections;
import java.util.List;

import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.IUIPadding;
import uk.ac.manchester.sisp.punch.ui.distribution.IDistribution;
import uk.ac.manchester.sisp.punch.ui.distribution.global.DistributionGlobal;
import uk.ac.manchester.sisp.punch.ui.update.easing.ui.UIEasingGroup;
import uk.ac.manchester.sisp.punch.ui.update.easing.ui.UIElementPacket;
import uk.ac.manchester.sisp.ribbon.common.IBounds2;
import uk.ac.manchester.sisp.ribbon.common.IVec2;
import uk.ac.manchester.sisp.ribbon.utils.MathUtils;

/* Distributes the supplied UIElements into a horizontal or vertical row. */
public class LinearDistribution <T extends IUIElement> implements IDistribution { /** TODO: North/South facing. **/
	
	/* Member Variables. */
	private final List<T> mUIElements; /** TODO: Avoid accessing elements in this way. **/
	
	/* Creates a horizontal LinearDistribution. */
	public LinearDistribution(final List<T> pUIElements) { /** TODO: Callers must always supply a copy. Find a nicer pattern. **/
		/* Initialize Member Variables. */
		this.mUIElements = pUIElements;
	}

	@Override public final void onSupplyDistributables(final List<IUIElement> pUIElements) { pUIElements.addAll(this.getUIElements()); }
	
	/* Sorts the UIElements. */
	public void onSortElements(final List<T> pUIElements) { 
		/* Sort the UIElements in X. */
		Collections.sort(pUIElements, IVec2.COMPARATOR_HORIZONTAL);
	}
	
	@Override
	public void onDistributeElements(final UIEasingGroup pUIEasingGroup, final IUIPadding pUIPadding, final IBounds2.I.W pResultingBounds) {
		/* Sort the UIElements. */
		this.onSortElements(this.getUIElements());
		/* Iterate the UIElements in order. (The DistributionMap does not guarantee us this order!) */
		for(final IUIElement lUIElement : this.getUIElements()) {
			/* Fetch the EasingPacket. */
			final UIElementPacket lEasingPacket = DistributionGlobal.onFetchPacket(pUIEasingGroup, lUIElement);
			/* Fetch the Bounds2. */
			final IBounds2.I      lBounds2      = new IBounds2.I.Impl(lEasingPacket.getX(), lEasingPacket.getY(), lEasingPacket.getX() + lEasingPacket.getWidth(), lEasingPacket.getY() + lEasingPacket.getHeight());
			/* Check the alignment. */
			if(this.isHorizontal()) {
				/* Update the EasingPacket's XPosition. */
				lEasingPacket.setX(pResultingBounds.getMaximumX());
				/* Update the MaximumX, compaensate for padding. */
				pResultingBounds.setMaximumX(lEasingPacket.getX() + MathUtils.onCalculateWidth(lBounds2) + pUIPadding.getPadding());
				/* Update the MinimumY and MaximumY. */
				pResultingBounds.setMinimumY(Math.min(pResultingBounds.getMinimumY(), lEasingPacket.getY()          ));
			}
			else {
				/* Update the EasingPacket's XPosition. */
				lEasingPacket.setY(pResultingBounds.getMaximumY());
				/* Update the MaximumX, compaensate for padding. */
				pResultingBounds.setMaximumY(lEasingPacket.getY() + MathUtils.onCalculateHeight(lBounds2) + pUIPadding.getPadding());
				/* Update the MinimumX and MaximumX. */
				pResultingBounds.setMinimumX(Math.min(pResultingBounds.getMinimumX(), lEasingPacket.getX()         ));
			}
		}
		/* Iterate the UIElements in order. (The DistributionMap does not guarantee us this order!) */
		for(final IUIElement lUIElement : this.getUIElements()) {
			/* Fetch the EasingPacket. */
			final UIElementPacket lEasingPacket = DistributionGlobal.onFetchPacket(pUIEasingGroup, lUIElement);
			/* Fetch the Bounds2. */
			final IBounds2.I      lBounds2      = new IBounds2.I.Impl(lEasingPacket.getX(), lEasingPacket.getY(), lEasingPacket.getX() + lEasingPacket.getWidth(), lEasingPacket.getY() + lEasingPacket.getHeight());
			/* Check the alignment. */
			if(this.isHorizontal()) {
				/* Align with the MinimumY. */
				lEasingPacket.setY(pResultingBounds.getMinimumY());
				/* Update the MaximumY. */
				pResultingBounds.setMaximumY(Math.max(pResultingBounds.getMaximumY(), lEasingPacket.getY() + MathUtils.onCalculateHeight(lBounds2)));
			}
			else {
				/* Align with the MinimumX. */
				lEasingPacket.setX(pResultingBounds.getMinimumX());
				/* Update the MaximumX. */
				pResultingBounds.setMaximumX(Math.max(pResultingBounds.getMaximumX(), lEasingPacket.getX() + MathUtils.onCalculateWidth(lBounds2)));
			}
		}
		/* Check the alignment. */
		if(this.isHorizontal()) {
			/* Withdraw the final superflouous padding. */
			pResultingBounds.setMaximumX(pResultingBounds.getMaximumX() - pUIPadding.getPadding());
		}
		else {
			/* Withdraw the final superflouous padding. */
			pResultingBounds.setMaximumY(pResultingBounds.getMaximumY() - pUIPadding.getPadding());
		}
	}
	
	@Override
	public boolean isAnchored() {
		return true;
	}

	@Override
	public final void dispose() {
		/* Clear the UIElements. */
		this.getUIElements().clear();
	}
	
	private final List<T> getUIElements() {
		return this.mUIElements;
	}
	
	public boolean isHorizontal() {
		return true;
	}

	
	
//	public boolean isCentralized() {
//		return false;
//	}
	
}