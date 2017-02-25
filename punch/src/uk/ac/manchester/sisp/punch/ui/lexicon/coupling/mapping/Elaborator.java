package uk.ac.manchester.sisp.punch.ui.lexicon.coupling.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.ac.manchester.sisp.punch.ui.core.IGroup;
import uk.ac.manchester.sisp.punch.ui.distribution.global.DistributionGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.ILexical;
import uk.ac.manchester.sisp.punch.ui.lexicon.compilation.Compilation;
import uk.ac.manchester.sisp.punch.ui.lexicon.compilation.Interpreter;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.Coupling;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.Decoupler;
import uk.ac.manchester.sisp.punch.ui.lexicon.documentation.Comment;
import uk.ac.manchester.sisp.punch.ui.lexicon.global.LexiconGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.literal.array.ArrayGroup;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.IContact;
import uk.ac.manchester.sisp.punch.ui.lexicon.sequencing.ISequential;
import uk.ac.manchester.sisp.punch.ui.update.easing.ui.UIEasingGroup;
import uk.ac.manchester.sisp.punch.ui.update.easing.ui.UIElementPacket;
import uk.ac.manchester.sisp.ribbon.utils.DataUtils;
import uk.ac.manchester.sisp.ribbon.utils.MathUtils;

public final class Elaborator extends Interpreter {
	
	/* Member Variables. */
	private final UIEasingGroup mUIEasingGroup;
	
	public Elaborator(final UIEasingGroup pUIEasingGroup) {
		/* Initialize Member Variables. */
		this.mUIEasingGroup = pUIEasingGroup;
	}

	@Override
	public final void onCourierTransit(final ArrayGroup pArrayGroup, final Compilation pCompilation) { 
		/* ArrayGroups are capable of processing themselves; however, if it's empty we'll need to assert some default dimensions. */
		if(pArrayGroup.getUIElements().isEmpty()) {
			/* Fetch the ArrayGroup's UIElementPacket. */
			final UIElementPacket lUIElementPacket = DistributionGlobal.onFetchPacket(this.getUIEasingGroup(), pArrayGroup);
			/* Assert the Minima for the ArrayGroup. */
			MathUtils.setDimension(lUIElementPacket, pArrayGroup.getMinima());
		}
		/* Disable default processing. */
		pCompilation.getInspectionStack().push(Boolean.FALSE);
	}
	
