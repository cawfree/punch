package uk.ac.manchester.sisp.punch.ui.courier.helper;

import java.util.List;

import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.collision.RayCastManager;
import uk.ac.manchester.sisp.punch.ui.core.Field;
import uk.ac.manchester.sisp.punch.ui.core.IGroup;
import uk.ac.manchester.sisp.punch.ui.core.IIcon;
import uk.ac.manchester.sisp.punch.ui.core.ISVGIcon;
import uk.ac.manchester.sisp.punch.ui.courier.IUICourier;
import uk.ac.manchester.sisp.ribbon.utils.DataUtils;

public class UIIterator<U> implements IUICourier<U> { /** TODO: We do need to rectify courier abstraction. There's no question. **/
	
	/* Member Variables. */
	private final List<IUIElement> mHierarchy;
	
	public UIIterator(final List<IUIElement> pHierarchy) {
		/* Initialize Member Variables. */
		this.mHierarchy = pHierarchy;
	}
	
	/* Overridable iteration steps. By default, we reverse iterate through the hierarchy to the root-most parent. */
	@Override public void onCourierTransit(final IIcon             pIcon, final U pCourierPackage){ this.onServeParent(pIcon,     pCourierPackage); };
	@Override public void onCourierTransit(final ISVGIcon       pSVGIcon, final U pCourierPackage){ this.onServeParent(pSVGIcon,  pCourierPackage); };
	@Override public void onCourierTransit(final ISVGIcon.IFlat pFlatIcon, final U pCourierPackage){ this.onServeParent(pFlatIcon, pCourierPackage); };
	@Override public void onCourierTransit(final Field           pField, final U pCourierPackage){ this.onServeParent(pField,    pCourierPackage); };
	@Override public <T extends IUIElement> void onCourierTransit(final IGroup<T>        pGroup, final U pCourierPackage){ this.onServeParent(pGroup,    pCourierPackage); };

	public void onServeParent(final IUIElement pChild, final U pU) {
		/* Fetch the Parent. */
		final IUIElement lParent = RayCastManager.onFetchParent(this.getHierarchy(), pChild);
		/* Ensure we're handling a valid Parent. */
		if(DataUtils.isNotNull(lParent)) {
			/* Export the UIAdapter. */
			lParent.onCourierDispatch(this, pU);
		}
	}
	
	private final List<IUIElement> getHierarchy() {
		return this.mHierarchy;
	}
	
}