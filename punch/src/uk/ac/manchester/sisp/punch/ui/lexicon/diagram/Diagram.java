package uk.ac.manchester.sisp.punch.ui.lexicon.diagram;

import java.util.List;

import uk.ac.manchester.sisp.punch.PunchModel;
import uk.ac.manchester.sisp.punch.global.PunchGlobal;
import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.collision.RayCastManager;
import uk.ac.manchester.sisp.punch.ui.core.Field;
import uk.ac.manchester.sisp.punch.ui.core.IGroup;
import uk.ac.manchester.sisp.punch.ui.core.context.IContext;
import uk.ac.manchester.sisp.punch.ui.core.encapsulation.IEncapsulation;
import uk.ac.manchester.sisp.punch.ui.drag.global.DragGlobal;
import uk.ac.manchester.sisp.punch.ui.drag.global.SlideGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.ILexical;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.Coupling;
import uk.ac.manchester.sisp.punch.ui.lexicon.courier.ILexiconCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.IDataType.ArrayType;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.DataGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.Decoupler;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.axiom.Axiom;
import uk.ac.manchester.sisp.punch.ui.lexicon.documentation.Comment;
import uk.ac.manchester.sisp.punch.ui.lexicon.literal.ILiteral;
import uk.ac.manchester.sisp.punch.ui.lexicon.literal.array.Array;
import uk.ac.manchester.sisp.punch.ui.lexicon.literal.constants.Dharma;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.IContact;
import uk.ac.manchester.sisp.punch.ui.lexicon.sequencing.ISequential;
import uk.ac.manchester.sisp.punch.ui.update.EUICommand;
import uk.ac.manchester.sisp.punch.ui.update.UIUpdateEvent;
import uk.ac.manchester.sisp.ribbon.common.IVec2;
import uk.ac.manchester.sisp.ribbon.io.EEntryMode;
import uk.ac.manchester.sisp.ribbon.ui.easing.IEasingConfiguration;
import uk.ac.manchester.sisp.ribbon.ui.pointer.UIPointerEvent;
import uk.ac.manchester.sisp.ribbon.utils.DataUtils;

/** TODO: Assert 2D, link orthographic... **/
/** TODO: Support Diagram as Lexical? This allows export... **/
public final class Diagram extends IContext.Impl { 
	
	/* Define the default Serialization UID. */
	private static final long serialVersionUID = 1L;
	
