package uk.ac.manchester.sisp.punch.ui.core.context;

import java.util.ArrayList;
import java.util.List;

import uk.ac.manchester.sisp.punch.PunchModel;
import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.core.IGroup;
import uk.ac.manchester.sisp.punch.ui.core.encapsulation.IEncapsulation;
import uk.ac.manchester.sisp.punch.ui.distribution.IDistribution;
import uk.ac.manchester.sisp.punch.ui.distribution.constants.PassiveDistribution;
import uk.ac.manchester.sisp.punch.ui.update.UIUpdateDispatcher;
import uk.ac.manchester.sisp.ribbon.common.IEnabled;
import uk.ac.manchester.sisp.ribbon.common.IScale;
import uk.ac.manchester.sisp.ribbon.common.IVisible;
import uk.ac.manchester.sisp.ribbon.opengl.IScreenParameters;
import uk.ac.manchester.sisp.ribbon.opengl.IScreenParametersListener;
import uk.ac.manchester.sisp.ribbon.ui.global.UIGlobal;
import uk.ac.manchester.sisp.ribbon.ui.pointer.UIPointerEvent;

/** TODO: Should probably be concrete. **/ /** TODO: ILifeCycle. **/
public interface IContext extends IGroup<IUIElement>, IVisible.W, IScale.W, IEnabled.W, IScreenParametersListener { 
	
	/* Define a default implementation. */
	public static class Impl extends IGroup.Impl<IUIElement> implements IContext {

		/* Define the default Serialization UID. */
		private static final long serialVersionUID = 1L;
		
		/* Member Variables. */
		private       boolean   mEnabled;
		private       boolean   mVisible;
		private       float     mScale;
		
		public Impl(final boolean pIsEnabled, final boolean pIsVisible) {
			super(0, 0, 0, 0);
			/* Initialize Member Variables. */
			this.mEnabled = pIsEnabled;
			this.mVisible = pIsVisible;
			this.mScale   = UIGlobal.UI_UNITY;
		}

		@Override public void    setVisible(final boolean pIsVisible) { this.mVisible = pIsVisible; }
		@Override public boolean isVisible ()                         { return this.mVisible;       }

		@Override public void    setScale(float pScale) { this.mScale = pScale; }
		@Override public float   getScale()             { return this.mScale;   }

		@Override public void    setEnabled(final boolean pIsEnabled) { this.mEnabled = pIsEnabled; }
		@Override public boolean isEnabled ()                         { return this.mEnabled;       }
		
		@Override public IDistribution onFetchDistribution(final List<IUIElement> pHierarchy, final UIUpdateDispatcher pUIUpdateDispatcher) {
			/* Return a PassiveDistribution; allow UIElements to be placed anywhere. */
			return new PassiveDistribution<IUIElement>(new ArrayList<IUIElement>(this.getUIElements()));
		}
		
		@Override public boolean isScissorContents() { return false; }
		/* We must always render a context, regardless of whether it's on screen or not. */
		@Override public boolean isScissorable() { return false; }

		@Override public boolean onHandleEvent(final UIPointerEvent pUIPointerEvent, final List<IUIElement> pHierarchy, final PunchModel pPunchModel) { return false; }

		@Override public void onScreenParametersChanged(final IScreenParameters pScreenParameters){ }

		@Override 
		public <U extends IUIElement> IEncapsulation onFetchEncapsulation(final List<IUIElement> pHierarchy, final IGroup<U> pEnclosure, final IUIElement pUIElement, final PunchModel pPunchModel) {
			/* Otherwise, in the interest of safety, we're not going to permit any form of change in encapsulation. */
			return IEncapsulation.ENCAPSULATION_DISABLED;
		}
		
	};
	
	/* Define some core event handling routines; return true to indicate the event has been consumed. */
	public abstract boolean onHandleEvent(final UIPointerEvent pUIPointerEvent, final List<IUIElement> pHierarchy, final PunchModel pPunchModel);
	/* Handle changes in Construction. */
	public abstract <U extends IUIElement> IEncapsulation onFetchEncapsulation(final List<IUIElement> pHierarchy, final IGroup<U> pEnclosure, final IUIElement pUIElement, final PunchModel pPunchModel);
	
}