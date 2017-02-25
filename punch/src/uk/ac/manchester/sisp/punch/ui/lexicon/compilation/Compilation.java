package uk.ac.manchester.sisp.punch.ui.lexicon.compilation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.core.IGroup;
import uk.ac.manchester.sisp.punch.ui.lexicon.ILexical;
import uk.ac.manchester.sisp.punch.ui.lexicon.cascade.Cascade;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.Coupling;
import uk.ac.manchester.sisp.punch.ui.lexicon.courier.ILexiconCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.Decoupler;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.axiom.Axiom;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.axiom.Invocation;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.axiom.Virtual;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.directional.SinkDecoupler;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.directional.SourceDecoupler;
import uk.ac.manchester.sisp.punch.ui.lexicon.documentation.Comment;
import uk.ac.manchester.sisp.punch.ui.lexicon.global.LexiconGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.literal.ILiteral;
import uk.ac.manchester.sisp.punch.ui.lexicon.literal.array.Array;
import uk.ac.manchester.sisp.punch.ui.lexicon.literal.array.ArrayGroup;
import uk.ac.manchester.sisp.punch.ui.lexicon.literal.constants.Dharma;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.IContact;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.constants.Feedback;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.constants.Iteration;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.constants.Selector;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.constants.Tunnel;
import uk.ac.manchester.sisp.punch.ui.lexicon.sequencing.ISequential;
import uk.ac.manchester.sisp.ribbon.common.IVec2;
import uk.ac.manchester.sisp.ribbon.utils.DataUtils;
import uk.ac.manchester.sisp.ribbon.utils.MathUtils;

public final class Compilation implements ILexiconCourier<Interpreter>, IVec2.I.W { 
	
	/* Define the default Serialization UID. */
	private static final long serialVersionUID = 1L;
	
	/* Member Variables. */
	private final LinkedList<IVec2.I.W>        mOffsetStack;
	private final LinkedList<List<IUIElement>> mHierarchyStack;
	private final LinkedList<Boolean>          mInspectionStack;
	
	public Compilation() {
		/* Initialize Member Variables. */
		this.mOffsetStack      = new LinkedList<IVec2.I.W>       ();
		this.mHierarchyStack   = new LinkedList<List<IUIElement>>();
		this.mInspectionStack  = new LinkedList<Boolean>();
		/* Assert that by default, we want to inspect internals. */
		this.getInspectionStack().push(Boolean.TRUE);
	}
	
