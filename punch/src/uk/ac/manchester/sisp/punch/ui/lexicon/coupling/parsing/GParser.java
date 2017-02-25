package uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.core.IGroup;
import uk.ac.manchester.sisp.punch.ui.distribution.global.DistributionGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.ILexical;
import uk.ac.manchester.sisp.punch.ui.lexicon.compilation.Compilation;
import uk.ac.manchester.sisp.punch.ui.lexicon.compilation.Interpreter;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.Coupling;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.branching.IBranch;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.branching.constants.Quotation;
import uk.ac.manchester.sisp.punch.ui.lexicon.courier.ILexiconCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.IDataType;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.DataGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.EDataDirection;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.IDataConduit;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.IDirectional;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.axiom.Axiom;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.directional.SinkDecoupler;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.directional.SourceDecoupler;
import uk.ac.manchester.sisp.punch.ui.lexicon.literal.ILiteral;
import uk.ac.manchester.sisp.punch.ui.lexicon.literal.array.Array;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.IContact;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.constants.Iteration;
import uk.ac.manchester.sisp.punch.ui.update.easing.ui.UIEasingGroup;
import uk.ac.manchester.sisp.ribbon.common.IDim2;
import uk.ac.manchester.sisp.ribbon.common.IVec2;
import uk.ac.manchester.sisp.ribbon.utils.DataUtils;
import uk.ac.manchester.sisp.ribbon.utils.MathUtils;

/* The base class for processing Punch Diagrams. */
public abstract class GParser<T extends IDriver, U extends GState<T, ?, ?>> extends Interpreter implements IDirectional, IConduitHandler<T, U> { 
	
	/* Member Variables. */
	private final UIEasingGroup   mUIEasingGroup;
	private final LinkedList<U>   mStateStack;
	private final LinkedList<U>   mExitStack;
	private       EDataDirection  mDataDirection;
	
	/* Static Analysis Constructor. */
	public GParser() { 
		this(null);
	}
	
	/* Prediction Constructor. */
	public GParser(final UIEasingGroup pUIEasingGroup) {
		this(pUIEasingGroup, EDataDirection.BIDIRECTIONAL);
	}
	
	/* Core Instantiation Body. */
	private GParser(final UIEasingGroup pUIEasingGroup, final EDataDirection pDataDirection) {
		/* Initialize Member Variables. */
		this.mUIEasingGroup = pUIEasingGroup;
		this.mStateStack    = new LinkedList<U>();
		this.mExitStack     = new LinkedList<U>();
		this.mDataDirection = pDataDirection;
	}

