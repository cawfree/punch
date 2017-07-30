package uk.ac.manchester.sisp.punch.ui.courier;

import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.core.Field;
import uk.ac.manchester.sisp.punch.ui.core.IGroup;
import uk.ac.manchester.sisp.punch.ui.core.IIcon;
import uk.ac.manchester.sisp.punch.ui.core.ISVGIcon;

public interface IUICourier <U> {
	
	public static class Adapter<U> implements IUICourier<U> {
		/* Generic UI Elements. */
		@Override public void onCourierTransit(final IIcon             pIcon, final U pCourierPackage){ };
		@Override public void onCourierTransit(final ISVGIcon       pSVGIcon, final U pCourierPackage){ };
		@Override public void onCourierTransit(final ISVGIcon.IFlat pFlatIcon, final U pCourierPackage){ };
		@Override public void onCourierTransit(final Field           pField, final U pCourierPackage){ };
		@Override public <T extends IUIElement> void onCourierTransit(final IGroup<T> pGroup, final U pCourierPackage){ };
	};
	
	/* Courier Distpatch Implementations. */
	public static interface Dispatch { public abstract <T> void onCourierDispatch(final IUICourier<T> pCourier, final T pCourierPackage); }
	
	/* Courier Entry Points. */
	public abstract void onCourierTransit(final IIcon             pIcon, final U pCourierPackage);
	public abstract void onCourierTransit(final ISVGIcon       pSVGIcon, final U pCourierPackage);
	public abstract void onCourierTransit(final ISVGIcon.IFlat pFlatIcon, final U pCourierPackage);
	public abstract void onCourierTransit(final Field           pField, final U pCourierPackage);
	public abstract <T extends IUIElement> void onCourierTransit(final IGroup<T>        pGroup, final U pCourierPackage);
	
}