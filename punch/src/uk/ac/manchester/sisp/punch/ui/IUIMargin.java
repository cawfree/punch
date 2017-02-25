package uk.ac.manchester.sisp.punch.ui;

import uk.ac.manchester.sisp.ribbon.common.IBounds2;

public interface IUIMargin {
	
	/* Static Declarations. */
	public static final IBounds2.I MARGIN_NONE = new IBounds2.I.Impl(0, 0, 0, 0);
	
	/* Defines the surrounding border to apply to a graphical element. */
	public abstract IBounds2.I getMargin();
	
}