	/* Handle a Coupling. */
	@Override public final <V extends ILexical, T extends ILexical & IGroup<V>> void onCourierTransit(final Coupling<V, T> pCoupling, final Compilation pCompilation) { 
		/* Process the internals. */
		pCompilation.onProcessInternals(pCoupling, pCoupling.getUIElements(), this);
		/* Determine if the Coupling has been closed. */
		final boolean           lIsClosed      = pCoupling.isClosed();
		/* Allocate the Succession. */
		final List<ILexical>    lSuccession    = new ArrayList<ILexical>();
		/* Add the SinkDecoupler. */
		lSuccession.add(pCoupling.getSinkDecoupler());
		/* Add the Internals. */
		lSuccession.addAll(pCoupling.getInternals());
		/* Add the SourceDecoupler. */
		lSuccession.add(pCoupling.getSourceDecoupler());
		/* Allocate the Packets. */
		final UIElementPacket[] lPackets       = new UIElementPacket[lSuccession.size()];
		/* Allocate a variable to track the MaximumHeight. */
		      int        lMaximumHeight = Integer.MIN_VALUE;
		/* Iterate the Succession. */
		for(int i = 0; i < lSuccession.size(); i++) {
			/* Fetch the Lexical. */
			final ILexical        lLexical         = lSuccession.get(i);
			/* Fetch the Lexical's UIElementPacket. */
			final UIElementPacket lUIElementPacket = DistributionGlobal.onFetchPacket(this.getUIEasingGroup(), lLexical);
			/* Buffer the UIElementPacket. */
			                      lPackets[i]      = lUIElementPacket;
			/* Update the MaximumHeight. */
			lMaximumHeight = Math.max(lMaximumHeight, LexiconGlobal.onPredictY(lUIElementPacket.getHeight()));
		}
		/* Iterate the Packets to ensure the MaximumHeight persists. */
		for(final UIElementPacket lUIElementPacket : lPackets) {
			/* Assign the MaximumHeight. */
			lUIElementPacket.setHeight(lMaximumHeight);
		}
		/* Fetch the MaximumWidth for the Internals. */
		      int lMaximumWidth = Integer.MIN_VALUE;
		/* Iterate the Internals' Packets. */
		for(int i = 1; i < lPackets.length - 1; i++) {
			/* Update the MaximumWidth. */
			lMaximumWidth = Math.max(lMaximumWidth, lPackets[i].getWidth());
		}
		/* Iterate the Internals' Packets. */
		for(int i = 1; i < lPackets.length - 1; i++) {
			/* Assign the MaximumWidth. */
			lPackets[i].setWidth(lMaximumWidth);
		}
		/* Set the SinkDecoupler's location to the origin of the Coupling. */
		lPackets[0].setX(0);
		
		/* Fetch the ActiveInternals. */
		final ILexical lActiveInternals = pCoupling.getActiveInternals();
		/* Find the Index of the ActiveInternals. */
		      int      lIndex;
		/* Iterate the Internals' Packets. */
		for(lIndex = 1; lIndex < (lPackets.length - 1); lIndex++) {
			/* Determine if the current Packet matches the ActiveInternals. */
			if(lActiveInternals.equals(lPackets[lIndex].getUIElement())) {
				/* End the iteration. */
				break;
			}
		}
		
		/* Initialize the XPosition. */
		int lXPosition = (-1 * (((lIndex - 1) * lMaximumWidth)) + lPackets[0].getWidth());
		/* Iterate the Internals' Packets. */
		for(int i = 1; i < lPackets.length - 1; i++) {
			/* Assign the XPosition to the Packet. */
			lPackets[i].setX(lXPosition);
			/* Increase the XPosition by the MaximumWidth. */
			lXPosition += lMaximumWidth;
		}
		/* Update the SourceDecoupler's location, depending on whether we're closing the Coupling or not. */
		DataUtils.getLastElementOf(lPackets).setX(lIsClosed ? (lPackets[0].getWidth() - pCoupling.getOverlap()) : (lMaximumWidth + lPackets[0].getWidth()));
		/* Fetch the Coupling's UIElementPacket. */
		final UIElementPacket lUIElementPacket = DistributionGlobal.onFetchPacket(this.getUIEasingGroup(), pCoupling);
		/* Assign the MaximumHeight to the Coupling. Use the XPosition to define the width. */
		MathUtils.setDimension(lUIElementPacket, (DataUtils.getLastElementOf(lPackets).getX() + DataUtils.getLastElementOf(lPackets).getWidth()), lMaximumHeight);
		/* Break conventional analysis. */
		pCompilation.getInspectionStack().push(Boolean.FALSE);
	}
	/* Handle a ISequential. */
	@Override public final <T extends ILexical> void onCourierTransit(final ISequential<T> pSequence, final Compilation pCompilation) {
		/* Fetch the ISequential's UIElements. */
		final List<T>         lUIElements      = new ArrayList<T>(pSequence.getUIElements());
		/* Remove the Cascades from the UIElements. */
		lUIElements.removeAll(LexiconGlobal.onAccumulateCascades(pSequence.getUIElements()));
		/* Fetch the Decoupler's UIElementPacket. */
		final UIElementPacket lUIElementPacket = DistributionGlobal.onFetchPacket(this.getUIEasingGroup(), pSequence);
		/* Determine if we're handling a valid ISequential. */
		if(!lUIElements.isEmpty()) {
			/* Sort the UIElements in X. */
			Collections.sort(lUIElements, pSequence.getComparator());
			/* Allocate a variable to track the MaximumY. */
			int lMaximumY  = Integer.MIN_VALUE;
			/* Declare the XPosition. */
			int lXPosition = pSequence.getMargin().getMinimumX();
			/* Process the Internals. */
			pCompilation.onProcessInternals(pSequence, pSequence.getUIElements(), this);
			/* Iterate the UIElements. */
			for(final ILexical lLexical : lUIElements) {
				/* Fetch the Lexical's UIEasingPacket. */
				final UIElementPacket lUIEasingPacket = DistributionGlobal.onFetchPacket(this.getUIEasingGroup(), lLexical);
				/* Set the XPosition of the UIEasingPacket. */
				lUIEasingPacket.setX(lXPosition);
				/* Update the XPosition. */
				lXPosition += lUIEasingPacket.getWidth() + pSequence.getPadding();
				/* Update the MaximumY. */
				lMaximumY   = Math.max(lMaximumY, (lUIEasingPacket.getY() + lUIEasingPacket.getHeight()));
			}
			/* Compensate for the superflouous padding. */
			lXPosition -= pSequence.getPadding();
			/* Append the Margin's MaximumX. */
			lXPosition += pSequence.getMargin().getMaximumX();
			/* Assign the UIElementPacket's Width and Height; merely encapsulate the UIElements. Ensure the height is a function of the CODE_DIM_HEIGHT_UNIT. */
			MathUtils.setDimension(lUIElementPacket, lXPosition, LexiconGlobal.onPredictY(lMaximumY));
		}
		else {
			/* Force the ISequential to reflect the Minima. */
			MathUtils.setDimension(lUIElementPacket, pSequence.getMinima());
		}
		/* Break conventional analysis. */
		pCompilation.getInspectionStack().push(Boolean.FALSE);
	}
	