	@Override
	public final <V extends ILexical, T extends ILexical & IGroup<V>> void onCourierTransit(final Coupling<V, T> pCoupling, final Interpreter pInterpreter) { 
		/* Determine if the Coupling is a Declaration. */
		final boolean lIsDeclaration = pCoupling.isDeclaration(this.getHierarchy());
		/* Determine if the Coupling is Floating. */
		if(lIsDeclaration) { 
			/* Allocate a new Hierarchy. */
			final List<IUIElement> lHierarchy = new ArrayList<IUIElement>();
			/* Buffer the Hierarchy onto the HierarchyStack. */
			this.getHierarchyStack().push(lHierarchy);
			/* Buffer the Coupling onto the Hierarchy. */
			lHierarchy.add(pCoupling);
			/* Allocate a new LocalOffset. */
			final IVec2.I.W lLocalOffset      = new IVec2.I.Impl();
			/* Buffer the LocalOffset. */
			this.getOffsetStack().push(lLocalOffset);
			/* Define the Coupling's Position. */
			final IVec2.I   lPosition         = pInterpreter.onDefinePosition(pCoupling, this);
			/* Withdraw the Coupling's position. */
			MathUtils.onWithdrawOffset(lLocalOffset, lPosition);
			/* Force the Interpreter to recognize we're handling a top-level Coupling. */
			pInterpreter.onEnterDeclaration(pCoupling, this);
		}
		/* Make a local copy of the Coupling's Position. (Here we unwrap the standard processing methodology to force Coupling I/O.) */ /** TODO: Just sort the Collection? **/
		final IVec2.I lPosition = (pInterpreter.onDefinePosition(pCoupling, this));
		/* Supply the Coupling's Offset. */
		MathUtils.onSupplyOffset(this.getOffsetStack().peek(), lPosition);
		/* Synchronize along T. */
		synchronized(pCoupling) {
			/* Dispatch to the Decoupler to the Interpreter. */
			pCoupling.onCourierDispatch(pInterpreter, this);
			/* Allocate the Succession. */
			final List<ILexical> lSuccession = new ArrayList<ILexical>();
			/* Add the SinkDecoupler to the Succession. */
			lSuccession.add(pCoupling.getSinkDecoupler());
			/* Add the Internals to the Succession. */
			lSuccession.addAll(pCoupling.getInternals());
			/* Add the SourceDecoupler to the Succession. */
			lSuccession.add(pCoupling.getSourceDecoupler());
			/* Iterate the Succession, whilst we're permitted to process the Internals. */
			for(int i = 0; i < lSuccession.size() && this.isInspectInternals(); i++) {
				/* Implement a hierarchical dispatch. */
				this.onHierarchyDispatch(lSuccession.get(i), pInterpreter);
			}
		}
		/* Determine if we're not allowed to process Internals. */
		if(!this.isInspectInternals()) {
			/* Pop the InspectionStack. */
			this.getInspectionStack().pop();
		}
		/* Withdraw the Coupling's Offset. */
		MathUtils.onWithdrawOffset(this.getOffsetStack().peek(), lPosition);
		
		/* Check again if the Coupling is floating. */
		if(lIsDeclaration) {
			/* Destroy the Declaration. */
			pInterpreter.onLeaveDeclaration(pCoupling, this);
			/* Pop the entry off the OffsetStack. */
			this.getOffsetStack().pop();
			/* Pop the Hierarchy off the HierarchyStack. */
			this.getHierarchyStack().pop();
		}
	}
	
	@Override
	public final <T extends ILexical> void onCourierTransit(final ISequential<T> pSequence, final Interpreter pInterpreter) { 
		/* Process as a standard Enclosure, ensure the contents are sorted horizontally. */
		this.onProcessEnclosure(pSequence, pInterpreter);
	}
	
	@Override
	public final <T extends IContact<?>> void onCourierTransit(final Decoupler<T> pDecoupler, final Interpreter pInterpreter) {
		/* Process the Decoupler using Imbrication. */
		this.onProcessEnclosure(pDecoupler, pInterpreter);
	}
	
	@Override
	public final void onCourierTransit(final ArrayGroup pArrayGroup, final Interpreter pInterpreter) {
		/* Process the ArrayGroup. */
		this.onProcessEnclosure(pArrayGroup, pInterpreter);
	}
	
	/* Formalises the generic way in which we process a Group. */
	public final <U extends ILexical, T extends IGroup<U> & ILexical> void onProcessEnclosure(final T pT, final Interpreter pInterpreter) {
		/* Make a local copy of T's Position. */
		final IVec2.I lPosition = (pInterpreter.onDefinePosition(pT, this));
		/* Use the standard methodology. */
		this.onProcessEnclosure(pT, pInterpreter, lPosition);
	}
	
	/* Formalises the generic way in which we process a Group.  Allows a custom position specification. */
	public final <U extends ILexical, T extends IGroup<U> & ILexical> void onProcessEnclosure(final T pT, final Interpreter pInterpreter, final IVec2.I pPosition) {
		/* Supply the T's Offset. */
		MathUtils.onSupplyOffset(this.getOffsetStack().peek(), pPosition);
		/* Synchronize along T. */
		synchronized(pT) {
			/* Dispatch to the Decoupler to the Interpreter. */
			pT.onCourierDispatch(pInterpreter, this);
			/* Process the Internals. */
			this.onProcessInternals(pT, pT.getUIElements(), pInterpreter);
		}
		/* Determine if we're not allowed to process Internals. */
		if(!this.isInspectInternals()) {
			/* Pop the InspectionStack. */
			this.getInspectionStack().pop();
		}
		/* Withdraw T's Offset. */
		MathUtils.onWithdrawOffset(this.getOffsetStack().peek(), pPosition);
	}
	
