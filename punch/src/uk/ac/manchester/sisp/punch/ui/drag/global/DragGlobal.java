package uk.ac.manchester.sisp.punch.ui.drag.global;

import java.util.ArrayList;
import java.util.List;

import uk.ac.manchester.sisp.punch.PunchModel;
import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.collision.RayCastAdapter;
import uk.ac.manchester.sisp.punch.ui.color.global.ColorGlobal;
import uk.ac.manchester.sisp.punch.ui.core.IGroup;
import uk.ac.manchester.sisp.punch.ui.core.ISVGIcon;
import uk.ac.manchester.sisp.punch.ui.core.context.IContext;
import uk.ac.manchester.sisp.punch.ui.core.encapsulation.IEncapsulation;
import uk.ac.manchester.sisp.punch.ui.distribution.IDistribution;
import uk.ac.manchester.sisp.punch.ui.distribution.constants.LinearDistribution;
import uk.ac.manchester.sisp.punch.ui.distribution.global.DistributionGlobal;
import uk.ac.manchester.sisp.punch.ui.update.EUICommand;
import uk.ac.manchester.sisp.punch.ui.update.UIUpdateDispatcher;
import uk.ac.manchester.sisp.punch.ui.update.UIUpdateEvent;
import uk.ac.manchester.sisp.punch.ui.update.easing.ui.UIEasingGroup;
import uk.ac.manchester.sisp.punch.ui.update.easing.ui.UIElementPacket;
import uk.ac.manchester.sisp.ribbon.common.IBounds2;
import uk.ac.manchester.sisp.ribbon.common.IVec2;
import uk.ac.manchester.sisp.ribbon.event.IEvent;
import uk.ac.manchester.sisp.ribbon.event.filter.IEventsFilter;
import uk.ac.manchester.sisp.ribbon.io.EEntryMode;
import uk.ac.manchester.sisp.ribbon.ui.easing.IEasingConfiguration;
import uk.ac.manchester.sisp.ribbon.ui.easing.global.EasingGlobal;
import uk.ac.manchester.sisp.ribbon.ui.pointer.EPointerAction;
import uk.ac.manchester.sisp.ribbon.ui.pointer.UIPointerDispatcher;
import uk.ac.manchester.sisp.ribbon.ui.pointer.UIPointerEvent;
import uk.ac.manchester.sisp.ribbon.ui.time.UITimeDispatcher;
import uk.ac.manchester.sisp.ribbon.utils.DataUtils;
import uk.ac.manchester.sisp.ribbon.utils.MathUtils;
import uk.ac.manchester.sisp.ribbon.utils.ResourceUtils;

/* Defines a bunch of useful functions for dragging about Punch. */
public final class DragGlobal {
	
	/* Define the Discarder. */
	private static final class Discarder extends IGroup.Impl<IUIElement> {

		/* Define the default Serialization UID. */
		private static final long serialVersionUID = 1L;
		
		/* Static Declarations. */
		private static final int DIM_WIDTH  = 72;
		private static final int DIM_HEIGHT = 72;
		
		/* Member Variables. */
		private ISVGIcon.IFlat mFlatIcon;
		
		public Discarder(int pX, int pY) {
			super(pX, pY, Discarder.DIM_WIDTH, Discarder.DIM_HEIGHT);
			/* Initialize Member Variables. */
			this.mFlatIcon = new ISVGIcon.Flat(0, 0, this.getWidth(), this.getHeight(), ResourceUtils.getResource(DragGlobal.class.getClassLoader(), "res/icon/cancel.svg"), ColorGlobal.RGBA_SWEET_GREY);
			/* Add the FlatIcon to the Group. */
			this.getUIElements().add(this.getFlatIcon());
		}
		
		@Override public final IDistribution onFetchDistribution(final List<IUIElement> pHierarchy, final UIUpdateDispatcher pUIUpdateDispatcher) {
			/* Use a LinearDistribution. */
			return new LinearDistribution<IUIElement>(new ArrayList<IUIElement>(this.getUIElements()));
		}
		
		/* Define some simple bounds. */
		@Override public final IBounds2.I getMargin() { return new IBounds2.I.Impl(10, 10, 10, 10); }

		private final ISVGIcon.IFlat getFlatIcon() {
			return this.mFlatIcon;
		}
		
	};
	
	/* Define the DragState. */
	private static enum EDragState {
		DRAG,
		HOLD, 
		DISTRIBUTE,
		DESTROY;
	};
	
