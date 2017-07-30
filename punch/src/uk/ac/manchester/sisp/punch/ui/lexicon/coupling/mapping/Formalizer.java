package uk.ac.manchester.sisp.punch.ui.lexicon.coupling.mapping;

import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.distribution.global.DistributionGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.compilation.Compilation;
import uk.ac.manchester.sisp.punch.ui.lexicon.compilation.Interpreter;
import uk.ac.manchester.sisp.punch.ui.lexicon.literal.array.ArrayGroup;
import uk.ac.manchester.sisp.punch.ui.update.easing.ui.UIEasingGroup;
import uk.ac.manchester.sisp.punch.ui.update.easing.ui.UIElementPacket;
import uk.ac.manchester.sisp.ribbon.common.IVec2.I;
import uk.ac.manchester.sisp.ribbon.utils.MathUtils;

/* At this stage, UIElementPackets are specified using AbsoluteY co-ordinates. These need to be refactored to match their parent containers. */
public final class Formalizer extends Interpreter { 
	
	/* Member Variables. */
	private final UIEasingGroup mUIEasingGroup;
	
	public Formalizer(final UIEasingGroup pUIEasingGroup) {
		/* Initialize Member Variables. */
		this.mUIEasingGroup = pUIEasingGroup;
	}
	
	@Override public final void onCourierTransit(final ArrayGroup pArrayGroup, final Compilation pCompilation) {
		/* Don't permit the contents of the ArrayGroup to be formalized. */
		pCompilation.getInspectionStack().push(Boolean.FALSE);
	}

	@SuppressWarnings("unchecked") @Override public final <T extends I & uk.ac.manchester.sisp.ribbon.common.IDim2.I> T onDefinePosition(final IUIElement pUIElement, final Compilation pCompilation) {
		/* Fetch the UIElementPacket. */
		final UIElementPacket lUIElementPacket = DistributionGlobal.onFetchPacket(this.getUIEasingGroup(), pUIElement);
		/* Withdraw the LocalOffset from the UIElementPacket. */
		MathUtils.onWithdrawOffset(lUIElementPacket, pCompilation.getLocalOffset());
		/* Return the modulated UIElementPacket. */
		return (T)lUIElementPacket;
	}

	private final UIEasingGroup getUIEasingGroup() {
		return this.mUIEasingGroup;
	}
	
}