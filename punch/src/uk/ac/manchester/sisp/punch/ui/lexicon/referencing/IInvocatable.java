package uk.ac.manchester.sisp.punch.ui.lexicon.referencing;

public interface IInvocatable<U> {
	/* Create the corresponding Invocation type. */
	public abstract U onCreateInvocation();
}
