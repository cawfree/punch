package uk.ac.manchester.sisp.punch.ui.lexicon.coupling.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.ac.manchester.sisp.punch.ui.core.IGroup;
import uk.ac.manchester.sisp.punch.ui.distribution.global.DistributionGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.ILexical;
import uk.ac.manchester.sisp.punch.ui.lexicon.compilation.Compilation;
import uk.ac.manchester.sisp.punch.ui.lexicon.compilation.Interpreter;
import uk.ac.manchester.sisp.punch.ui.lexicon.compilation.global.ISensitive;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.Coupling;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.IDataConduit;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.Decoupler;
import uk.ac.manchester.sisp.punch.ui.lexicon.global.LexiconGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.literal.array.ArrayGroup;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.IContact;
import uk.ac.manchester.sisp.punch.ui.lexicon.sequencing.ISequential;
import uk.ac.manchester.sisp.punch.ui.update.easing.ui.UIEasingGroup;
import uk.ac.manchester.sisp.punch.ui.update.easing.ui.UIElementPacket;
import uk.ac.manchester.sisp.ribbon.utils.DataUtils;

public final class Segmenter extends Interpreter implements ISensitive<ILexical> { 
	
	/* Member Variables. */
	private final UIEasingGroup  mUIEasingGroup;
	private final List<Integer>  mRippleList;
	private final List<ILexical> mSensitivies;
	
	public Segmenter(final UIEasingGroup pUIEasingGroup) {
		/* Initialize Member Variables. */
		this.mUIEasingGroup = pUIEasingGroup;
		this.mRippleList    = new ArrayList<Integer> ();
		this.mSensitivies   = new ArrayList<ILexical>();
	}
	
	@Override
	public final void onCourierTransit(final ArrayGroup pArrayGroup, final Compilation pCompilation) {
		/* Disable default processing. */
		pCompilation.getInspectionStack().push(Boolean.FALSE);
	}
	
	@Override
	public final void onEnterDeclaration(final Coupling<?, ?> pCoupling, final Compilation pCompilation) { 
		/* Ensure top-level Couplings are added to the Sensitivity List. */
		this.getSensitivies().add(pCoupling);
	}
	
	/* Handle a Coupling. */
	@Override public final <V extends ILexical, T extends ILexical & IGroup<V>> void onCourierTransit(final Coupling<V, T> pCoupling, final Compilation pCompilation) { 
		/* Fetch the Coupling's UIElementPacket. */
		final UIElementPacket   lUIElementPacket = DistributionGlobal.onFetchPacket(this.getUIEasingGroup(), pCoupling);
		/* Allocate the MinimumY. */
		      int               lMinimumY        = Integer.MAX_VALUE;
		/* Allocate the Succession. */
		final List<ILexical>    lSuccession      = new ArrayList<ILexical>();
		/* Add the SinkDecoupler to the Succession. */
		lSuccession.add(pCoupling.getSinkDecoupler());
		/* Add all of the Coupling's Internals to the Succession. */
		lSuccession.addAll(pCoupling.getInternals());
		/* Add the SourceDecoupler to the Succession. */
		lSuccession.add(pCoupling.getSourceDecoupler());
		/* Allocate an array for the corresponding UIElementPackets. */
		final UIElementPacket[] lPackets         = new UIElementPacket[lSuccession.size()];
		/* Assign the Offset to the SinkDecoupler, Internals and SourceDecoupler. (Here, we assume they will always rest at the origin). */
		for(int i = 0; i < lSuccession.size(); i++) {
			/* Fetch the Lexical. */
			final ILexical  lLexical        = lSuccession.get(i);
			/* Determine whether there is an existing UIElementPacket for the Lexical. */
			UIElementPacket lUIEasingPacket = DistributionGlobal.onFetchPacket(this.getUIEasingGroup(), lLexical);
			/* Update the Packets. */
			lPackets[i]                     = lUIEasingPacket;
			/* Initialize the YPosition. */
			lUIEasingPacket.setY(Integer.MAX_VALUE);
			/* Buffer the Lexical as a Sensitivity. */
			this.getSensitivies().add(lLexical);
		}
		/* Dispatch to internals. */
		pCompilation.onProcessInternals(pCoupling, pCoupling.getUIElements(), this);
		/* Assign the Offset to the SinkDecoupler, Internals and SourceDecoupler. (Here, we assume they will always rest at the origin). */
		for(int i = 0; i < lSuccession.size(); i++) {
			/* Allocate the LocalOffset. */
			UIElementPacket lUIEasingPacket = lPackets[i];
			/* Determine if the dispatch provided us with height information. */
			if(lUIEasingPacket.getY() != Integer.MAX_VALUE) {
				/* Update the MinimumY. */
				lMinimumY = Math.min(lMinimumY, lUIEasingPacket.getY());
			}
		}
		/* Filter the MinimumY to track either the MinimumY of the contained components, or the pre-assigned YIndex of the Coupling. */
		lMinimumY = ((lMinimumY != Integer.MAX_VALUE) ? lMinimumY : lUIElementPacket.getY());
		/* Force the MinimumY to rest at the given location. */
		lUIElementPacket.setY(lMinimumY);
		/* Iterate the Succession. */
		for(int i = 0; i < lSuccession.size(); i++) {
			/* Apply the MinimumY. */
			lPackets[i].setY(lMinimumY);
		}
		/* Break conventional analysis. */
		pCompilation.getInspectionStack().push(Boolean.FALSE);
	}

