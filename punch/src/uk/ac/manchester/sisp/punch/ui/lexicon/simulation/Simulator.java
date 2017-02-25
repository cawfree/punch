package uk.ac.manchester.sisp.punch.ui.lexicon.simulation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.manchester.sisp.punch.exception.PunchException;
import uk.ac.manchester.sisp.punch.ui.core.IGroup;
import uk.ac.manchester.sisp.punch.ui.lexicon.ILexical;
import uk.ac.manchester.sisp.punch.ui.lexicon.compilation.Compilation;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.Coupling;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.GParser;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.GState;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.branching.constants.Quotation;
import uk.ac.manchester.sisp.punch.ui.lexicon.courier.ILexiconCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.IDataType;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.IDataType.ArrayType;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.courier.IDataCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.DataGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.EDataDirection;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.IDataConduit;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.types.BooleanType;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.axiom.Axiom;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.axiom.Invocation;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.directional.SinkDecoupler;
import uk.ac.manchester.sisp.punch.ui.lexicon.functions.Elapsed;
import uk.ac.manchester.sisp.punch.ui.lexicon.functions.Nand;
import uk.ac.manchester.sisp.punch.ui.lexicon.functions.Print;
import uk.ac.manchester.sisp.punch.ui.lexicon.functions.courier.IFunctionCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.literal.ILiteral;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.IContact;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.constants.Iteration;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.constants.Selector;
import uk.ac.manchester.sisp.punch.ui.lexicon.simulation.branching.ArrayBranch;
import uk.ac.manchester.sisp.punch.ui.lexicon.simulation.branching.CyclicBranch;
import uk.ac.manchester.sisp.punch.ui.lexicon.simulation.branching.SelectBranch;
import uk.ac.manchester.sisp.ribbon.common.IVec2;
import uk.ac.manchester.sisp.ribbon.utils.DataUtils;
import uk.ac.manchester.sisp.ribbon.utils.MathUtils;
import uk.ac.manchester.sisp.ribbon.utils.ResourceUtils;

public final class Simulator extends GParser<SDriver, GState<SDriver, ?, ?>> implements IFunctionCourier<Compilation> {
	
	/* Member Variables. */
	
	public Simulator() { 
		/* Initialize Member Variables. */
		
	}

	/** TODO: always generate 'true' branches, just decide whether or not to actually run them. **/
	@Override
	protected final void onIncreasedScope(final SinkDecoupler pSinkDecoupler, final GState<SDriver, ?, ?> pGState, final Compilation pCompilation) { 
		/* Iterate the Contacts. */
		for(final IContact<IContact.Link> lContact : pSinkDecoupler.getUIElements()) { 
			/* Fetch the Contact's Offset. */
			final IVec2.I lOffset = new IVec2.I.Impl(this.onDefinePosition(lContact, pCompilation));
			/* Allocate the LexiconCourier. */
			final ILexiconCourier<Simulator> lLexiconCourier = new ILexiconCourier.Adapter<Simulator>() {
				/* Handle a Contact. */
				@Override public final void onCourierTransit(final IContact<?> pContact, final Simulator pSimulator) {
					/* Allocate the Quotation. */
					final Quotation<SDriver> lQuotation = new Quotation<SDriver>(lContact, lOffset, pSimulator, pGState, pCompilation);
					/* Buffer the Quotation onto the Branches. */
					pGState.getBranches().add(lQuotation);
				}
				/* Handle an Iteration. */
				@Override public final void onCourierTransit(final Iteration pIteration, final Simulator pSimulator) {
					/* Allocate the DataCourier. */
					final IDataCourier<Simulator> lDataCourier = new IDataCourier.Adapter<Simulator>() {
						/* Handle an ArrayType. */
						@Override public final void onCourierTransit(final ArrayType pArrayType, final Simulator pSimulator) { 
							/* Fetch the Driver. */
							final List<?> lDriver = (List<?>)pSimulator.onFetchDriver(pCompilation.getSubIndex(pSimulator, pIteration)).getValue();
							/* Buffer an ArrayBranch. */
							pGState.getBranches().add(new ArrayBranch(pIteration, lDriver, lOffset, pSimulator, pGState, pCompilation));
						}
						/* Handle a BooleanType. */
						@Override public final void onCourierTransit(final BooleanType pBooleanType, final Simulator pCourierPackage) {
							/* Fetch the Driver. */
							final Boolean lDriver = (Boolean)pSimulator.onFetchDriver(pCompilation.getSubIndex(pSimulator, pIteration)).getValue();
							/* Buffer a CyclicBranch. */
							pGState.getBranches().add(new CyclicBranch(pIteration, lDriver, lOffset, pSimulator, pGState, pCompilation));
						}
					};
					/* Handle the type of data the Iteration is Sinking. */
					pIteration.getDataType(EDataDirection.SINK).onCourierDispatch(lDataCourier, pSimulator);
				}
				/* Handle a Selector. */
				@Override public final void onCourierTransit(final Selector pSelector, final Simulator pSimulator) {
					/* Allocate a SelectBranch. */
					final SelectBranch lSelectBranch = new SelectBranch(lOffset, pSimulator, pGState, pCompilation);
					/* Buffer the SelectBranch. */
					pGState.getBranches().add(lSelectBranch);
				}
			};
			/* Export the LexiconCourier via the Contact. */
			lContact.onCourierDispatch(lLexiconCourier, this);
		}
	}

