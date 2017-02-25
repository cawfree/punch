package uk.ac.manchester.sisp.punch;

import java.util.List;

import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.collision.RayCastAdapter;
import uk.ac.manchester.sisp.punch.ui.core.context.IContext;
import uk.ac.manchester.sisp.punch.ui.key.UIKeyEvent;
import uk.ac.manchester.sisp.punch.ui.update.UIUpdateDispatcher;
import uk.ac.manchester.sisp.punch.ui.update.UIUpdateEvent;
import uk.ac.manchester.sisp.ribbon.event.IEvent;
import uk.ac.manchester.sisp.ribbon.event.filter.IEventsFilter;
import uk.ac.manchester.sisp.ribbon.opengl.IGLES20;
import uk.ac.manchester.sisp.ribbon.opengl.IGLEventListener;
import uk.ac.manchester.sisp.ribbon.ui.pointer.EPointerAction;
import uk.ac.manchester.sisp.ribbon.ui.pointer.EPointerIndex;
import uk.ac.manchester.sisp.ribbon.ui.pointer.UIPointerDispatcher;
import uk.ac.manchester.sisp.ribbon.ui.pointer.UIPointerEvent;
import uk.ac.manchester.sisp.ribbon.utils.DataUtils;
import uk.ac.manchester.sisp.ribbon.utils.ResourceUtils;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;

public final class PunchController implements MouseListener, KeyListener, IGLEventListener, IEventsFilter <UIPointerEvent, UIPointerDispatcher> {
	
	/* Model-View-Controller Integration. */
	private final PunchModel mPunchModel;
	private final PunchView  mPunchView;
	
	public PunchController(final PunchModel pWiresModel, final PunchView pWiresView) {
		/* Initialize Member Variables. */
		this.mPunchModel                 = pWiresModel;
		this.mPunchView                  = pWiresView;
		/* Configure this class for UIPointerEvent listening. */
		this.getPunchModel().getUIPointerDispatcher().getEventFilters().add(this);
		/* Configure the PunchController for implicitly handling UIUpdateEvents. */
		this.getPunchModel().getUIUpdateDispatcher().getEventFilters().add(new IEventsFilter<UIUpdateEvent, UIUpdateDispatcher>(){ @Override public final boolean onHandleEvent(final UIUpdateEvent pUIUpdateEvent, final UIUpdateDispatcher pEventsDispatcher) {
			/* Iterate through each generic IUIElement reference. */	
			for(final IUIElement lUIElement : pUIUpdateEvent.getUIElements() ) {
				/* Delegate the UIUpdateEvent to the PunchView. */
				lUIElement.onCourierDispatch(getPunchView(), pUIUpdateEvent.getUICommand());
			}
			/* State that we wish to handle further events. */
			return true;
		/* Ignore disposal operations. */
		} @Override public void dispose() {} });
	}

	@Override
	public final boolean onHandleEvent(final UIPointerEvent pUIPointerEvent, final UIPointerDispatcher pUIPointerDispatcher) { 
		/* Synchronize along the DeferList. */
		synchronized(this.getPunchModel().getDeferList()) { 
			/* Determine if the UIDeferFilter has elapsed. */
			if(this.getPunchModel().getUIDeferFilter().isElapsed(pUIPointerEvent.getObjectTimeSeconds())) { 
				/* Buffer the UIPointerEvent onto the DeferList. */
				this.getDeferList().add(pUIPointerEvent);
				/* Allocate a boolean to ensure we're still able to process the DeferList. */
				boolean lIsProcessQueue = !this.getDeferList().isEmpty();
				/* Iterate the DeferList. */
				for(int i = 0; i < this.getDeferList().size() && lIsProcessQueue; i++) {
					/* Fetch the UIPointerEvent. */
					final UIPointerEvent lUIPointerEvent = this.getDeferList().get(i);
					/* Determine if we're still allowed to process the UIPointerEvent. */
					if(lIsProcessQueue) {
						/* Synchronize along the PunchModel's Contexts. */
						synchronized(this.getPunchModel().getContexts()) {
							/* Allocate a boolean to assert whether the event has been delegated. */
							boolean lIsDelegated = false;
							/* Allocate a RayCastAdapter. */
							final RayCastAdapter lRayCastAdapter = new RayCastAdapter();
							/* Reverse iterate the Contexts whilst the event has not been delegated. (Contexts at higher indices have a higher priority!) */
							for(int j = this.getPunchModel().getContexts().size() - 1; (j >= 0 && !lIsDelegated); j--) {
								/* Fetch the Context. */
								final IContext lContext = this.getPunchModel().getContexts().get(j);
								/* Ensure the Context is enabled. */
								if(lContext.isEnabled()) {
									/* Allocate a UIPointerEvent for the Context. */
									final UIPointerEvent lContextualizedEvent = new UIPointerEvent(lUIPointerEvent);
									/* Transform the UIPointerEvent to compensate for Scale. */
									UIPointerEvent.onTransformPointer(lContextualizedEvent, 0, 0, lContext.getScale());
									/* Delegate the UIPointerEvent to the RayCastAdapter. */
									lRayCastAdapter.onTransmitRay(lContextualizedEvent, lContext);
									/* Have the Context handle the event. Determine whether to continue propagation. */
									lIsDelegated = lContext.onHandleEvent(lContextualizedEvent, lRayCastAdapter.getCollisionResults(), this.getPunchModel());
								}
							}
						}
					}
				}
				/* Clear the DeferList. */
				this.getDeferList().clear();
			}
			else {
				/* Determine if we're processing the kind of UIPointerEvent worth deferring. */
				switch(pUIPointerEvent.getPointerAction()) {
					case POINTER_DRAGGED : 
					case POINTER_PRESSED : 
					case POINTER_RELEASE : 
						/* Synchronize along the DeferList. */
						synchronized(this.getDeferList()) {
							/* Defer the UIPointerEvent. */
							this.getDeferList().add(pUIPointerEvent);
						}
					break;
					default              : 
						/* Don't buffer the UIPointerEvent. */
					break;
				}
			}
		}
		/* Indicate to the UIPointerDispatcher that we wish to continue handling further events. */
		return true;
	}
	
