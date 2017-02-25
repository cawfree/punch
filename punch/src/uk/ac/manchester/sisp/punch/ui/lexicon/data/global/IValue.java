package uk.ac.manchester.sisp.punch.ui.lexicon.data.global;

public interface IValue<T> {
	
	public static interface Default <T> {
		/* Returns the default value associated. */
		public abstract T getDefault();
	}
	
	public static interface W <T> extends IValue<T> {
		/* Assign the associated Value. */
		public abstract void setValue(final T pT);
	}
	
	/* Returns the Value associated with the type. */
	public abstract T getValue();
}