	/* Define the AnonymizedDrag. This handles custom drag operations for Lexical types. */
	private static final ILexiconCourier<IContext> onAnonymizedDrag(final IContext pContext, final UIPointerEvent pUIPointerEvent, final List<IUIElement> pHierarchy, final PunchModel pPunchModel, final boolean[] pIsDelegated) {
		/* Allocate the LexiconCourier. */
		final ILexiconCourier<IContext> lLexiconCourier = new ILexiconCourier.Adapter<IContext>() { 
			/* Iterate the Hierarchy. */
			@Override public final void onCourierTransit(final IContact<?> pContact, final IContext pContext) {
				/* Define the LexiconCourier. */
				final ILexiconCourier<IContact<?>> lLexiconCourier = new ILexiconCourier.Adapter<IContact<?>>() { 
					/* Handle a Coupling. */
					@SuppressWarnings("unchecked") @Override public final <V extends ILexical, T extends ILexical & IGroup<V>> void onCourierTransit(final Coupling<V, T> pCoupling, final IContact<?> pContact) { 
						/* Determine if the Coupling is floating. */
						if(!pCoupling.isClosed()) { 
							/* Fetch the Contact's Decoupler. */
							final Decoupler<IContact<?>> lDecoupler = (Decoupler<IContact<?>>)pHierarchy.get(pHierarchy.indexOf(pContact) - 1);
							/* Synchronize along the Decoupler, give the Contact preference. */
							synchronized(lDecoupler) {
								/* Remove the Contact. */
								lDecoupler.getUIElements().remove(pContact);
								/* Insert the Contact at the end of the Contacts. (This gives it priority in the re-distribution.) */ /** TODO: Backup using code. **/
								lDecoupler.getUIElements().add(pContact);
							}
							/* Implement a periodic drag. */
							DragGlobal.onPeriodicDrag(pContext, pHierarchy, pPunchModel, pUIPointerEvent, pContact, pCoupling);
							/* Assert that the event was handled. */
							pIsDelegated[0] = true;
						}
					}
					/* Handle a Contact. */
					@Override public final void onCourierTransit(final Array pArray, final IContact<?> pContact) { 
						/* Just drag the array. */
						DragGlobal.onEncapsulationDrag(pContext, pHierarchy, pPunchModel, pUIPointerEvent, pArray, true);
						/* Assert that the event was handled. */
						pIsDelegated[0] = true;
					} 
				};
				/* Reverse iterate the hierarchy whilst the event hasn't been delegated. */
				for(int i = pHierarchy.indexOf(pContact) - 1; i >= 0 && (!pIsDelegated[0]); i--) {
					/* Fetch the UIElement at this index. */ /** TODO: Ensure a context only holds a certain kind of element. c'mon. **/
					final IUIElement lUIElement = pHierarchy.get(i);
					/* Ensure we're handling a Lexical. */
					if(lUIElement instanceof ILexical) {
						/* Cast accordingly. */
						final ILexical lLexical = (ILexical)lUIElement;
						/* Export the LexiconCourier. */
						lLexical.onCourierDispatch(lLexiconCourier, pContact);
					}
				}
			}
			/* Handle a Coupling. */
			@Override public final <V extends ILexical, T extends ILexical & IGroup<V>> void onCourierTransit(final Coupling<V, T> pCoupling, final IContext pContext) { 
				/** TODO: Don't pull out if connected? **/
				/* Schedule a Drag. */
				DragGlobal.onEncapsulationDrag(pContext, pHierarchy, pPunchModel, pUIPointerEvent, pCoupling, true);
				/* Assert that the event was handled. */
				pIsDelegated[0] = true;
			}
			/* Handle a Literal. */
			@Override public final void onCourierTransit(final ILiteral<?> pLiteral, final IContext pContext) { 
				/* Schedule a Drag. */
				DragGlobal.onEncapsulationDrag(pContext, pHierarchy, pPunchModel, pUIPointerEvent, pLiteral, true);
				/* Assert that the event was handled. */
				pIsDelegated[0] = true;
			}
			/* Handle a Comment. */
			@Override public final void onCourierTransit(final Comment pComment, final IContext pContext) { 
				/* Schedule a Drag. */
				DragGlobal.onEncapsulationDrag(pContext, pHierarchy, pPunchModel, pUIPointerEvent, pComment, true);
				/* Assert that the event was handled. */
				pIsDelegated[0] = true;
			}
			/* Handle an Axiom. */
			@Override public final <T extends IContact<?>> void onCourierTransit(final Axiom<T> pAxiom, final IContext pContext) { 
				/* Schedule a Drag. */
				DragGlobal.onEncapsulationDrag(pContext, pHierarchy, pPunchModel, pUIPointerEvent, pAxiom, true);
				/* Assert that the event was handled. */
				pIsDelegated[0] = true;
			}

			@Override
			public final <T extends ILexical> void onCourierTransit(final ISequential<T> pSequential, final IContext pContext) { 
				/* Fetch the owning Coupling. */
				final Coupling<?, ?> lCoupling = (Coupling<?, ?>)RayCastManager.onFetchParent(pHierarchy, pSequential);
				/* Fetch the Trend. */
				final IVec2.F lDelta = pPunchModel.getUIPointerDispatcher().onCalculateTrend(new IVec2.F.Impl());
				/* Ensure the user is dragging horizontally. */
				if((Math.abs(lDelta.getX()) > Math.abs(lDelta.getY()))) { 
					/* Synchronize along the Coupling. */
					synchronized(lCoupling) { 
						/* Begin a slide operation. */
						SlideGlobal.onSlide(pContext, pHierarchy, pPunchModel, pUIPointerEvent, lCoupling, lCoupling.getInternals(), true, false);
					}
					/* Assert that the event was handled. */
					pIsDelegated[0] = true;
				}
			}
			
		};
		/* Return the LexiconCourier. */
		return lLexiconCourier;
	}
	
