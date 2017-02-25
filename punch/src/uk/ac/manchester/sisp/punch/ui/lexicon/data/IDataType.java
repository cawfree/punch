package uk.ac.manchester.sisp.punch.ui.lexicon.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import uk.ac.manchester.sisp.punch.ui.lexicon.data.courier.IDataCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.IValue;
import uk.ac.manchester.sisp.punch.ui.lexicon.referencing.IReferential;
import uk.ac.manchester.sisp.ribbon.common.IColorRGBA;

public interface IDataType<T> extends IColorRGBA, Serializable, IReferential<IDataType<?>>, IDataCourier.Dispatch, IValue.Default<T> { 
	
	/* ArrayType Definition. */
	public static final class ArrayType implements IDataType<List<?>> { 
		
		/* Define the default Serialization UID. */
		private static final long serialVersionUID = 1L;
		
		/* Member Variables.. */
		private final IDataType<?> mReference;
		
		public ArrayType(final IDataType<?> pReference) { 
			/* Initialize Member Variables. */
			this.mReference = pReference;
		}
		
		/* Generic Courier Dispatch. */
		@Override public final <T> void onCourierDispatch(final IDataCourier<T> pDataCourier, final T pCourierPackage) { pDataCourier.onCourierTransit(this, pCourierPackage); }
		
		@Override public final float[]      getColor()     { return this.getReference().getColor();         }
		@Override public       int          getDimension() { return this.getReference().getDimension() + 1; }
		@Override public final IDataType<?> getReference() { return this.mReference;                        }
		@Override public final List<?>      getDefault()   { return new ArrayList<Object>();                }
		
	};
	
	/* Returns the Dimension of the DataConduit. */
	public abstract int    getDimension();
	
}