	/* Define how we process a standard root-most Lexical component. */
	private final <U extends ILexical> void onProcessLexical(final U pU, final Interpreter pInterpreter) {
		/* Make a local copy of T's Position. */
		final IVec2.I lPosition = (pInterpreter.onDefinePosition(pU, this));
		/* Supply U's Offset. */
		MathUtils.onSupplyOffset(this.getOffsetStack().peek(), lPosition);
		/* Synchronize along U. */
		synchronized(pU) {
			/* Allow the Interpreter to handle U. */
			pU.onCourierDispatch(pInterpreter, this);
		}
		/* Withdraw U's Offset. */
		MathUtils.onWithdrawOffset(this.getOffsetStack().peek(), lPosition);
	}
	
	/* Defines the method for processing the internal contents of a Group. */
	public <U extends ILexical, T extends IGroup<U> & ILexical> void onProcessInternals(final T pT, final List<U> pLexicals, final Interpreter pInterpreter) {
		/* Make a safe copy of the Lexicals. */
		final List<U> lLexicals = new ArrayList<U>(pLexicals);
		/* Have the Interpreter define the method of ordering. */
		pInterpreter.onSortLexicals(pT, lLexicals);
		/* Iterate the Lexicals. */
		for(int i = 0; i < lLexicals.size() && this.isInspectInternals(); i++) {
			/* Fetch the Lexical. */
			final U lLexical = lLexicals.get(i);
			/* Implement a hierarchical dispatch. */
			this.onHierarchyDispatch(lLexical, pInterpreter);
		}
	}
	
	/* Manages a dispatch to a Lexical and whilst managing it's hierarchical context. */
	public final void onHierarchyDispatch(final ILexical pLexical, final Interpreter pInterpreter) {
		/* Add the Lexical to the Hierarchy. */
		this.getHierarchy().add(pLexical);
		/* Recursively dispatch the Compilation process the Contact. */
		pLexical.onCourierDispatch(this, pInterpreter);
		/* Remove the Lexical from the Hierarchy. */
		this.getHierarchy().remove(this.getHierarchy().size() - 1);
	}
	
	/* Generic Lexical Handling. (Non-Enclosure Types.) */
	@Override public final void onCourierTransit(final ILiteral<?> pLiteral, final Interpreter pInterpreter) { this.onProcessLexical(pLiteral, pInterpreter); }
	@Override public final void onCourierTransit(final IContact<?> pContact, final Interpreter pInterpreter) { this.onProcessLexical(pContact, pInterpreter); }
	@Override public final void onCourierTransit(final Comment     pComment, final Interpreter pInterpreter) { this.onProcessLexical(pComment, pInterpreter); }
	@Override public final void onCourierTransit(final Cascade     pCascade, final Interpreter pInterpreter) { this.onProcessLexical(pCascade, pInterpreter); }
	
	/* Decoupler Dispatch Routines. */
	@Override public final                         void onCourierTransit(final   SinkDecoupler   pSinkDecoupler, final Interpreter pInterpreter) { this.onCourierTransit((Decoupler<?>)  pSinkDecoupler, pInterpreter); }
	@Override public final                         void onCourierTransit(final SourceDecoupler pSourceDecoupler, final Interpreter pInterpreter) { this.onCourierTransit((Decoupler<?>)pSourceDecoupler, pInterpreter); }
	@Override public final <T extends IContact<?>> void onCourierTransit(final        Axiom<T>           pAxiom, final Interpreter pInterpreter) { this.onCourierTransit((Decoupler<?>)          pAxiom, pInterpreter); }
	
