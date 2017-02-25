package uk.ac.manchester.sisp.punch.ui.lexicon.cascade.generation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.manchester.sisp.punch.ui.core.IGroup;
import uk.ac.manchester.sisp.punch.ui.lexicon.ILexical;
import uk.ac.manchester.sisp.punch.ui.lexicon.cascade.Cascade;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.GState;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.IDriver;

public final class CState <V extends ILexical, X extends ILexical & IGroup<V>> extends GState<IDriver, V, X> {
	
	/* Member Variables. */
	private final int                         mX;
	private final int                         mWidth;
	private final Map<Integer, List<Cascade>> mCascadeMap;
	private final Map<Integer, Integer>       mExtensionMap;
	
	protected CState(final List<X> pInternals, final int pX, final int pWidth) {
		/* Supply the Internals. */
		super(pInternals);
		/* Initialize Member Variables. */
		this.mX            = pX;
		this.mWidth        = pWidth;
		this.mCascadeMap   = new HashMap<Integer, List<Cascade>>();
		this.mExtensionMap = new HashMap<Integer, Integer>      ();
	}
	
	public final int getWidth() { 
		return this.mWidth;
	}
	
	public final int getX() { 
		return this.mX;
	}
	
	protected final Map<Integer, List<Cascade>> getCascadeMap() { 
		return this.mCascadeMap;
	}
	
	protected final Map<Integer, Integer> getExtensionMap() { 
		return this.mExtensionMap;
	}
	
}