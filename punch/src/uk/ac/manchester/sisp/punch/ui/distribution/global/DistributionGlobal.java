package uk.ac.manchester.sisp.punch.ui.distribution.global;

import java.util.ArrayList;
import java.util.List;

import uk.ac.manchester.sisp.punch.PunchModel;
import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.core.IGroup;
import uk.ac.manchester.sisp.punch.ui.courier.IUICourier;
import uk.ac.manchester.sisp.punch.ui.courier.helper.UIIterator;
import uk.ac.manchester.sisp.punch.ui.distribution.IDistribution;
import uk.ac.manchester.sisp.punch.ui.update.UIUpdateDispatcher;
import uk.ac.manchester.sisp.punch.ui.update.easing.ui.UIEasingGroup;
import uk.ac.manchester.sisp.punch.ui.update.easing.ui.UIElementPacket;
import uk.ac.manchester.sisp.ribbon.common.IBounds2;
import uk.ac.manchester.sisp.ribbon.ui.easing.IEasingConfiguration;
import uk.ac.manchester.sisp.ribbon.utils.DataUtils;
import uk.ac.manchester.sisp.ribbon.utils.MathUtils;
import uk.ac.manchester.sisp.ribbon.utils.ResourceUtils;

public final class DistributionGlobal { 
	
	/* Re-distributes the hierarchy starting at the specified Group. Interpolates the process to produce a smooth animation. */
	public static final <T extends IGroup<?>> T onDistributeHierarchy(final PunchModel pPunchModel, final List<IUIElement> pHierarchy, final T pGroup, final IEasingConfiguration pEasingConfiguration) {
		/* Allocate a reference to the generated UIEasingGroup. */
		final UIEasingGroup lUIEasingGroup = DistributionGlobal.onDistributionCore(pPunchModel.getUIUpdateDispatcher(), pHierarchy, pGroup, pEasingConfiguration, pPunchModel);
		/* Determine the kind of update to apply. */
		switch(pEasingConfiguration.getEasingAlgorithm()) {
			case NONE : 
				/* Now we will immediately evaluate the UIEasingGroup. Iterate the contents. */
				for(final UIElementPacket lUIElementPacket : lUIEasingGroup.getEasingPackets()) {
					/* Force the UIElementPacket to evaluate the terminal value. */
					lUIEasingGroup.onProgressEasing(lUIElementPacket, Float.NaN);
				}
				/* Dispose of the UIEasingGroup. */
				lUIEasingGroup.dispose();
			break;
			default   : 
				/* Update the EasingGroup's time reference. (This will smooth lagging responses.) */
				lUIEasingGroup.setObjectTimeSeconds(ResourceUtils.getSystemTimeSeconds());
				/* Export the EasingGroup. */
				pPunchModel.getUISecondsDispatcher().getEventFilters().add(lUIEasingGroup);
			break;
		}
		/* Return the Group. */
		return pGroup;
	}
	