	/* Define the Anonymized. This handles custom release operations for Lexical types. */
	private static final ILexiconCourier<IContext> onAnonymizedRelease(final IContext pContext, final UIPointerEvent pUIPointerEvent, final List<IUIElement> pHierarchy, final PunchModel pPunchModel, final boolean[] pIsDelegated) {
		/* Allocate the LexiconCourier. */
		final ILexiconCourier<IContext> lLexiconCourier = new ILexiconCourier.Adapter<IContext>() {
			/* Handle a Dharma. */
			@Override public final void onCourierTransit(final Dharma pDharma, final IContext pContext) {
				/* Determine if the user double-clicked. */
				if(UIPointerEvent.isDoubleClick(pUIPointerEvent)) { 
					/* Change the value. */
					pDharma.setValue(DataUtils.getCachedBoolean(!pDharma.getValue()));
					/* Export for graphical updates. */
					pPunchModel.getUIUpdateDispatcher().onRibbonEvent(new UIUpdateEvent(pUIPointerEvent.getObjectTimeSeconds(), EUICommand.UPDATE, pDharma));
				}
			}
			/* Handle a Comment. */
			@Override public final void onCourierTransit(final Comment pComment, final IContext pContext) {
				/* Determine if the user double-clicked. */
				if(UIPointerEvent.isDoubleClick(pUIPointerEvent)) { 
					/* Schedule the Comment for updates. */
					Field.onListen(pContext, pHierarchy, (IGroup<?>)RayCastManager.onFetchParent(pHierarchy, pComment), pComment, pPunchModel);
				}
			}
			/* Handle a Coupling. */
			@Override public final <V extends ILexical, T extends ILexical & IGroup<V>>void onCourierTransit(final Coupling<V, T> pCoupling, final IContext pContext) {
				/* Determine if the user has double clicked. */
				if(UIPointerEvent.isDoubleClick(pUIPointerEvent)) {
					/* Define whether we're handling a state change. (A click on the SinkDecoupler or SourceDecoupler.) */
					final boolean lIsExpansion = pHierarchy.contains(pCoupling.getSinkDecoupler()) || pHierarchy.contains(pCoupling.getSourceDecoupler());
					/* Process the result. */
					if(lIsExpansion) {
						/* Change the state of the Coupling. */
						pCoupling.onChangeState(pPunchModel, pHierarchy, IEasingConfiguration.CONFIGURATION_SMOOTH);
					}
					/* Assert that the event was handled. */
					pIsDelegated[0] = true;
				}
			}
		};
		/* Return the LexiconCourier. */
		return lLexiconCourier;
	}
	
	/* Member Variables. */
	
	public Diagram(final boolean pIsEnabled, final boolean pIsVisible, final PunchModel pPunchModel) {
		super(pIsEnabled, pIsVisible);
	}

//	@Override
//	public IVectorPathGroup[] getVectorPathGroups(final ArrayStore.Float pFloatStore, final VectorPathContext pVectorPathContext) {
//		/* Allocate a simple shape for debugging. */
//		return new IVectorPathGroup[]{ 
//			/* Allocate a simple filled Rectangle. (Use IVectorPathGroup.Impl to force evaluation!) */
//			new IVectorPathGroup.Impl(new VectorPath[]{ pVectorPathContext.onRectangle(pFloatStore, 0.0f, 0.0f, this.getWidth(), this.getHeight()).onCreatePath(pFloatStore) }, new IPathDefinition[]{ new IPathDefinition.Fill(ColorGlobal.RGBA_CHARCOAL) })
//		};
//	}
	