	@Override public <V extends IContact<?>> void onCourierTransit(final Axiom<V> pAxiom, final Compilation pCompilation) { 
		/* In this instance, we're going to fake a return state from the Axiom. */
		final GState<SDriver, ?, ?> lGState   = this.onPrepareState(null, this.getStateStack(), pCompilation); /** TODO: ? **/
		/* Fetch the Axiom's Contacts. */
		final List<V>               lContacts = pAxiom.getUIElements();
		/* Filter the Contacts into Sinks and Sources. */
		final List<V>               lSinks    = this.onFilterDirection(lContacts, EDataDirection.SINK);
		final List<V>               lSources  = this.onFilterDirection(lContacts, EDataDirection.SOURCE);
		/* Process the Sinks. */
		pCompilation.onProcessInternals(pAxiom, lSinks, this);
		/* Push the State onto the ExitStack. By default, it'll be empty. */
		this.getExitStack().push(lGState);
		/* Handle the Axiom via the FunctionCourier. */
		pAxiom.onCourierDispatch((IFunctionCourier<Compilation>)this, pCompilation);
		/* Process the Sources. */
		pCompilation.onProcessInternals(pAxiom, lSources, this);
		/* Pop the ExitStack. */
		this.getExitStack().pop();
		/* Prevent standard handling. */
		pCompilation.getInspectionStack().push(Boolean.FALSE);
	}