	/** TODO: This needs cleaning up. **/
	/* Iterates through the hierarchy and re-distributes all Domains. */
	public static final UIEasingGroup onDistributionCore(final UIUpdateDispatcher pUIUpdateDispatcher, final List<IUIElement> pHierarchy, final IGroup<?> pGroup, final IEasingConfiguration pEasingConfiguration, final PunchModel pPunchModel) {
		/* Allocate a variable to track whether the Distribution requires anchoring. */
		final boolean[] lIsAnchored = new boolean[]{ false };
		/* Define the DistributionList. */
		final List<IDistribution> lDistributionList = new ArrayList<IDistribution>();
		/* Define the UIIterator. */
		/** TODO: Really, really resolve the EasingPacket implementation. **/
		final IUICourier<UIEasingGroup> lUICourier = new UIIterator<UIEasingGroup>(pHierarchy) {
			/* Handle a Grouped instance. */
			@Override public final <T extends IUIElement> void onCourierTransit(final IGroup<T> pGroup, final UIEasingGroup pUIEasingGroup) {
				/* Declare a reference to the Distribution. */
				final IDistribution lDistribution;
				/* Synchronize upon the IGroup and re-distribute. */
				synchronized(pGroup) { 
					/* Fetch the Distribution. */
					lDistribution = pGroup.onFetchDistribution(pHierarchy, pUIUpdateDispatcher);
				}
				/* Determine if the Distribution is non-null. */
				if(DataUtils.isNotNull(lDistribution)) {
					/* Distribute the Hierarchy. */
					DistributionGlobal.onOrchestrateDistribution(pUIEasingGroup, pGroup, pHierarchy, lDistribution);
					/* Update the IsAnchored status. */
					lIsAnchored[0] = lDistribution.isAnchored();
					/* Buffer the Distribution. */
					lDistributionList.add(lDistribution);
//					/* Dispose of the returned Distribution. */
//					lDistribution.dispose();
				}
				/*  Serve parents in the hierarchy. */
				this.onServeParent(pGroup, pUIEasingGroup);
			}
			@Override public final void onServeParent(final IUIElement pChild, final UIEasingGroup pUIEasingGroup) {
				/* Determine whether the distribution operation has finished. (Handling the parent-most UIElement, or the current distribution resulted in a non-net change for the current parent.) */
				//final boolean lIsFinished = (DataUtils.isZero(pHierarchy.indexOf(pChild)) || DataUtils.isNotNull(pEasingPacket) && ((pEasingPacket.getX() == pChild.getX() && pEasingPacket.getY() == pChild.getY() && pEasingPacket.getWidth() == pChild.getWidth() && pEasingPacket.getHeight() == pChild.getHeight())));
				/** TODO: We can't do early exists because of the Separator feedback... **/
				final boolean lIsFinished = DataUtils.isNull(pHierarchy) || DataUtils.isZero(pHierarchy.indexOf(pChild));
				/* Fetch the outer-most parent's UIEasingPacket. */
				final UIElementPacket lUIElementPacket = DistributionGlobal.onFetchPacket(pUIEasingGroup, pChild);
				/** TODO: If this is the way we're distributing, then we can just refactor immunity control. **/
				/* Ensure we've handed a valid total distribution, and the user expects us to issue an anchor. */
				if(DataUtils.isNotNull(lUIElementPacket) && lIsAnchored[0]) { 
					/* Calculate the total difference in position. (We'll use this to make the parent layout appear fixed, and it's the internals that reposition themselves.) */
					final int lDeltaX = lUIElementPacket.getX() - pChild.getX();
					final int lDeltaY = lUIElementPacket.getY() - pChild.getY();
					/* Withdraw the total offset. */
					MathUtils.onWithdrawOffset(lUIElementPacket, lDeltaX, lDeltaY);
				}
				/* Determine whether we're handling the final entry. */
				if(!lIsFinished) { 
					/* Reset the Anchored status. */
					lIsAnchored[0] = false;
					/* Serve the parent. */
					super.onServeParent(pChild, pUIEasingGroup);
				}
			}
		};
		/* Allocate the UIEasingGroup. */ /** TODO: Externalize these parameters. **/ /** TODO: try passing NaN to see if we can further promote modularity. **/
		final UIEasingGroup lUIEasingGroup = new UIEasingGroup(pUIUpdateDispatcher, pEasingConfiguration, ResourceUtils.getSystemTimeSeconds()) {
			/* Define custom disposal operations. */
			@Override public void dispose() {
				/* Perform a standard disposal. */
				super.dispose();
				/* Iterate the DistributionList. */
				for(final IDistribution lDistribution : lDistributionList) {
					/* Dispose of the Distribution. */
					lDistribution.dispose();
				}
				/* Remove our reference from the DeferList. (This allows callers to easily register the UIEasingGroup with the UIDeferFilter). */
				pPunchModel.getUIDeferFilter().getDeferList().remove(this);
			}
		};
		/* Export the UICourier. Define the first iteration using a null EasingPacket, as we have yet to distribute any group. */
		pGroup.onCourierDispatch(lUICourier, lUIEasingGroup);
		/* Allocate and return the EasingGroup. */
		return lUIEasingGroup;
	}
	
	/* Defines whether a corresponding UIElementPacket exists for a given UIElement within a UIEasingGroup. */
	private static final boolean isPacketized(final UIEasingGroup pUIEasingGroup, final IUIElement pUIElement) {
		/* Determine whether we can fetch a corresponding packet. */
		return DataUtils.isNotNull(DistributionGlobal.onFetchPacket(pUIEasingGroup, pUIElement));
	}
	