	@Override
	public final void onLeaveDeclaration(final Coupling<?, ?> pCoupling, final Compilation pCompilation) {
		/* Accumulate all the unique indices generated by the mapping. */
		this.onAccumulateDataPaths();
		/* Decimate missing ranks. */
		this.onGenerateDataPaths();
		/* Finally, decimate the blank rows. */
		this.onDecimateRanks();
	}

	/* Handle a ISequential. */
	@Override public <T extends ILexical> void onCourierTransit(final ISequential<T> pSequence, final Compilation pCompilation) {
		/* Make a safe copy of the ISequential's UIElements. */
		final List<T>           lUIElements = new ArrayList<T>(pSequence.getUIElements());
		/* Remove the Cascades from the UIElements. (We don't wish to process them here.) */
		lUIElements.removeAll(LexiconGlobal.onAccumulateCascades(lUIElements));
		/* Allocate a variable to track the MinimumY. */
		      int               lMinimumY   = Integer.MAX_VALUE;
		/* Allocate the Packets. */
		final UIElementPacket[] lPackets    = new UIElementPacket[lUIElements.size()];
		/* Order the UIElements in X. */
		Collections.sort(lUIElements, pSequence.getComparator());
		/* Iterate the UIElements. */
		for(int i = 0; i < lUIElements.size(); i++) {
			/* Fetch the UIElement. */
			final T lUIElement = lUIElements.get(i);
			/* Buffer the UIElement as a Sensitivity. */
			this.getSensitivies().add(lUIElement);
			/* First, attempt to fetch an existing entry for the UIElement. */
			      UIElementPacket lUIElementPacket = DistributionGlobal.onFetchPacket(this.getUIEasingGroup(), lUIElement);
			/* Determine if the entry exists. For example, a Literal or a Contact will generate it's own location; it's data sources we depend upon. */
			if(lUIElementPacket.getY() == lUIElement.getY() && lUIElementPacket.getX() == lUIElementPacket.getX()) {
				/* Update the Offset with the initial AbsoluteIndex. */
				lUIElementPacket.setY(pCompilation.getSubIndex(this, lUIElement) * LexiconGlobal.CODE_DIM_HEIGHT_UNIT);
			}
			/* Update the Packets. */
			lPackets[i] = lUIElementPacket;
		}
		/* Process the Internals. */
		pCompilation.onProcessInternals(pSequence, pSequence.getUIElements(), this);
		/* Iterate the UIElements. */
		for(int i = 0; i < lUIElements.size(); i++) {
			/* Update the MinimumY. */
			lMinimumY = Math.min(lMinimumY, lPackets[i].getY());
		}
		/* Assign the MinimumY to the ISequential. */
		DistributionGlobal.onFetchPacket(this.getUIEasingGroup(), pSequence).setY(lMinimumY);
		/* Break conventional analysis. */
		pCompilation.getInspectionStack().push(Boolean.FALSE);
	}
	
	/* Handle a Decoupler. */
	@Override public <T extends IContact<?>>void onCourierTransit(final Decoupler<T> pDecoupler, final Compilation pCompilation) { /** TODO: Do all Minima tweaking here. **/
		/* Fetch the Contacts. */
		final List<T>         lContacts        = pDecoupler.getUIElements();
		/* Allocate a variable to track the MinimumY and MaximumX. */
		      int             lMinimumY        = Integer.MAX_VALUE;
		/* Iterate the Contacts. */
		for(final IContact<?> lContact : lContacts) {
			/* Fetch the Contact's Mapping. */
			final UIElementPacket lMapping = DistributionGlobal.onFetchPacket(this.getUIEasingGroup(), lContact);
			/* Buffer the Contact as a Sensitivity. */
			this.getSensitivies().add(lContact);
			/* Update the Minima. */
			lMinimumY = Math.min(lMinimumY, lMapping.getY());
		}
		/* Determine whether we can wrap the Decoupler's index more tightly. */
		if(!lContacts.isEmpty()) { 
			/* Assign the YPosition; this ensures the Decoupler will wrap the UIElement. */
			DistributionGlobal.onFetchPacket(this.getUIEasingGroup(), pDecoupler).setY(lMinimumY);
		}
	}
	
