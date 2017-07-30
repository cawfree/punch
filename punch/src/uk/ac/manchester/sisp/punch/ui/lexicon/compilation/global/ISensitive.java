package uk.ac.manchester.sisp.punch.ui.lexicon.compilation.global;

import java.util.List;

public interface ISensitive<T> {
	/* Returns the List of Sensitive references. */
	public abstract List<T> getSensitivies();
}