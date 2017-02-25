package uk.ac.manchester.sisp.punch.ui.lexicon.cascade.generation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import uk.ac.manchester.sisp.punch.ui.core.IGroup;
import uk.ac.manchester.sisp.punch.ui.distribution.global.DistributionGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.ILexical;
import uk.ac.manchester.sisp.punch.ui.lexicon.cascade.Cascade;
import uk.ac.manchester.sisp.punch.ui.lexicon.compilation.Compilation;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.Coupling;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.GParser;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.IDriver;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.branching.constants.Sequencer;
import uk.ac.manchester.sisp.punch.ui.lexicon.courier.ILexiconCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.IDataType;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.IDataType.ArrayType;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.courier.IDataCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.DataGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.EDataDirection;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.IDataConduit;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.types.BooleanType;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.types.NumberType;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.types.NumberType.F32;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.types.NumberType.I32;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.types.VoidType;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.Decoupler;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.axiom.Axiom;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.directional.SinkDecoupler;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.directional.SourceDecoupler;
import uk.ac.manchester.sisp.punch.ui.lexicon.functions.Print;
import uk.ac.manchester.sisp.punch.ui.lexicon.functions.courier.IFunctionCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.global.LexiconGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.literal.ILiteral;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.IContact;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.constants.Feedback;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.constants.Iteration;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.constants.Selector;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.constants.Tunnel;
import uk.ac.manchester.sisp.punch.ui.lexicon.sequencing.ISequential;
import uk.ac.manchester.sisp.punch.ui.update.EUICommand;
import uk.ac.manchester.sisp.punch.ui.update.UIUpdateEvent;
import uk.ac.manchester.sisp.punch.ui.update.easing.ui.UIEasingGroup;
import uk.ac.manchester.sisp.punch.ui.update.easing.ui.UIElementPacket;
import uk.ac.manchester.sisp.ribbon.common.IDim2;
import uk.ac.manchester.sisp.ribbon.common.IVec2;
import uk.ac.manchester.sisp.ribbon.utils.DataUtils;
import uk.ac.manchester.sisp.ribbon.utils.MathUtils;
import uk.ac.manchester.sisp.ribbon.utils.ResourceUtils;

public final class Cascader extends GParser<IDriver, CState<?, ?>> { 
	
	public Cascader(final UIEasingGroup pUIEasingGroup) { 
		/* Assert that the Cascader uses prediction. */
		super(pUIEasingGroup);
	}

	@Override
	public final void onCarry(final IDriver pDriver, final IDataConduit pSink, final CState<?, ?> pCState, final Compilation pCompilation) {
		/* Handle the super implementation. */
		super.onCarry(pDriver, pSink, pCState, pCompilation);
		/* Insert a default Cascade. */
		this.onInsertCascade(pDriver.getDataConduit(), pSink, pCState, pCompilation);
	}

	@Override
	public final void onError(final IDriver pDriver, final IDataConduit pSink, final CState<?, ?> pCState, final Compilation pCompilation) { 
		/* Handle as usual. */
		super.onError(pDriver, pSink, pCState, pCompilation);
		/* Insert a standard Cascade. */
		this.onInsertCascade(pDriver.getDataConduit(), pSink, pCState, pCompilation);
	}
	
	@Override
	public final <V extends IContact<?>> void onCourierTransit(final Axiom<V> pAxiom, final Compilation pCompilation) {
		/* Assert type propagation. */
		this.onPropagateTypes(pAxiom, pCompilation);
		/* Handle as normal. */
		super.onCourierTransit(pAxiom, pCompilation);
		/* Enforce a boundary extension after processing the Axiom. */
		this.onBoundaryExtension(pAxiom, pCompilation.getX(), this.getStateStack().peek(), pCompilation);
		/* Fetch the Axiom's Contacts. */
		final List<V> lContacts = pAxiom.getUIElements();
		/* Filter the Contacts into Sinks and Sources. */
		final List<V> lSources  = this.onFilterDirection(lContacts, EDataDirection.SOURCE);
		/* By default, the ExitStack will be missing drivers because it's empty. So we'll fake some. */
		for(final V lContact : lSources) {
			/* Define a fake return driver along the Sourcing Contact. */
			this.onInsertCascade(lContact, lContact, 0, pCompilation.getSubIndex(this, lContact), this.getStateStack().peek(), pCompilation);
		}
	}

