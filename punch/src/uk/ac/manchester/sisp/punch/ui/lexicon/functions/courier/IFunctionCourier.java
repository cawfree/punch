package uk.ac.manchester.sisp.punch.ui.lexicon.functions.courier;

import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.axiom.Invocation;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.axiom.Virtual;
import uk.ac.manchester.sisp.punch.ui.lexicon.functions.Elapsed;
import uk.ac.manchester.sisp.punch.ui.lexicon.functions.Nand;
import uk.ac.manchester.sisp.punch.ui.lexicon.functions.Print;

/* A Courier used for defining the functionality of specific Axiomatic constructs within the Punch Programming Language. */
public interface IFunctionCourier <U> {
	
	/* Adapter class for lossy implementations. */
	public static class Adapter<U> implements IFunctionCourier<U> { 
		@Override public void onCourierTransit(final Invocation pInvocation, final U pCourierPackage) { }
		@Override public void onCourierTransit(final    Virtual    pVirtual, final U pCourierPackage) { }
		@Override public void onCourierTransit(final    Elapsed    pElapsed, final U pCourierPackage) { }
		@Override public void onCourierTransit(final      Print      pPrint, final U pCourierPackage) { }
		@Override public void onCourierTransit(final       Nand       pNand, final U pCourierPackage) { }
	};
	
	/* Courier Distpatch Implementations. */
	public static interface Dispatch { public abstract <T> void onCourierDispatch(final IFunctionCourier<T> pCourier, final T pCourierPackage); }
	
	/* Courier Entry Points. */
	public abstract void onCourierTransit(final Invocation pInvocation, final U pCourierPackage);
	public abstract void onCourierTransit(final    Virtual    pVirtual, final U pCourierPackage);
	public abstract void onCourierTransit(final    Elapsed    pElapsed, final U pCourierPackage);
	public abstract void onCourierTransit(final      Print      pPrint, final U pCourierPackage);
	public abstract void onCourierTransit(final       Nand       pNand, final U pCourierPackage);
	
}