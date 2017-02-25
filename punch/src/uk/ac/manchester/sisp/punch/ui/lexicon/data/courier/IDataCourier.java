package uk.ac.manchester.sisp.punch.ui.lexicon.data.courier;

import uk.ac.manchester.sisp.punch.ui.lexicon.data.IDataType.ArrayType;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.types.BooleanType;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.types.NumberType;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.types.NumberType.F32;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.types.NumberType.I32;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.types.VoidType;

public interface IDataCourier <U> {
	
	public static class Adapter<U> implements IDataCourier<U> { 
		/* Courier Entry Points. */
		@Override public void onCourierTransit(final      VoidType    pVoidType, final U pCourierPackage) { } 
		@Override public void onCourierTransit(final     ArrayType   pArrayType, final U pCourierPackage) { } 
		@Override public void onCourierTransit(final   BooleanType pBooleanType, final U pCourierPackage) { } 
		@Override public void onCourierTransit(final NumberType<?>  pNumberType, final U pCourierPackage) { } 
		/* Generic Number Dispatch. */
		@Override public void onCourierTransit(final           I32         pI32, final U pCourierPackage) { this.onCourierTransit((NumberType<?>) pI32, pCourierPackage); }
		@Override public void onCourierTransit(final           F32         pF32, final U pCourierPackage) { this.onCourierTransit((NumberType<?>) pF32, pCourierPackage); }
	};
	
	/* Courier Dispatch Implementations. */
	public static interface Dispatch { public abstract <T> void onCourierDispatch(final IDataCourier<T> pCourier, final T pCourierPackage); }
	
	/* Courier Entry Points. */
	public abstract void onCourierTransit(final      VoidType    pVoidType, final U pCourierPackage);
	public abstract void onCourierTransit(final     ArrayType   pArrayType, final U pCourierPackage);
	public abstract void onCourierTransit(final   BooleanType pBooleanType, final U pCourierPackage);
	public abstract void onCourierTransit(final NumberType<?>  pNumberType, final U pCourierPackage);
	public abstract void onCourierTransit(final           I32         pI32, final U pCourierPackage);
	public abstract void onCourierTransit(final           F32         pF32, final U pCourierPackage);
	
}