	/** TODO: Why should synchronized be required here? Why aren't distributions effective? **/
	/* Allows the diagram to periodically update and respond to a user's drag operations. The Enclosure doesn't have to be the immediate parent, just a source of distribution. */
	public static final void onPeriodicDrag(final IContext pContext, final List<IUIElement> pHierarchy, final PunchModel pPunchModel, final UIPointerEvent pUIPointerEvent, final IUIElement pUIElement, final IGroup<?> pEnclosure) { 
		/* Define the StateBuffer. */
		final EDragState[]     lStateBuffer     = new EDragState[]    { EDragState.HOLD }; 
		/* Define the PointerBuffer. */
		final UIPointerEvent[] lPointerBuffer   = new UIPointerEvent[]{ pUIPointerEvent };
		/* Define the UIElementPacket. */
		final UIElementPacket  lUIElementPacket = new UIElementPacket(pUIElement);
		/* Allocate the Delta. (The amount by which we need to transition the UIElement. Initialize using the position of the UIElementPacket.) */
		final IVec2.F.W        lDelta           = new IVec2.F.Impl((float)lUIElementPacket.getX(), (float)lUIElementPacket.getY());
		/* Allocate a boolean to track whether the user has made a valid drag. */
		final boolean[]        lIsDragged       = new boolean[]{ false };
		/* Define the UIEasingGroup. */
		final UIEasingGroup    lUIEasingGroup   = new UIEasingGroup(pPunchModel.getUIUpdateDispatcher(), IEasingConfiguration.CONFIGURATION_DRAG, pUIPointerEvent.getObjectTimeSeconds()) {
			/* Member Variables. */
			private UIEasingGroup mDistribution;
			/* Handle the UIEasingGroup's event. */
			@Override public final boolean onHandleEvent(final IEvent pEvent, final UITimeDispatcher pUITimeDispatcher) {
				/* Synchronize along the StateBuffer. */
				synchronized(lStateBuffer) {
					/* Process the current state. */
					switch(DataUtils.getLastElementOf(lStateBuffer)) {
						case DESTROY : 
							/* Here, we allow the destruction case to result in a final distribution.  */
						case DRAG    : 
							/* Allocate a boolean to determine if the task has been destroyed. */
							final boolean lIsDestroyed = (DataUtils.getLastElementOf(lStateBuffer).equals(EDragState.DESTROY));
							/* Check if we've exceeded the period of inactivity, and an actual drag has taken place, or we're destroying the drag task. */
							if(lIsDestroyed) {
								/* Re-distribute the diagram. */
								this.mDistribution = DistributionGlobal.onDistributionCore(this.getUIUpdateDispatcher(), pHierarchy, pEnclosure, IEasingConfiguration.CONFIGURATION_APP, pPunchModel);
								/* Export the Distribution. */
								pPunchModel.getUISecondsDispatcher().getEventFilters().add(this.getDistribution());
								/* Determine whether we're ending in destruction. */
								if(lIsDestroyed) {
									/* Kill the master UIEasingGroup; this ends the drag task. */
									return false;
								}
								/* Move to the distribution case by updating the StateBuffer. */
								DataUtils.onPushArrayElement(lStateBuffer, EDragState.DISTRIBUTE);
							}
							else {
								/* Allow the UIEasingGroup to update position of the UIElementPacket. */
								super.onHandleEvent(pEvent, pUITimeDispatcher);
							}
						break;
						case DISTRIBUTE : 
							/* Determine whether the Distribution has finished. */
							if(!this.getDistribution().isAlive(pEvent.getObjectTimeSeconds())) {
								/* Re-initialize the Delta. */
								MathUtils.setPosition(lDelta, (float)pUIElement.getX(), (float)pUIElement.getY());
								/* Update the UIPointerBuffer using the last UIPointerEvent. */
								DataUtils.onPushArrayElement(lPointerBuffer, pPunchModel.getUIPointerDispatcher().getPointerEvent());
								/* Assert that on entering the hold state, we're restarting a new drag task altogether. */
								lIsDragged[0] = false;
								/* Move back to the Hold case. */
								DataUtils.onPushArrayElement(lStateBuffer, EDragState.HOLD);
							}
						break;
						default : 
							/* Do nothing. */
						break;
					}
				}
				/* Interact with the events management system. */
				return true;
			}
			/* Handle custom disposal. */
			@Override public void dispose() {
				super.dispose();
				/* Synchronize along the PunchModel's DeferList. */
				synchronized(pPunchModel.getDeferList()) {
					/* Clear the DeferList. (We don't want to depend on any backlogged pointer events.) */
					pPunchModel.getDeferList().clear();
				}
				/* Allow user input to persist once again. */
				pPunchModel.getUIDeferFilter().getDeferList().remove(this);
			}
			/* Determine whether the UIEasingGroup is still active. */
			@Override public final boolean isAlive(final float pCurrentTimeSeconds) {
				/* Don't let the UIEasingGroup die. */
				return true;
			}
			/* Getters. */
			private final UIEasingGroup getDistribution() {
				return this.mDistribution;
			}
		};
		/* Allocate the UIPointerFilter. */
		final IEventsFilter<UIPointerEvent, UIPointerDispatcher> lUIPointerFilter = new IEventsFilter<UIPointerEvent, UIPointerDispatcher>() {
			/* Handle disposal. */
			@Override public void dispose() { }
			/* Handle the UIPointerEvent. */
			@Override public final boolean onHandleEvent(final UIPointerEvent pUIPointerEvent, final UIPointerDispatcher pUIPointerDispatcher) {
				/* Synchronize along the StateBuffer. */
				synchronized(lStateBuffer) {
					/* Determine if the user has released the mouse. */
					if(pUIPointerEvent.getPointerAction().equals(EPointerAction.POINTER_RELEASE)) {
						/* Assert the destruction state. */
						DataUtils.onPushArrayElement(lStateBuffer, EDragState.DESTROY);
					}
					/* Process the current state. */
					switch(DataUtils.getLastElementOf(lStateBuffer)) {
						case HOLD : 
							/* Assert that the user has dragged successfully. */
							lIsDragged[0] = true;
							/* Allow the UIElement to be dragged. */
							DataUtils.onPushArrayElement(lStateBuffer, EDragState.DRAG);
						break;
						case DRAG : 
							/* Update the PointerBuffer. (We're always interested in the 'final' UIPointerEvent of a certain task.) */
							DataUtils.onPushArrayElement(lPointerBuffer, pUIPointerEvent);
							/* Fetch the UIPointerDispatcher's Delta. (DXDY) */
							final IVec2.I lDXDY =  pUIPointerDispatcher.onFetchDelta();
							/* Update the Delta. (Account for the render scale!) */
							MathUtils.onSupplyOffset(lDelta, (lDXDY.getX() / pContext.getScale()), (lDXDY.getY() / pContext.getScale()));
							/* Update the bounds of the ongoing EasingGroup. We update the final point, not whatever is currently interpolated because we won't have reached it yet! */
							lUIElementPacket.onUpdateBounds(lUIEasingGroup.getEasingConfiguration(), lUIEasingGroup.getObjectTimeSeconds(), pUIPointerEvent.getObjectTimeSeconds(), lDelta.getX(), lDelta.getY());
							/* Update the EasingGroup's ObjectTimeSeconds. */
							lUIEasingGroup.setObjectTimeSeconds(pUIPointerEvent.getObjectTimeSeconds());
						break;
						case DESTROY :
							/* Allow the UIPointerFilter to be destroyed. */
							return false;
						default : 
							/* Do nothing. */
						break;
					}
				}
				/* Don't let the UIPointerFilter die. */
				return true;
			}
			
		};
		/* Add the UIElementPacket to the UIEasingGroup. */
		lUIEasingGroup.getEasingPackets().add(lUIElementPacket);
		/* Export the UIEasingGroup. */
		pPunchModel.getUISecondsDispatcher().getEventFilters().add(lUIEasingGroup);
		pPunchModel.getUIPointerDispatcher().getEventFilters().add(lUIPointerFilter);
		/* Prevent event dispatch for the duration of the task. */
		pPunchModel.getUIDeferFilter().getDeferList().add(lUIEasingGroup);
	}
	