	@Override
	public final void onStarved(final IDataConduit pDataConduit, final IDataType<?> pExpectedType, final CState<?, ?> pCState, final Compilation pCompilation) { 
		/* Handle the parent implementation. */
		super.onStarved(pDataConduit, pExpectedType, pCState, pCompilation);
		/* Initialize the XPosition to Cascade between. We'll start with the current Compilation's XPosition. */
		      int     lXPosition  = pCompilation.getX();
		/* Fetch the Index. */
		final Integer lIndex      = pCompilation.getAbsoluteIndex(); 
		/* Allocate a boolean to determine if we've hit the finish point for Starved Cascade Generation. */
		      boolean lIsFinished = false;
		/* Fetch the Cascade in the current context. */
		final Cascade lCascade    = DataUtils.getLastElementOf(pCState.getCascadeMap().get(lIndex));
		/* Determine if there's an existing Starved Cascade within the current state. */
		if(DataUtils.isNotNull(lCascade) && DataUtils.isNull(lCascade.getSourceConduit())) { 
			/* Just extend this one. */
			lCascade.setWidth(lXPosition - lCascade.getX());
			/* Update the ExtensionMap. */
			pCState.getExtensionMap().put(lIndex, (lCascade.getX() + lCascade.getWidth()));
		}
		else {
			/* We're going to iterate the whole CascadeStack and buffer starved Cascades. */
			for(int i = 0; i < this.getStateStack().size() && !lIsFinished; i++) { 
				/* Fetch the CState. */
				final CState<?, ?>  lCState      = this.getStateStack().get(i);
				/* Fetch the Progression. */
				final int           lProgression = lCState.getExtensionMap().get(lIndex);
				/* Fetch the Cascades at this Index. */
				final List<Cascade> lCascades    = lCState.getCascadeMap().get(lIndex);
				/* Determine if we're finished. (This is where we find an existing driver.) */
				                    lIsFinished |= !lCascades.isEmpty();
				/* Insert a Cascade. */
				this.onInsertCascade(null, pDataConduit, lXPosition - lProgression, pCompilation.getAbsoluteIndex(), lCState, pCompilation);
				/* Update the XPosition for the next State. */
				lXPosition = lProgression;
			}
		}
	}
	
	@Override
	public final void onTrashAll(final Compilation pCompilation) { 
		/* Trash as normal. */
		super.onTrashAll(pCompilation);
		/* Fetch the State. */
		final CState<?, ?> lCState = this.getStateStack().peek();
		/* Clear the Cascades in the CascadeMap. */
		for(final Entry<Integer, List<Cascade>> lEntrySet : lCState.getCascadeMap().entrySet()) { 
			/* Empty the Branches at this Index. */
			lEntrySet.getValue().clear();
		}
		/* Reset the ExtensionMap. */
		for(final Entry<Integer, Integer> lEntrySet : lCState.getExtensionMap().entrySet()) { 
			/* Overwrite the entry. */
			lCState.getExtensionMap().put(lEntrySet.getKey(), lCState.getX());
		}
	}

	/* High-level Cascade implementation. Assumes a given Cascade width given the state of the Compilation. */
	private final void onInsertCascade(final IDataConduit pSourceConduit, final IDataConduit pSinkConduit, final CState<?, ?> pCState, final Compilation pCompilation) { 
		/* Calculate the Width of the Cascade based on the progression of the ExtensionMap. */
		final int lWidth = pCompilation.getX() - pCState.getExtensionMap().get(pCompilation.getAbsoluteIndex());
		/* Call the specific implementation of the Cascade insertion method. */
		this.onInsertCascade(pSourceConduit, pSinkConduit, lWidth, pCompilation.getAbsoluteIndex(), pCState, pCompilation);
	}
	
