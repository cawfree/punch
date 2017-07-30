package uk.ac.manchester.sisp.punch.ui.lexicon.data.global;

import uk.ac.manchester.sisp.punch.ui.lexicon.data.IDataType;

public interface IDataDirectional { 
	
	/* Writeable type. */
	public static interface W extends IDataConduit {
		/* Assign a new DataType. */
		public abstract void setDataType(final EDataDirection pDataDirection, final IDataType<?> pDataType);
	}
	
	/* Returns the associated DataType for a given DataDirection. */
	public abstract IDataType<?> getDataType(final EDataDirection pDataDirection);

}