	/** TODO: This method allows Enclosure I/O whilst a distribution is in progress, seems fine, but would have to go to the mapped solution if it proves erroneous in other contexts. **/
	/* Allows a UIElement to be removed from a container and dragged into a new one. During dragging, the UIElement is rendered top-most along the Context. If the UIElement does not exist within the Context, pass a null hierarchy. */
	public static final void onEncapsulationDrag(final IContext pContext, final List<IUIElement> pHierarchy, final PunchModel pPunchModel, final UIPointerEvent pUIPointerEvent, final IUIElement pUIElement, final boolean pIsDeletable) {
		/* Allocate the StateBuffer. */
		final EDragState[]     lStateBuffer     = new EDragState[]    { EDragState.DRAG };
		/* Allocate the DragOffset. (Used to quantify the relative offset from the UIPointerEvent and the Element being dragged, in absolute terms.) */
		final IVec2.I.Impl lDragOffset = new IVec2.I.Impl();
		/* First, determine if we're processing an existing hierarchy. */
		if(DataUtils.isNotNull(pHierarchy)) { 
			/* Supply the UIPointerEvent to the DragOffset. (It initially represents the Mouse.) */
			MathUtils.onSupplyOffset  (lDragOffset, pUIPointerEvent);
			/* Withdraw the Absolute Coordinates of the UIElement. */
			MathUtils.onWithdrawOffset(lDragOffset, RayCastAdapter.onFetchAbsoluteCoordinates(new IVec2.I.Impl(), pHierarchy, pUIElement));
			/** TODO: Always exists on the Context, so...? **/
			/* Remove the Context from the Hierarchy. (It has no place in the actual element structure; it's a veneer/facade.) */
			pHierarchy.remove(pContext);
			/* Determine if the hierarchy is still valid. */
			if(pHierarchy.indexOf(pUIElement) > 0) {
//				/* Fetch the UIElement's Container. */
//				IGroup<?> lEnclosure = (IGroup<?>) RayCastManager.onFetchParent(pHierarchy, pUIElement);
//				System.out.println("recommend "+lEnclosure);
				/* Allow the Context to define the Encapsulation. */
				synchronized(pContext) {
					/* Reverse iterate the hierarchy. */
					for(int i = pHierarchy.indexOf(pUIElement) - 1; i >= 0; i--) { 
						/* Fetch the Enclosure. */
						      IGroup<?>      lEnclosure     = (IGroup<?>) pHierarchy.get(i);
						/* Fetch the Context's Encapsulation. */
						final IEncapsulation lEncapsulation = pContext.onFetchEncapsulation(pHierarchy, lEnclosure, pUIElement, pPunchModel);
						/* Determine if the Encapsulation is enabled. */
						if(lEncapsulation.isEnabled()) {
							/* Override the Enclosure; the Encapsulation may recommend a more suitable container. */ /** TODO: THIS AFFECTS THE HIERARCHY! MAKE THE CALL SAFER, SUPPLY A MODIFIABLE? **/
							lEnclosure = lEncapsulation.onDefineEnclosure(lEnclosure, pHierarchy);
							/* Calculate the UIElement's AbsoluteCoords. */
							final IVec2.I.W lAbsoluteCoords = RayCastAdapter.onFetchAbsoluteCoordinates(new IVec2.I.Impl(), pHierarchy, pUIElement);
							/* Synchronize along the Enclosure. */
							synchronized(lEnclosure) {
								/* Remove the UIElement. */
								lEncapsulation.onEncapsulate      (EEntryMode.WITHDRAW, pHierarchy, lEnclosure.getUIElements(), pUIElement);
								/* Assign the AbsoluteCoords to the UIElement. */
								MathUtils.setPosition(pUIElement, lAbsoluteCoords.getX(), lAbsoluteCoords.getY());
								/* Add the UIElement to the Context. */
								pContext.getUIElements().add(pUIElement);
								/* Allow the Encapsulation to respond. */
								lEncapsulation.onPostEncapsulation(EEntryMode.WITHDRAW,   pContext, pHierarchy, lEnclosure, pUIElement, pPunchModel);
								/* End the iteration. */
								break;
							}
						}
					}
				}
			}
		}
		
		/* The UIElement now rests on the Context. Ensure it's top-most for dragging. */
		synchronized(pContext) {
			/* Remove the UIElement. */
			pContext.getUIElements().remove(pUIElement);
			/* Re-insert the UIElement at the top of the stack. */
			pContext.getUIElements().add(pUIElement);
		}
		
		/* Determine whether the UIElement may be deleted. */
		if(pIsDeletable) {
			/* Allocate a new Discarder. */
			final Discarder lDiscarder = new Discarder(0, 0);
			/* Distribute the Discarder. */
			DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lDiscarder, IEasingConfiguration.CONFIGURATION_NONE);
			/* Initialize the location of the Discarder. (Hide off screen, bottom right.) */
			MathUtils.setPosition(lDiscarder, ((pPunchModel.getScreenWidth() - lDiscarder.getWidth()) - pContext.getX()), ((pPunchModel.getScreenHeight()) - pContext.getY()));
			/* Allocate a UIElementPacket for the Discarder. */
			final UIElementPacket lUIElementPacket = new UIElementPacket(lDiscarder);
			/* Allocate a UIEasingGroup for the Discarder. */
			final UIEasingGroup lUIEasingGroup = new UIEasingGroup(pPunchModel.getUIUpdateDispatcher(), IEasingConfiguration.CONFIGURATION_APP, pUIPointerEvent.getObjectTimeSeconds()) {
				
				boolean b = false;
				/* Handle the TimedEvent. */
				@Override public boolean onHandleEvent(final IEvent pEvent, final UITimeDispatcher pEventsDispatcher) {
					/* Interpolate as usual. */
					super.onHandleEvent(pEvent, pEventsDispatcher);
					/* Synchronize along the StateBuffer. */
					synchronized(lStateBuffer) {
						/* Process the current state. */
						switch(DataUtils.getLastElementOf(lStateBuffer)) {
							case DESTROY :
								/* Ensure the EasingPacket's not Easing. */
								if(!b) {
									this.setEasingConfiguration(IEasingConfiguration.CONFIGURATION_APP);
									b= true;
									/* Update the bounds of the ongoing EasingGroup. We update the final point, not whatever is currently interpolated because we won't have reached it yet! */
									lUIElementPacket.onUpdateBounds(this.getEasingConfiguration(), this.getObjectTimeSeconds(), pEvent.getObjectTimeSeconds(), lDiscarder.getX(), lDiscarder.getY() + lDiscarder.getHeight());
									/* Update the EasingGroup's ObjectTimeSeconds. */
									this.setObjectTimeSeconds(pEvent.getObjectTimeSeconds());
								}
							break;
							default : 
								/* Do nothing. */
							break;
						}
					}
					/* Ensure the UIEasingGroup never dies. */
					return this.isAlive(pEvent.getObjectTimeSeconds());
				}
				/* Handle lifecycle interaction. */
				@Override public boolean isAlive(float pCurrentTimeSeconds) {
					/* Ensure the UIEasingGroup is not discarded. */
					return !b || super.isAlive(pCurrentTimeSeconds);
				}
				@Override
				public void dispose() { 
					/* Dispose as usual. */
					super.dispose();
					/* Synchronize along the DeferList. */
					synchronized(pPunchModel.getDeferList()) {
						/* Clear the DeferList. (We don't want any of the history of the drag to be available to the user.) */
						pPunchModel.getDeferList().clear();
						/* Remove ourselves from the UIDeferList; this will allow the user to interact with the application again! */
						pPunchModel.getUIDeferFilter().getDeferList().remove(lDiscarder);
					}
					/* Synchronize along the Context. */
					synchronized(pContext) {
						/* Remove the Discarder from the Context. */
						pContext.getUIElements().remove(lDiscarder);
						/* Synchronize along the Context. */
						synchronized(pContext) {
							/* Distribute the Context. */
							DistributionGlobal.onDistributeHierarchy(pPunchModel, null, pContext, IEasingConfiguration.CONFIGURATION_NONE);
						}
						/* Determine whether the user dragged a deletable object onto the Discarder. */
						if(lDiscarder.getUIElements().size() > 1) { /** TODO: Move to the Discarder. **/
							/* Inform the user that we've deleted the construct. */
							System.err.println("Deleted!");
						}
						/* Schedule the Discarder for deletion. */
						pPunchModel.getUIUpdateDispatcher().onRibbonEvent(new UIUpdateEvent(ResourceUtils.getSystemTimeSeconds(), EUICommand.DESTROY, lDiscarder));
					}
				}
			};
			/* Assign the initial terminals for the UIElementPacket. */
			MathUtils.onWithdrawOffset(lUIElementPacket, 0, lDiscarder.getHeight());
			/* Allocate graphical context for the Discarder. */
			pPunchModel.getUIUpdateDispatcher().onRibbonEvent(new UIUpdateEvent(pUIPointerEvent.getObjectTimeSeconds(), EUICommand.CREATE, lDiscarder));
			/* Synchronize along the Context. */
			synchronized(pContext) {
				/* Add the Discarder to the Context. */
				pContext.getUIElements().add(lDiscarder);
			}
			/* Add the UIElementPacket to the UIEasingGroup. */
			lUIEasingGroup.getEasingPackets().add(lUIElementPacket);
			/* Update the UIEasingGroup. */
			lUIEasingGroup.setObjectTimeSeconds(ResourceUtils.getSystemTimeSeconds());
			/* Add the UIEasingGroup to the UITimeDispatcher's EventFilters. */
			pPunchModel.getUISecondsDispatcher().getEventFilters().add(lUIEasingGroup);
			/* Register the UIEasingGroup with the DeferList. */
			pPunchModel.getUIDeferFilter().getDeferList().add(lDiscarder);
		}
		
