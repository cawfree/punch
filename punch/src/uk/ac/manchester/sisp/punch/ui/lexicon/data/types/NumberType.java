package uk.ac.manchester.sisp.punch.ui.lexicon.data.types;

import uk.ac.manchester.sisp.punch.ui.color.global.ColorGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.IDataType;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.courier.IDataCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.DataGlobal;
import uk.ac.manchester.sisp.ribbon.utils.DataUtils;

public abstract class NumberType<U> implements IDataType<U> { 
	
	/* Define the default Serialization UID. */
	private static final long serialVersionUID = 1L;
	
	/* 32-bit Signed Integer. (2's Complement.) */
	public static final class I32 extends NumberType<Integer> { 
		
		/* Define the default Serialization UID. */
		private static final long serialVersionUID = 1L;
		
		/* Generic Courier Dispatch. */
		@Override public final <T> void onCourierDispatch(final IDataCourier<T> pDataCourier, final T pCourierPackage) { pDataCourier.onCourierTransit(this, pCourierPackage); }
		
		/* Assert Type Definitions. */
		@Override public final float[]        getColor() { return ColorGlobal.RGBA_DATA_I32;     }
		@Override public final Integer      getDefault() { return DataUtils.getCachedInteger(0); }
		@Override public final int     getNumberOfBits() { return Integer.SIZE;                  }
		
	}
	
	/* 32-bit Float. */
	public static final class F32 extends NumberType<Float> { 
		
		/* Define the default Serialization UID. */
		private static final long serialVersionUID = 1L;
		
		/* Generic Courier Dispatch. */
		@Override public final <T> void onCourierDispatch(final IDataCourier<T> pDataCourier, final T pCourierPackage) { pDataCourier.onCourierTransit(this, pCourierPackage); }
		
		/* Assert Type Definitions. */
		@Override public final float[]        getColor() { return ColorGlobal.RGBA_DATA_FLOAT; }
		@Override public final Float        getDefault() { return new Float(0);                }
		@Override public final int     getNumberOfBits() { return Float.SIZE;                  }
		
	}
	
	/* Generic Courier Dispatch. */
	@Override public <T> void onCourierDispatch(final IDataCourier<T> pDataCourier, final T pCourierPackage) { pDataCourier.onCourierTransit(this, pCourierPackage); }
	
	/* Type width specifiers. */
	          public abstract int          getNumberOfBits();
	/* Static Scalar Overrides. */
	@Override public final    int             getDimension() { return DataGlobal.DIMENSION_SCALAR; }
	@Override public final    IDataType<U>    getReference() { return this;                        }
	
}