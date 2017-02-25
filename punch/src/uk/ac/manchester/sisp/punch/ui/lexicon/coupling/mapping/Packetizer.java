package uk.ac.manchester.sisp.punch.ui.lexicon.coupling.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.manchester.sisp.punch.ui.distribution.global.DistributionGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.ILexical;
import uk.ac.manchester.sisp.punch.ui.lexicon.compilation.Compilation;
import uk.ac.manchester.sisp.punch.ui.lexicon.compilation.Interpreter;
import uk.ac.manchester.sisp.punch.ui.lexicon.compilation.global.ISensitive;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.EDataDirection;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.Decoupler;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.axiom.Axiom;
import uk.ac.manchester.sisp.punch.ui.lexicon.global.LexiconGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.literal.ILiteral;
import uk.ac.manchester.sisp.punch.ui.lexicon.literal.array.ArrayGroup;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.IParameter;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.IContact;
import uk.ac.manchester.sisp.punch.ui.update.easing.ui.UIEasingGroup;
import uk.ac.manchester.sisp.punch.ui.update.easing.ui.UIElementPacket;
import uk.ac.manchester.sisp.ribbon.common.IVec2;
import uk.ac.manchester.sisp.ribbon.utils.DataUtils;
import uk.ac.manchester.sisp.ribbon.utils.MathUtils;

/* Any components subject to Ripple should be placed here, and added to the sensitivity list. */
public final class Packetizer extends Interpreter implements ISensitive<ILexical> {
	
	/* Member Variables. */
	private final UIEasingGroup  mUIEasingGroup;
	private       int            mDriverLength;
	private final List<Integer>  mRippleList;
	private final List<ILexical> mSensitivies;
	
	public Packetizer(final UIEasingGroup pUIEasingGroup) {
		/* Initialize Member Variables. */
		this.mUIEasingGroup = pUIEasingGroup;
		this.mDriverLength  = DataUtils.JAVA_NULL_INDEX;
		this.mRippleList    = new ArrayList<Integer>();
		this.mSensitivies   = new ArrayList<ILexical>();
	}
	
	@Override
	public final void onCourierTransit(final ArrayGroup pArrayGroup, final Compilation pCompilation) {
		/* Disable default processing. */
		pCompilation.getInspectionStack().push(Boolean.FALSE);
	}

	@Override
	public final void onCourierTransit(final ILiteral<?> pLiteral, final Compilation pCompilation) { 
		/* Allocate an Offset for the Contact. */
		final UIElementPacket lUIElementPacket = DistributionGlobal.onFetchPacket(this.getUIEasingGroup(), pLiteral);
		/* Allow the Index to be compensated for Ripples. */
		final Integer         lRippledIndex    = this.onCompensateRipples(pCompilation.getAbsoluteIndex());
		/* Set the UIElementPacket's Index. */
		lUIElementPacket.setY(LexiconGlobal.CODE_DIM_HEIGHT_UNIT * lRippledIndex);
		/* Buffer as a sensitivity. */
		this.getSensitivies().add(pLiteral);
	}
	