	@Override public final void mouseWheelMoved(final MouseEvent pMouseEvent) {
		/* Synchronize along the Contexts. */
		synchronized(this.getPunchModel().getContexts()) {
			/* Fetch the CodeContext. */
			final IContext lCodeContext = DataUtils.getFirstElementOf(this.getPunchModel().getContexts()); /** TODO: Harder implementation. **/
			/* Ensure we're not deferring input. */
			if(this.getPunchModel().getUIDeferFilter().isElapsed(ResourceUtils.getSystemTimeSeconds())) {
				/* Determine if the user is holding control. */
				if(pMouseEvent.isControlDown()) {
					/* Smooth-zoom the diagram using UIReactiveEasing. */
					lCodeContext.setScale((lCodeContext.getScale() + ((pMouseEvent.getRotation()[1] > 0.0f ? 0.1f : -0.1f))));
				}
				else {
					/* Calculate the WiresModel OffsetX and OffsetY. */
					final float lOffsetX =  pMouseEvent.isShiftDown() ? lCodeContext.getX() + pMouseEvent.getRotation()[0] * 50.0f : lCodeContext.getX();
					final float lOffsetY = !pMouseEvent.isShiftDown() ? lCodeContext.getY() + pMouseEvent.getRotation()[1] * 50.0f : lCodeContext.getY();
					/* Delegate these offsets to the UIReactiveOffset. */
					lCodeContext.setX(Math.round(lOffsetX));
					lCodeContext.setY(Math.round(lOffsetY));
				}
			}
		}
	}
	
	@Override public final void onInitialize(final IGLES20 pGLUnused) {
	
	}
	
	/** TODO: Abstract to Wires! **/
	@Override public final void onDisplay(final IGLES20 pGLUnused) {
		/* Fetch the PunchView's FrameSeconds. */
		final float lFrameSeconds = this.getPunchView().getFrameSeconds();
		/* Encapsulate the FrameSeconds as a TimedEvent. */
		final IEvent lTimedEvent  = new IEvent(){ @Override public final float getObjectTimeSeconds() { return lFrameSeconds; } @Override public void dispose() { } };
		/* Delegate the TimedEvent to the UITimeDispatcher. */
		this.getPunchModel().getUISecondsDispatcher().onRibbonEvent(lTimedEvent);
	}
	