	/* Handle a Coupling. */
	@Override public final <V extends ILexical, X extends ILexical & IGroup<V>> void onCourierTransit(final Coupling<V, X> pCoupling, final Compilation pCompilation) {
		/* Determine if the Coupling is a sub-call. */
		final boolean   lIsSubCall = pCompilation.isSubCall();
		/* Fetch the Width of the SinkDecoupler. (This is important because each of the Internals will use this for processing, believing themselves to all rest next to the SinkDecoupler.) */
		final int       lXPosition = this.onDefinePosition(pCoupling.getSinkDecoupler(), pCompilation).getWidth();
		/* Define the Origin; this is the offset used by Internals so that different cases believe themselves to rest at the SinkDecoupler boundary. */
		final IVec2.I.W lOrigin    = new IVec2.I.Impl(lXPosition, 0);
		/* Fetch the Coupling's Internals. */
		final List<X>   lInternals = pCoupling.getInternals();
		/* Determine if the Coupling is a sub-call. */
		if(lIsSubCall) { 
			/* Force Contacts to Sink. */
			this.setDataDirection(EDataDirection.SINK);
			/* Visit the SinkDecoupler via the Compilation. This will register the Contacts in the Sink mode. */
			pCompilation.onHierarchyDispatch(pCoupling.getSinkDecoupler(), this);
		}
		
		/* Move to the Origin. */
		MathUtils.onSupplyOffset  (pCompilation, lOrigin);
		/* First, allocate the State for the Coupling's execution. We'll assume that by default we'll execute the first case. */
		final U lGState = this.onPrepareState(lInternals, this.getStateStack(), pCompilation);
		/* Return from the Origin. */
		MathUtils.onWithdrawOffset(pCompilation, lOrigin);
		
		/* Synchronize along the SinkDecoupler. (We know this is at position zero so we'll forego the offset.) */
		synchronized(pCoupling.getSinkDecoupler()) {
			/* Assert that we're increasing the Scope. This is the GState's opportunity to initialize Branch mapping and prepare iteration control. */
			this.onIncreasedScope(pCoupling.getSinkDecoupler(), lGState, pCompilation);
		}

		/* Push the GState onto the StateStack. */
		this.getStateStack().push(lGState);
		/* Determine if we weren't allowed to take the Branch. */
		if(((!lGState.getBranches().isEmpty()) && !this.isBranchEnabled(lGState.getBranches()))) { 
			/* We weren't allowed to process the Branch! First, trash the drivers. */
			this.onTrashAll(pCompilation);
			/* Pop the GState. */
			this.getStateStack().pop();
			/* Fetch the SourceDecoupler's Location. */
			final IVec2.I lLocation = this.onDefinePosition(pCoupling.getSourceDecoupler(), pCompilation);
			/* Synchronize along the SourceDecoupler. */
			synchronized(pCoupling.getSourceDecoupler()) { 
				/* Move to the SourceDecoupler. */
				MathUtils.onSupplyOffset  (pCompilation, lLocation);
					/* Force defaults at the boundary. */
					this.onDefaultBoundary(pCoupling.getSourceDecoupler(), lGState, pCompilation);
				/* Come away from the SourceDecoupler. */
				MathUtils.onWithdrawOffset(pCompilation, lLocation);
			}
			/* Buffer the GState onto the ExitStack. */
			this.getExitStack().push(lGState);
		}
		else { 
			/* Begin the execution branch. */
			while(true) { 
				/* Allow Contacts to maintain their natural DataDirection. */
				this.setDataDirection(EDataDirection.BIDIRECTIONAL);
				/* Execute the Internals. */
				this.onHandleInternals(lOrigin, lGState, pCompilation);
				/* Force Contacts to Sink. */
				this.setDataDirection(EDataDirection.SINK);
				/* Visit the SourceDecoupler; we'll force interconnects here. */
				pCompilation.onHierarchyDispatch(pCoupling.getSourceDecoupler(), this);
				/* Synchronize along the SourceDecoupler. */
				synchronized(pCoupling.getSourceDecoupler()) { 
					/* Trash the isolated drivers. */
					this.onTrashImmediate(pCoupling.getSourceDecoupler(), lGState, pCompilation);
					/* Fetch the SourceDecoupler's Location. */
					final IVec2.I lLocation = this.onDefinePosition(pCoupling.getSourceDecoupler(), pCompilation);
					/* Adjust the LocalOffset to work within the confines of the SourceDecoupler. */
					MathUtils.onSupplyOffset  (pCompilation.getLocalOffset(), lLocation);
						/* Assert that we've decreased Scope. Contacts have been registered and the Internals layout has now been processed. The state passed represents the old definition. */
						this.onDecreasedScope(pCoupling.getSourceDecoupler(), pCoupling, lGState, pCompilation);
					/* Withdraw the SourceDecoupler's position from the LocalOffset. */
					MathUtils.onWithdrawOffset(pCompilation.getLocalOffset(), lLocation);
				}
				
				/* Iteratively process the Branch logic. */
				for(final IBranch<T> lBranch : lGState.getBranches()) { 
					/* Update the branch. */
					lBranch.logic();
				}
				/* Determine if the Branches are still active. */
				if(!this.isAmbivalent(lGState.getBranches()) && this.isBranchEnabled(lGState.getBranches())) {
					/* Iterate the Branches. */
					for(final IBranch<T> lBranch : lGState.getBranches()) { 
						/* Prepare the Branch for the next iteration. */
						lBranch.next(this, lGState, pCompilation);
					}
				}
				else { 
					/* Let's pop the GState from the StateStack. */
					this.getStateStack().pop();
					/* Push the GState onto the ExitStack. */
					this.getExitStack().push(lGState);
					/* End the iterative behaviour. */
					break;
				}
			}
			
			/* Determine if the Coupling is a sub-call. */
			if(lIsSubCall) { 
				/* Force Contacts to Source data. */
				this.setDataDirection(EDataDirection.SOURCE);
				/* Here we're going to feed output data from the SourceDecoupler. */
				pCompilation.onHierarchyDispatch(pCoupling.getSourceDecoupler(), this);
				/* Pop the ExitStack. */
				this.getExitStack().pop();
			}
		}
		
		/* Allow Contacts to maintain their natural DataDirection. */
		this.setDataDirection(EDataDirection.BIDIRECTIONAL);
		/* Disable standard inspection. */
		pCompilation.getInspectionStack().push(Boolean.FALSE);
	}
	