	/* Inserts a Cascade on the parameterized CState of a specified width at a given Index. The XPosition in determined by the progression of the CState's ExtensionMap at the given Index. */
	private final void onInsertCascade(final IDataConduit pSourceConduit, final IDataConduit pSinkConduit, final int pWidth, final Integer pIndex, final CState<?, ?> pCState, final Compilation pCompilation) { 
		/* Allocate the Cascade. */
		final Cascade lCascade = new Cascade(pCState.getExtensionMap().get(pIndex), pWidth, pSourceConduit, pSinkConduit);
		/* Update the ExtensionMap. */
		pCState.getExtensionMap().put(pIndex, (lCascade.getX() + lCascade.getWidth()));
		/* Buffer the Cascade into the CascadeMap. */
		pCState.getCascadeMap().get(pIndex).add(lCascade);
	}

	@Override
	protected final void onIncreasedScope(final SinkDecoupler pSinkDecoupler, final CState<?, ?> pCState, final Compilation pCompilation) { 
		/* Determine if we're handling a sub-call. */
		if(pCompilation.isSubCall()) { 
			/* Propagate the types. */
			this.onPropagateTypes(pSinkDecoupler, pCompilation);
		}
		/* Handle the parent call. */
		super.onIncreasedScope(pSinkDecoupler, pCState, pCompilation);
		/* Initialize the Boundary extensions. */
		this.onBoundaryExtension(pSinkDecoupler, DataUtils.getCachedInteger(pCState.getX()), pCState, pCompilation); 
		/* Let's initialize the Arrays here too; let's see what Extensions we'll be using. */
		for(final Entry<Integer, Integer> lEntrySet : pCState.getExtensionMap().entrySet()) { 
			/* Initialize the Cascades at this Index. */
			pCState.getCascadeMap().put(lEntrySet.getKey(), new ArrayList<Cascade>(0));
		}
		/* Determine if we're handling a sub-call. */
		if(pCompilation.isSubCall()) { 
			/* Iterate the affected Indices. */
			for(final Entry<Integer, Integer> lEntrySet : pCState.getExtensionMap().entrySet()) { 
				/* Fetch the last-most Cascade at this Index. */
				final Cascade lCascade = DataUtils.getLastElementOf(this.getStateStack().peek().getCascadeMap().get(lEntrySet.getKey()));
				/* Ensure the Cascade isn't null and it isn't a Starved Cascade. */
				if(DataUtils.isNotNull(lCascade) && !lCascade.isStarved()) {
					/* Extend the Cascade to the SinkDecoupler boundary. */
					lCascade.setWidth(pCompilation.getX() - lCascade.getX());
					/* Initialize a Cascade on the CState. */
					pCState.getCascadeMap().get(lEntrySet.getKey()).add(new Cascade(pCompilation.getX(), 0, lCascade.getSourceConduit(), lCascade.getSinkConduit()));
				}
			}
		}
		/* Buffer the Sequencer. */
		pCState.getBranches().add(new Sequencer(pCState));
	}
	
