package uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.axiom;

import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.Coupling;
import uk.ac.manchester.sisp.punch.ui.lexicon.courier.ILexiconCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.functions.courier.IFunctionCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.IContact;
import uk.ac.manchester.sisp.punch.ui.lexicon.referencing.IReferential;

/* Define the Invocation; this is a reference to an existing graphical Coupling. */
public final class Invocation extends Axiom<IContact.Link> implements IReferential<Coupling<?, ?>> {
		
	/* Define the default Serialization UID. */
	private static final long serialVersionUID = 1L;
	
	/* Member Variables. */
	private final Coupling<?, ?> mReference;
	
	public Invocation(int pX, int pY, final Coupling<?, ?> pCoupling) {
		super(pX, pY);
		/* Initialize the Reference. */
		this.mReference = pCoupling;
	}
	
	/* Courier Dispatch Operations. */
	@Override public final <T> void onCourierDispatch(final IFunctionCourier<T> pFunctionCourier, final T pCourierPackage) { pFunctionCourier.onCourierTransit(this, pCourierPackage); }
	@Override public final <T> void onCourierDispatch(final  ILexiconCourier<T>  pLexiconCourier, final T pCourierPackage) {  pLexiconCourier.onCourierTransit(this, pCourierPackage); }
	
	/* Return the reference to the Coupling. */
	@Override public final Coupling<?, ?> getReference() { return this.mReference; }
	
}