	/* Manages the interaction order for processing a Coupling's Internals as indicated by the current GState. Configures the state and devises pseudo-position methodology. */
	@SuppressWarnings("unchecked") private final <V extends ILexical, X extends ILexical & IGroup<V>> void onHandleInternals(final IVec2.I pOrigin, final U pGState, final Compilation pCompilation) { 
		/* Fetch the active procedure indicated by the State. (Here we sneakily gain some implicit casting.) */
		final X lInternals = (X)pGState.getContents().get(pGState.getIndex());
		/* Push the Lexical onto the Hierarchy. */
		pCompilation.getHierarchy().add(lInternals);
		/* Perform a dispatch on the Lexical; feign the position! */
		pCompilation.onProcessEnclosure(lInternals, this, pOrigin);
		/* Remove the Lexical from the Hierarchy. */
		pCompilation.getHierarchy().remove(lInternals);
	}
	
	/* Called when we've entered a SinkDecoupler on a Coupling and a new state has been prepared. This is prior to when the Contacts are expected to Source data to the Internals. */
	protected void onIncreasedScope(final SinkDecoupler pSinkDecoupler, final U pGState, final Compilation pCompilation) {
		/** TODO: We don't have to do this if we can just supply the state through onQuoteConduit! **/
//		/* Push the GState onto the StateStack. */
//		this.getStateStack().push(pGState);
		/* Iterate the Contacts. */
		for(final IContact<IContact.Link> lContact : pSinkDecoupler.getUIElements()) {
			/* Fetch the Contact's Offset. */
			final IVec2.I      lOffset    = new IVec2.I.Impl(this.onDefinePosition(lContact, pCompilation));
			/* Allocate the Quotation. */
			final Quotation<T> lQuotation = new Quotation<T>(lContact, lOffset, this, pGState, pCompilation);
			/* Buffer the Quotation onto the Branches. */
			pGState.getBranches().add(lQuotation);
		}
//		/* Pop the GState off the Stack. */
//		this.getStateStack().pop();
	};
	
	/* Called when we're left a SinkDecoupler on a Coupling, and the old state has been popped. */
	protected void onDecreasedScope(final SourceDecoupler pSourceDecoupler, final Coupling<?, ?> pCoupling, final U pGState, final Compilation pCompilation) { };
	
	/* Destroys all drivers within the supplied GState. */
	public void onTrashAll(final Compilation pCompilation) { 
		/* Allocate a List for all the Defunct Drivers within the GState. */
		final List<Entry<Integer, T>> lDefunct = new ArrayList<Entry<Integer, T>>();
		/* Iterate the EntrySet. */
		for(final  Entry<Integer, T>  lEntrySet : this.getStateStack().peek().getDriverMap().entrySet()) { 
			/* Buffer the EntrySert into the Defunct Driver List. */
			lDefunct.add(lEntrySet);
		}
		/* Iterate the Defunct Drivers. */
		for(final Entry<Integer, T> lEntrySet : lDefunct) { 
			/* Trash the driver. */
			this.onTrash(lEntrySet.getKey(), lEntrySet.getValue(), this.getStateStack().peek(), pCompilation);
		}
	}
	