	@Override
	public <T extends IContact<?>> void onCourierTransit(final Decoupler<T> pDecoupler, final Compilation pCompilation) { 
		/* Fetch the Decoupler's Contacts. */
		final List<T>           lContacts = pDecoupler.getUIElements();
		/* Allocate the Packets. */
		final UIElementPacket[] lPackets  = new UIElementPacket[lContacts.size()];
		/* Iterate the Contacts. */
		for(int i = 0; i < lContacts.size(); i++) {
			/* Fetch the Contact. */
			final T               lContact             = lContacts.get(i);
			/* Allow the Index to be compensated for Ripples. */
			final Integer         lRippledIndex        = this.onCompensateRipples(pCompilation.getSubIndex(this, lContact));
			/* Calculate the YPosition. */
			final int             lYPosition           = LexiconGlobal.CODE_DIM_HEIGHT_UNIT * lRippledIndex;
			/* Next, we'll allocate a List of Imbricates. */
			final List<T>   lImbricates      = new ArrayList<T>(0);
			/* Iterate the previous Contacts. */
			for(int j = 0; j < i; j++) {
				/* Fetch the potential Imbricate. */
				final T   lImbricate     = lContacts.get(j);
				/* Fetch the ImbricateOffset. */
				final IVec2.I.W lImbricateOffset = lPackets[j];
				/* Determine whether the Contact is Imbricate. */
				if(lImbricateOffset.getY() == lYPosition) {
					/* Buffer the Imbricate. */
					lImbricates.add(lImbricate);
				}
			}
			/* Allocate an Offset for the Contact. */
			final UIElementPacket lUIElementPacket = DistributionGlobal.onFetchPacket(this.getUIEasingGroup(), lContact);
			/* Initialize the Contact's Location. */
			MathUtils.setPosition(lUIElementPacket, 0, lYPosition);
			/* Buffer the Packet. */
			lPackets[i] = lUIElementPacket;
			/* Next, determine if we've found any imbricates. */
			if(!lImbricates.isEmpty()) { /** TODO: How to ensre if suppoted. **/
				/* Handle the Contact as an additional Imbricate. */
				lImbricates.add(lContact);
				/* Sort the Imbricates. */
				Collections.sort(lImbricates, pDecoupler.getComparator());
				/* Allocate a count for the DriverLength for the Imbricates. (Number of Sourcing Contacts.) */
				      int     lDriverLength = 0;
				/* Allocate a boolean for tracking whether Imbrication is supported. */
				      boolean lIsSupported  = pDecoupler.getDataDirection().equals(EDataDirection.BIDIRECTIONAL);
				/* Iterate the Imbricates. */
				for(int j = lImbricates.size() - 1; j >= 0; j--) { /** TODO: Must update lIsSupported. **/
					/* Update the DriverLength. */
					lDriverLength += DataUtils.booleanToInt(lImbricates.get(j).getDataDirection().equals(EDataDirection.SOURCE)) ;
				}
				/* Update the MaximumDriverLength. */
				this.setDriverLength(Math.max(this.getDriverLength(), lDriverLength));
				/* Determine if we're handling a Source Contact. */
				if(lContact.getDataDirection().equals(EDataDirection.SOURCE)) {
					/* Ensure we don't exceed the driver limit. */
					lIsSupported &= (lDriverLength <= LexiconGlobal.CODE_MAX_DRIVERS);
				}
				/* Handle the Imbrication metric. */
				if(lIsSupported) {
					/* Iterate the Imbricates. (Here we align right-to-left.) */
					for(int j = lImbricates.size() - 1; j >= 0; j--) {
						/* Calculate the delta. */
						final int lDelta = (lImbricates.size() - 1) - j;
						/* Set the Imbricate's predicted XPosition. */
						lPackets[lContacts.indexOf(lImbricates.get(j))].setX(lDelta * IParameter.DIM);
					}
				}
				else { /** TODO: This expects just Contacts during the Ripple!!! **/
					/* Insert a ripple for this index. */
					this.onInsertRipple(lRippledIndex);
				}
			}
			/* Register the Contact as a Sensitivity now that it has been placed. */
			this.getSensitivies().add(lContact);
		}
		/* Prevent standard analysis. */
		pCompilation.getInspectionStack().push(Boolean.FALSE);
	}
	
