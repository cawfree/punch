package uk.ac.manchester.sisp.punch.ui.lexicon.referencing;

public interface IReferential<T> { 
	/* Returns a reference to the allocated type. */
	public abstract T getReference();
}