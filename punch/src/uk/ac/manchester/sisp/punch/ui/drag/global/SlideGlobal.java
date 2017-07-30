package uk.ac.manchester.sisp.punch.ui.drag.global;

import java.util.List;

import uk.ac.manchester.sisp.punch.PunchModel;
import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.core.IGroup;
import uk.ac.manchester.sisp.punch.ui.core.context.IContext;
import uk.ac.manchester.sisp.punch.ui.distribution.global.DistributionGlobal;
import uk.ac.manchester.sisp.punch.ui.update.easing.ui.UIEasingGroup;
import uk.ac.manchester.sisp.punch.ui.update.easing.ui.UIElementPacket;
import uk.ac.manchester.sisp.ribbon.common.IVec2;
import uk.ac.manchester.sisp.ribbon.event.filter.IEventsFilter;
import uk.ac.manchester.sisp.ribbon.ui.easing.IEasingConfiguration;
import uk.ac.manchester.sisp.ribbon.ui.pointer.UIPointerDispatcher;
import uk.ac.manchester.sisp.ribbon.ui.pointer.UIPointerEvent;
import uk.ac.manchester.sisp.ribbon.utils.DataUtils;
import uk.ac.manchester.sisp.ribbon.utils.MathUtils;
import uk.ac.manchester.sisp.ribbon.utils.ResourceUtils;

public final class SlideGlobal { 
	
	/* Define the TaskState; used to control sliding. */
	private static enum ETaskState { 
		SLIDE, DESTROY;
	};
	