	/** TODO: could accumulate a mapping in the state now **/
	@Override
	protected final void onDecreasedScope(final SourceDecoupler pSourceDecoupler, final Coupling<?, ?> pCoupling, final CState<?, ?> pGState, final Compilation pCompilation) { 
		/* Handle as normal. */
		super.onDecreasedScope(pSourceDecoupler, pCoupling, pGState, pCompilation);
		/* Determine if we're handling the first state. */
		if(pGState.getIndex() == 0) { 
			/* Propagate the types. */
			this.onPropagateTypes(pSourceDecoupler, pCompilation);
		}
		/* Allocate a List to hold the Cascades. */
		final List<Cascade> lCascades = new ArrayList<Cascade>();
		/* Allocate a List to hold the Defunct Cascades. */
		final List<Cascade> lDefunct;
		/* Cast the Group. */
		@SuppressWarnings("unchecked") final ISequential<ILexical> lSequential = (ISequential<ILexical>)pGState.getContents().get(pGState.getIndex()); /** TODO: Horrendous architecture! This WILL cause issues later. **/
		/* Synchronize along the Defunct State's Internals. */
		synchronized(lSequential) { 
			/* Fetch the Defunct Cascades. */
			lDefunct = LexiconGlobal.onAccumulateCascades(lSequential.getUIElements());
		}
		/* Iterate the Defunct State's CascadeMap. */
		for(final Entry<Integer, List<Cascade>> lEntrySet : pGState.getCascadeMap().entrySet()) { 
			/* Calculate the YPosition. */
			final int lYPosition = ((lEntrySet.getKey() - pCompilation.getAbsoluteIndex()) * LexiconGlobal.CODE_DIM_HEIGHT_UNIT);
			/* Iterate the Cascades. */
			for(final Cascade lCascade : lEntrySet.getValue()) { 
				/* Withdraw the XOffset from the Cascade. */
				lCascade.setX(lCascade.getX() - pGState.getX());
				/* Assign the YPosition to the Cascade. */
				lCascade.setY(lYPosition);
				/* Buffer the Cascade into the Cascades. */
				lCascades.add(lCascade);
			}
		}
		/* Determine if we want to trade Cascades at all. */
		if(!pCoupling.isClosed()) { 
			/* Swap the Cascades. */
			this.onSwapCascades(lSequential, lDefunct, lCascades);
		}
		else {
			/* Iterate the Defunct Cascades. */
			for(final Cascade lDefunctary : lDefunct) {
				/* Fetch the UIEasingPacket. */
				final UIElementPacket lUIElementPacket = DistributionGlobal.onFetchPacket(this.getUIEasingGroup(), lDefunctary);
				/* Remove the UIElementPacket. */
				this.getUIEasingGroup().getEasingPackets().remove(lUIElementPacket);
			}
		}
		/* Determine if we're handling a sub-call. If so, we'll need to update the extension of the next state along the StateStack to prevent overdraw! */
		if(pCompilation.isSubCall()) { 
			/* Fetch the SourceDecoupler's Dimension. */
			final IDim2.I      lDimension = (IDim2.I)this.onDefinePosition(pSourceDecoupler, pCompilation);
			/* Pop the StateStack. */
			final CState<?, ?> lCState    = this.getStateStack().pop();
			/* Implement a boundary extension on the parent state. (The old state is already popped!) */
			this.onBoundaryExtension(pSourceDecoupler, (pCompilation.getX() + lDimension.getWidth()), this.getStateStack().peek(), pCompilation);
			/* Push the CState back onto the StateStack. */
			this.getStateStack().push(lCState);
		}
		
	}
	