	/* Axiom Dispatch Routines. */
	@Override public final void onCourierTransit(final Invocation pInvocation, final Interpreter pInterpreter) { this.onCourierTransit((Axiom<?>) pInvocation, pInterpreter); }
	@Override public final void onCourierTransit(final    Virtual    pVirtual, final Interpreter pInterpreter) { this.onCourierTransit((Axiom<?>)    pVirtual, pInterpreter); }
	
	/* Literal Dispatch Routines. */
	@Override public final void onCourierTransit(final Dharma pDharma, final Interpreter pInterpreter) { this.onCourierTransit((ILiteral<?>)pDharma, pInterpreter); }
	
	/* Contact Dispatch Routines. */
	@Override public final void onCourierTransit(final IContact.Link      pLink, final Interpreter pInterpreter) { this.onCourierTransit((IContact<?>)pLink,      pInterpreter); }
	@Override public final void onCourierTransit(final     Iteration pIteration, final Interpreter pInterpreter) { this.onCourierTransit((IContact<?>)pIteration, pInterpreter); }
	@Override public final void onCourierTransit(final      Feedback  pFeedback, final Interpreter pInterpreter) { this.onCourierTransit((IContact<?>)pFeedback,  pInterpreter); }
	@Override public final void onCourierTransit(final      Selector  pSelector, final Interpreter pInterpreter) { this.onCourierTransit((IContact<?>)pSelector,  pInterpreter); }
	@Override public final void onCourierTransit(final        Tunnel    pTunnel, final Interpreter pInterpreter) { this.onCourierTransit((IContact<?>)pTunnel,    pInterpreter); }
	
	/* Coupling Dispatch Routines. */
	@Override public final void onCourierTransit(final     Array     pArray, final Interpreter pInterpreter) { this.onCourierTransit((Coupling<?, ?>)    pArray, pInterpreter); }
	
	/* Returns the AbsoluteIndex (relative to the master coupling) of the current LocalOffset. */
	public final Integer getAbsoluteIndex() {
		/* Convert the YPosition into a corresponding Index. */
		return this.getAbsoluteIndex(this.getLocalOffset());
	}
	
	/* Returns the AbsoluteIndex (relative to the master coupling) of the current LocalOffset. */
	public final Integer getAbsoluteIndex(final IVec2.I pLocalOffset) {
		/* Convert the YPosition into a corresponding Index. */
		return DataUtils.getCachedInteger(LexiconGlobal.onPredictIndex(pLocalOffset.getY()));
	}
	
	/* Allows the user to specify an array of children, and returns the corresponding Index of the last-most child as if it were nested by all UIElements before it. This calculation is made on top of the LocalIndex. */
	public final Integer getSubIndex(final Interpreter pInterpreter, final ILexical ... pLexicals) {
		/* Return the SubIndex using the LocalOffset. */
		return this.getSubIndex(this.getLocalOffset(), pInterpreter, pLexicals);
	}
	
	/* Allows the user to specify an array of children, and returns the corresponding Index of the last-most child as if it were nested by all UIElements before it. This calculation is made on top of the LocalIndex. */
	public final Integer getSubIndex(final IVec2.I.W pLocalOffset, final Interpreter pInterpreter, final ILexical ... pLexicals) {
		/* Calculate the SubPosition. */
		final IVec2.I lSubPosition = this.getSubPosition(pLocalOffset, pInterpreter, pLexicals);
		/* Return the correspdoning Index of the SubPosition. */
		return this.getAbsoluteIndex(lSubPosition);
	}
	
	/* Calculates the SubPosition of a nested component. */
	public final IVec2.I.W getSubPosition(final Interpreter pInterpreter, final IUIElement ... pUIElements) {
		/* Return the LocalOffset. */
		return this.getSubPosition(this.getLocalOffset(), pInterpreter, pUIElements);
	}
	
