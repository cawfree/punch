package uk.ac.manchester.sisp.punch.ui.lexicon.global;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import uk.ac.manchester.sisp.punch.ui.lexicon.ILexical;
import uk.ac.manchester.sisp.punch.ui.lexicon.cascade.Cascade;
import uk.ac.manchester.sisp.punch.ui.lexicon.courier.ILexiconCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.IContact;
import uk.ac.manchester.sisp.ribbon.utils.DataUtils;

/** TODO: Core should probably be an interface. **/
/** TODO: Define whole default insertions rather than just the interpolation type. this will allow separator L->R **/
public final class LexiconGlobal {
	
	/* Static Comparators. */ /** TODO: Reverse all these elements. **/
	public static final Comparator<IContact<?>> COMPARATOR_IMBRICATION = new Comparator<IContact<?>>() { @Override public final int compare(final IContact<?> arg0, final IContact<?> arg1) { return arg0.getDataDirection().equals(arg1.getDataDirection()) ? Integer.compare(arg0.getX(), arg1.getX()) : arg0.getDataDirection().compareTo(arg1.getDataDirection()); } };
	
	/* Cascade Accumulator. */
	public static final ILexiconCourier<List<Cascade>> COURIER_CASCADE_ACCUMULATOR = new ILexiconCourier.Adapter<List<Cascade>>() { @Override public final void onCourierTransit(final Cascade pCascade, final List<Cascade> pCascades) { pCascades.add(pCascade); } };
	