	@Override public final void onCourierTransit(final Invocation pInvocation, final Compilation pCompilation) {
		/* Fetch the Coupling. */
		final Coupling<?, ?>        lCoupling    = pInvocation.getReference();
		/* Fetch the Axiom's Contacts. */
		final List<IContact.Link>   lContacts    = pInvocation.getUIElements();
		/* Filter the Contacts into Sinks and Sources. */
		final List<IContact.Link>   lSinks       = this.onFilterDirection(lContacts, EDataDirection.SINK);
		final List<IContact.Link>   lSources     = this.onFilterDirection(lContacts, EDataDirection.SOURCE);
		/* Allocate the LocalOffset. */
		final IVec2.I.W             lLocalOffset = new IVec2.I.Impl();
		/* Process the Sinks. */
		pCompilation.onProcessInternals(pInvocation, lSinks, this);
		/* Allocate a new GState. */
		final GState<SDriver, ?, ?> lGState      = this.onPrepareState(null, this.getStateStack(), pCompilation); /** TODO: ? **/
		
		/* Synchronize along the Coupling's SinkDecoupler. */
		synchronized(lCoupling.getSinkDecoupler()) {
			/* Supply the SinkDecoupler's Offset. */
			MathUtils.onSupplyOffset  (lLocalOffset, this.onDefinePosition(lCoupling.getSinkDecoupler(), pCompilation));
			/* Iterate the SinkDecoupler's Contacts. */
			for(final IContact<?> lContact : lCoupling.getSinkDecoupler().getUIElements()) {
				/* Supply the Contact's Offset. */
				MathUtils.onSupplyOffset  (lLocalOffset, this.onDefinePosition(lContact, pCompilation));
				/* Iterate the Invocation's Links. */
				for(final IContact.Link lLink : pInvocation.getUIElements()) {
					/* Determine if we've found a matching reference. */
					if(lLink.getReference().equals(lContact)) {
						/* Calculate the LinkIndex. */
						final Integer lLinkIndex    = pCompilation.getSubIndex(this, lLink);
						/* Calculate the ContactIndex. */
						final Integer lContactIndex = pCompilation.getAbsoluteIndex(lLocalOffset);
						/* Fetch the Driver. */
						final SDriver lSDriver      = this.onFetchDriver(lLinkIndex);
						/* Supply the GState with the appropriate input data.  */
						lGState.getDriverMap().put(lContactIndex, lSDriver);
					}
				}
				/* Withdraw the Contact's Offset. */
				MathUtils.onWithdrawOffset(lLocalOffset, this.onDefinePosition(lContact, pCompilation));
			}
			/* Withdraw the SinkDecoupler's Offset. */
			MathUtils.onWithdrawOffset(lLocalOffset, this.onDefinePosition(lCoupling.getSinkDecoupler(), pCompilation));
		}
		
		/* Push the GState onto the StateStack. */
		this.getStateStack().push(lGState);
		/* Visit the Invocation's Coupling. */
		lCoupling.onCourierDispatch(pCompilation, this);
		/* Pop the GState off the StateStack. */
		this.getStateStack().pop();
		
		/* Make a copy of the ExitStack's DriverMap. */
		final Map<Integer, SDriver> lDriverMap = new HashMap<Integer, SDriver>(this.getExitStack().peek().getDriverMap());
		/* Clear the ExitStack's DriverMap. */
		this.getExitStack().peek().getDriverMap().clear();
		
		/* Synchronize along the Coupling's SourceDecoupler. */
		synchronized(lCoupling.getSourceDecoupler()) {
			/* Supply the SourceDecoupler's Offset. */
			MathUtils.onSupplyOffset  (lLocalOffset, this.onDefinePosition(lCoupling.getSourceDecoupler(), pCompilation));
			/* Iterate the SourceDecoupler's Contacts. */
			for(final IContact<?> lContact : lCoupling.getSourceDecoupler().getUIElements()) {
				/* Supply the Contact's Offset. */
				MathUtils.onSupplyOffset  (lLocalOffset, this.onDefinePosition(lContact, pCompilation));
				/* Iterate the Invocation's Links. */
				for(final IContact.Link lLink : pInvocation.getUIElements()) {
					/* Determine if we've found a matching reference. */
					if(lLink.getReference().equals(lContact)) {
						/* Calculate the LinkIndex. */
						final Integer lLinkIndex    = pCompilation.getSubIndex(this, lLink);
						/* Calculate the ContactIndex. */
						final Integer lContactIndex = pCompilation.getAbsoluteIndex(lLocalOffset);
						/* Supply the ExitState with the properly formatted output data.  */
						this.getExitStack().peek().getDriverMap().put(lLinkIndex, lDriverMap.get(lContactIndex));
					}
				}
				/* Withdraw the Contact's Offset. */
				MathUtils.onWithdrawOffset(lLocalOffset, this.onDefinePosition(lContact, pCompilation));
			}
			/* Withdraw the SinkDecoupler's Offset. */
			MathUtils.onWithdrawOffset(lLocalOffset, this.onDefinePosition(lCoupling.getSourceDecoupler(), pCompilation));
		}
		/* Process the Sources. */
		pCompilation.onProcessInternals(pInvocation, lSources, this);
		/* Pop the ExitStack. */
		this.getExitStack().pop();
		/* Prevent standard handling. */
		pCompilation.getInspectionStack().push(Boolean.FALSE);
	}
	
	@Override
	public final void onCourierTransit(final Nand pNand, final Compilation pCompilation) { 
		/* Fetch the Inputs. */
		final Boolean  lA        = (Boolean)this.onFetchDriver(pCompilation.getSubIndex(this, pNand.getA())).getValue();
		final Boolean  lB        = (Boolean)this.onFetchDriver(pCompilation.getSubIndex(this, pNand.getB())).getValue();
		/* Compute the result. */
		final Boolean  lResult   = !(lA & lB);
		/* Push the result onto the stack. */
		final SDriver  lDriver   = new SDriver(pNand.getResult(), DataGlobal.TYPE_DHARMA, lResult);
		/* Push the Driver onto the State. */
		this.getStateStack().peek().getDriverMap().put(pCompilation.getSubIndex(this, pNand.getResult()), lDriver);
	}