	/* Fills the RippleList with the sequence of each unique datapath index within the mapping. */
	private final void onAccumulateDataPaths() {
		/* Clear the RippleList. */
		this.getRippleList().clear();
		/* Iterate the Mapping. */
		for(final ILexical lLexical : this.getSensitivies()) {
			/* Fetch the UIEasingPacket. */
			final UIElementPacket lUIElementPacket = DistributionGlobal.onFetchPacket(this.getUIEasingGroup(), lLexical);
			/* Determine if the UIElement is a datapath. */
			final boolean         lIsDataPath      = (lLexical instanceof IDataConduit);
			/* Fetch the UIElement's Index. */
			final Integer lIndex = DataUtils.getCachedInteger(lUIElementPacket.getY() / LexiconGlobal.CODE_DIM_HEIGHT_UNIT);
			/* Determine if the RippleList doesn't contain the Index. */
			if(!this.getRippleList().contains(lIndex) && lIsDataPath) {
				/* Add the Index. */
				this.getRippleList().add(lIndex);
			}
		}
		/* Sort the RippleList. */
		Collections.sort(this.getRippleList());
	}

	/* Sorts the Mapping into discrete DataPaths. Callers must ensure the RippleList contains sequential unique Cascade Indices only. */
	private final void onGenerateDataPaths() { 
		/* Iterate the Mapping. */
		for(final ILexical lLexical : this.getSensitivies()) {
			/* Fetch the UIEasingPacket. */
			final UIElementPacket lUIElementPacket = DistributionGlobal.onFetchPacket(this.getUIEasingGroup(), lLexical);
			/* Fetch the UIElement's Index. */
			final Integer lIndex = DataUtils.getCachedInteger(lUIElementPacket.getY() / LexiconGlobal.CODE_DIM_HEIGHT_UNIT);
			/* Determine if we're handling an invalid datapath. */
			if(!this.getRippleList().contains(lIndex)) {
				/* Allocate a variable to determine if the UIElementPacket has been re-assigned to a new index. */
				boolean lIsAssigned = false;
				/* Iterate the RippleList. */
				for(int i = 0; i < this.getRippleList().size() && !lIsAssigned; i++) {
					/* Fetch the Ripple. */
					final Integer lRipple = this.getRippleList().get(i);
					/* Determine whether the Ripple lies at a greater Index. */
					if(lIndex < lRipple) {
						/* Overwrite the index. */
						lUIElementPacket.setY((int)lRipple * LexiconGlobal.CODE_DIM_HEIGHT_UNIT);
						/* Assert that we've assigned a new index. */
						lIsAssigned = true;
					}
				}
				/* Determine if the index has not been assigned. */
				if(!lIsAssigned) {
					/* Fetch the last-most Ripple. */
					Integer lRipple = DataUtils.getLastElementOf(this.getRippleList());
					/* Ensure we're handling a valid Ripple. (If there aren't any Ripples, coerce to the baseline.) */
					        lRipple = DataUtils.isNotNull(lRipple) ? lRipple : DataUtils.getCachedInteger(0);
					/* Assign the UIElementPacket to the Ripple's Index. */
					lUIElementPacket.setY((int)lRipple * LexiconGlobal.CODE_DIM_HEIGHT_UNIT);
				}
			}
		}
	}
	
	/* Eliminates redundant rows from the RippleList. */
	private final void onDecimateRanks() { 
		/* Next, eliminate redundancies. */
		for(int i = this.getRippleList().size() - 1; i > 0; i--) {
			/* Fetch the Indices. (0 -> Higher, 1 -> Lower). */
			final Integer lIndex0     = this.getRippleList().get(i + 0);
			final Integer lIndex1     = this.getRippleList().get(i - 1);
			/* Calculate the difference between the indices. */
			final int     lDifference = ((lIndex0 - lIndex1) - 1);
			/* Determine if there's a sizeable difference in the code. (This indicates totally blank space within a layout when a sequence hasn't incremented.) */
			if(lDifference > 0) {
				/* Iterate the Mapping. */
				for(final ILexical lLexical : this.getSensitivies()) {
					/* Fetch the UIEasingPacket. */
					final UIElementPacket lUIElementPacket = DistributionGlobal.onFetchPacket(this.getUIEasingGroup(), lLexical);
					/* Fetch the Index. */
					final Integer         lIndex           = DataUtils.getCachedInteger(lUIElementPacket.getY() / LexiconGlobal.CODE_DIM_HEIGHT_UNIT);
					/* Check if the Entry rests at an affected index. */
					if(lIndex >= lIndex0) {
						/* Withdraw the difference by overwriting the entry. */
						lUIElementPacket.setY((int)(lIndex- lDifference) * LexiconGlobal.CODE_DIM_HEIGHT_UNIT);
					}
				}
			}
		}
	}
	
	private final UIEasingGroup getUIEasingGroup() {
		return this.mUIEasingGroup;
	}
	
	private final List<Integer> getRippleList() {
		return this.mRippleList;
	}
	
	@Override
	public final List<ILexical> getSensitivies() {
		return this.mSensitivies;
	}

}