	/* Forces default allocations at all Contact regions of a SourceDecoupler. */
	private final void onDefaultBoundary(final SourceDecoupler pSourceDecoupler, final U pGState, final Compilation pCompilation) { 
		/* Iterate the SourceDecoupler's Contacts. */
		for(final IContact<IContact.Link> lContact : pSourceDecoupler.getUIElements()) { 
			/* Calculate the Contact's Sub-Index. */
			final Integer lIndex   = pCompilation.getSubIndex(this, lContact);
			/* Allocate the default. */
			final T       lDefault = this.onDefault(lContact, lContact.getDataType(EDataDirection.SOURCE), pCompilation);
			/* Ensure that the Index is empty. */
			assert DataUtils.isNull(pGState.getDriverMap().get(lIndex));
			/* Push this default value into the GState. */
			pGState.getDriverMap().put(lIndex, lDefault);
		}
	}
	
	/* Destroys any drivers that aren't writing to a Contact at the SourceDecoupler boundary. */
	private final void onTrashImmediate(final SourceDecoupler pSourceDecoupler, final U pGState, final Compilation pCompilation) { 
		/* Allocate a List of Entries that need to be trashed. */
		final List<Entry<Integer, T>> lDisposables = new ArrayList<Entry<Integer, T>>();
		/* Next, we should trash any drivers which aren't writing to a Contact. Iterate the existing drivers. */
		for(final Entry<Integer, T> lEntrySet : pGState.getDriverMap().entrySet()) {
			/* Allocate a boolean to determine if the a corresponding Contact has been found. */
			boolean lIsFound = false;
			/* Iterate the Contacts whilst we've not found a matching return Index for the driver. */
			for(int i = 0; i < pSourceDecoupler.getUIElements().size() && !lIsFound; i++) { 
				/* Fetch the Contact.  */
				final IContact<IContact.Link> lContact  = pSourceDecoupler.getUIElements().get(i);
				/* Calculate the Contact's Index. */
				final Integer                 lIndex    = pCompilation.getSubIndex(this, pSourceDecoupler, lContact);
				/* Determine if the Index matches the EntrySet's entry. */
				                              lIsFound |= lIndex.equals(lEntrySet.getKey());
			}
			/* Determine if a return passage wasn't found. */
			if(!lIsFound) { 
				/* Assert that the EntrySet can be disposed of. */
				lDisposables.add(lEntrySet);
			}
		}
		/* Iterate the Disposables. (We trash them in this way to avoid a ConcurrentModificationException!) */
		for(final Entry<Integer, T> lDisposable : lDisposables) { 
			/* Trash the element. */
			this.onTrash(lDisposable.getKey(), lDisposable.getValue(), pGState, pCompilation);
		}
	}

	/* Forbidden process regions. */
	@Override public final void onCourierTransit(final Array pArray, final Compilation pCompilation) {
		/* Handle as a generic Literal. */
		this.onCourierTransit((ILiteral<?>)pArray, pCompilation);
		/* Prevent internal inspection. */
		pCompilation.getInspectionStack().push(Boolean.FALSE); 
	}
	
	@Override public final void onCourierTransit(final ILiteral<?> pLiteral, final Compilation pCompilation) {
		/* Fetch the Index. */
		final Integer lIndex  = pCompilation.getAbsoluteIndex();
		/* Fetch the current GState. */
		final U       lGState = this.getStateStack().peek();
		/* Attempt to fetch the Driver for the current Index. */
		final T       lDriver = lGState.getDriverMap().get(lIndex);
		/* Was there a Driver? */
		if(DataUtils.isNotNull(lDriver)) {
			/* Trash it. */
			this.onTrash(pCompilation.getAbsoluteIndex(), lDriver, lGState, pCompilation);
		}
		/* Allocate the Driver for the Literal. */
		this.onAlloc(pCompilation.getAbsoluteIndex(), this.onAlloc(pLiteral, pLiteral.getDataType(EDataDirection.SOURCE), pCompilation), lGState, pCompilation);
	}
	