	@Override 
	public final void onCourierTransit(final Print pPrint, final Compilation pCompilation) {
		/* Print the driving value to the console. */
		System.out.println(this.onFetchDriver(pCompilation.getSubIndex(this, pPrint.getA())).getValue());
	}
	
	@Override 
	public final void onCourierTransit(final Elapsed pElapsed, final Compilation pCompilation) { 
		/* Fetch the current time. */
		final Float lCurrentTimeSeconds = (Float)ResourceUtils.getSystemTimeSeconds();
		/* Push the result onto the stack. */
		final SDriver  lDriver   = new SDriver(pElapsed.getResult(), DataGlobal.TYPE_F32, lCurrentTimeSeconds);
		/* Push the Driver onto the State. */
		this.getStateStack().peek().getDriverMap().put(pCompilation.getSubIndex(this, pElapsed.getResult()), lDriver);
	}

	@Override protected final SDriver onDefault(final IContact<?> pContact, final IDataType<?> pDataType, final Compilation pCompilation) { 
		/* Allocate a new SDriver. */
		final SDriver lSDriver = new SDriver(pContact, pDataType, pDataType.getDefault());
		/* Return the SDriver. */
		return lSDriver;
	}
	
	@Override protected final SDriver onAlloc(final IContact<?> pContact, final IDataType<?> pDataType, final Compilation pCompilation) { 
		/* Make a copy of the input data, by default. */
		return new SDriver(pContact, pDataType, this.onFetchDriver(pCompilation.getAbsoluteIndex()).getValue());
	}

	@Override protected final SDriver onAlloc(final ILiteral<?> pLiteral, final IDataType<?> pDataType, final Compilation pCompilation) { 
		/* Allocate a new SDriver. */
		final SDriver lSDriver = new SDriver(pLiteral, pDataType, pLiteral.getValue());
		/* Return the SDriver. */
		return lSDriver;
	}
	
	/** TODO: This never gets called any more. refactor! lose it! **/
	@Override protected final SDriver onAlloc(final Iteration pIteration, final IDataType<?> pDriverType, final IDataType<?> pSourcingType, final Compilation pCompilation) { 
		System.out.println("alloc iter");
		/* Allocate the ValueBuffer. */
		final Object[] lValueBuffer = new Object[] { null };
		/* Allocate the DataCourier. */
		final IDataCourier<Simulator> lDataCourier = new IDataCourier.Adapter<Simulator>() {
			/* Handle an ArrayType. */
			@Override public final void onCourierTransit(final   ArrayType  pArrayType, final Simulator pSimulator) { DataUtils.onPushArrayElement(lValueBuffer, (DataUtils.getFirstElementOf((List<?>)pSimulator.onFetchDriver(pCompilation.getAbsoluteIndex()).getValue()))); }
			/* Handle a BooleanType. */
			@Override public final void onCourierTransit(final BooleanType pDharmaType, final Simulator pSimulator) { DataUtils.onPushArrayElement(lValueBuffer, DataGlobal.TYPE_I32.getDefault()); }
		};
		/* Export the DataCourier to the DriverType. */
		pDriverType.onCourierDispatch(lDataCourier, this);
		/* Allocate the SDriver. It's DataType will match that which is being sourced by the Iteration. */
		final SDriver lSDriver = new SDriver(pIteration, pIteration.getDataType(EDataDirection.SOURCE), DataUtils.getFirstElementOf(lValueBuffer));
		/* Return the SDriver. */
		return lSDriver;
	}

	@Override protected final <V extends ILexical, X extends ILexical & IGroup<V>> GState<SDriver, ?, ?> onPrepareState(final List<X> pInternals, final LinkedList<GState<SDriver, ?, ?>> pStateStack, final Compilation pCompilation) { 
		/* Allocate a new GState. */
		return new GState<SDriver, V, X>(pInternals);
	}
	
	/* Diagram Error Handling. */
	@Override public final void onStarved(final IDataConduit pDataConduit, final    IDataType<?> pExpectedType, final GState<SDriver, ?, ?> pSState, final Compilation pCompilation) { throw new PunchException("Diagram is malformed."); }
	@Override public final void onError  (final      SDriver      pDriver, final    IDataConduit         pSink, final GState<SDriver, ?, ?> pSState, final Compilation pCompilation) { throw new PunchException("Diagram is malformed."); }
	
}