	/* Calculates the SubPosition of a nested component. */
	public final IVec2.I.W getSubPosition(final IVec2.I pLocalOffset, final Interpreter pInterpreter, final IUIElement ... pUIElements) {
		/* Allocate the LocalOffset. */
		final IVec2.I.W lLocalOffset = new IVec2.I.Impl(pLocalOffset);
		/* Iterate the Lexicals. */
		for(final IUIElement lUIElement : pUIElements) {
			/* Supply the Lexical's Postion to the OffsetStack. */
			MathUtils.onSupplyOffset(lLocalOffset, pInterpreter.onDefinePosition(lUIElement, this));
		}
		/* Return the LocalOffset. */
		return lLocalOffset;
	}
	
	/* Calculates the SuperPosition of a parent component. */
	public final IVec2.I.W getSuperPosition(final Interpreter pInterpreter, final IUIElement ... pUIElements) {
		/* Use the existing LocalOffset. */
		return this.getSuperPosition(this.getLocalOffset(), pInterpreter, pUIElements);
	}
	
	/* Calculates the SuperPosition of a parent component. */
	public final IVec2.I.W getSuperPosition(final IVec2.I pLocalOffset, final Interpreter pInterpreter, final IUIElement ... pUIElements) {
		/* Allocate the LocalOffset. */
		final IVec2.I.W lLocalOffset = new IVec2.I.Impl(pLocalOffset);
		/* Iterate the Lexicals. */
		for(final IUIElement lUIElement : pUIElements) {
			/* Supply the Lexical's Postion to the OffsetStack. */
			MathUtils.onWithdrawOffset(lLocalOffset, pInterpreter.onDefinePosition(lUIElement, this));
		}
		/* Return the LocalOffset. */
		return lLocalOffset;
	}
	
	/* Returns the LocalOffset indicated by the OffsetStack. */
	public final IVec2.I.W getLocalOffset() {
		/* Return the top-most entry on the OffsetStack. */
		return this.getOffsetStack().peek();
	}
	
	/* Fetches the current Hierarchy off the HierarchyStack. */
	public final List<IUIElement> getHierarchy() {
		/* Fetch the top-most element along the HierarchyStack. */
		return this.getHierarchyStack().peek();
	}
	
	/* Determines whether the Compilation is allowed to assess the Internal contents of a Group. */
	public final boolean isInspectInternals() {
		/* Return the top-most entry along the InspectionStack. */
		return this.getInspectionStack().peek();
	}
	
	/* Defines whether the GParser is operating within the outermost confines of the Declaration Coupling. */
	public final boolean isSubCall() {
		/* Fetch the owning Coupling. */
		final Coupling<?, ?> lCoupling  =  this.onFetchCoupling();
		/* Determine whether the Coupling is a declaration. */
		final boolean        lIsSubCall = DataUtils.isNotNull(lCoupling) ? !lCoupling.isDeclaration(this.getHierarchy()) : false;
		/* Return the metric. */
		return lIsSubCall;
	}
	
	/* Fetches the Parent Coupling of the current Compilation context. */
	public final Coupling<?, ?> onFetchCoupling() { 
		/* Reverse iterate the hierarchy. */
		for(int i = this.getHierarchy().size() - 1; i >= 0; i--) {
			/* Fetch the UIElement. */
			final IUIElement lUIElement = this.getHierarchy().get(i);
			/* Determine if we've found a Coupling. */
			if(lUIElement instanceof Coupling) { 
				/* Return the Coupling. */
				return (Coupling<?, ?>)lUIElement;
			}
		}
		/* A Coupling couldn't be found. */
		return null;
	}
	
	private final LinkedList<IVec2.I.W> getOffsetStack() {
		return this.mOffsetStack;
	}
	
	private final LinkedList<List<IUIElement>> getHierarchyStack() {
		return this.mHierarchyStack;
	}
	
	public final LinkedList<Boolean> getInspectionStack() {
		return this.mInspectionStack;
	}
	
	@Override
	public final void setX(int pX) {
		this.getLocalOffset().setX(pX);
	}

	@Override
	public final void setY(int pY) {
		this.getLocalOffset().setY(pY);
	}
	
	@Override public final int getX() { return this.getLocalOffset().getX(); }
	@Override public final int getY() { return this.getLocalOffset().getY(); }
	
}