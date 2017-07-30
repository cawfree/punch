package uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.manchester.sisp.punch.ui.core.IGroup;
import uk.ac.manchester.sisp.punch.ui.lexicon.ILexical;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.branching.IBranch;
import uk.ac.manchester.sisp.ribbon.common.IDisposable;
import uk.ac.manchester.sisp.ribbon.common.IEnabled;

public class GState <T extends IDriver, V extends ILexical, X extends ILexical & IGroup<V>> implements IEnabled.W, IDisposable{ 
	
	/* Member Variables. */
	private final List<X>          mContents;
	private       int              mIndex;
	private       Map<Integer, T>  mDriverMap;
	private final List<IBranch<T>> mBranches;
	private       boolean          mEnabled; /** TODO: Reflect branches. **/
	
	public GState(final List<X> pContents) {
		/* Initialize Member Variables. */
		this.mContents  = pContents;
		this.mIndex     = 0;
		this.mDriverMap = new HashMap  <Integer, T>(0);
		this.mBranches  = new ArrayList<IBranch<T>>(0);
		/* Upon initialization, a state is enabled by default. */
		this.mEnabled   = true;
	}
	
	@Override
	public void dispose() {
		/* Empty the DriverMap. */
		this.getDriverMap().clear();
		/* Destroy our reference to the DriverMap. */
		this.mDriverMap = null;
	}
	
	public final List<X> getContents() { 
		return this.mContents;
	}
	
	public final void setIndex(final int pIndex) { 
		this.mIndex = pIndex;
	}
	
	public final int getIndex() { 
		return this.mIndex;
	}
	
	public final Map<Integer, T> getDriverMap() {
		return this.mDriverMap;
	}
	
	public final List<IBranch<T>> getBranches() { 
		return this.mBranches;
	}

	@Override
	public final void setEnabled(final boolean pIsEnabled) { 
		this.mEnabled = pIsEnabled;
	}

	@Override
	public final boolean isEnabled() { 
		return this.mEnabled;
	}

}