	/* Mouse events. */
	@Override public final void mouseDragged(final MouseEvent pMouseEvent)  { this.getPunchModel().getUIPointerDispatcher().onUpdatePointer(this.getPunchView().getFrameSeconds(), pMouseEvent.getX(), pMouseEvent.getY(), pMouseEvent.getClickCount(), EPointerAction.POINTER_DRAGGED, (pMouseEvent.getButton() == 1 ? EPointerIndex.LEFT : pMouseEvent.getButton() == 3 ? EPointerIndex.RIGHT : EPointerIndex.MIDDLE), pMouseEvent.isShiftDown(), pMouseEvent.isControlDown()); }
	@Override public final void mousePressed(final MouseEvent pMouseEvent)  { this.getPunchModel().getUIPointerDispatcher().onUpdatePointer(this.getPunchView().getFrameSeconds(), pMouseEvent.getX(), pMouseEvent.getY(), pMouseEvent.getClickCount(), EPointerAction.POINTER_PRESSED, (pMouseEvent.getButton() == 1 ? EPointerIndex.LEFT : pMouseEvent.getButton() == 3 ? EPointerIndex.RIGHT : EPointerIndex.MIDDLE), pMouseEvent.isShiftDown(), pMouseEvent.isControlDown()); }
	@Override public final void mouseReleased(final MouseEvent pMouseEvent) { this.getPunchModel().getUIPointerDispatcher().onUpdatePointer(this.getPunchView().getFrameSeconds(), pMouseEvent.getX(), pMouseEvent.getY(), pMouseEvent.getClickCount(), EPointerAction.POINTER_RELEASE, (pMouseEvent.getButton() == 1 ? EPointerIndex.LEFT : pMouseEvent.getButton() == 3 ? EPointerIndex.RIGHT : EPointerIndex.MIDDLE), pMouseEvent.isShiftDown(), pMouseEvent.isControlDown()); }
	@Override public final void   mouseMoved(final MouseEvent pMouseEvent)  { this.getPunchModel().getUIPointerDispatcher().onUpdatePointer(this.getPunchView().getFrameSeconds(), pMouseEvent.getX(), pMouseEvent.getY(), pMouseEvent.getClickCount(), EPointerAction.POINTER_MOVED,   (pMouseEvent.getButton() == 1 ? EPointerIndex.LEFT : pMouseEvent.getButton() == 3 ? EPointerIndex.RIGHT : EPointerIndex.MIDDLE), pMouseEvent.isShiftDown(), pMouseEvent.isControlDown()); }
	
	/* Unused event listeners. */
	@Override public final void mouseClicked(final MouseEvent pUnused)                                                                  { }
	@Override public final void mouseEntered(final MouseEvent pUnused)                                                                  { }
	@Override public final void  mouseExited(final MouseEvent pUnused)                                                                  { }
	@Override public final void    onResized(final IGLES20  pGLUnused, final int pX, final int pY, final int pWidth, final int pHeight) { }
	@Override public final void    onDispose(final IGLES20  pGLUnused)                                                                  { }
	
	@Override public final void  keyReleased(final KeyEvent pKeyEvent)                                                                  { }
	
	@Override public synchronized final void keyPressed(final KeyEvent pKeyEvent)                                                                    { 
		/* Fetch the CurrentTimeSeconds. */
		final float lCurrentTimeSeconds = this.getPunchView().getFrameSeconds();
		/* Delegate the UIKeyEvent. */
		final UIKeyEvent lUIKeyEvent = new UIKeyEvent(lCurrentTimeSeconds, pKeyEvent.getKeyChar(), pKeyEvent.getKeyCode(), (DataUtils.booleanToInt(pKeyEvent.isControlDown()) * UIKeyEvent.FLAG_KEY_CTRL) | (DataUtils.booleanToInt(pKeyEvent.isAltDown()) * UIKeyEvent.FLAG_KEY_ALT) | (DataUtils.booleanToInt(pKeyEvent.isShiftDown()) * UIKeyEvent.FLAG_KEY_SHIFT));
		/** TODO: Define an architecture for stopping event propagation until (i.e. holding this command) until we're ready to handle again. **/
		/* Handle the KeyEvent. */
		if(lUIKeyEvent.isControlDown()) {
			/* Process the KeyEvent's KeyCode. */
			switch(lUIKeyEvent.getKeyCode()){
				case UIKeyEvent.KEY_CODE_SPACE : 
					/** TODO: (CTRL + SPACE): Use this command to navigate the user to the console. **/
				break;
				default : /* Ignore all other KeyCodes. */ break;
			}
		}
		else {
			/* Delegate the UIKeyEvent to the UIKeyDispatcher. */
			this.getPunchModel().getUIKeyDispatcher().onRibbonEvent(lUIKeyEvent);
		}
	}

	@Override public final void dispose() {}
	
	public final PunchModel getPunchModel() {
		return this.mPunchModel;
	}
	
	private final PunchView getPunchView() {
		return this.mPunchView;
	}
	
	private final List<UIPointerEvent> getDeferList() {
		return this.getPunchModel().getDeferList();
	}
	
//	private final EasingGroup getScaleGroup() {
//		return this.mScaleGroup;
//	}
//	
//	private final EasingGroup getOffsetGroup() {
//		return this.mOffsetGroup;
//	}
//	
//	private final EasingPacket<IScale.W> getScalePacket() {
//		return this.mScalePacket;
//	}
//	
//	private final EasingPacket<IVec2.I.W> getOffsetPacket() {
//		return this.mOffsetPacket;
//	}
	
}