	@Override public <V extends IContact<?>> void onCourierTransit(final Axiom<V> pAxiom, final Compilation pCompilation) { 
		/* Fetch the Axiom's Contacts. */
		final List<V> lContacts = pAxiom.getUIElements();
		/* In this instance, we're going to fake a return state from the Axiom. */
		final U       lState    = this.onPrepareState(this.getStateStack().peek().getContents(), this.getStateStack(), pCompilation);
		/* Filter the Contacts into Sinks and Sources. */
		final List<V> lSinks    = this.onFilterDirection(lContacts, EDataDirection.SINK);
		final List<V> lSources  = this.onFilterDirection(lContacts, EDataDirection.SOURCE);
		/* Process the Sinks. */
		pCompilation.onProcessInternals(pAxiom, lSinks, this);
		/* By default, the ExitStack will be missing drivers because it's empty. So we'll fake some. */
		for(final V lContact : lSources) {
			/* Define a fake return driver along the Sourcing Contact. */
			lState.getDriverMap().put(pCompilation.getSubIndex(this, lContact), this.onAlloc(lContact, lContact.getDataType(EDataDirection.SOURCE), pCompilation));
		}
		/* Push the State onto the ExitStack. By default, it'll be empty. */
		this.getExitStack().push(lState);
		/* Process the Sources. */
		pCompilation.onProcessInternals(pAxiom, lSources, this);
		/* Pop the ExitStack. */
		this.getExitStack().pop();
		/* Prevent standard handling. */
		pCompilation.getInspectionStack().push(Boolean.FALSE);
	}
	
	/* Allocates a List of Contacts filtered by the specified DataDirection. */
	public final <V extends IContact<?>> List<V> onFilterDirection(final List<V> pContacts, final EDataDirection pDataDirection) {
		/* Allocate a new List. */
		final List<V> lContacts = new ArrayList<V>();
		/* Iterate the Contacts. */
		for(final V lV : pContacts) {
			/* Check the DataDirection. */
			if(lV.getDataDirection().equals(pDataDirection)) {
				/* Buffer the Contact. */
				lContacts.add(lV);
			}
		}
		/* Return the Contacts. */
		return lContacts;
	}
	
	@Override public final void onCourierTransit(final IContact<?> pContact, final Compilation pCompilation) { 
		/* Fetch the Index. */
		final Integer lIndex  = pCompilation.getAbsoluteIndex();
		/* Fetch the GState. */
		final U       lGState = this.getStateStack().peek();
		/* Fetch the Driver. */
		final T       lDriver = this.onFetchDriver(lIndex);
		/* Determine if we're handling a Carry. */
		if(this.isQuoteConduit(pContact)) { 
			/* Implement the Quotation. */
			this.onQuoteConduit(pContact, lGState, pCompilation); /** TODO: Abstract state! **/
		}
		else {
			/* Determine if the Contact is returning from a call. */
			final boolean lIsReturn = this.isReturnData(pContact);
			/* Handle the Metric.  */
			if(lIsReturn) { 
				/* Fetch the ExitState. */
				final U lExitState  = this.getExitStack().peek();
				/* Determine if the ExitState ended successfully. */
				if(lExitState.isEnabled()) { 
					/* Fetch the ExitDriver. */
					final T lExitDriver = lExitState.getDriverMap().get(lIndex);
					/* Determine if there's an ExitDriver. */
					if(DataUtils.isNotNull(lExitDriver)) {
						/* Determine if the Driver is not null. */
						if(DataUtils.isNotNull(lDriver)) {
							/* Determine if the types are incompatible. */
							if(DataGlobal.isCompatible(lDriver.getDataType(EDataDirection.SOURCE), lExitDriver.getDataType(EDataDirection.SOURCE))) {
								/* The type is overwritten! */ 
								this.onWrite(lExitDriver, lDriver, lGState, pCompilation);
							}
							/* Trash the old driver. */
							this.onTrash(pCompilation.getAbsoluteIndex(), lDriver, lGState, pCompilation);
						}
						/* Push the ExitDriver onto the GState's DriverMap. */
						lGState.getDriverMap().put(lIndex, lExitDriver);
					}
				}
				else {
					/* The state didn't actually execute, so we need to return some default data. */
					this.onAlloc(pCompilation.getAbsoluteIndex(), this.onDefault(pContact, pContact.getDataType(EDataDirection.SOURCE), pCompilation), lGState, pCompilation);
				}
			} 
			else { 
				/* Determine whether we're handling a null driver, or a driver which sources the void type. (This indicates a starved path.) */
				if(DataUtils.isNull(lDriver)) { 
					/* Allocate a Starved path. */
					this.onStarved(pContact, pContact.getDataType(EDataDirection.SINK), lGState, pCompilation);
				}
				else { 
					/* Determine if the types are compatible. */
					if(DataGlobal.isCompatible(pContact.getDataType(EDataDirection.SINK), lDriver.getDataType(EDataDirection.SOURCE))) { 
						/* Buffer a generic compatible data flow. */
						this.onCarry(lDriver, pContact, lGState, pCompilation);
					}
					else { 
						/* Allocate an error path. (We can't flow between these two types.) */
						this.onError(lDriver, pContact, lGState, pCompilation);
					}
				}
			}
		}
	}
	
