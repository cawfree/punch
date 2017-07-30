package uk.ac.manchester.sisp.punch.ui.lexicon.compilation;

import java.util.Collections;
import java.util.List;

import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.core.IGroup;
import uk.ac.manchester.sisp.punch.ui.lexicon.ILexical;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.Coupling;
import uk.ac.manchester.sisp.punch.ui.lexicon.courier.ILexiconCourier;
import uk.ac.manchester.sisp.ribbon.common.IDim2;
import uk.ac.manchester.sisp.ribbon.common.IDisposable;
import uk.ac.manchester.sisp.ribbon.common.IVec2;

public abstract class Interpreter extends ILexiconCourier.Adapter<Compilation> implements IDisposable {
	
	/* Member Variables. */
	
	public Interpreter() {
		/* Initialize Member Variables. */
	}
	
	/* Used to define the position by which to associate with the Lexical. */
	@SuppressWarnings("unchecked") public <T extends IVec2.I & IDim2.I> T onDefinePosition(final IUIElement pUIElement, final Compilation pCompilation) {
		/* By default, we'll return the Lexical's current position. */
		return (T)pUIElement;
	}
	
	/* Defines the mechanism by which the internal UIElements are sorted. */
	public <U extends ILexical, T extends IGroup<U> & ILexical> void onSortLexicals(final T pT, final List<U> pLexicals) {
		/* By default, use the standard mechanism supplied by the Group. */
		Collections.sort(pLexicals, pT.getComparator());
	}

	@Override
	public final void dispose() {
		
	}
	
	/* Called when processing a floating Coupling. */
	public void onEnterDeclaration(final Coupling<?, ?> pCoupling, final Compilation pCompilation) { }
	public void onLeaveDeclaration(final Coupling<?, ?> pCoupling, final Compilation pCompilation) { }
	
}