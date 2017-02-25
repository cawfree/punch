package uk.ac.manchester.sisp.punch.ui.lexicon.decoupler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.color.global.ColorGlobal;
import uk.ac.manchester.sisp.punch.ui.core.IGroup;
import uk.ac.manchester.sisp.punch.ui.distribution.IDistribution;
import uk.ac.manchester.sisp.punch.ui.distribution.constants.LinearDistribution;
import uk.ac.manchester.sisp.punch.ui.lexicon.ILexical;
import uk.ac.manchester.sisp.punch.ui.lexicon.courier.ILexiconCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.IDirectional;
import uk.ac.manchester.sisp.punch.ui.lexicon.global.LexiconGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.IContact;
import uk.ac.manchester.sisp.punch.ui.update.UIUpdateDispatcher;
import uk.ac.manchester.sisp.ribbon.common.IBounds2;
import uk.ac.manchester.sisp.ribbon.common.IColorRGBA;
import uk.ac.manchester.sisp.ribbon.common.IDim2;
import uk.ac.manchester.sisp.ribbon.io.ArrayStore;
import uk.ac.manchester.sisp.ribbon.opengl.vector.VectorPath;
import uk.ac.manchester.sisp.ribbon.opengl.vector.VectorPathContext;
import uk.ac.manchester.sisp.ribbon.opengl.vector.global.ELineCap;
import uk.ac.manchester.sisp.ribbon.opengl.vector.global.ELineJoin;
import uk.ac.manchester.sisp.ribbon.opengl.vector.global.IPathDefinition;
import uk.ac.manchester.sisp.ribbon.opengl.vector.global.IStroke;
import uk.ac.manchester.sisp.ribbon.opengl.vector.global.IVectorPathGroup;

public abstract class Decoupler<U extends IContact<?>> extends IGroup.Impl<U> implements ILexical, IDim2.I.W, IStroke, IDirectional, IColorRGBA { 

	/* Define the default Serialization UID. */
	private static final long serialVersionUID = 1L;

	/* Static Declarations. */
	private static final int        DEFAULT_PADDING = 4;
	private static final IBounds2.I DEFAULT_MARGIN  = new IBounds2.I.Impl(4, 4, 4, 4); /** TODO: Each should really be a function of Contact.DIM. ((HEIGHT_UNIT - Contact.DIM / 2)) **/
	
	public Decoupler(final int pX, final int pY) {
		super(pX, pY, LexiconGlobal.CODE_DIM_HEIGHT_UNIT, LexiconGlobal.CODE_DIM_HEIGHT_UNIT);
	}
	
	/* Courier Dispatch Operations. */
	@Override public <T> void onCourierDispatch(final ILexiconCourier<T> pLexiconCourier, final T pCourierPackage) { pLexiconCourier.onCourierTransit(this, pCourierPackage); }
	
	@Override
	public IDistribution onFetchDistribution(final List<IUIElement> pHierarchy, final UIUpdateDispatcher pUIUpdateDispatcher) { 
		/* Make a copy of the Contacts. */
		final List<IContact<?>> lContacts = new ArrayList<IContact<?>>(this.getUIElements());
		/* Return the Distribution. */
		return new LinearDistribution<IContact<?>>(lContacts) {
			/* Define the order to sort the Contacts by. */
			@Override public final void onSortElements(final List<IContact<?>> pContacts) { 
				/* Sort the Contacts using Imbrication. */
				Collections.sort(pContacts, LexiconGlobal.COMPARATOR_IMBRICATION);
			}
			/* Assert that we'll arrange the Contacts vertically. */
			@Override public final boolean isHorizontal() { return false; }
		};
	}

	@Override
	public final IVectorPathGroup[] getVectorPathGroups(final ArrayStore.Float pFloatStore, final VectorPathContext pVectorPathContext) {
		/* Allocate a simple shape for debugging. */
		return new IVectorPathGroup[]{ 
			/* Allocate a simple filled Rectangle. (Use IVectorPathGroup.Impl to force evaluation!) */
			new IVectorPathGroup.Impl(new VectorPath[]{ pVectorPathContext.onRectangle(pFloatStore, (this.getStrokeWidth() * 0.5f), (this.getStrokeWidth() * 0.5f), this.getWidth() - (this.getStrokeWidth()), this.getHeight() - (this.getStrokeWidth())).onCreatePath(pFloatStore) }, new IPathDefinition[]{ new IPathDefinition.Fill(this.getColor()), new IPathDefinition.Stroke(ColorGlobal.RGBA_BLACK, this.getStrokeWidth(), ELineCap.BUTT, ELineJoin.MITER) })
		};
	}

	@Override
	public final IDim2.I getMinima() {
		return new IDim2.I.Impl(LexiconGlobal.CODE_DIM_HEIGHT_UNIT, LexiconGlobal.CODE_DIM_HEIGHT_UNIT);
	}

	@Override 
	public float getStrokeWidth() { 
		return 1.0f; 
	}
	
	@Override 
	public final ELineJoin getLineJoin() { 
		return ELineJoin.MITER;
	}
	
	@Override 
	public final ELineCap getLineCap() { 
		return ELineCap.BUTT; 
	}
	
	/** TODO: Abstract to contact/syntax width calculation **/
	@Override
	public final IBounds2.I getMargin() { 
		return Decoupler.DEFAULT_MARGIN;
	}
	
	@Override
	public final int getPadding() { 
		return Decoupler.DEFAULT_PADDING;
	}

	@Override public final boolean isScissorContents() { return false; }

	@SuppressWarnings("unchecked") @Override public final Comparator<U> getComparator() { return (Comparator<U>)LexiconGlobal.COMPARATOR_IMBRICATION; }

	@Override
	public boolean isScissorable() {
		return false;
	}
	
	@Override
	public float[] getColor() {
		/* Return the default Decoupler colour. */
		return ColorGlobal.RGBA_FILL_CORE;
	}
	
}