	/* Abstract Data Handlers. This enables integration with the base GParser architecture to allow custom runtime representations. */
	protected abstract T   onAlloc(final IContact<?> pContact, final IDataType<?> pDataType, final Compilation pCompilation);
	protected abstract T onDefault(final IContact<?> pContact, final IDataType<?> pDataType, final Compilation pCompilation);
	protected abstract T   onAlloc(final ILiteral<?> pLiteral, final IDataType<?> pDataType, final Compilation pCompilation);
	
	/* Customizable Allocation Handlers. */
	protected abstract T onAlloc(final Iteration pIteration, final IDataType<?> pDriverType, final IDataType<?> pSourcingType, final Compilation pCompilation);
	
	/* Abstract State Definition. */
	protected abstract <V extends ILexical, X extends ILexical & IGroup<V>> U onPrepareState(final List<X> pInternals, final LinkedList<U> pStateStack, final Compilation pCompilation); /** TODO: Supply the Stack? Dangerous? **/
	
	/* Unary Implementations. */
	@Override public void onTrash(final Integer pIndex, final T pDriver, final U pGState, final Compilation pCompilation) { 
		/* Remove the reference from the referenced DriverMap. */
		pGState.getDriverMap().remove(pIndex);
	}
	
	/* Trash helper method; dispose of a driver on the current state. */
	public final void onTrash(final Integer pIndex, final U pGState, final Compilation pCompilation) { 
		/* Fetch the Driver. */
		final T lDriver = pGState.getDriverMap().get(pIndex);
		/* Assure the Driver is never null. */
		assert DataUtils.isNotNull(lDriver);
		/* Trash it! */
		this.onTrash(pIndex, lDriver, this.getStateStack().peek(), pCompilation);
	}
	
	@Override public void onAlloc(final Integer pIndex, final T pDriver, final U pGState, final Compilation pCompilation) { 
		/* Buffer the DataConduit onto the DriverStack. */
		pGState.getDriverMap().put(pIndex, pDriver);
	}
	
	/* Overwrite flag. */
	@Override public void onWrite(final T pDriver, final T pDefunct, final U pGState, final Compilation pCompilation) { }
	
	/* Starvation indicator. */
	@Override public void onStarved(final IDataConduit pDataConduit, final IDataType<?> pExpectedType, final U pGState, final Compilation pCompilation) { }
	
	/* Binary Implementations. */
	@Override public void onCarry(final T pDriver, final IDataConduit pSink, final U pGState, final Compilation pCompilation) { }
	@Override public void onError(final T pDriver, final IDataConduit pSink, final U pGState, final Compilation pCompilation) { }

	/* Position Fetch. */
	@SuppressWarnings("unchecked") @Override public final <V extends IVec2.I & IDim2.I> V onDefinePosition(final IUIElement pUIElement, final Compilation pCompilation) {
		/* Return the UIElementPacket; we wish to use prediction for the rest locations of the UIElements. */
		return DataUtils.isNull(this.getUIEasingGroup()) ? (V)pUIElement : (V)DistributionGlobal.onFetchPacket(this.getUIEasingGroup(), pUIElement);
	}
	