	/* Allows a user to slide a group of UIElements. */
	public synchronized static final <U extends IUIElement> void onSlide(final IContext pContext, final List<IUIElement> pHierarchy, final PunchModel pPunchModel, final UIPointerEvent pUIPointerEvent, final IGroup<?> pParent, final List<U> pUIElements, final boolean pIsHorizontal, final boolean pIsVertical) { 
		/* Allocate the StateBuffer. All actions must be synchronized along this. */
		final ETaskState[] lStateBuffer = new ETaskState[] { ETaskState.SLIDE };
		final IVec2.I.W    lMassDelta   = new IVec2.I.Impl();
		/* Allocate a new UIEasingGroup. */
		final UIEasingGroup lUIEasingGroup = new UIEasingGroup(pPunchModel.getUIUpdateDispatcher(), IEasingConfiguration.CONFIGURATION_DRAG, ResourceUtils.getSystemTimeSeconds()) {
			/* Custom LifeTime Specification. */
			@Override public final boolean isAlive(final float pCurrentTimeSeconds) {  
				/* Synchronize along the StateBuffer. */
				synchronized(lStateBuffer) {
					/* Ensure the task hasn't been destroyed. This allows a rapid distribution response by stifling easing immediately. */
					return (!DataUtils.getLastElementOf(lStateBuffer).equals(ETaskState.DESTROY));
				}
			}
			/* Define custom disposal operations. */
			@Override public final void dispose() { 
				/* Dispose as usual. */
				super.dispose();
				/* Synchronize along the DeferList. */
				synchronized(pPunchModel.getUIDeferFilter().getDeferList()) { 
					/* Remove the UIEasingGroup from the DeferList. */
					pPunchModel.getUIDeferFilter().getDeferList().remove(this);
					/* Also clear the PunchModel's DeferList. (We won't want to persist the drag data.) */
					pPunchModel.getDeferList().clear();
					/* Re-distribute the hierarchy. */
					DistributionGlobal.onDistributeHierarchy(pPunchModel, pHierarchy, pParent, IEasingConfiguration.CONFGIRUATION_RESPONSE);
				}
			} 
		};
		/* Allocate a new UIPointerFilter. */
		final IEventsFilter<UIPointerEvent, UIPointerDispatcher> lUIPointerFilter = new IEventsFilter<UIPointerEvent, UIPointerDispatcher>() {
			/* Handle the UIPointerEvent. */
			@Override public final boolean onHandleEvent(final UIPointerEvent pUIPointerEvent, final UIPointerDispatcher pUIPointerDispatcher) {
				/* Allocate a boolean to track if the task is still alive. */
				final boolean lIsAlive;
				/* Synchronize along the StateBuffer. */
				synchronized(lStateBuffer) { 
					/* Process the PointerAction. */
					switch(pUIPointerEvent.getPointerAction()) { 
						case POINTER_DRAGGED : 
							/* Fetch the UIPointerDispatcher's Delta. (DXDY) */
							final IVec2.I.W lDXDY = new IVec2.I.Impl(pUIPointerDispatcher.onFetchDelta());
							/* Update the MassDelta. */
							MathUtils.onSupplyOffset(lMassDelta, lDXDY);
							/* Iterate the UIElementPackets. */
							for(final UIElementPacket lUIElementPacket : lUIEasingGroup.getEasingPackets()) { 
								/* Calculate the DeltaX and DeltaY. */
								final float     lDeltaX = lUIElementPacket.getX() + (pIsHorizontal ? (lDXDY.getX() / pContext.getScale()) : 0);
								final float     lDeltaY = lUIElementPacket.getY() + (pIsVertical   ? (lDXDY.getY() / pContext.getScale()) : 0);
								/* Update the bounds of the ongoing EasingGroup. We update the final point, not whatever is currently interpolated because we won't have reached it yet! */
								lUIElementPacket.onUpdateBounds(lUIEasingGroup.getEasingConfiguration(), lUIEasingGroup.getObjectTimeSeconds(), pUIPointerEvent.getObjectTimeSeconds(), lDeltaX, lDeltaY);
							}
							/* Update the EasingGroup's ObjectTimeSeconds. */
							lUIEasingGroup.setObjectTimeSeconds(pUIPointerEvent.getObjectTimeSeconds());
						break;
						case POINTER_RELEASE : 
							/* After the user has stopped dragging, bring the task to an end. */
							DataUtils.onPushArrayElement(lStateBuffer, ETaskState.DESTROY);
						break;
						default              : 
							/* Ignore other cases. */
						break;
					}
					/* Update the task metric. */
					lIsAlive = (!DataUtils.getLastElementOf(lStateBuffer).equals(ETaskState.DESTROY));
				}
				/* Return the lifetime metric. */
				return lIsAlive;
			}
			/* Define custom disposal operations. */
			@Override public final void dispose() { 
				/** TODO: **/
			}
		};
		/* Iterate the UIElements. */
		for(final U lUIElement : pUIElements) {
			/* Allocate a UIElementPacket for the UIElement. */
			final UIElementPacket lUIElementPacket = new UIElementPacket(lUIElement);
			/* Buffer the UIElementPacket into the UIEasingGroup. */
			lUIEasingGroup.getEasingPackets().add(lUIElementPacket);
		}
		/* Synchronize along the DeferFilter's DeferList. */
		synchronized(pPunchModel.getUIDeferFilter().getDeferList()) { 
			/* Add the UIEasingGroup to the DeferFilter's DeferList. This will prevent UIPointerEvent data from being conventionally dispatched through the application. */
			pPunchModel.getUIDeferFilter().getDeferList().add(lUIEasingGroup);
			/* Add the UIPointerFilter to the UIPointerDispatcher. */
			pPunchModel.getUIPointerDispatcher().getEventFilters().add(lUIPointerFilter);
			/* Reset the UIEasingGroup's ObjectTimeSeconds. */
			lUIEasingGroup.setObjectTimeSeconds(ResourceUtils.getSystemTimeSeconds());
			/* Add the UIEasingGroup to the UISecondsDispatcher. (This in turn drives the Slide Task!) */
			pPunchModel.getUISecondsDispatcher().getEventFilters().add(lUIEasingGroup);
		}
	}
	
}