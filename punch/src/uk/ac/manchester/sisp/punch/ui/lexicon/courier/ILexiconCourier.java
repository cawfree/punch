package uk.ac.manchester.sisp.punch.ui.lexicon.courier;

import uk.ac.manchester.sisp.punch.ui.core.IGroup;
import uk.ac.manchester.sisp.punch.ui.lexicon.ILexical;
import uk.ac.manchester.sisp.punch.ui.lexicon.cascade.Cascade;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.Coupling;
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

public interface ILexiconCourier <U> {
	
	public static class Adapter<U> implements ILexiconCourier<U> {
		@Override public <T extends IContact<?>>                              void onCourierTransit(final         Axiom<T>           pAxiom, final U pCourierPackage) { this.onCourierTransit((Decoupler<?>)pAxiom,  pCourierPackage);          };
		@Override public                                                      void onCourierTransit(final       Invocation      pInvocation, final U pCourierPackage) { this.onCourierTransit((Axiom<?>)pInvocation, pCourierPackage);          };
		@Override public                                                      void onCourierTransit(final          Virtual         pVirtual, final U pCourierPackage) { this.onCourierTransit((Axiom<?>)pVirtual,    pCourierPackage);          };
		@Override public                                                      void onCourierTransit(final            Array           pArray, final U pCourierPackage) { this.onCourierTransit((Coupling<?,?>)pArray, pCourierPackage);          };
		@Override public                                                      void onCourierTransit(final           Dharma          pDharma, final U pCourierPackage) { this.onCourierTransit((ILiteral<?>)pDharma,  pCourierPackage);          };
		@Override public                                                      void onCourierTransit(final          Comment         pComment, final U pCourierPackage) {                                                                         };
		@Override public                                                      void onCourierTransit(final          Cascade         pCascade, final U pCourierPackage) {                                                                         };
		@Override public                                                      void onCourierTransit(final      ILiteral<?>         pLiteral, final U pCourierPackage) {                                                                         };
		@Override public                                                      void onCourierTransit(final      IContact<?>         pContact, final U pCourierPackage) {                                                                         };
		@Override public                                                      void onCourierTransit(final    IContact.Link            pLink, final U pCourierPackage) { this.onCourierTransit((IContact<?>)pLink,      pCourierPackage);        };
		@Override public                                                      void onCourierTransit(final        Iteration       pIteration, final U pCourierPackage) { this.onCourierTransit((IContact<?>)pIteration, pCourierPackage);        };
		@Override public                                                      void onCourierTransit(final         Feedback        pFeedback, final U pCourierPackage) { this.onCourierTransit((IContact<?>)pFeedback,  pCourierPackage);        };
		@Override public                                                      void onCourierTransit(final         Selector        pSelector, final U pCourierPackage) { this.onCourierTransit((IContact<?>)pSelector,  pCourierPackage);        };
		@Override public                                                      void onCourierTransit(final           Tunnel           Tunnel, final U pCourierPackage) { this.onCourierTransit((IContact<?>)Tunnel,     pCourierPackage);        };
		@Override public <V extends ILexical, T extends ILexical & IGroup<V>> void onCourierTransit(final   Coupling<V, T>        pCoupling, final U pCourierPackage) {                                                                         };
		@Override public <T extends ILexical>                                 void onCourierTransit(final   ISequential<T>      pSequential, final U pCourierPackage) {                                                                         };
		@Override public <T extends IContact<?>>                              void onCourierTransit(final     Decoupler<T>       pDecoupler, final U pCourierPackage) {                                                                         };
		@Override public                                                      void onCourierTransit(final    SinkDecoupler   pSinkDecoupler, final U pCourierPackage) { this.onCourierTransit((Decoupler<?>)  pSinkDecoupler, pCourierPackage); };
		@Override public                                                      void onCourierTransit(final  SourceDecoupler pSourceDecoupler, final U pCourierPackage) { this.onCourierTransit((Decoupler<?>)pSourceDecoupler, pCourierPackage); };
		@Override public                                                      void onCourierTransit(final       ArrayGroup      pArrayGroup, final U pCourierPackage) {                                                                         };
	};
	
	/* Courier Distpatch Implementations. */
	public static interface Dispatch { public abstract <T> void onCourierDispatch(final ILexiconCourier<T> pCourier, final T pCourierPackage); }

	/* Lexical Entry Points. */
	public abstract <T extends IContact<?>>                              void onCourierTransit(final         Axiom<T>           pAxiom, final U pCourierPackage);
	public abstract                                                      void onCourierTransit(final       Invocation      pInvocation, final U pCourierPackage);
	public abstract                                                      void onCourierTransit(final          Virtual         pVirtual, final U pCourierPackage);
	public abstract                                                      void onCourierTransit(final            Array           pArray, final U pCourierPackage);
	public abstract                                                      void onCourierTransit(final           Dharma          pDharma, final U pCourierPackage);
	public abstract                                                      void onCourierTransit(final          Comment         pComment, final U pCourierPackage);
	public abstract                                                      void onCourierTransit(final          Cascade         pCascade, final U pCourierPackage);
	public abstract                                                      void onCourierTransit(final           Tunnel          pTunnel, final U pCourierPackage);
	public abstract                                                      void onCourierTransit(final      ILiteral<?>         pLiteral, final U pCourierPackage);
	public abstract                                                      void onCourierTransit(final      IContact<?>         pContact, final U pCourierPackage);
	public abstract                                                      void onCourierTransit(final        Iteration       pIteration, final U pCourierPackage);
	public abstract                                                      void onCourierTransit(final         Feedback        pFeedback, final U pCourierPackage);
	public abstract                                                      void onCourierTransit(final         Selector        pSelector, final U pCourierPackage);
	public abstract                                                      void onCourierTransit(final    IContact.Link            pLink, final U pCourierPackage);
	public abstract <V extends ILexical, T extends ILexical & IGroup<V>> void onCourierTransit(final   Coupling<V, T>        pCoupling, final U pCourierPackage);
	public abstract <T extends ILexical>                                 void onCourierTransit(final   ISequential<T>      pSequential, final U pCourierPackage);
	public abstract <T extends IContact<?>>                              void onCourierTransit(final     Decoupler<T>       pDecoupler, final U pCourierPackage);
	public abstract                                                      void onCourierTransit(final    SinkDecoupler   pSinkDecoupler, final U pCourierPackage);
	public abstract                                                      void onCourierTransit(final  SourceDecoupler pSourceDecoupler, final U pCourierPackage);
	public abstract                                                      void onCourierTransit(final       ArrayGroup      pArrayGroup, final U pCourierPackage);
	
}