	/** TODO: honest-to-god kill this method. swap for mapping instead. **/
	/* Returns the corresponding UIElementPacket within a UIEasingGroup for a given UIElement. */
	public static final UIElementPacket onFetchPacket(final UIEasingGroup pUIEasingGroup, final IUIElement pUIElement) {
		/* Allocate a reference to the UIElementPacket. */
		UIElementPacket lUIElementPacket = null;
		/* Allocate a variable to track whether the UIElement is contained. */
		boolean         lIsContained     = false;
		/* Iterate the EasingPackets. */
		for(int i = 0; i < pUIEasingGroup.getEasingPackets().size() && (!lIsContained); i++) {
			/* Assign the UIElementPacket. */
			lUIElementPacket = pUIEasingGroup.getEasingPackets().get(i);
			/* Update the result. */
			lIsContained     = lUIElementPacket.getUIElement().equals(pUIElement);
		}
		/* Return the UIElementPacket. */
		return lIsContained ? lUIElementPacket : null;
	}
	
	/** TODO: Don't pass Group, only distributable, have it extend padding. **/
	/* Handles memory allocations, sequential execution and temporal dependencies for a hierarchical distribution. */
	/** TODO: Goddamn resolve this! **/
	private static final void onOrchestrateDistribution(final UIEasingGroup pUIEasingGroup, final IGroup<?> pGroup, final List<IUIElement> pHierarchy, final IDistribution pDistribution) {
		/* Allocate a reference to the Distributables. */
		final List<IUIElement> lDistributables     = new ArrayList<IUIElement>();
		/* Allocate the Bounds. */
		final IBounds2.I.W     lDistributionBounds = new IBounds2.I.Impl(0, 0, 0, 0);
		/* Synchronize upon the Distribution. */
		synchronized(pDistribution) {
			/* Fetch the Distributables. */
			pDistribution.onSupplyDistributables(lDistributables);
		}
		/* Initialize the DistributionMap and BoundsMap. */
		for(final IUIElement lUIElement : lDistributables) {
			/* Ensure the UIEasingGroup doesn't already contain the UIElement. */
			if(!DistributionGlobal.isPacketized(pUIEasingGroup, lUIElement)) {
				/* Allocate a new entry. */
				pUIEasingGroup.getEasingPackets().add(new UIElementPacket(lUIElement));
			}
		}
		/* Have the Distributor position the local EasingPackets. */
		pDistribution.onDistributeElements(pUIEasingGroup, pGroup, lDistributionBounds);
		/* Calculate the bounded distribution limits. */
		final int lDeltaX = Math.round(lDistributionBounds.getMinimumX() - pGroup.getMargin().getMinimumX());
		final int lDeltaY = Math.round(lDistributionBounds.getMinimumY() - pGroup.getMargin().getMinimumY());
		/* Calculate the external changes in the Domain. */
		final int lBoundedWidth  = Math.max(pGroup.getMinima().getWidth(),  (lDistributionBounds.getMaximumX() - lDistributionBounds.getMinimumX()) + (pGroup.getMargin().getMinimumX() + pGroup.getMargin().getMaximumX()));
		final int lBoundedHeight = Math.max(pGroup.getMinima().getHeight(), (lDistributionBounds.getMaximumY() - lDistributionBounds.getMinimumY()) + (pGroup.getMargin().getMinimumY() + pGroup.getMargin().getMaximumY()));
		/* Next, offset all internal components by the delta. (We're reconfiguring the bounds of the owning group, therefore we need to compensate the absolute positioning). */ 
		for(final IUIElement lUIElement : lDistributables) {
			/* Fetch the Packetization. */
			final UIElementPacket lUIElementPacket = DistributionGlobal.onFetchPacket(pUIEasingGroup, lUIElement);
			/* Ensure the Packet still exists. (The caller may have removed it.) */ /** TODO: This arose from Cascade defunct, just don't generate types we don't need. **/
			if(DataUtils.isNotNull(lUIElementPacket)) {
				/* Withdraw the offset from the EasingPacket. */
				MathUtils.onWithdrawOffset(lUIElementPacket, lDeltaX, lDeltaY);
			}
		}
		/* Allocate a UIElementPacket for the Group. */
		final UIElementPacket lUIElementPacket = new UIElementPacket(pGroup);
		/* Update the Position of the UIElementPacket. */
		lUIElementPacket.setX(pGroup.getX() + lDeltaX);
		lUIElementPacket.setY(pGroup.getY() + lDeltaY);
		/* Update the Bounds of the UIElementPacket. */
		lUIElementPacket.setWidth(lBoundedWidth);
		lUIElementPacket.setHeight(lBoundedHeight);
		/* Buffer the UIElementPacket. */
		pUIEasingGroup.getEasingPackets().add(lUIElementPacket);
	}
	
	/* Prevent external construction. */
	private DistributionGlobal() {}
	
}