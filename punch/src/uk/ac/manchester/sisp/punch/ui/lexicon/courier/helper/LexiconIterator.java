package uk.ac.manchester.sisp.punch.ui.lexicon.courier.helper;

import java.util.List;

import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.collision.RayCastManager;
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
import uk.ac.manchester.sisp.ribbon.utils.DataUtils;

public class LexiconIterator<U> implements ILexiconCourier<U> {
	
	/* Member Variables. */
	private final List<IUIElement> mHierarchy;
	
	public LexiconIterator(final List<IUIElement> pHierarchy) {
		/* Initialize Member Variables. */
		this.mHierarchy = pHierarchy;
	}

	/* Overridable iteration steps. By default, we reverse iterate through the hierarchy to the root-most parent. */
	@Override public                                                      void onCourierTransit(final          Virtual         pVirtual, final U pCourierPackage) { this.onServeParent(pVirtual,         pCourierPackage); }
	@Override public                                                      void onCourierTransit(final       Invocation      pInvocation, final U pCourierPackage) { this.onServeParent(pInvocation,      pCourierPackage); }
	@Override public <T extends IContact<?>>                              void onCourierTransit(final         Axiom<T>           pAxiom, final U pCourierPackage) { this.onServeParent(pAxiom,           pCourierPackage); }
	@Override public                                                      void onCourierTransit(final            Array           pArray, final U pCourierPackage) { this.onServeParent(pArray,           pCourierPackage); }
	@Override public                                                      void onCourierTransit(final           Dharma          pDharma, final U pCourierPackage) { this.onServeParent(pDharma,          pCourierPackage); }
	@Override public                                                      void onCourierTransit(final      IContact<?>         pContact, final U pCourierPackage) { this.onServeParent(pContact,         pCourierPackage); }
	@Override public                                                      void onCourierTransit(final    IContact.Link            pLink, final U pCourierPackage) { this.onServeParent(pLink,            pCourierPackage); }
	@Override public                                                      void onCourierTransit(final      ILiteral<?>         pLiteral, final U pCourierPackage) { this.onServeParent(pLiteral,         pCourierPackage); }
	@Override public                                                      void onCourierTransit(final        Iteration       pIteration, final U pCourierPackage) { this.onServeParent(pIteration,       pCourierPackage); }
	@Override public                                                      void onCourierTransit(final         Feedback        pFeedback, final U pCourierPackage) { this.onServeParent(pFeedback,        pCourierPackage); }
	@Override public                                                      void onCourierTransit(final         Selector        pSelector, final U pCourierPackage) { this.onServeParent(pSelector,        pCourierPackage); }
	@Override public                                                      void onCourierTransit(final           Tunnel          pTunnel, final U pCourierPackage) { this.onServeParent(pTunnel,          pCourierPackage); }
	@Override public                                                      void onCourierTransit(final          Comment         pComment, final U pCourierPackage) { this.onServeParent(pComment,         pCourierPackage); }
	@Override public                                                      void onCourierTransit(final          Cascade         pCascade, final U pCourierPackage) { this.onServeParent(pCascade,         pCourierPackage); }
	@Override public <V extends ILexical, T extends ILexical & IGroup<V>> void onCourierTransit(final    Coupling<V,T>        pCoupling, final U pCourierPackage) { this.onServeParent(pCoupling,        pCourierPackage); }
	@Override public <T extends ILexical>                                 void onCourierTransit(final   ISequential<T>      pSequential, final U pCourierPackage) { this.onServeParent(pSequential,      pCourierPackage); }
	@Override public <T extends IContact<?>>                              void onCourierTransit(final     Decoupler<T>       pDecoupler, final U pCourierPackage) { this.onServeParent(pDecoupler,       pCourierPackage); }
	@Override public                                                      void onCourierTransit(final    SinkDecoupler   pSinkDecoupler, final U pCourierPackage) { this.onServeParent(pSinkDecoupler,   pCourierPackage); }
	@Override public                                                      void onCourierTransit(final  SourceDecoupler pSourceDecoupler, final U pCourierPackage) { this.onServeParent(pSourceDecoupler, pCourierPackage); }
	@Override public                                                      void onCourierTransit(final       ArrayGroup      pArrayGroup, final U pCourierPackage) { this.onServeParent(pArrayGroup,      pCourierPackage); }
	
	/* Dispatches to the parent of the child UIElement, as indicated by the hierarchy. */
	public void onServeParent(final IUIElement pChild, final U pU) {
		/* Fetch the Parent. */
		final IUIElement lParent = RayCastManager.onFetchParent(this.getHierarchy(), pChild);
		/* Ensure we're handling a valid Parent. */
		if(DataUtils.isNotNull(lParent) && lParent instanceof ILexical) { /** TODO: Very poor architecture. Improve! **/
			/* Export the UIAdapter. */
			((ILexical)lParent).onCourierDispatch(this, pU);
		}
	}
	
	private final List<IUIElement> getHierarchy() {
		return this.mHierarchy;
	}
	
}