	@Override
	public final boolean onHandleEvent(final UIPointerEvent pUIPointerEvent, final List<IUIElement> pHierarchy, final PunchModel pPunchModel) {
		/* Fetch the Iteration index. */
		int i = PunchGlobal.onFetchDelegationIndex(pHierarchy);
		/* Assert the DelegationFlag. If i is null, we'll skip delegation entirely. */
		final boolean[] lIsDelegated = new boolean[]{ (i == DataUtils.JAVA_NULL_INDEX) };
		/* Next, reverse iterate from the iteration index backwards through the hierarchy; ensure we don't handle the Context itself. */
		while((!DataUtils.isZero(i)) && (!lIsDelegated[0])) {
			/* Fetch the UIElement. */
			final IUIElement lUIElement = pHierarchy.get(i);
			/* Determine if we're handling a type of Lexical. */ /** TODO: What's a cleaner way to do this? Likely another Courier. **/
			if(lUIElement instanceof ILexical) {
				/* Fetch the Lexical. */
				final ILexical lLexical = (ILexical)lUIElement;
				/* Handle the PointerIndex. */
				switch(pUIPointerEvent.getPointerIndex()) {
					case LEFT  : 
						/* Process the type of PointerAction. */
						switch(pUIPointerEvent.getPointerAction()) {
							case POINTER_DRAGGED : 
								/* Determine if the Lexical lies directly on top of the Diagram. */
								if(RayCastManager.onFetchParent(pHierarchy, lUIElement).equals(this)) {
									/* Drag the UIElement. */
									DragGlobal.onEncapsulationDrag(this, pHierarchy, pPunchModel, pUIPointerEvent, lLexical, true);
								}
								else {
									/* Export the Drag Courier; supply the Context as a CourierPackage. */
									lLexical.onCourierDispatch(Diagram.onAnonymizedDrag(this, pUIPointerEvent, pHierarchy, pPunchModel, lIsDelegated), this);
								}
							break;
							case POINTER_RELEASE :
								/* Export the Release Courier; supply the Context as a CourierPackage. */
								lLexical.onCourierDispatch(Diagram.onAnonymizedRelease(this, pUIPointerEvent, pHierarchy, pPunchModel, lIsDelegated), this);
							break;
							default : /* Ignore all other types of PointerAction. */ break;
						}
					break;
					case RIGHT : 
						/** TODO: Build in some request to the right-click menu! **/
					break;
					default : /* Ignore other types of PointerIndex. */ break;
				}
			}
			/* Decrement the iteration index. */
			i--;
		}
		/* Do not allow further event propagation. Consume the event. (Nom-nom-nom.) */
		return true;
	}
	