	/* Manages Cascade implementation and graphical context allocation for a Coupling's Cascades. Attempts to reuse Cascades wherever possible. */
	private final void onSwapCascades(final ISequential<ILexical> pSequential, final List<Cascade> pDefunct, final List<Cascade> pCascades) { 
		/* Iterate the Cascades. */
		for(int i = pCascades.size() - 1; i >= 0; i--) {
			/* Fetch the Cascade. */
			final Cascade lCascade = pCascades.get(i);
			/* Allocate the Search Metric. */
			      boolean lIsFound = false;
			/* Iterate the Defunct Cascades. */
			for(int j = pDefunct.size() - 1; j >= 0 && (!lIsFound); j--) {
				/* Fetch the Defunct Cascade. */	
				final Cascade lDefunct = pDefunct.get(j);
				/* Ensure we're handling valid connections. */
				if(DataUtils.isNotNull(lCascade.getSourceConduit()) && DataUtils.isNotNull(lDefunct.getSourceConduit())) { 
					/* Test the Sources too. */
					lIsFound |= lDefunct.getSinkConduit().equals(lCascade.getSinkConduit()) && lDefunct.getSourceConduit().equals(lCascade.getSourceConduit());
				}
				/* Determine if the Cascade already exists. */
				if(lIsFound) { 
					/* Remove the Cascades. */
					 pDefunct.remove(lDefunct);
					pCascades.remove(lCascade);
					/* Allocate the UIElementPacket. */
					final UIElementPacket lUIElementPacket = DistributionGlobal.onFetchPacket(this.getUIEasingGroup(), lDefunct);
					/* Determine whether the Cascade isn't shape changing. */
					if(!lUIElementPacket.isTransforming()) {
						/* Force an update along the Cascades' graphical implementation anyway. */
						this.getUIEasingGroup().getUIUpdateDispatcher().onRibbonEvent(new UIUpdateEvent(ResourceUtils.getSystemTimeSeconds(), EUICommand.UPDATE, lDefunct));
					}
					/* Update the bounds. */
					MathUtils.setPosition (lUIElementPacket, lCascade.getX(), lCascade.getY());
					/* Update the dimension. */
					MathUtils.setDimension(lUIElementPacket, lCascade);
				}
			}
		}
		/* Iterate the Defunct Cascades. */
		for(final Cascade lDefunct : pDefunct) {
			/* Fetch the UIEasingPacket. */
			final UIElementPacket lUIElementPacket = DistributionGlobal.onFetchPacket(this.getUIEasingGroup(), lDefunct);
			/* Remove the UIElementPacket. */
			this.getUIEasingGroup().getEasingPackets().remove(lUIElementPacket);
		}
		/* Establish their graphical context. */
		this.getUIEasingGroup().getUIUpdateDispatcher().onRibbonEvent(new UIUpdateEvent(ResourceUtils.getSystemTimeSeconds(), EUICommand.CREATE, pCascades.toArray(new Cascade[pCascades.size()])) {
			/* Define custom disposal upon allocation of the graphical elements. */
			@Override public final void dispose() { 
				/* Implement standard disposal. */
				super.dispose();
				/* Synchronize along the Coupling. */
				synchronized(pSequential) {
					/* Add all remaining Cascades. */
					pSequential.getUIElements().addAll(0, pCascades);
					/* Remove the remaining Defunct Cascades. */
					pSequential.getUIElements().removeAll(pDefunct);
					/* Clear the Cascades. */
					pCascades.clear();
					/* Fetch all the Cascades within the Coupling. */
					for(final ILexical lLexical : pSequential.getUIElements()) {
						/* Export the accumulator. */
						lLexical.onCourierDispatch(LexiconGlobal.COURIER_CASCADE_ACCUMULATOR, pCascades);
					}
					/* Remove all the Cascades from the Coupling's UIElements. */
					pSequential.getUIElements().removeAll(pCascades);
					/* Re-insert the Cascades; now they won't overlap! */
					pSequential.getUIElements().addAll(0, pCascades);
				}
				/* Destroy their graphical context. */
				getUIEasingGroup().getUIUpdateDispatcher().onRibbonEvent(new UIUpdateEvent(ResourceUtils.getSystemTimeSeconds(), EUICommand.DESTROY, pDefunct.toArray(new Cascade[pDefunct.size()])));
			} 
		});
	}
	
	/* Updates the ExtentMap for a given CState so that the Indices the Decoupler lays over align.  */
	private final void onBoundaryExtension(final Decoupler<?> pDecoupler, final Integer pOffset, final CState<?, ?> pCState, final Compilation pCompilation) { 
		/* Fetch the Decoupler's Dimension. */
		final IDim2.I lDimension = (IDim2.I)this.onDefinePosition(pDecoupler, pCompilation);
		/* Fetch the Height of the Decoupler. (Enforce dimension restrictions using a coerced height.) */
		      int     lHeight    = lDimension.getHeight();
		/* Here initialize the ExtensionMap for the Coupling. All indices must be initialized to zero to show that as of yet, none of the data has passed. */
		while(lHeight > 0) { 
			/* Decrement the Height by the CODE_DIM_HEIGHT_UNIT. */
			lHeight -= LexiconGlobal.CODE_DIM_HEIGHT_UNIT;
			/* Calculate the Index. */
			final Integer lIndex = ((lHeight / LexiconGlobal.CODE_DIM_HEIGHT_UNIT) + pCompilation.getAbsoluteIndex());
			/* Determine if it doesn't contain a key. */
			if(!pCState.getExtensionMap().containsKey(lIndex)) { /** TODO: Do at State Initialization! **/ 
				/* Initialize the ExtensionMap for this Index. */
				pCState.getExtensionMap().put(lIndex, pCState.getX());
			}
			/* Determine whether the Decoupler feeds at this Index. */
			if(this.isFeedIndex(pDecoupler, lIndex, pCompilation)) { 
				/* Initialize this Index on the CState. */
				pCState.getExtensionMap().put(lIndex, pOffset);
			}
		}
	}

