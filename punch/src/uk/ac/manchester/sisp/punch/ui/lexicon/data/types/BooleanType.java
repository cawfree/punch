package uk.ac.manchester.sisp.punch.ui.lexicon.data.types;

import uk.ac.manchester.sisp.punch.ui.color.global.ColorGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.IDataType;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.courier.IDataCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.DataGlobal;

public final class BooleanType implements IDataType<Boolean> { 
	
	/* Define the default Serialization UID. */
	private static final long serialVersionUID = 1L;
	
	/* Generic Courier Dispatch. */
	@Override public final <T> void onCourierDispatch(final IDataCourier<T> pDataCourier, final T pCourierPackage) { pDataCourier.onCourierTransit(this, pCourierPackage); }
	
	/* Assert Type Definitions. */
	@Override public final   float[]                getColor() { return ColorGlobal.RGBA_DATA_BOOLEAN; }
	@Override public final   int                getDimension() { return DataGlobal.DIMENSION_SCALAR;   }
	@Override public final   IDataType<Boolean> getReference() { return this;                          }
	@Override public Boolean                      getDefault() { return Boolean.FALSE;                 }
	
}