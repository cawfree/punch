package uk.ac.manchester.sisp.punch.ui.core.search;

import uk.ac.manchester.sisp.punch.PunchModel;
import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.color.global.ColorGlobal;
import uk.ac.manchester.sisp.punch.ui.core.Field;
import uk.ac.manchester.sisp.punch.ui.core.IGroup;
import uk.ac.manchester.sisp.punch.ui.core.ISVGIcon;
import uk.ac.manchester.sisp.ribbon.common.IBounds2;
import uk.ac.manchester.sisp.ribbon.io.ArrayStore;
import uk.ac.manchester.sisp.ribbon.opengl.vector.VectorPath;
import uk.ac.manchester.sisp.ribbon.opengl.vector.VectorPathContext;
import uk.ac.manchester.sisp.ribbon.opengl.vector.global.ELineCap;
import uk.ac.manchester.sisp.ribbon.opengl.vector.global.ELineJoin;
import uk.ac.manchester.sisp.ribbon.opengl.vector.global.IPathDefinition;
import uk.ac.manchester.sisp.ribbon.opengl.vector.global.IVectorPathGroup;
import uk.ac.manchester.sisp.ribbon.utils.DataUtils;
import uk.ac.manchester.sisp.ribbon.utils.ResourceUtils;

public final class SearchBox extends IGroup.Impl<IUIElement> { 

	/* Define the default Serialization UID. */
	private static final long serialVersionUID = 1L;
	
	/* Static Declarations. */
	private static final float RADIUS_CORNER = 4.0f;
	
	/* Member Variables. */
	private final ISVGIcon mSearchIcon;
	private final Field   mField;

	public SearchBox(final int pX, final int pY, final int pWidth, final PunchModel pPunchModel) { /** TODO: Default text. **/
		super(pX, pY, pWidth, DataUtils.JAVA_NULL_INDEX);
		/* Initialize Member Variables. */ /** TODO: Must abstract width/height parameters! **/
		this.mSearchIcon = new ISVGIcon.Flat(0, 0, 14, 14, ResourceUtils.getResource(this.getClass().getClassLoader(), "res/icon/search.svg"), ColorGlobal.RGBA_SWEET_GREY);
		this.mField      = new Field(0, 0, PunchModel.FILE_FONT_CODE, "Create", 12.0f, ColorGlobal.RGBA_SWEET_GREY, pPunchModel); /** TODO: Staticize. **/
		/* Assign the IGroup's Height. */
		this.setHeight(this.getSearchIcon().getHeight() + (this.getMargin().getMinimumY() + this.getMargin().getMaximumY()));
		/* Assert the SearchIcon's location. */
		this.getSearchIcon().setX(this.getMargin().getMinimumX());
		this.getSearchIcon().setY(this.getMargin().getMinimumY());
		/* Reposition the Field. */
		this.getField().setX(this.getSearchIcon().getX() + this.getSearchIcon().getWidth() + this.getPadding());
		this.getField().setY((this.getHeight() - this.getField().getHeight()) >> 1);
		/* Add the UIElements to the IGroup. */
		this.getUIElements().add(this.getField());
		this.getUIElements().add(this.getSearchIcon());
	}
	
	/* Control the Header's look-and-feel. */
	@Override public final IVectorPathGroup[] getVectorPathGroups(final ArrayStore.Float pFloatStore, final VectorPathContext pVectorPathContext) {
		/* Allocate a simple shape for debugging. */
		return new IVectorPathGroup[]{ /** TODO: Strokes not working now? What? **/
			/* Allocate a simple filled Rectangle. (Use IVectorPathGroup.Impl to force evaluation!) */
			new IVectorPathGroup.Impl(new VectorPath[]{ pVectorPathContext.onRoundedRectangle(pFloatStore, 0.5f, 0.5f, (this.getWidth() - 1.0f), (this.getHeight() - 1.0f), SearchBox.RADIUS_CORNER).onCreatePath(pFloatStore) }, new IPathDefinition[]{ new IPathDefinition.Fill(ColorGlobal.RGBA_WHITE), new IPathDefinition.Stroke(ColorGlobal.RGBA_SWEET_GREY, 1.0f, ELineCap.BUTT, ELineJoin.ROUND) })
		};
	}

	@Override
	public IBounds2.I getMargin() {
		return new IBounds2.I.Impl(this.getPadding(), this.getPadding(), this.getPadding(), this.getPadding());
	}

	@Override
	public final int getPadding() {
		return 4;
	}

	private final ISVGIcon getSearchIcon() {
		return this.mSearchIcon;
	}
	
	public final Field getField() {
		return this.mField;
	}

}