	/* Handle a Decoupler. (Wrap the Contacts using their predicted maxima and minima.) */
	@Override public final <T extends IContact<?>> void onCourierTransit(final Decoupler<T> pDecoupler, final Compilation pCompilation) { 
		/* Fetch the Decoupler's Contacts. */
		final List<T>           lContacts        = pDecoupler.getUIElements();
		/* Fetch the Decoupler's UIElementPacket. */
		final UIElementPacket   lUIElementPacket = DistributionGlobal.onFetchPacket(this.getUIEasingGroup(), pDecoupler);
		/* Allocate the Packets. */
		final UIElementPacket[] lPackets         = new UIElementPacket[lContacts.size()];
		/* Determine if the Decoupler possesses any Contacts at all. */
		if(!lContacts.isEmpty()) {
			/* Allocate some variables to track the MinimumX, MaximumX and MaximumY. */
			int lMinimumX = Integer.MAX_VALUE;
			int lMaximumX = Integer.MIN_VALUE;
			int lMaximumY = Integer.MIN_VALUE;
			/* Iterate the Contacts. */
			for(int i = 0; i < lContacts.size(); i++) {
				/* Fetch the Contact. */
				final T               lContact        = lContacts.get(i);
				/* Fetch the Contact's UIEasingPacket. */
				final UIElementPacket lUIEasingPacket = DistributionGlobal.onFetchPacket(this.getUIEasingGroup(), lContact);
				/* Buffer the UIEasingPacket. */
				                      lPackets[i]     = lUIEasingPacket;
				/* Supply the Margin's Minima to the UIEasingPacket. (This aids visual consistency for the Contacts; promotes alignment.) */
				MathUtils.onSupplyOffset(lUIEasingPacket, pDecoupler.getMargin().getMinimumX(), pDecoupler.getMargin().getMinimumY());
				/* Update the MinimumX. */
				lMinimumX = Math.min(lMinimumX, lUIEasingPacket.getX() - pDecoupler.getMargin().getMinimumX());
				/* Update the MaximumX and MaximumY. */
				lMaximumX = Math.max(lMaximumX, lUIEasingPacket.getX() + lUIEasingPacket.getWidth() + pDecoupler.getMargin().getMaximumX());
				lMaximumY = Math.max(lMaximumY, lUIEasingPacket.getY() + lUIEasingPacket.getHeight());
			}
			/* Next, coerce the MaximumY to fit the CODE_DIM_HEIGHT_UNIT. */
			lMaximumY = LexiconGlobal.onPredictY(lMaximumY);
			/* Assign the MaximumX and MaximumY to the Decoupler's dimensions. */
			MathUtils.setDimension(lUIElementPacket, (lMaximumX - lMinimumX), lMaximumY);
			/* Iterate the Contacts again; we'll want to correct their alignment. */
			for(int i = 0; i < lContacts.size(); i++) { 
				/* Fetch the Contact's UIEasingPacket. */
				final UIElementPacket lUIEasingPacket = lPackets[i];
				/* Force a right-hand alignment. */
				lUIEasingPacket.setX((lUIElementPacket.getWidth() - (lUIEasingPacket.getX() + lUIEasingPacket.getWidth())) + lMinimumX);
			}
		}
		else {
			/* Force the Decoupler to reflect the Minima. */
			MathUtils.setDimension(lUIElementPacket, pDecoupler.getMinima());
		}
		/* Break conventional analysis. */
		pCompilation.getInspectionStack().push(Boolean.FALSE);
	}
	/* Handle a Comment. */
	@Override public final void onCourierTransit(final Comment pComment, final Compilation pCompilation) {
		/* Fetch the Comment's UIElementPacket. */
		final UIElementPacket lUIElementPacket = DistributionGlobal.onFetchPacket(this.getUIEasingGroup(), pComment);
		/* Calculate the OffsetY required to allow the Comment to rest centrally to the CODE_DIM_HEIGHT_UNIT. */
		final int             lOffsetY         = ((LexiconGlobal.CODE_DIM_HEIGHT_UNIT - lUIElementPacket.getHeight()) >> 1);
		/* Apply the OffsetY. */
		lUIElementPacket.setY(lUIElementPacket.getY() + lOffsetY);
	}
	
	private final UIEasingGroup getUIEasingGroup() {
		return this.mUIEasingGroup;
	}
	
}