		/* Allocate the UIElementPacket. */
		final UIElementPacket  lUIElementPacket = new UIElementPacket(pUIElement);
		/* Allocate the Delta. (The amount by which we need to transition the UIElement. Initialize using the position of the UIElementPacket.) */
		final IVec2.F.W        lDelta           = new IVec2.F.Impl((float)lUIElementPacket.getX(), (float)lUIElementPacket.getY());
		/* Allocate the UIEasingGroup. */
		final UIEasingGroup    lUIEasingGroup   = new UIEasingGroup(pPunchModel.getUIUpdateDispatcher(), IEasingConfiguration.CONFIGURATION_DRAG, pUIPointerEvent.getObjectTimeSeconds()) {
			/* Handle the TimedEvent. */
			@Override public final boolean onHandleEvent(final IEvent pEvent, final UITimeDispatcher pUITimeDispatcher) {
				/* Synchronize along the StateBuffer. */
				synchronized(lStateBuffer) {
					/* Process the current state. */
					switch(DataUtils.getLastElementOf(lStateBuffer)) {
						case DRAG : 
							/* Handle the event as usual. */
							super.onHandleEvent(pEvent, pUITimeDispatcher);
						break;
						case DISTRIBUTE :
							/* Handle the event as usual. */
							super.onHandleEvent(pEvent, pUITimeDispatcher);
							/* Determine whether the UIEasingGroup has finished interpolating. */ /** TODO: And ensure we're not distributing. Distribution Queue! **/
							if(!EasingGlobal.isMidEasing(pEvent.getObjectTimeSeconds(), this)) {
								/* Allocate a new RayCastAdapter. */
								final RayCastAdapter lRayCastAdapter = new RayCastAdapter();
								/* Synchronize along the Context. */
								synchronized(pContext) {
									/* Allocate the Ray. */
									final IVec2.I.W lRay = new IVec2.I.Impl(lUIElementPacket);
									/* Supply the Context's offset to the Ray. (This is because the Context is acting as a facade during the drag.) */
									MathUtils.onSupplyOffset(lRay, pContext);
									/* Supply the Offset to the Ray. */
									MathUtils.onSupplyOffset(lRay, lDragOffset);
									/* Momentarily remove the UIElement from the Context's UIElements. (This makes it invisible to the RayCast.) */
									pContext.getUIElements().remove(pUIElement);
									/* Transmit the Ray using the position of the Pointer. */
									final List<IUIElement> lHierarchy = lRayCastAdapter.onTransmitRay(lRay, pContext);
									/* Push the UIElement back onto the Context. */
									pContext.getUIElements().add(pUIElement);
//									/* Allocate a boolean to detect if a valid Enclosure was found. */
//									boolean lIsFound = false;
									/* Reverse iterate the Hierarchy. */
									for(int i = lHierarchy.size() - 1; i >= 0; i--) {
										/* Fetch the UIElement. */
										final IUIElement lUIElement = lHierarchy.get(i);
										/* Determine if the UIElement is a Group type. */
										if(lUIElement instanceof IGroup<?>) {
											/* Cast accordingly. */
											      IGroup<?>      lGroup         = (IGroup<?>)lUIElement;
											/* Fetch the Encapsulation procedure for this combination; if the user has hovered over a discarder, ensure the UIElement is encapsulated. */
											final IEncapsulation lEncapsulation = (lGroup instanceof Discarder) ? new IEncapsulation.Simple() { @Override public final IGroup<?> onDefineEnclosure(final IGroup<?> pSuggestion, final List<IUIElement> pHierarchy) { return pSuggestion; } } : pContext.onFetchEncapsulation(lHierarchy, lGroup, pUIElement, pPunchModel);
											/* Determine if the Encapsulation is supported. */
											if(lEncapsulation.isEnabled()) {
												/* Overwrite the Group reference using the Encapsulation's intended target. */
												lGroup = lEncapsulation.onDefineEnclosure(lGroup, lHierarchy);
												/* Fetch the Group's co-ordinates. */
												final IVec2.I.W lAbsoluteCoords = RayCastAdapter.onFetchAbsoluteCoordinates(new IVec2.I.Impl(), lHierarchy, lGroup);
												/* Withdraw the offset imposed by the Context, which is acting as a veneer. */
												MathUtils.onWithdrawOffset(lAbsoluteCoords, pContext);
												/* Synchronize along the Group. */
												synchronized(lGroup) {
													/* Remove the UIElement from the Context. */
													pContext.getUIElements().remove(pUIElement);
													/* Force the UIElement to use co-ordinates relative to the Group. */
													MathUtils.onWithdrawOffset(pUIElement, lAbsoluteCoords);
													/* Encapsulate the UIElement. */
													lEncapsulation.onEncapsulate(EEntryMode.SUPPLY, lHierarchy, lGroup.getUIElements(), pUIElement);
													/* Respond to the encapsulation. */
													lEncapsulation.onPostEncapsulation(EEntryMode.SUPPLY, pContext, lHierarchy, lGroup, pUIElement, pPunchModel);
//													/* Assert that we've found a suitable Enclosure. */
//													lIsFound = true;
												}
												/* End the iteration. */
												break;
											}
										}
									}
									/* Enter the destruction. */
									DataUtils.onPushArrayElement(lStateBuffer, EDragState.DESTROY);
//									/* Determine if we didn't find a valid enclosure. */
//									if(!lIsFound) {
//										/* Just redistribute the Context immediately to ensure we capture further UIPointerEvents. */
//										DistributionGlobal.onDistribute(pPunchModel, lHierarchy, pContext);
//									}
								}
								/* Kill the filter. */ /** TODO: Stateful! **/
								return false;
							}
						break;
						default : 
							/* Ignore all other cases. */
						break;
					}
				}
				/* By default, never die. */
				return this.isAlive(pEvent.getObjectTimeSeconds());
			}
			/* Assert that the UIEasingGroup can't be killed conventionally. */
			@Override public boolean isAlive(float pCurrentTimeSeconds) { return true; }
			/* Implement custom disposal. */
			@Override public void dispose() {
				/* Dispose as normal. */
				super.dispose();
				/* Synchronize along the DeferList. */
				synchronized(pPunchModel.getDeferList()) {
					/* Clear the DeferList. (We don't want any of the history of the drag to be available to the user.) */
					pPunchModel.getDeferList().clear();
					/* Remove ourselves from the UIDeferList; this will allow the user to interact with the application again! */
					pPunchModel.getUIDeferFilter().getDeferList().remove(this);
				}
			}
		};
		/* Register the UIElementPacket with the UIEasingGroup. */
		lUIEasingGroup.getEasingPackets().add(lUIElementPacket);
		/* At this stage, the UIElement rests upon the Context. */
		final IEventsFilter<UIPointerEvent, UIPointerDispatcher> lUIPointerFilter = new IEventsFilter<UIPointerEvent, UIPointerDispatcher>() {
			/* Handle the UIPointerEvent. */
			@Override public final boolean onHandleEvent(final UIPointerEvent pUIPointerEvent, final UIPointerDispatcher pUIPointerDispatcher) {
				/* Synchronize along the StateBuffer. */
				synchronized(lStateBuffer) {
					/* Determine if the UIPointerEvent has been released. */
					if(pUIPointerEvent.getPointerAction().equals(EPointerAction.POINTER_RELEASE)) {
						/* Assert the distribution state. */
						DataUtils.onPushArrayElement(lStateBuffer, EDragState.DISTRIBUTE);
					}
					/* Process the current state. */
					switch(DataUtils.getLastElementOf(lStateBuffer)) {
						case DRAG : 
							/* Fetch the UIPointerDispatcher's Delta. (DXDY) */
							final IVec2.I lDXDY =  pUIPointerDispatcher.onFetchDelta();
							/* Update the Delta. (Account for the render scale!) */
							MathUtils.onSupplyOffset(lDelta, (lDXDY.getX() / pContext.getScale()), (lDXDY.getY() / pContext.getScale()));
							/* Update the bounds of the ongoing EasingGroup. We update the final point, not whatever is currently interpolated because we won't have reached it yet! */
							lUIElementPacket.onUpdateBounds(lUIEasingGroup.getEasingConfiguration(), lUIEasingGroup.getObjectTimeSeconds(), pUIPointerEvent.getObjectTimeSeconds(), lDelta.getX(), lDelta.getY());
							/* Update the EasingGroup's ObjectTimeSeconds. */
							lUIEasingGroup.setObjectTimeSeconds(pUIPointerEvent.getObjectTimeSeconds());
						break;
						case DESTROY : 
							/* Kill the filter. */
							return true;
						default : 
							/* Do nothing. */
						break;
					}
				}
				/* Don't let the UIPointerFilter die. */
				return true;
			}
			/* Implement disposal of the UIPointerFilter. */
			@Override public void dispose() { }
		};
		/* Register the UIEasingGroup. */
		pPunchModel.getUISecondsDispatcher().getEventFilters().add(lUIEasingGroup);
		/* Supply the UIPointerFilter. */
		pPunchModel.getUIPointerDispatcher().getEventFilters().add(lUIPointerFilter);
		/* Prevent event dispatch for the duration of the task. (The UIEasingGroup itself will de-assert the flag.) */
		pPunchModel.getUIDeferFilter().getDeferList().add(lUIEasingGroup);
	}
	
	// whilst dragging, update positions, build momentum ideally
	/* Allows the user to slide a set of UIElements. */
	public static final <T extends IUIElement> void onSlideComponents(final IContext pContext, final List<IUIElement> pHierarchy, final PunchModel pPunchModel, final UIPointerEvent pUIPointerEvent, final IGroup<?> pGroup, final List<T> pUIElements, final boolean pIsHorizontal, final boolean pIsVertical) { 
		/* Define the StateBuffer. All operations must be synchronized along it's contents. */
		final EDragState[] lStateBuffer = new EDragState[] { EDragState.DRAG }; 
		/* Define the PointerBuffer. */
		final UIPointerEvent[] lPointerBuffer   = new UIPointerEvent[]{ pUIPointerEvent };
		/* If we got here, we're dragging over a Selective. We'll initialize a drag; this allows users to pan through the Selective cases. */
		final UIEasingGroup lUIEasingGroup = new UIEasingGroup(pPunchModel.getUIUpdateDispatcher(), IEasingConfiguration.CONFIGURATION_APP, ResourceUtils.getSystemTimeSeconds()) {
			/* Define custom disposal operations. */
			@Override public void dispose() { 
				/* Dispose as usual. */
				super.dispose();
				/* Synchronize along the PunchModel's DeferList. */
				synchronized(pPunchModel.getDeferList()) { 
					/* Remove the UIEasingGroup from the UIDeferFilter. */
					pPunchModel.getUIDeferFilter().getDeferList().remove(this);
					/* Clear the DeferList. We won't be interested in the accumulated UIPointerEvent data. */
					pPunchModel.getDeferList().clear();
					/* Implement a distribution. */
					DistributionGlobal.onDistributeHierarchy(pPunchModel, pHierarchy, pGroup, IEasingConfiguration.CONFIGURATION_SMOOTH);
				}
			}
			/* Add some lifetime qualifiers. */
			@Override public final boolean isAlive(final float pCurrentTimeSeconds) { 
				/* Synchronize along the StateBuffer. */
				synchronized(lStateBuffer) { 
					/* Determine if the task is still active. */
					if(DataUtils.getFirstElementOf(lStateBuffer).equals(EDragState.DRAG)) {
						/* Make sure it remains alive. */
						return true;
					}
					else {
						/* Use a standard lifetime. */
						return super.isAlive(pCurrentTimeSeconds);
					}
				}
			}
		};
		/* Register the UIEasingGroup onto the UIDeferFilter's DeferList. */
		pPunchModel.getUIDeferFilter().getDeferList().add(lUIEasingGroup);
		/* Buffer the UIElements involved. (We're going to buffer every case.) */
		for(final T lT : pUIElements) {
			/* Allocate a UIElementPacket. */
			final UIElementPacket lUIElementPacket = new UIElementPacket(lT);
			/* Buffer the UIElementPAcket into the UIEasingGroup. */
			lUIEasingGroup.getEasingPackets().add(lUIElementPacket);
		}
		/* Add the UIElementGroup to the UISecondDispatcher's Event Filters. */
		pPunchModel.getUISecondsDispatcher().getEventFilters().add(lUIEasingGroup);
		
		
		/* Allocate the UIPointerFilter. */
		final IEventsFilter<UIPointerEvent, UIPointerDispatcher> lUIPointerFilter = new IEventsFilter<UIPointerEvent, UIPointerDispatcher>() {
			/* Handle disposal. */
			@Override public void dispose() { }
			/* Handle the UIPointerEvent. */
			@Override public final boolean onHandleEvent(final UIPointerEvent pUIPointerEvent, final UIPointerDispatcher pUIPointerDispatcher) {
				/* Synchronize along the StateBuffer. */
				synchronized(lStateBuffer) {
					/* Determine if the user has released the mouse. */
					if(pUIPointerEvent.getPointerAction().equals(EPointerAction.POINTER_RELEASE)) { 
						/* Assert the destruction state. */
						DataUtils.onPushArrayElement(lStateBuffer, EDragState.DESTROY);
					}
					/* Process the current state. */
					switch(DataUtils.getLastElementOf(lStateBuffer)) { 
						/* Handle a drag update. */
						case DRAG : 
							/* Update the PointerBuffer. (We're always interested in the 'final' UIPointerEvent of a certain task.) */
							DataUtils.onPushArrayElement(lPointerBuffer, pUIPointerEvent);
							/* Fetch the UIPointerDispatcher's Delta. (DXDY) */
							final IVec2.I lDXDY =  pUIPointerDispatcher.onFetchDelta();
							/* Iterate the UIElementPackets. */
							for(final UIElementPacket lUIElementPacket : lUIEasingGroup.getEasingPackets()) { 
								/* Calculate the X and Y. */
								final float lDeltaX = lUIElementPacket.getX() + (pIsHorizontal ? (lDXDY.getX() / pContext.getScale()) : 0.0f);
								final float lDeltaY = lUIElementPacket.getY() + (pIsVertical   ? (lDXDY.getY() / pContext.getScale()) : 0.0f);
								/* Update the bounds of the ongoing EasingGroup. We update the final point, not whatever is currently interpolated because we won't have reached it yet! */
								lUIElementPacket.onUpdateBounds(lUIEasingGroup.getEasingConfiguration(), lUIEasingGroup.getObjectTimeSeconds(), pUIPointerEvent.getObjectTimeSeconds(), lDeltaX, lDeltaY);
							}
							/* Update the EasingGroup's ObjectTimeSeconds. */
							lUIEasingGroup.setObjectTimeSeconds(pUIPointerEvent.getObjectTimeSeconds());
						break;
						case DESTROY : 
							/* Allow the UIPointerFilter to be destroyed. */
							return false;
						default : 
							/* Do nothing. */
						break;
					}
					/* Don't let the UIPointerFilter die. */
					return true; /** TODO: MUST KILL! **/
				}
			}
		};
		
		pPunchModel.getUIPointerDispatcher().getEventFilters().add(lUIPointerFilter);
		
	}
	
	/* Prevent external instantiation of this class. */
	private DragGlobal() { }
	
}