	/* Forces Contacts to handle a Quotation. */ /** TODO: Should be able to specify the state instead of peeking only! This would avoid the Quotation push/pop onIncreasedScope methodology. **/
	public final void onQuoteConduit(final IContact<?> pContact, final U pGState, final Compilation pCompilation) { 
		/* Define the LexiconCourier. */
		final ILexiconCourier<GParser<T, U>> lLexiconCourier = new ILexiconCourier.Adapter<GParser<T,U>>() {
			/* Handle a Contact. */
			@Override public final void onCourierTransit(final IContact<?> pContact, final GParser<T, U> pGParser) { 
				/* By default, a Contact will just make an allocation of it's input data. This is pass-by-value. */
				pGParser.onAlloc(pCompilation.getAbsoluteIndex(), pGParser.onAlloc(pContact, pContact.getDataType(EDataDirection.SOURCE), pCompilation), pGState, pCompilation);
			}
			/* Handle an Iteration. */
			@Override public final void onCourierTransit(final Iteration pIteration, final GParser<T, U> pGParser) {
				/* Have the implementor provide a custom definition for Quoting on an Iteration. The type of data the Iteration is Sinking should be used to initialize what is sourced.*/
				pGParser.onAlloc(pCompilation.getAbsoluteIndex(), pGParser.onAlloc(pIteration, pIteration.getDataType(EDataDirection.SINK), pIteration.getDataType(EDataDirection.SOURCE), pCompilation), pGState, pCompilation);
			}
		};
		/* Export the LexiconCourier. */
		pContact.onCourierDispatch(lLexiconCourier, this);
	}
	
	/* Defines whether the current state of the compilation indicates that a generic Contact represents a pass-by-value buffer between sources. */
	private final boolean isQuoteConduit(final IContact<?> pContact) {
		/* If we're operating in source mode or the Contact's DataDirection is Sinking, then the Contact must be a Quote. (This means we must create a pass-by-value allocation.) */
		return this.getDataDirection().equals(EDataDirection.SOURCE) && pContact.getDataDirection().equals(EDataDirection.SINK);
	}
	
	/* Defines whether a Contact returns Data from a call; this results in a write back to the original track. */
	private final boolean isReturnData(final IContact<?> pContact) { 
		/* If we have a Source contact which is operating in a non-Sinking context. (This indicates a write back.) */
		return pContact.getDataDirection().equals(EDataDirection.SOURCE) && !this.getDataDirection().equals(EDataDirection.SINK);
	}
	
	/* Returns the Driver for a given Index. */
	public final T onFetchDriver(final Integer pIndex) {
		/* Allocate the Driver reference. We'll use this for iteration. */
		     T lDriver = null;
		/* Iterate the DriverStack. */
		for(int i = 0; i < this.getStateStack().size() && DataUtils.isNull(lDriver); i++) { 
			/* Fetch the Driver at the current DriverStack Index. */
			lDriver = this.getStateStack().get(i).getDriverMap().get(pIndex);
		}
		/* Return the Driver. */
		return lDriver;
	}
	
	/* Defines whether a List of Branches indicates that we're allowed to take a proposed branch (All branches must be enabled in order to take!) */
	private final boolean isBranchEnabled(final List<IBranch<T>> pBranches) { 
		/* Allocate a boolean. */
		boolean lIsEnabled = true;
		/* Iterate the Branches. */
		for(int i = 0; i < pBranches.size() && lIsEnabled; i++) { 
			/* Fetch the Branch. */
			final IBranch<T> lBranch     = pBranches.get(i);
			/* Update the search metric. */
			lIsEnabled &= lBranch.isEnabled();
		}
		/* Return the metric. */
		return lIsEnabled;
	}
	
	/** TODO: **/
	private final boolean isAmbivalent(final List<IBranch<T>> pBranches) { 
		/* Allocate a count. */
		int lCount = 0;
		/* Iterate the Branches. */
		for(final IBranch<T> lBranch : pBranches) {
			/* Update the Count. */
			lCount += DataUtils.booleanToInt(lBranch.isAmbivalent());
		}
		/* Determine whether every Branch was Ambivolent. */
		return lCount == pBranches.size();
	}

	protected final UIEasingGroup getUIEasingGroup() {
		return this.mUIEasingGroup;
	}
	
	public final LinkedList<U> getStateStack() { 
		return this.mStateStack;
	}
	
	protected final LinkedList<U> getExitStack() { 
		return this.mExitStack;
	}
	
	private final void setDataDirection(final EDataDirection pDataDirection) { 
		this.mDataDirection = pDataDirection;
	}
	
	@Override public final EDataDirection getDataDirection() {
		return this.mDataDirection;
	}
	
}