	/* Returns a List of Cascades contained in the parameterized List of Lexicals. */
	public static final <U extends ILexical> List<Cascade> onAccumulateCascades(final List<U> pLexicals) { 
		/* Allocate the Cascades. */
		final List<Cascade> lCascades = new ArrayList<Cascade>();
		/* Iterate the Lexicals. */
		for(final ILexical lLexical : pLexicals) {
			/* Export the Accumulator. */
			lLexical.onCourierDispatch(LexiconGlobal.COURIER_CASCADE_ACCUMULATOR, lCascades);
		}
		/* Return the Cascades. */
		return lCascades;
	}
	
//	/* Define Base Interpretation Types. */
//	private static final ILiason<IUIElement>   LIASON_IS_REFINEABLE         = new ILiason<IUIElement>() { @Override public final boolean isLiason(final IUIElement pAlice) { return IRefineable.class.isAssignableFrom(pAlice.getClass()); } };
//	
//	/* Declare Refineable Liason Types. */
//	private static final ILiason<IUIElement>   LIASON_REFINEABLE_PARCEL     = new ILiason<IUIElement>() { @Override public final boolean isLiason(final IUIElement pAlice) { return Parcel.class.isAssignableFrom(pAlice.getClass());                                                                                           } };
//	private static final ILiason<IUIElement>   LIASON_REFINEABLE_COUPLING   = new ILiason<IUIElement>() { @Override public final boolean isLiason(final IUIElement pAlice) { return (LexiconGlobal.LIASON_IS_REFINEABLE.isLiason(pAlice) && LexiconGlobal.LIASON_CORE_COUPLING.isLiason((((IRefineable)pAlice)).getLexical())); } };
//	private static final ILiason<IUIElement>   LIASON_REFINEABLE_AXIOM      = new ILiason<IUIElement>() { @Override public final boolean isLiason(final IUIElement pAlice) { return (LexiconGlobal.LIASON_IS_REFINEABLE.isLiason(pAlice) &&    LexiconGlobal.LIASON_CORE_AXIOM.isLiason((((IRefineable)pAlice)).getLexical())); } };
//	
//	/* Declare Refinement Liason Types. */
//	private static final ILiason<IUIElement>   LIASON_REFINEMENT_VISIBILITY = new ILiason<IUIElement>() { @Override public final boolean isLiason(final IUIElement pAlice) { return Refinement.IVisibility.class.isAssignableFrom(pAlice.getClass()); } };
//	private static final ILiason<IUIElement>   LIASON_REFINEMENT_SINGULAR   = new ILiason<IUIElement>() { @Override public final boolean isLiason(final IUIElement pAlice) { return   Refinement.ISingular.class.isAssignableFrom(pAlice.getClass()); } };
//	private static final ILiason<IUIElement>   LIASON_REFINEMENT_INTERFACE  = new ILiason<IUIElement>() { @Override public final boolean isLiason(final IUIElement pAlice) { return              Interface.class.isAssignableFrom(pAlice.getClass()); } };
//	
//	/* Define Core Liason Types. */
//	private static final ILiason<IUIElement>   LIASON_CORE_COUPLING         = new ILiason<IUIElement>() { @Override public final boolean isLiason(final IUIElement pAlice) { return          Coupling.class.isAssignableFrom(pAlice.getClass()); } };
//	private static final ILiason<IUIElement>   LIASON_CORE_CONTACT          = new ILiason<IUIElement>() { @Override public final boolean isLiason(final IUIElement pAlice) { return           Contact.class.isAssignableFrom(pAlice.getClass()); } };
//	private static final ILiason<IUIElement>   LIASON_CORE_COMMENT          = new ILiason<IUIElement>() { @Override public final boolean isLiason(final IUIElement pAlice) { return           Comment.class.isAssignableFrom(pAlice.getClass()); } };
//	private static final ILiason<IUIElement>   LIASON_CORE_DHARMA           = new ILiason<IUIElement>() { @Override public final boolean isLiason(final IUIElement pAlice) { return            Dharma.class.isAssignableFrom(pAlice.getClass()); } };
//	private static final ILiason<IUIElement>   LIASON_CORE_SEQUENCE         = new ILiason<IUIElement>() { @Override public final boolean isLiason(final IUIElement pAlice) { return          ISequential.class.isAssignableFrom(pAlice.getClass()); } };
//	private static final ILiason<IUIElement>   LIASON_CORE_CONCURRENCY      = new ILiason<IUIElement>() { @Override public final boolean isLiason(final IUIElement pAlice) { return IConcurrency.Impl.class.isAssignableFrom(pAlice.getClass()); } };
//	private static final ILiason<IUIElement>   LIASON_CORE_AXIOM            = new ILiason<IUIElement>() { @Override public final boolean isLiason(final IUIElement pAlice) { return             Axiom.class.isAssignableFrom(pAlice.getClass()); } };
//	private static final ILiason<IUIElement>   LIASON_CORE_SINKDECOUPLER    = new ILiason<IUIElement>() { @Override public final boolean isLiason(final IUIElement pAlice) { return     SinkDecoupler.class.isAssignableFrom(pAlice.getClass()); } };
//	private static final ILiason<IUIElement>   LIASON_CORE_SOURCEDECOUPLER  = new ILiason<IUIElement>() { @Override public final boolean isLiason(final IUIElement pAlice) { return   SourceDecoupler.class.isAssignableFrom(pAlice.getClass()); } };
//	
//	/* Static Declarations. */
//	private static final List<ILiason<List<IUIElement>>> LIST_FILTERS_NULL  = Collections.unmodifiableList(new ArrayList<ILiason<List<IUIElement>>>());
//	
//	/* Define the Enumeration List. (Read Only!) */
//	@SuppressWarnings("serial") public static final List<ILiason<IUIElement>> LIST_LIASON_ENUMERATION  = Collections.unmodifiableList(new ArrayList<ILiason<IUIElement>>() { { 
//		/* Buffer the Refineable Types. */
//		this.add(LexiconGlobal.LIASON_REFINEABLE_PARCEL);
//		this.add(LexiconGlobal.LIASON_REFINEABLE_COUPLING);
//		this.add(LexiconGlobal.LIASON_CORE_DHARMA);
//		this.add(LexiconGlobal.LIASON_REFINEABLE_AXIOM);
//		/* Buffer the Refinement Types. */
//		this.add(LexiconGlobal.LIASON_REFINEMENT_VISIBILITY);
//		this.add(LexiconGlobal.LIASON_REFINEMENT_SINGULAR);
//		this.add(LexiconGlobal.LIASON_REFINEMENT_INTERFACE);
//		/* Buffer the Core Types. */
//		this.add(LexiconGlobal.LIASON_CORE_CONTACT);
//		this.add(LexiconGlobal.LIASON_CORE_COUPLING);
//		this.add(LexiconGlobal.LIASON_CORE_COMMENT);
//		this.add(LexiconGlobal.LIASON_CORE_SINKDECOUPLER);
//		this.add(LexiconGlobal.LIASON_CORE_SOURCEDECOUPLER);
//		this.add(LexiconGlobal.LIASON_CORE_AXIOM);
//		this.add(LexiconGlobal.LIASON_CORE_SEQUENCE);
//		this.add(LexiconGlobal.LIASON_CORE_CONCURRENCY);
//	} });
//	
//	/* Next, we'll define the Filter Array Map. This indicates which Liasons are subject to what filters, depending on a specific UIElement. (Read Only!) */
//	@SuppressWarnings("serial") public static final Map<ILiason<IUIElement>, Map<ILiason<IUIElement>, List<ILiason<List<IUIElement>>>>> MAP_FILTER_ARRAY = Collections.unmodifiableMap(new HashMap<ILiason<IUIElement>, Map<ILiason<IUIElement>, List<ILiason<List<IUIElement>>>>>() { { 
//		/* Define the Parcel's Filter Array. */
//		this.put(LexiconGlobal.LIASON_REFINEABLE_PARCEL, Collections.unmodifiableMap(new HashMap<ILiason<IUIElement>, List<ILiason<List<IUIElement>>>>() { {
//			/* Handle an Visibility. */
//			this.put(LexiconGlobal.LIASON_REFINEMENT_VISIBILITY, LexiconGlobal.LIST_FILTERS_NULL);
//			/* Handle an Interface. */
//			this.put(LexiconGlobal.LIASON_REFINEMENT_INTERFACE,  LexiconGlobal.LIST_FILTERS_NULL);
//			/* Handle a Parcel. */
//			this.put(LexiconGlobal.LIASON_REFINEABLE_PARCEL,     LexiconGlobal.LIST_FILTERS_NULL);
//			/* Handle a Refineable Coupling. */
//			this.put(LexiconGlobal.LIASON_REFINEABLE_COUPLING,   LexiconGlobal.LIST_FILTERS_NULL);
//			/* Handle a Refineable Axiom. */
//			this.put(LexiconGlobal.LIASON_REFINEABLE_AXIOM,      LexiconGlobal.LIST_FILTERS_NULL);
//			/* Handle an non-Refineable Coupling. */
//			this.put(LexiconGlobal.LIASON_CORE_COUPLING,         LexiconGlobal.LIST_FILTERS_NULL);
//			/* Handle an non-Refineable Axiom. */
//			this.put(LexiconGlobal.LIASON_CORE_AXIOM,            LexiconGlobal.LIST_FILTERS_NULL);
//			/* Handle a ISequential. */
//			this.put(LexiconGlobal.LIASON_CORE_SEQUENCE,         LexiconGlobal.LIST_FILTERS_NULL);
//			/* Handle a Concurrency. */
//			this.put(LexiconGlobal.LIASON_CORE_CONCURRENCY,      LexiconGlobal.LIST_FILTERS_NULL);
//		} }));
//		/* Define the Concurrency's Filter Array. */
//		this.put(LexiconGlobal.LIASON_CORE_CONCURRENCY, Collections.unmodifiableMap(new HashMap<ILiason<IUIElement>, List<ILiason<List<IUIElement>>>>() { {
//			/* Handle a Core Coupling. */
//			this.put(LexiconGlobal.LIASON_CORE_COUPLING,       LexiconGlobal.LIST_FILTERS_NULL);
//			/* Handle a Refineable Coupling. */
//			this.put(LexiconGlobal.LIASON_REFINEABLE_COUPLING, LexiconGlobal.LIST_FILTERS_NULL);
//		} }));
//		/* Define the ISequential's Filter Array. */
//		this.put(LexiconGlobal.LIASON_CORE_SEQUENCE, Collections.unmodifiableMap(new HashMap<ILiason<IUIElement>, List<ILiason<List<IUIElement>>>>() { {
//			/* Handle a Core Coupling. */
//			this.put(LexiconGlobal.LIASON_CORE_COUPLING,       LexiconGlobal.LIST_FILTERS_NULL);
//			/* Handle a Refineable Coupling. */
//			this.put(LexiconGlobal.LIASON_REFINEABLE_COUPLING, LexiconGlobal.LIST_FILTERS_NULL);
//			/* Handle a Refineable Axiom. */
//			this.put(LexiconGlobal.LIASON_REFINEABLE_AXIOM,    LexiconGlobal.LIST_FILTERS_NULL);
//			/* Handle a Decoupler. */
//			this.put(LexiconGlobal.LIASON_CORE_AXIOM,          LexiconGlobal.LIST_FILTERS_NULL);
//			/* Handle a Dharma. */
//			this.put(LexiconGlobal.LIASON_CORE_DHARMA,         LexiconGlobal.LIST_FILTERS_NULL);
//			/* Handle a Comment. */
//			this.put(LexiconGlobal.LIASON_CORE_COMMENT,        LexiconGlobal.LIST_FILTERS_NULL);
//		} }));
//		/* Define the SinkDecoupler's Filter Array. */
//		this.put(LexiconGlobal.LIASON_CORE_SINKDECOUPLER, Collections.unmodifiableMap(new HashMap<ILiason<IUIElement>, List<ILiason<List<IUIElement>>>>() { {
//			/* Handle a Core Coupling. */
//			this.put(LexiconGlobal.LIASON_CORE_CONTACT, LexiconGlobal.LIST_FILTERS_NULL);
//		} }));
//		/* Define the SourceDecoupler's Filter Array. */
//		this.put(LexiconGlobal.LIASON_CORE_SOURCEDECOUPLER, Collections.unmodifiableMap(new HashMap<ILiason<IUIElement>, List<ILiason<List<IUIElement>>>>() { {
//			/* Handle a Core Coupling. */
//			this.put(LexiconGlobal.LIASON_CORE_CONTACT, LexiconGlobal.LIST_FILTERS_NULL);
//		} }));
//		/* Define the Axiom's Filter Array. */
//		this.put(LexiconGlobal.LIASON_CORE_AXIOM, Collections.unmodifiableMap(new HashMap<ILiason<IUIElement>, List<ILiason<List<IUIElement>>>>() { {
//			/* Handle a Core Coupling. */
//			this.put(LexiconGlobal.LIASON_CORE_CONTACT, LexiconGlobal.LIST_FILTERS_NULL);
//		} }));
//	} });
//	
//	/* Define the Construction Array Map. This indicates the types of response a certain enclosure performs for a given filter. */
//	@SuppressWarnings("serial") public static final Map<ILiason<IUIElement>, Map<ILiason<List<IUIElement>>, IEncapsulation>> MAP_ENCAPSULATION_ARRAY = Collections.unmodifiableMap(new HashMap<ILiason<IUIElement>, Map<ILiason<List<IUIElement>>, IEncapsulation>>() { { 
//		/* Define a Parcel's Response. */
//		this.put(LexiconGlobal.LIASON_REFINEABLE_PARCEL, Collections.unmodifiableMap(new HashMap<ILiason<List<IUIElement>>, IEncapsulation>() { { } }));
//	} });
//	
//	/* Define the Default EasingConfiguration for different containers. */
//	@SuppressWarnings("serial") public static final Map<ILiason<IUIElement>, IEasingConfiguration> MAP_SUPPLICATION_CONFIGURATION = Collections.unmodifiableMap(new HashMap<ILiason<IUIElement>, IEasingConfiguration>() { { 
//		/* Ensure a Concurrency feels tangibly distinct. */
//		this.put(LexiconGlobal.LIASON_CORE_CONCURRENCY,  IEasingConfiguration.CONFIGURATION_DROP);
//		/* Ensure a ISequential feels smooth. */
//		this.put(LexiconGlobal.LIASON_CORE_SEQUENCE,     IEasingConfiguration.CONFIGURATION_SMOOTH);
//		/* Ensure Decouplers feel smooth. */ /** TODO: There's a clear inheritance-style problem here. **/
//		this.put(LexiconGlobal.LIASON_CORE_AXIOM,           IEasingConfiguration.CONFIGURATION_SMOOTH);
//		this.put(LexiconGlobal.LIASON_CORE_SINKDECOUPLER,   IEasingConfiguration.CONFIGURATION_SMOOTH);
//		this.put(LexiconGlobal.LIASON_CORE_SOURCEDECOUPLER, IEasingConfiguration.CONFIGURATION_SMOOTH);
//	} });
//	
//	/* Define the Default EasingConfiguration for different containers. */
//	@SuppressWarnings("serial") public static final Map<ILiason<IUIElement>, IEasingConfiguration> MAP_WITHDRAWAL_CONFIGURATION = Collections.unmodifiableMap(new HashMap<ILiason<IUIElement>, IEasingConfiguration>() { { 
//		/* Ensure a Decoupler feels smooth. */
//		this.put(LexiconGlobal.LIASON_CORE_AXIOM,           IEasingConfiguration.CONFIGURATION_SMOOTH);
//		this.put(LexiconGlobal.LIASON_CORE_SINKDECOUPLER,   IEasingConfiguration.CONFIGURATION_SMOOTH);
//		this.put(LexiconGlobal.LIASON_CORE_SOURCEDECOUPLER, IEasingConfiguration.CONFIGURATION_SMOOTH);
//	} });
//	
//	/* Define the Default Construction Map. */
//	@SuppressWarnings("serial") public static final Map<ILiason<IUIElement>, Map<ILiason<IUIElement>, IEncapsulation>> MAP_ENCAPSULATION_DEFAULTS = Collections.unmodifiableMap(new HashMap<ILiason<IUIElement>, Map<ILiason<IUIElement>, IEncapsulation>>() { { 
//		/* Define the Parcel's Defaults. */
//		this.put(LexiconGlobal.LIASON_REFINEABLE_PARCEL, Collections.unmodifiableMap(new HashMap<ILiason<IUIElement>, IEncapsulation>() { { 
////			/* Define a default Core Coupling encapsulation. */
////			this.put(LexiconGlobal.LIASON_CORE_COUPLING, new RefinementGlobal.Construction() { @Override public final Refinement[] onCreateRefinements() { return new Refinement[]{ new Refinement.Public(0, 0) };                                    } });
////			/* Define a default Core Numeric encapsulation. */
////			this.put(LexiconGlobal.LIASON_CORE_NUMERIC,  new RefinementGlobal.Construction() { @Override public final Refinement[] onCreateRefinements() { return new Refinement[]{ new Refinement.Private(0, 0),   new Refinement.Final(0, 0)  };    } });
////			/* Define a default Core Axiom encapsulation. */
////			this.put(LexiconGlobal.LIASON_CORE_AXIOM,    new RefinementGlobal.Construction() { @Override public final Refinement[] onCreateRefinements() { return new Refinement[]{ new Refinement.Protected(0, 0), new Refinement.Abstract(0, 0)  }; } });
//		} }));
//		/* Define the Concurrency's Defaults. */
//		this.put(LexiconGlobal.LIASON_CORE_CONCURRENCY, Collections.unmodifiableMap(new HashMap<ILiason<IUIElement>, IEncapsulation>() { { 
////			/* Define Refineable Coupling encapsulation; we wish to strip it of it's Refinements. */
////			this.put(LexiconGlobal.LIASON_REFINEABLE_COUPLING, new RefinementGlobal.Decomposition() { @Override public final IEasingConfiguration getEasingConfiguration() { return LexiconGlobal.MAP_SUPPLICATION_CONFIGURATION.get(LexiconGlobal.LIASON_CORE_CONCURRENCY); }; });
//		} }));
//		/* Define the ISequential's Defaults. */
//		this.put(LexiconGlobal.LIASON_CORE_SEQUENCE, Collections.unmodifiableMap(new HashMap<ILiason<IUIElement>, IEncapsulation>() { { 
////			/* Define Refineable Coupling encapsulation; we wish to strip it of it's Refinements. */
////			this.put(LexiconGlobal.LIASON_REFINEABLE_COUPLING, new RefinementGlobal.Decomposition() { @Override public final IEasingConfiguration getEasingConfiguration() { return LexiconGlobal.MAP_SUPPLICATION_CONFIGURATION.get(LexiconGlobal.LIASON_CORE_SEQUENCE);    }; });
////			/* Define Refineable Coupling encapsulation; we wish to strip it of it's Refinements. */
////			this.put(LexiconGlobal.LIASON_REFINEABLE_AXIOM,    new RefinementGlobal.Decomposition() { @Override public final IEasingConfiguration getEasingConfiguration() { return LexiconGlobal.MAP_SUPPLICATION_CONFIGURATION.get(LexiconGlobal.LIASON_CORE_SEQUENCE);    }; });
//		} }));
//		/* Define the SinkDecoupler's Defaults. */ /** TODO: Generic Decoupler, onForceDataDirection, etc. **/
//		this.put(LexiconGlobal.LIASON_CORE_SINKDECOUPLER, Collections.unmodifiableMap(new HashMap<ILiason<IUIElement>, IEncapsulation>() { { 
////			/* Ensure we handle an appropriate DataDirection for the Contact. */
////			this.put(LexiconGlobal.LIASON_CORE_CONTACT, new IEncapsulation.Simple());
//			
////			{ @Override public final <T extends IUIElement> void onEncapsulate(final EEntryMode pEntryMode, final List<IUIElement> pHierarchy, final List<T> pUIElements, final IUIElement pUIElement) {
////				/* Handle Encapsulation as Normal. */
////				super.onEncapsulate(pEntryMode, pHierarchy, pUIElements, pUIElement);
////				/* Determien if we're handling a supplication. */
////				if(pEntryMode.equals(EEntryMode.SUPPLY)) {
////					/* Ensure the Contact is a Sink. */
////					((Contact)pUIElement).setDataDirection(EDataDirection.SINK);
////				}
////			} });
//		} }));
//		/* Define the SourceDecoupler's Defaults. */
//		this.put(LexiconGlobal.LIASON_CORE_SOURCEDECOUPLER, Collections.unmodifiableMap(new HashMap<ILiason<IUIElement>, IEncapsulation>() { { 
//			/* Ensure we handle an appropriate DataDirection for the Contact. */
//			this.put(LexiconGlobal.LIASON_CORE_CONTACT, new IEncapsulation.Simple());
//			
////			{ @Override public final <T extends IUIElement> void onEncapsulate(final EEntryMode pEntryMode, final List<IUIElement> pHierarchy, final List<T> pUIElements, final IUIElement pUIElement) {
////				/* Handle Encapsulation as Normal. */
////				super.onEncapsulate(pEntryMode, pHierarchy, pUIElements, pUIElement);
////				/* Determien if we're handling a supplication. */
////				if(pEntryMode.equals(EEntryMode.SUPPLY)) {
////					/* Ensure the Contact is a Sink. */
////					((Contact)pUIElement).setDataDirection(EDataDirection.SOURCE);
////				}
////			} });
//		} }));
//	} });
	
