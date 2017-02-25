package uk.ac.manchester.sisp.punch.ui.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.IUIElementList;
import uk.ac.manchester.sisp.punch.ui.IUIMargin;
import uk.ac.manchester.sisp.punch.ui.IUIMinima;
import uk.ac.manchester.sisp.punch.ui.IUIPadding;
import uk.ac.manchester.sisp.punch.ui.courier.IUICourier;
import uk.ac.manchester.sisp.punch.ui.distribution.IDistributable;
import uk.ac.manchester.sisp.punch.ui.distribution.IDistribution;
import uk.ac.manchester.sisp.punch.ui.update.UIUpdateDispatcher;
import uk.ac.manchester.sisp.ribbon.common.IBounds2;
import uk.ac.manchester.sisp.ribbon.common.IDim2;
import uk.ac.manchester.sisp.ribbon.common.IOpacity;
import uk.ac.manchester.sisp.ribbon.io.ArrayStore;
import uk.ac.manchester.sisp.ribbon.opengl.vector.VectorPathContext;
import uk.ac.manchester.sisp.ribbon.opengl.vector.global.IVectorPathGroup;
import uk.ac.manchester.sisp.ribbon.ui.global.UIGlobal;

public interface IGroup <U extends IUIElement> extends IIcon, IDim2.I.W, IUIElementList<U>, IUIMargin, IUIPadding, IUIMinima, IDistributable, IOpacity.W {
	
	public static class Impl <U extends IUIElement> extends IIcon.Impl implements IGroup<U> {

		/* Define the default Serialization UID. */
		private static final long serialVersionUID = 1L;
		
		/* Member Variables. */
		private       int     mWidth;
		private       int     mHeight;
		private final List<U> mUIElements;
		private       float   mOpacity;
		
		public Impl(final int pX, final int pY, final int pWidth, final int pHeight) {
			super(pX, pY);
			/* Initialize Member Variables. */
			this.mWidth      = pWidth;
			this.mHeight     = pHeight;
			this.mUIElements = new ArrayList<U>(0);
			this.mOpacity    = UIGlobal.UI_UNITY;
		}
		
		@Override
		public IDistribution onFetchDistribution(final List<IUIElement> pHierarchy, final UIUpdateDispatcher pUIUpdateDispatcher) {
			/* By default, don't distribute. */
			return null;
		}
		
		@Override public final <T> void onCourierDispatch(final IUICourier<T> pUICourier, final T pCourierPackage) { pUICourier.onCourierTransit(this, pCourierPackage);          }
		
		@Override 
		public void setWidth(final int pWidth) { 
			this.mWidth = pWidth;
		}
		
		@Override 
		public int getWidth() { 
			return this.mWidth;
		}
		
		@Override public IDim2.I getMinima() {
			return IUIMinima.MINIMA_NULL;
		}

		@Override public void setHeight(final int pHeight) { this.mHeight = pHeight; }
		@Override public int  getHeight()                  { return this.mHeight;    }
		
		public final List<U> getUIElements() {
			return this.mUIElements;
		}
		
		@Override
		public IVectorPathGroup[] getVectorPathGroups(final ArrayStore.Float pFloatStore, final VectorPathContext pVectorPathContext) {
			/* By default, refuse to allocate a definition. */
			return IVectorPathGroup.GROUP_NULL_LAYOUT;
		}
		
		@Override public IBounds2.I getMargin() { return IUIMargin.MARGIN_NONE; }

		@Override public int getPadding() { return 20; } /** TODO: Staticize. **/

		@Override public boolean isScissorContents() { return false; } /** TODO: Should allow groups to specify the scissor region! default 0, entity whole box or some subbox in between (search field!) **/
		
		@Override
		public void setOpacity(float pOpacity) {
			this.mOpacity = pOpacity;
		}
		
		@Override
		public float getOpacity() {
			return this.mOpacity;
		}
		
		@Override
		public boolean isInternalsDelegation(final List<IUIElement> pHierarchy) {
			/* By default, we want contained UIElements to update the hierarchy. */
			return true;
		}
		
		@Override
		public boolean isHierarchicalEncapsulation() {
			/* By default, we want nested groups to freely respond to encapsulation requests. */
			return true;
		}

		@Override
		public Comparator<U> getComparator() {
			/* Return a useless comparator. */
			return new Comparator<U>() { @Override public final int compare(final U pU, final U p2) { return 0; } };
		}
		
	};
	
	/* Defines whether it restricts hierarchical encapsulation. */
	public abstract boolean       isHierarchicalEncapsulation();
	/* Defines whether to restrict rendering within the bounds of the IGroup. */
	public abstract boolean       isScissorContents();
	/* Defines whether the Group allows it's internals to update the hierarchy. */
	public abstract boolean       isInternalsDelegation(final List<IUIElement> pHierarchy);
	/* Defines a recommended Comparator to used when processing internal elements. */
	public abstract Comparator<U> getComparator();
}