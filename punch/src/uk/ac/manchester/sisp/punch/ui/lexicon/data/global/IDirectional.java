package uk.ac.manchester.sisp.punch.ui.lexicon.data.global;

public interface IDirectional {
	
	public static interface W extends IDirectional {
		/* Reconfigure the DataDirection. */
		public abstract void setDataDirection(final EDataDirection pDataDirection);
	}
	
	/* Forces implementors to define a DataDirection.  */
	public abstract EDataDirection getDataDirection();
}