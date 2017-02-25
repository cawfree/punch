package uk.ac.manchester.sisp.punch.ui.lexicon.literal.array;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.collision.RayCastManager;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.Coupling;
import uk.ac.manchester.sisp.punch.ui.lexicon.courier.ILexiconCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.IDataType;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.IDataType.ArrayType;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.DataGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.EDataDirection;
import uk.ac.manchester.sisp.punch.ui.lexicon.global.LexiconGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.literal.ILiteral;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.IContact;
import uk.ac.manchester.sisp.ribbon.common.IVec2;
import uk.ac.manchester.sisp.ribbon.utils.ResourceUtils;

/** TODO: MUST NOT BE ABLE TO invoke an array! fix! **/
public final class Array extends Coupling<ILiteral<?>, ArrayGroup> implements ILiteral<List<?>> {

	/* Define the default Serialization UID. */
	private static final long serialVersionUID = 1L;
	
	/* Static Definitions. */
	public static final ArrayType TYPE_ARRAY_UNINTIALIZED = new ArrayType(DataGlobal.TYPE_VOID);

	/* Member Variables. */
	private final IContact<IContact.Link> mContact;
	
	/** TODO: Define a minima that totally wraps the sequence of elements **/
	public Array(final int pX, final int pY) {
		super(pX, pY, new ArrayGroup(0, 0, LexiconGlobal.CODE_DIM_HEIGHT_UNIT, LexiconGlobal.CODE_DIM_HEIGHT_UNIT));
		/* Initialize the Contact. */
		this.mContact = new IContact.Impl(this.getSourceDecoupler().getMargin().getMinimumX(), this.getSourceDecoupler().getMargin().getMinimumY(), ResourceUtils.getResource(this.getClass().getClassLoader(), "res/icon/contact/array.svg"), Array.TYPE_ARRAY_UNINTIALIZED, this.getDataDirection());
		/* Add the Contact to the SourceDecoupler. */
		this.getSourceDecoupler().getUIElements().add(this.getContact());
	}
	
	/* Courier Dispatch Implementations. */
	@Override public <V> void onCourierDispatch(final ILexiconCourier<V> pLexiconCourier, final V pCourierPackage) { pLexiconCourier.onCourierTransit(this, pCourierPackage); }
	
	@Override
	public final boolean isDeclaration(final List<IUIElement> pHierarchy) {
		/* Expand the Declaration state by asserting that an Array is also a Declaration within an ArrayGroup context. */
		return super.isDeclaration(pHierarchy) || RayCastManager.onFetchParent(pHierarchy, this) instanceof ArrayGroup;
	}

	@Override
	public final int getOverlap() { 
		/* Allow the SourceDecoupler to completely overlap the SinkDecoupler. */
		return this.getSinkDecoupler().getWidth();
	}

	@Override
	public final EDataDirection getDataDirection() {
		return EDataDirection.SOURCE;
	}

	@Override
	public final List<?> getValue() { /** TODO: force extern synchronization? **/
		/* Allocate a reference to the ArrayData. */
		final List<ILiteral<?>> lArrayData;
		/* Synchronize along the Internals. */
		synchronized(this) {
			/* Fetch the Internals. */
			final ArrayGroup lInternals = this.getActiveInternals();
			/* Make a safe copy of the ArrayData. */
			                 lArrayData = new ArrayList<ILiteral<?>>(lInternals.getUIElements());
		}
		/* Sort the ArrayData horizontally. (This defines their order of precedence.) */
		Collections.sort(lArrayData, IVec2.COMPARATOR_HORIZONTAL);
		/* Allocate the Value. */
		final List<Object> lValues = new ArrayList<Object>();
		/* Iterate the ArrayData. */
		for(final ILiteral<?> lLiteral : lArrayData) {
			/* Buffer the Literal's Value into the Values. */
			lValues.add(lLiteral.getValue());
		}
		/* Return the Values. */
		return lValues;
	}

	/** TODO: Improve modularity. **/
	@Override
	public final IDataType<?> getDataType(final EDataDirection pDataDirection) { 
		/* Return the Contact's DataType. */
		return this.getContact().getDataType(pDataDirection);
	}
	
	/* Defines whether the array is in an uninitialized stage. */
	public final boolean isUninitialized() { /** TODO: force extern synchronization **/
		/* Synchronize upon ourself. */
		synchronized(this) { 
			/* Fetch the ArrayGroup. */
			final ArrayGroup lArrayGroup = this.getInternals().get(0);
			/* Determine whether the Internals contains any entries. If not, the array is uninitialized. */
			return lArrayGroup.getUIElements().isEmpty();
		}
	}
	
	public final IContact<IContact.Link> getContact() {
		return this.mContact;
	}
	
	@Override
	protected final boolean isGenerateCascades() {
		/* Define that an Array may not use Cascade generation. */
		return false;
	}
	
	/* Returns the Internals which has the current focus of the user. */
	@Override public final ArrayGroup getActiveInternals() { 
		/* The Array will only ever possess a single level of Internals, so we'll just return this. */
		return this.getInternals().get(0);
	}
	
}