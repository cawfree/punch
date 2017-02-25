package uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact;

import java.io.File;

import uk.ac.manchester.sisp.punch.ui.lexicon.courier.ILexiconCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.IDataType;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.DataGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.EDataDirection;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.IDataConduit;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.IDataDirectional;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.IDirectional;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.IDynamicType;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.IParameter;
import uk.ac.manchester.sisp.punch.ui.lexicon.referencing.IInvocatable;
import uk.ac.manchester.sisp.punch.ui.lexicon.referencing.IReferential;
import uk.ac.manchester.sisp.ribbon.common.IColorRGBA;

public interface IContact <T extends IContact<?>> extends IParameter, IDirectional.W, IColorRGBA, IDataConduit, IDataDirectional.W, IInvocatable<T> { 
	
	/* Define the Link; a Contact which carries a logical tie to it's corresponding entry point along a core invocation. */
	public static class Link extends IContact.Impl implements IReferential<IContact<?>>, ILexiconCourier.Dispatch { 
		
		/* Define the default Serialization UID. */
		private static final long serialVersionUID = 1L;
		
		/* Member Variables. */
		private final IContact<?> mReference;
		
		public Link(final int pX, final int pY, final File pFile, final IDataType<?> pDataType, final EDataDirection pDataDirection, final IContact<?> pReference) {
			super(pX, pY, pFile, pDataType, pDataDirection);
			/* Initialize Member Variables. */
			this.mReference = pReference;
		}
		
		/* Generic Courier Dispatch. */
		@Override public final <U> void onCourierDispatch(final ILexiconCourier<U> pLexiconCourier, final U pCourierPackage) { pLexiconCourier.onCourierTransit(this, pCourierPackage); }
		
		/* Return the Reference to the supplied Contact. */
		@Override public final IContact<?> getReference() { return this.mReference; }
		
	};
	
	/* Default Implementation Stub. */
	public static class Impl extends IParameter.Impl implements IContact<IContact.Link> { 
		
		/* Define the default Serialization UID. */
		private static final long serialVersionUID = 1L;
		
		/* Member Variables. */
		private IDataType<?>   mDataType;
		private EDataDirection mDataDirection;
		
		public Impl(final int pX, final int pY, final File pFile, final IDataType<?> pDataType, final EDataDirection pDataDirection) {
			super(pX, pY, pFile);
			/* Initialize Member Variables. */
			this.mDataType      = pDataType;
			this.mDataDirection = pDataDirection;
		}
		
		/* Generic Courier Dispatch. */
		@Override public <U> void onCourierDispatch(final ILexiconCourier<U> pLexiconCourier, final U pCourierPackage) { pLexiconCourier.onCourierTransit(this, pCourierPackage); }
		
		@Override
		public final void setDataDirection(final EDataDirection pDataDirection) {
			this.mDataDirection = pDataDirection;
		}

		@Override
		public final EDataDirection getDataDirection() {
			return this.mDataDirection;
		}

		@Override
		public final float[] getColor() {
			/* Return the color at the DataDirection. */
			return this.getDataType(this.getDataDirection()).getColor();
		}

		@Override
		public final void setDataType(final EDataDirection pDataDirection, final IDataType<?> pDataType) {
			/* Overwrite the DataType. */
			this.mDataType = pDataType;
		}
		
		@Override
		public final IDataType<?> getDataType(final EDataDirection pDataDirection) {
			return this.mDataType;
		}

		@Override
		public final Link onCreateInvocation() {
			/* Allocate a new Link which carries a reference to ourself. */
			return new Link(this.getX(), this.getY(), this.getFile(), this.getDataType(this.getDataDirection()), this.getDataDirection(), this);
		}
		
	};
	
	/* Default Implementation Stub. */ /** TODO: Mustn't be Invocatable. **/
	public static class Dynamic extends IParameter.Impl implements IContact<IContact.Link>, IDynamicType { 
		
		/* Define the default Serialization UID. */
		private static final long serialVersionUID = 1L;
		
		/* Member Variables. */
		private       EDataDirection mDataDirection;
		private final IDataType<?>[] mDataTypes;
		
		public Dynamic(final int pX, final int pY, final File pFile, final EDataDirection pDataDirection) {
			super(pX, pY, pFile);
			/* Initialize Member Variables. */
			this.mDataDirection = pDataDirection;
			this.mDataTypes     = new IDataType[] { DataGlobal.TYPE_VOID, DataGlobal.TYPE_VOID, DataGlobal.TYPE_VOID };
		}
		
		/* Generic Courier Dispatch. */
		@Override public <U> void onCourierDispatch(final ILexiconCourier<U> pLexiconCourier, final U pCourierPackage) { pLexiconCourier.onCourierTransit(this, pCourierPackage); }
		
		@Override
		public final void setDataDirection(final EDataDirection pDataDirection) {
			this.mDataDirection = pDataDirection;
		}
		
		@Override
		public final void setDataType(final EDataDirection pDataDirection, final IDataType<?> pDataType) {
			/* Update the DataTypes at the asserted Index. */
			this.getDataTypes()[pDataDirection.ordinal()] = pDataType;
		}

		@Override
		public final EDataDirection getDataDirection() {
			return this.mDataDirection;
		}

		@Override
		public float[] getColor() {
			/* A Dynamic Type will always reflect the propagating type. */
			return this.getDataType(this.getDataDirection()).getColor();
		}

		@Override
		public final Link onCreateInvocation() {
			/* Allocate a new Link which carries a reference to ourself. */
			return new Link(this.getX(), this.getY(), this.getFile(), this.getDataType(this.getDataDirection()), this.getDataDirection(), this);
		}

		@Override
		public final IDataType<?>[] getDataTypes() {
			return this.mDataTypes;
		}

		@Override
		public final IDataType<?> getDataType(final EDataDirection pDataDirection) { 
			/* Return the associated type at this position. */
			return this.getDataTypes()[pDataDirection.ordinal()];
		}
		
	};
	
}