	@Override
	public final <T extends IContact<?>> void onCourierTransit(final Axiom<T> pAxiom, final Compilation pCompilation) { 
		/* Handle as an ordinary Decoupler. */
		this.onCourierTransit((Decoupler<?>)pAxiom, pCompilation);
		/* Fetch the Contacts. */
		final List<T> lContacts = pAxiom.getUIElements();
		/* Define the ContactMap. */
		final Map<Integer, List<T>> lContactMap = new HashMap<Integer, List<T>>();
		/* Iterate the Contacts. */
		for(final T lContact : lContacts) {
			/* Fetch the Contact's Index. */
			final Integer       lIndex = DataUtils.getCachedInteger(DistributionGlobal.onFetchPacket(this.getUIEasingGroup(), lContact).getY() / LexiconGlobal.CODE_DIM_HEIGHT_UNIT);
			/* Fetch the corresponding ContactRank. */
			      List<T> lContactRank = lContactMap.get(lIndex);
			/* Determine if the ContactRank doesn't exist. */
			if(DataUtils.isNull(lContactRank)) {
				/* Create the new ContactRank. */
				lContactRank = new ArrayList<T>();
				/* Buffer the ContactRank into the ContactMap. */
				lContactMap.put(lIndex, lContactRank);
			}
			/* Push the Contact into the ContactRank. */
			lContactRank.add(lContact);
		}
		/* Iterate the ContactMap. */
		for(final Entry<Integer, List<T>> lEntrySet : lContactMap.entrySet()) {
			/* Fetch the ContactRank. */
			final List<T> lContactRank     = lEntrySet.getValue();
			/* Allocate a boolean to count the NumberOfDrivers. */
			      int           lNumberOfDrivers = 0;
			/* Iterate the ContactRank. */
			for(final T lContact : lContactRank) {
				/* Update the NumberOfDrivers. */
				lNumberOfDrivers += DataUtils.booleanToInt(lContact.getDataDirection().equals(EDataDirection.SOURCE));
			}
			/* Calculate the Difference between this row's NumberOfDrivers and the MaximumDriverLength. */
			final int lDifference = this.getDriverLength() - lNumberOfDrivers;
			/* Iterate the Contacts. */
			for(final IContact<?> lContact : lContactRank) {
				/* Supply a horizontal offset proportional to the Difference. */
				MathUtils.onSupplyOffset(DistributionGlobal.onFetchPacket(this.getUIEasingGroup(), lContact), lDifference * IParameter.DIM, 0);
			}
		}
	}

	/* Increases an AbsoluteIndex to account for conflicts with existing RippleIndices. */
	private final Integer onCompensateRipples(int pAbsoluteIndex) {
		/* Iterate the RippleList. */
		for(final Integer lRipple : this.getRippleList()) {
			/* Update the Index. */
			pAbsoluteIndex += DataUtils.booleanToInt(pAbsoluteIndex >= lRipple);
		}
		/* Return the Index. */
		return DataUtils.getCachedInteger(pAbsoluteIndex);
	}
	
	/* Inserts a Ripple into the RippleList and redistributes conflicting indices. */
	private final void onInsertRipple(final Integer pAbsoluteIndex) {
		/* Insert a Ripple at this index. */
		this.getRippleList().add(pAbsoluteIndex);
		/* Sort the RippleList to ensure ascending Ripples. */
		Collections.sort(this.getRippleList());
		/* Force the Layout to Ripple to compensate for the additional vertex. */
		this.onRippleLayout(pAbsoluteIndex);
	}
	
	/* Updates existing entries to account for Ripple. */
	private final void onRippleLayout(final Integer pRippleIndex) {
		/* Iterate the Sensitivity List. */
		for(final ILexical lLexical : this.getSensitivies()) {
			/* Fetch the UIElementPacket. */
			final UIElementPacket lUIElementPacket = DistributionGlobal.onFetchPacket(this.getUIEasingGroup(), lLexical);
			/* Fetch the Index. */
			final Integer         lIndex           = DataUtils.getCachedInteger(lUIElementPacket.getY() / LexiconGlobal.CODE_DIM_HEIGHT_UNIT);
			/* Update the Index. */
			lUIElementPacket.setY(lUIElementPacket.getY() + (DataUtils.booleanToInt(lIndex >= pRippleIndex)) * LexiconGlobal.CODE_DIM_HEIGHT_UNIT);
		}
	}
	
	private final void setDriverLength(final int pDriverLength) {
		this.mDriverLength = pDriverLength;
	}
	
	private final int getDriverLength() {
		return this.mDriverLength;
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