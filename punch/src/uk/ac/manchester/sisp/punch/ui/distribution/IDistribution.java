package uk.ac.manchester.sisp.punch.ui.distribution;

import java.util.List;

import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.IUIPadding;
import uk.ac.manchester.sisp.punch.ui.update.easing.ui.UIEasingGroup;
import uk.ac.manchester.sisp.ribbon.common.IBounds2;
import uk.ac.manchester.sisp.ribbon.common.IDisposable;

public interface IDistribution extends IDisposable { /** TODO: Extend EasingGroup! **/
	
	/* Forces a Distribution to supply the elements it intends to distribute. */
	public abstract void onSupplyDistributables(final List<IUIElement> pUIElements);
	/* Distributes the elements. */ /** TODO: Assure master immutability? **/
	public abstract void onDistributeElements(final UIEasingGroup pUIEasingGroup, final IUIPadding pUIPadding, final IBounds2.I.W pResultingBounds);
	/* States whether a Distribution must remain fixed to it's current location. */
	public abstract boolean isAnchored();
	
}