	@Override 
	public final <U extends IUIElement> IEncapsulation onFetchEncapsulation(final List<IUIElement> pHierarchy, final IGroup<U> pEnclosure, final IUIElement pUIElement, final PunchModel pPunchModel) {
		/* Define the EncapsulationBuffer. */
		final IEncapsulation[] lEncapsulationBuffer = new IEncapsulation[]{ super.onFetchEncapsulation(pHierarchy, pEnclosure, pUIElement, pPunchModel) };
		/* Ensure we're handling a Lexical UIElement. */
		if(pUIElement instanceof ILexical) {
			/* Ensure the Enclosure is a Lexical too. */
			if(pEnclosure instanceof ILexical) {
				/* Cast accordingly. */
				final ILexical lLexical   = ((ILexical) pUIElement);
				final ILexical lEnclosure = ((ILexical) pEnclosure);
				/* Define the LexiconCourier. */
				final ILexiconCourier<IEncapsulation[]> lLexiconCourier = new ILexiconCourier.Adapter<IEncapsulation[]>() { 
					/* Handle an Array Type. */
					@Override public final void onCourierTransit(final Array pArray, final IEncapsulation[] pEncapsulationBuffer) { 
						/* First, ensure that the Array is open. */
						if(!pArray.isClosed()) {
							/* Define the LexiconCourier. */
							final ILexiconCourier<IEncapsulation[]> lLexiconCourier = new ILexiconCourier.Adapter<IEncapsulation[]>() { 
								/* Handle a Literal. */
								@Override public final void onCourierTransit(final ILiteral<?> pLiteral, final IEncapsulation[] pEncapsulationBuffer) { 
									/* Allocate the Redirection. (This directs Array-level Encpasulation requests to the Internals.) */
									final IEncapsulation lRedirection = new IEncapsulation.Redirection() {
										/* Specify the Enclosure Targets. */
										@Override public final IGroup<?> getManagingEnclosure() { return pArray;                      }
										@Override public final IGroup<?> getTargetEnclosure()   { return pArray.getActiveInternals(); }
									};
									/* Determine if the Array is uninitialized. */
									if(pArray.isUninitialized()) {
										/* Buffer the Redirection into the EncapsulationBuffer. */
										DataUtils.onPushArrayElement(pEncapsulationBuffer, lRedirection);
										/* Update the Array Contact's DataType. */
										pArray.getContact().setDataType(pArray.getDataDirection(), new ArrayType(pLiteral.getDataType(pArray.getDataDirection())));
									}
									else {
										/* Determine if we're adding the Literal to the Array, or taking it away. */
										final boolean lIsSupplication;
										/* Synchronize along the Array's Internals. */
										synchronized(pArray.getInternals()) { lIsSupplication = !(pArray.getActiveInternals().getUIElements().contains(lLexical)); }
										/* Determine if we're supplying the UIElement. */
										if(lIsSupplication) {
											/* Confirm the types are compatible. */
											final boolean lIsCompatible = DataGlobal.isCompatible(pArray.getDataType(pArray.getDataDirection()).getReference(), pLiteral.getDataType(pLiteral.getDataDirection()));
											/* Assess whether the types are compatible. */
											if(lIsCompatible) {
												/* Allow the insertion. */
												DataUtils.onPushArrayElement(pEncapsulationBuffer, lRedirection);
											}
										}
										else {
											/* Allocate a new Redirection. */
											DataUtils.onPushArrayElement(pEncapsulationBuffer, new IEncapsulation.Redirection() {
												/* Define a custom Encapsulation response. */
												@Override public final <T extends IUIElement> void onEncapsulate(final EEntryMode pEntryMode, final List<IUIElement> pHierarchy, final List<T> pUIElements, final IUIElement pUIElement) {
													/* Perform initial encapsulation. */
													super.onEncapsulate(pEntryMode, pHierarchy, pUIElements, pUIElement);
													/* Determine if the UIElements is empty. */
													if(pUIElements.isEmpty()) {
														/* Reset the Contact's Data Type to emulate the Array's uninitialized state. */
														pArray.getContact().setDataType(pArray.getDataDirection(), Array.TYPE_ARRAY_UNINTIALIZED);
													}
												}
												/* Specify the Enclosure Targets. */
												@Override public final IGroup<?> getManagingEnclosure() { return pArray;                      }
												@Override public final IGroup<?> getTargetEnclosure()   { return pArray.getActiveInternals(); }
											});
										}
									}
								}
								/* Handle an Array Type. */
								@Override public final void onCourierTransit(final Array pArray, final IEncapsulation[] pEncapsulationBuffer) {
									/* Export the Array as a Literal type. */
									this.onCourierTransit((ILiteral<?>)pArray, pEncapsulationBuffer);
								}
							};
							/* Export the LexiconCourier to the specific instance of the Lexical. */
							lLexical.onCourierDispatch(lLexiconCourier, pEncapsulationBuffer);
						}
					}
					/* Handle a Coupling Type. */
					@Override public final <V extends ILexical, T extends ILexical & IGroup<V>> void onCourierTransit(final Coupling<V, T> pCoupling, final IEncapsulation[] pEncapsulationBuffer) { 
						/* Determine whether the Coupling is open. */
						if(!pCoupling.isClosed()) {
							/* Allocate the Internals Redirection. */
							final IEncapsulation lRedirection = new IEncapsulation.Redirection() {
								/* Allocate the Redirection. (This directs Array-level Encpasulation requests to the Internals.) */
								@Override public final IGroup<?> getTargetEnclosure()   { return pCoupling.getActiveInternals(); }
								@Override public final IGroup<?> getManagingEnclosure() { return pCoupling;                      }
							};
							/* Define the LexiconCourier. */
							final ILexiconCourier<IEncapsulation[]> lLexiconCourier = new ILexiconCourier.Adapter<IEncapsulation[]>() { 
								/* Handle a Comment. */
								@Override public final void onCourierTransit(final Comment pComment, final IEncapsulation[] pEncapsulationBuffer) {
									/* Buffer the Redirection. */
									DataUtils.onPushArrayElement(pEncapsulationBuffer, lRedirection);
								}
								/* Handle the Literal. */
								@Override public void onCourierTransit(final ILiteral<?> pLiteral, final IEncapsulation[] pEncapsulationBuffer) {
									/* Buffer the Redirection. */
									DataUtils.onPushArrayElement(pEncapsulationBuffer, lRedirection);
								}
								/* Handle an Axiom. */
								@Override public final <X extends IContact<?>> void onCourierTransit(final Axiom<X> pAxiom, final IEncapsulation[] pEncapsulationBuffer) {
									/* Buffer the Redirection. */
									DataUtils.onPushArrayElement(pEncapsulationBuffer, lRedirection);
								}
								/* Handle a Coupling. */
								@Override public final <X extends ILexical, Y extends ILexical & IGroup<X>> void onCourierTransit(final Coupling<X, Y> pCoupling, final IEncapsulation[] pEncapsulationBuffer) {
									/* Buffer the Redirection. */
									DataUtils.onPushArrayElement(pEncapsulationBuffer, lRedirection);
								}
								/* Handle a Contact. */
								@Override public final void onCourierTransit(final IContact<?> pContact, final IEncapsulation[] pEncapsulationBuffer) { 
									/* Determine the Decoupler that the user is dragging over. */
									final Decoupler<?>   lDecoupler   = pHierarchy.contains(pCoupling.getSinkDecoupler()) ? pCoupling.getSinkDecoupler() : pCoupling.getSourceDecoupler();
									/* Define the Redirection. */
									final IEncapsulation lRedirection = new IEncapsulation.Redirection() {
										/* Allocate the Redirection. (Point towards the Decoupler.) */
										@Override public final IGroup<?> getTargetEnclosure()   { return lDecoupler; }
										@Override public final IGroup<?> getManagingEnclosure() { return pCoupling;  }
										/* Define a custom encapsulation process. */
										@Override public final <Z extends IUIElement> void onEncapsulate(final EEntryMode pEntryMode, final List<IUIElement> pHierarchy, final List<Z> pUIElements, final IUIElement pUIElement) {
											/* Encapsulate as normal. */
											super.onEncapsulate(pEntryMode, pHierarchy, pUIElements, pUIElement);
											/* Assign the DataDirection of the Decoupler to the Contact to enforce consistency and analysis validity. */
											pContact.setDataDirection(lDecoupler.getDataDirection());
										}
									};
									/* Buffer the Redirection. */
									DataUtils.onPushArrayElement(pEncapsulationBuffer, lRedirection);
								}
								/* Handle an Array. (Treat it like a Literal.) */
								@Override public final void onCourierTransit(final Array pArray, final IEncapsulation[] pEncapsulationBuffer) { this.onCourierTransit((ILiteral<?>)pArray, pEncapsulationBuffer); }
							};
							/* Export the LexiconCourier to the Lexical. */
							lLexical.onCourierDispatch(lLexiconCourier, pEncapsulationBuffer);
						}
					}
				};
				/* Export the LexiconCourier to process the EncapsulationBuffer. */
				lEnclosure.onCourierDispatch(lLexiconCourier, lEncapsulationBuffer);
			}
		}
		/* Return the contents of the EncapsulationBuffer. */
		return DataUtils.getLastElementOf(lEncapsulationBuffer);
	}
	
}