	/* Determine if the Decoupler contains a Contact at the corresponding Index. */
	private final boolean isFeedIndex(final Decoupler<?> pDecoupler, final Integer pIndex, final Compilation pCompilation) {
		/* Allocate the Search Metric. */
		boolean lIsFeeding = false;
		/* Iterate the Contacts. */
		for(int i = 0; i < pDecoupler.getUIElements().size() && !lIsFeeding; i++) { 
			/* Fetch the Contact's Index. */
			final Integer lIndex     = pCompilation.getSubIndex(this, pDecoupler.getUIElements().get(i));
			/* Update the Search Metric. */
			              lIsFeeding = lIndex.equals(pIndex);
		}
		/* Return the Search Metric. */
		return lIsFeeding;
	}
	
	/* Asserts type propagation along the Cascades. */
	private final void onPropagateTypes(final ILexical pLexical, final Compilation pCompilation) { 
		/* Define the LexiconCourier. */
		final ILexiconCourier<Cascader> lLexiconCourier = new ILexiconCourier.Adapter<Cascader>() {
			/* Handle a Decoupler. */
			@Override public final <V extends IContact<?>> void onCourierTransit(final Decoupler<V> pDecoupler, final Cascader pCascader) { 
				/* Iterate the Decoupler's Contacts. */
				for(final IContact<?> lContact : pDecoupler.getUIElements()) { 
					/* Fetch the Position. */
					final IVec2.I lPosition = pCascader.onDefinePosition(lContact, pCompilation);
					/* Update the LocalOffset. */
					  MathUtils.onSupplyOffset(pCompilation.getLocalOffset(), lPosition);
					/* Define type propagation. */
					  pCascader.onPropagateTypes(lContact, pCompilation);
					/* Withdraw upon the LocalOffset. */
					MathUtils.onWithdrawOffset(pCompilation.getLocalOffset(), lPosition);
				}
			}
			/* Handle a Selector. */
			@Override public final void onCourierTransit(final Selector pSelector, final Cascader pCascader) { 
				/* Calculate the Index. */
				final Integer lIndex  = pCompilation.getAbsoluteIndex();
				/* Fetch the Driving Type. */
				final IDriver lDriver = pCascader.onFetchDriver(lIndex);
				/* Allocate the TypeBuffer. We'll use this to define the DataTypes for the Dynamic Selector. */
				final IDataType<?>[] lTypeBuffer = new IDataType<?>[] { DataGlobal.TYPE_VOID, DataGlobal.TYPE_VOID, DataGlobal.TYPE_VOID };
				/* Determine if we're handling a valid driver. */
				if(DataUtils.isNotNull(lDriver)) { 
					/* Define the DataCourier. */
					final IDataCourier<Cascader> lDataCourier = new IDataCourier.Adapter<Cascader>() { 
						/* Handle a BooleanType. */
						@Override public final void onCourierTransit(final BooleanType pBooleanType, final Cascader pCascader) { 
							/* Assert compatible propagation. */
							Arrays.fill(lTypeBuffer, pBooleanType);
						}
					};
					/* Export the DataCourier. */
					lDriver.getDataType(EDataDirection.SOURCE).onCourierDispatch(lDataCourier, pCascader);
				}
				/* Copy the contents of the TypeBuffer into the Selector's DataTypes. */
				System.arraycopy(lTypeBuffer, 0, pSelector.getDataTypes(), 0, lTypeBuffer.length);
			}
			/* Handle an Iteration. */
			@Override public final void onCourierTransit(final Iteration pIteration,final Cascader pCascader) { 
				/* Calculate the Index. */
				final Integer lIndex  = pCompilation.getAbsoluteIndex();
				/* Fetch the Driving Type. */
				final IDriver lDriver = pCascader.onFetchDriver(lIndex);
				/* Determine if we're handling a valid driver. */
				if(DataUtils.isNotNull(lDriver)) { 
					/* Define the DataCourier. */
					final IDataCourier<Cascader> lDataCourier = new IDataCourier<Cascader>() { /** TODO: Use the other approach! **/
						/* Handle a BooleanType. */
						@Override public final void onCourierTransit(final BooleanType pBooleanType, final Cascader pCascader) { 
							/* Assert compatible propagation. */
							pIteration.getDataTypes()[EDataDirection.SINK.ordinal()  ] = pBooleanType;
							/* Have the terminal provide an iteration count. */
							pIteration.getDataTypes()[EDataDirection.SOURCE.ordinal()] = DataGlobal.TYPE_I32;
						}
						/* Handle an ArrayType. */
						@Override public final void onCourierTransit(final ArrayType pArrayType, final Cascader pCascader) { 
							/* Assert compatible propagation. */
							pIteration.getDataTypes()[EDataDirection.SINK.ordinal()  ] = pArrayType;
							/* Index the Array. */
							pIteration.getDataTypes()[EDataDirection.SOURCE.ordinal()] = pArrayType.getReference();
						}
						/* Unused Handlers. */
						@Override public final void onCourierTransit(final           F32        pF32, final Cascader pCascader) { Arrays.fill(pIteration.getDataTypes(), DataGlobal.TYPE_VOID); }
						@Override public final void onCourierTransit(final           I32        pI32, final Cascader pCascader) { Arrays.fill(pIteration.getDataTypes(), DataGlobal.TYPE_VOID); }
						@Override public final void onCourierTransit(final NumberType<?> pNumberType, final Cascader pCascader) { Arrays.fill(pIteration.getDataTypes(), DataGlobal.TYPE_VOID); }
						@Override public final void onCourierTransit(final      VoidType   pVoidType, final Cascader pCascader) { Arrays.fill(pIteration.getDataTypes(), DataGlobal.TYPE_VOID); }
					};
					/* Export the DataCourier. */
					lDriver.getDataType(EDataDirection.SOURCE).onCourierDispatch(lDataCourier, pCascader);
				}
				else { 
					/* Assert a faulty propagation. */
					Arrays.fill(pIteration.getDataTypes(), DataGlobal.TYPE_VOID);
				}
			}
			/* Handle a Feedback. */
			@Override public final void onCourierTransit(final Feedback pFeedback, final Cascader pCascader) { 
				/* Okay, we're going to fetch the corresponding Conduit on the Coupling's SinkDecoupler. */
				final Coupling<?, ?> lParent      = pCompilation.onFetchCoupling();
				/* Allocate a LocalOffset that we can use to access alternate regions of the Coupling. */
				final IVec2.I.W      lLocalOffset = new IVec2.I.Impl();
				/* Latch onto the Feedback's Index. */
				final Integer        lIndex       = pCompilation.getAbsoluteIndex();
				/* Supply the Feedback's Location. */
				MathUtils.onSupplyOffset(lLocalOffset, pCascader.onDefinePosition(pFeedback, pCompilation));
				/* Supply the SourceDecoupler's Location. (Here we take advantage of the fact we know the Feedback exists along the SourceDecoupler only.) */
				MathUtils.onSupplyOffset(lLocalOffset, pCascader.onDefinePosition(lParent.getSourceDecoupler(), pCompilation));
				/* Withdraw the LocalOffset from the Compilation. */
				MathUtils.onWithdrawOffset(pCompilation, lLocalOffset);
					/* Next we'll iterate the SinkDecoupler's Contacts. */
					synchronized(lParent.getSinkDecoupler()) { 
						/* Define a metric for tracking if the value was assigned. */
						boolean lIsAssigned = false;
						/* Iterate the Contacts. */
						for(final IContact<?> lContact : lParent.getSinkDecoupler().getUIElements()) { 
							/* Determine whether the Contact rests at the same location as the Feedback. */
							if(pCompilation.getSubIndex(pCascader, lContact).equals(lIndex) && ((lContact instanceof Tunnel) || (lContact instanceof Selector))) { /** TODO: Make more advanced! **/
								/* Feed the Contact's Sourcing type through the Feedback. */
								Arrays.fill(pFeedback.getDataTypes(), lContact.getDataType(EDataDirection.SOURCE));
								/* Assert that the Feedback's DataTypes have been assigned. */
								lIsAssigned = true;
								/* End the iteration. */
								break;
							}
						}
						/* Determine if the type wasn't assigned. */
						if(!lIsAssigned) { 
							/* Assert a faulty propagation. */
							Arrays.fill(pFeedback.getDataTypes(), DataGlobal.TYPE_VOID);
						}
					}
				/* Supply the LocalOffset back onto the Compilation. */
				MathUtils.onSupplyOffset  (pCompilation, lLocalOffset);
			}
			/* Handle a Tunnel. */
			@Override public final void onCourierTransit(final Tunnel pTunnel, final Cascader pCascader) { 
				/* Feed the driving type. */
				pCascader.onFeedType(pTunnel, pCompilation.getAbsoluteIndex(), pCompilation);
			}
			/* Handle an Axiom. */
			@Override public final <T extends IContact<?>> void onCourierTransit(final Axiom<T> pAxiom, final Cascader pCascader) {
				/* Allocate a FunctionCourier. */
				final IFunctionCourier<Cascader> lFunctionCourier = new IFunctionCourier.Adapter<Cascader>() {
					/* Handle a Print Function. */
					@Override public final void onCourierTransit(final Print pPrint, final Cascader pCascader) { 
						/* Feed the driving type. */
						pCascader.onFeedType(pPrint.getA(), pCompilation.getAbsoluteIndex(), pCompilation);
					}
				};
				/* Export the FunctionCourier. */
				pAxiom.onCourierDispatch(lFunctionCourier, pCascader);
			}
		};
		/* Export the LexiconCourier. */
		pLexical.onCourierDispatch(lLexiconCourier, this);
	}
	
