package uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing;

import uk.ac.manchester.sisp.punch.ui.lexicon.data.IDataType;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.EDataDirection;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.IDataConduit;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.IDataDirectional;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.IDirectional;

public interface IDriver extends IDataDirectional, IDirectional {
	
	/* Default Implementation Stub. */
	public static class Impl implements IDriver { 
		
		/* Member Variables. */
		private final IDataConduit mDataConduit;
		private final IDataType<?> mDataType;
		
		/* Constructor Body. */
		public Impl(final IDataConduit pDataConduit, final IDataType<?> pDataType) {
			/* Initialize Member Variables. */
			this.mDataConduit = pDataConduit;
			this.mDataType    = pDataType;
		}
		
		/* Getters. */
		@Override public final IDataType<?>   getDataType(final EDataDirection pDataDirection) { return this.mDataType;        }
		@Override public final IDataConduit                                   getDataConduit() { return this.mDataConduit;     }
		@Override public final EDataDirection                               getDataDirection() { return EDataDirection.SOURCE; }
		
	};
	
	/* Returns the DataConduit associated with the Driver. */
	public abstract IDataConduit getDataConduit();
	
}