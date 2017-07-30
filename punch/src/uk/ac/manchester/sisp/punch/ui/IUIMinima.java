package uk.ac.manchester.sisp.punch.ui;

import uk.ac.manchester.sisp.ribbon.common.IDim2;

public interface IUIMinima {
	
	/* Static Declarations. */
	public static final IDim2.I MINIMA_NULL = new IDim2.I.Impl(0, 0);
	
	/* Returns the minimum dimensions for a UIElement. */
	public abstract IDim2.I getMinima();
}