	/* Feeds the driving type through a Dynamic Contact. */
	private final void onFeedType(final IContact.Dynamic pDynamic, final Integer pIndex, final Compilation pCompilation) { 
		/* Fetch the Driver. */
		final IDriver lCDriver = this.onFetchDriver(pIndex);
		/* Apply the bounded type. */
		Arrays.fill(pDynamic.getDataTypes(), DataUtils.isNotNull(lCDriver) ? lCDriver.getDataType(EDataDirection.SOURCE) : DataGlobal.TYPE_VOID);
	}

	/* Driver Implementations. */
	@Override protected final IDriver   onAlloc(final IContact<?> pContact, final IDataType<?>   pDataType,                                   final Compilation pCompilation) { return new IDriver.Impl(pContact, pDataType);       }
	@Override protected final IDriver onDefault(final IContact<?> pContact, final IDataType<?>   pDataType,                                   final Compilation pCompilation) { return new IDriver.Impl(pContact, pDataType);       }
	@Override protected final IDriver   onAlloc(final Iteration pIteration, final IDataType<?> pDriverType, final IDataType<?> pSourcingType, final Compilation pCompilation) { return new IDriver.Impl(pIteration, pSourcingType); }
	
	/* Literal Implementation. */
	@Override protected final IDriver   onAlloc(final ILiteral<?> pLiteral, final IDataType<?>   pDataType, final Compilation pCompilation) {
		/* Update the Extension for this Index. (Jump to the beginning of the Literal; this allows us to graphically visualise the overwrite disontinuity.) */
		this.getStateStack().peek().getExtensionMap().put(pCompilation.getAbsoluteIndex(), pCompilation.getX());
		/* Allocate a Cascade for this Index. */
		this.onInsertCascade(pLiteral, pLiteral, this.getStateStack().peek(), pCompilation);
		/* Return a standard driver. */
		return new IDriver.Impl(pLiteral, pDataType); 
	}
	
	@Override
	public final void onWrite(final IDriver pDriver, final IDriver pDefunct, final CState<?, ?> pCState, final Compilation pCompilation) { 
		super.onWrite(pDriver, pDefunct, pCState, pCompilation);
		/* Update the Extension for this Index. */
		this.getStateStack().peek().getExtensionMap().put(pCompilation.getAbsoluteIndex(), (pCompilation.getX() + (this.onDefinePosition(pDriver.getDataConduit(), pCompilation).getWidth())));
	}

	/* State Definitions. */
	@Override protected final <V extends ILexical, X extends ILexical & IGroup<V>> CState<?, ?> onPrepareState(final List<X> pInternals, final LinkedList<CState<?, ?>> pStateStack, final Compilation pCompilation) {
		/* Fetch the owning Coupling's width. */
		final int lWidth = this.onDefinePosition(pCompilation.onFetchCoupling(), pCompilation).getWidth();
		/* Allocate a new CState. */
		return new CState<V, X>(pInternals, pCompilation.getX(), lWidth);
	}

}