	/* Static Declarations. */
	public static final int CODE_DIM_HEIGHT_UNIT = 48;
	public static final int CODE_MAX_DRIVERS     =  1; // Defines the maximum number of writes to a single data line.
	
	/* Returns the index a YPosition matches to. */
	private static final int onFetchIndex(final int pYPosition) {
		/* Defines how many fixed code dimension intervals fit within the specified YPosition. */
		return (pYPosition / LexiconGlobal.CODE_DIM_HEIGHT_UNIT);
	}
	
	/* Returns a predicted Y value. */
	public static final int onPredictY(final int pYPosition) {
		/* Convert a predicted index back into a corresponding YPosition. */
		return (LexiconGlobal.onFetchIndex(pYPosition + (LexiconGlobal.CODE_DIM_HEIGHT_UNIT >> 1)) - DataUtils.booleanToInt(pYPosition < 0)) * LexiconGlobal.CODE_DIM_HEIGHT_UNIT;
	}
	
	/* Provides the corresponding index relative to a container. */
	public static final int onPredictIndex(final int pYPosition) {
		/* Predict the YPosition and convert it to a corresponding index. */
		return LexiconGlobal.onFetchIndex(LexiconGlobal.onPredictY(pYPosition));
	}
	
//	/* Developer Sanity Test Code. */
//	static {
//		/* First, we'll iterate the Filter Array.  */
//		for(final Entry<ILiason<IUIElement>, Map<ILiason<IUIElement>, List<ILiason<List<IUIElement>>>>> lFilterMap : LexiconGlobal.MAP_FILTER_ARRAY.entrySet()) {
//			/* Next, we must iterate the FilterMap's Table. */
//			for(final Entry<ILiason<IUIElement>, List<ILiason<List<IUIElement>>>> lFilters : lFilterMap.getValue().entrySet()) {
//				/* Iterate the PrioritizedFilters. */
//				for(final ILiason<List<IUIElement>> lPrioritizedFilter : lFilters.getValue()) {
//					/* Attempt to fetch the corresponding IEncapsulation. */
//					final IEncapsulation lEncapsulation = LexiconGlobal.MAP_ENCAPSULATION_ARRAY.get(lFilterMap.getKey()).get(lPrioritizedFilter);
//					/* Determine if a concrete definition of the Construction has not been defined. */
//					if(DataUtils.isNull(lEncapsulation)) {
//						/* Inform the developer that the Construction is unfinished. */
//						System.err.println("FATAL BUILD ERROR: Detected Incomplete Construction! { [Container: "+LexiconGlobal.getFieldFromValue(LexiconGlobal.class, lFilterMap.getKey()).getName()+"], [Filter: "+LexiconGlobal.getFieldFromValue(LexiconGlobal.class, lPrioritizedFilter).getName()+"] }");
//					}
//				}
//			}
//		}
//	}
	
//	/** TODO: Abstract to some reflection class. **/
//	private static final Field getFieldFromValue(final Class<?> pClass, final Object pValue) {
//		/* Iterate the Fields. */
//		for(final Field lField : pClass.getDeclaredFields()) {
//			/* Attempt to find the Matching Field for the given value. */
//			try { if(lField.get(pValue).equals(pValue)) { return lField; } } catch (final Exception pException) { /* Ignore Exceptions. */ }
//		}
//		/* Else, the Field could not be found. Return null. */
//		return null;
//	}
	
	/* Member Variables. */
	private LexiconGlobal() {}
	
}