package uk.ac.manchester.sisp.punch.ui.lexicon.literal;

import java.util.List;

import uk.ac.manchester.sisp.punch.PunchModel;
import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.IUIPadding;
import uk.ac.manchester.sisp.punch.ui.color.global.ColorGlobal;
import uk.ac.manchester.sisp.punch.ui.core.Field;
import uk.ac.manchester.sisp.punch.ui.core.IGroup;
import uk.ac.manchester.sisp.punch.ui.distribution.IDistribution;
import uk.ac.manchester.sisp.punch.ui.distribution.global.DistributionGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.IAtomic;
import uk.ac.manchester.sisp.punch.ui.lexicon.ILexical;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.EDataDirection;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.IDataConduit;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.IValue;
import uk.ac.manchester.sisp.punch.ui.update.UIUpdateDispatcher;
import uk.ac.manchester.sisp.punch.ui.update.easing.ui.UIEasingGroup;
import uk.ac.manchester.sisp.ribbon.common.IBounds2;
import uk.ac.manchester.sisp.ribbon.io.ArrayStore;
import uk.ac.manchester.sisp.ribbon.opengl.vector.VectorPath;
import uk.ac.manchester.sisp.ribbon.opengl.vector.VectorPathContext;
import uk.ac.manchester.sisp.ribbon.opengl.vector.global.ELineCap;
import uk.ac.manchester.sisp.ribbon.opengl.vector.global.ELineJoin;
import uk.ac.manchester.sisp.ribbon.opengl.vector.global.IPathDefinition;
import uk.ac.manchester.sisp.ribbon.opengl.vector.global.IStroke;
import uk.ac.manchester.sisp.ribbon.opengl.vector.global.IVectorPathGroup;
import uk.ac.manchester.sisp.ribbon.ui.easing.IEasingConfiguration;
import uk.ac.manchester.sisp.ribbon.utils.DataUtils;

public interface ILiteral<V> extends ILexical, IDataConduit, IAtomic, IValue<V> { /** TODO: To class. Avoid replication. **/
	
	/* Textual Literal Base Implementation. */
	public static abstract class Input<T> extends IGroup.Impl<IUIElement> implements ILiteral<T>, IStroke { 

		/* Define the default Serialization UID. */
		private static final long serialVersionUID = 1L;
		
		/* Member Variables. */
		private final Field mField; 
		
		public Input(int pX, int pY, final String pValue, final PunchModel pPunchModel) {
			super(pX, pY, DataUtils.JAVA_NULL_INDEX, DataUtils.JAVA_NULL_INDEX);
			/* Initialize Member Variables. */
			this.mField = new Field(0, 0, PunchModel.FILE_FONT_REGULAR, pValue, 24.0f, new float[]{ 0.0f, 0.0f, 0.0f, 1.0f }, pPunchModel);
			/* Reposition the Field. */
			this.getField().setX((Math.round(getStrokeWidth()) << 1));
			this.getField().setY(this.getMargin().getMinimumY());
			/* Add the Field. */
			this.getUIElements().add(this.getField());
			/* Distribute the Group. */
			DistributionGlobal.onDistributeHierarchy(pPunchModel, null, this, IEasingConfiguration.CONFIGURATION_NONE);
		}
		
		/* Define the Bounds. This is some fudge I've made to fix a Numeric to the CODE_DIM_HEIGHT_WIDTH. It'd be nice to come up with something more constant. */
		@Override public final IBounds2.I getMargin() { return new IBounds2.I.Impl(0, 4, 0, 0); }

		@Override
		public IVectorPathGroup[] getVectorPathGroups(final ArrayStore.Float pFloatStore, final VectorPathContext pVectorPathContext) {
			/* Allocate a simple shape for debugging. */
			return new IVectorPathGroup[]{ 
				/* Background. */
				new IVectorPathGroup.Impl(new VectorPath[]{ pVectorPathContext.onRectangle(pFloatStore, 0.0f, 0.0f, this.getWidth(), this.getHeight()).onCreatePath(pFloatStore) }, new IPathDefinition[]{ new IPathDefinition.Fill(ColorGlobal.RGBA_FILL_CORE) }),
				/* Stroke. */
				new IVectorPathGroup.Impl(new VectorPath[]{ pVectorPathContext.onRectangle(pFloatStore, (this.getStrokeWidth() / 2.0f), (this.getStrokeWidth() / 2.0f), (this.getWidth() - this.getStrokeWidth()), (this.getHeight() - this.getStrokeWidth())).onCreatePath(pFloatStore) }, new IPathDefinition[]{ new IPathDefinition.Stroke(this.getDataType(this.getDataDirection()).getColor(), this) }),
			};
		}
		
		@Override
		public final IDistribution onFetchDistribution(final List<IUIElement> pHierarchy, final UIUpdateDispatcher pUIUpdateDispatcher) {
			/* Define a custom Distribution. */
			return new IDistribution() {
				/* Do nothing upon disposal. */
				@Override public final void dispose() { }
				/* Infer distributables. */
				@Override public final void onSupplyDistributables(final List<IUIElement> pUIElements) {}
				/* Apply the custom Distribution. */
				@Override public void onDistributeElements(final UIEasingGroup pUIEasingGroup, final IUIPadding pUIPadding, final IBounds2.I.W pResultingBounds) {
					/* Simply buffer the bounds of the Field. */
					pResultingBounds.setMaximumX(getField().getX() + getField().getWidth()  + (Math.round(getStrokeWidth()) << 1));
					pResultingBounds.setMaximumY(getField().getY() + getField().getHeight() + (Math.round(getStrokeWidth()) << 1));
				}
				/* Assert a fixed anchor. */
				@Override public boolean isAnchored() { return true; }
			};
		}
		
		public final Field getField() {
			return this.mField;
		}
		
		/* Literal Constants. */
		@Override public final float          getStrokeWidth()   { return ILiteral.STROKE_WIDTH;       }
		@Override public final ELineJoin      getLineJoin()      { return ILiteral.STROKE_LINE_JOIN;   }
		@Override public final ELineCap       getLineCap()       { return ILiteral.STROKE_LINE_CAP;    }
		@Override public final EDataDirection getDataDirection() { return EDataDirection.SOURCE;       }
		
	}
	
	/* Look-and-feel Constants. */
	public static final float     STROKE_WIDTH     = 4.0f;
	public static final ELineCap  STROKE_LINE_CAP  = ELineCap.BUTT;
	public static final ELineJoin STROKE_LINE_JOIN = ELineJoin.MITER;
	
}