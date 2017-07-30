package uk.ac.manchester.sisp.punch.ui.lexicon.data.global;

import uk.ac.manchester.sisp.punch.ui.lexicon.data.IDataType;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.types.BooleanType;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.types.NumberType.F32;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.types.NumberType.I32;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.types.VoidType;

public final class DataGlobal { 
	
	/* Dimension Constants. */
	public static final int       DIMENSION_SCALAR = 0;
	
	/* Primitive Type Constants. */
	public static final IDataType<Boolean> TYPE_DHARMA = new BooleanType();
	public static final IDataType<Void>    TYPE_VOID   = new VoidType();
	public static final IDataType<Integer> TYPE_I32    = new I32();
	public static final IDataType<Float>   TYPE_F32    = new F32();
	
	/* Determines if two DataConduits are compatible with one another. */
	public static final boolean isCompatible(IDataType<?> pA, IDataType<?> pB) { 
		/* First, ensure the data types have the same dimension. */
		if(pA.getDimension() == pB.getDimension()) {
			/* Iterate the dimensions to fetch the Last-Most DataType. */
			for(int i =  pA.getDimension() - 1; i >= 0; i--) {
				/* Update the DataType References. */
				pA = pA.getReference();
				pB = pB.getReference();
			}
			/* Confirm the classes are matching. */
			return pA.getClass().equals(pB.getClass());
		}
		/* Assert that the DataTypes are incompatible. */
		return false;
		
	}
	
	/* Returns the core reference pointed to by a DataType. */
	public static final IDataType<?> getCoreReference(IDataType<?> pDataType) {
		/* Iterate the dimensions to fetch the Last-Most DataType. */
		for(int i =  pDataType.getDimension() - 1; i >= 0; i--) {
			/* Update the DataType References. */
			pDataType = pDataType.getReference();
		}
		/* Return the reference. */
		return pDataType;
	}
	
	/* Defines whether a DataType is a Scalar. */
	public static final boolean isScalar(final IDataType<?> pDataType) {
		/* Determine if we're handling a Scalar Dimension. */
		return pDataType.getDimension() == DataGlobal.DIMENSION_SCALAR;
	}
	
	/* Prevent external instantiaion of this class. */
	private DataGlobal() { }
	
}