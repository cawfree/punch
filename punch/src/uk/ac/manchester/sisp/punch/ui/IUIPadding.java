package uk.ac.manchester.sisp.punch.ui;

public interface IUIPadding {
	
	public static IUIPadding PADDING_NULL = new IUIPadding() {
		/* Define negligible padding. */
		@Override public int getPadding() { return 0; }
	};
	
	public static class Impl implements IUIPadding.W {
		/* Member Variables. */
		private int mPadding;
		/* Constructor. */
		public Impl() {
			this(0);
		}
		/* Constructor. */
		public Impl(final int pPadding) {
			this.mPadding = pPadding;
		}
		/* Getters and Setters. */
		public void setPadding(final int pPadding) {
			this.mPadding = pPadding;
		}
		public int getPadding() {
			return this.mPadding;
		}
	}
	
	public static interface W extends IUIPadding {
		public abstract void setPadding(final int pPadding);
	}
	
	/* Returns the internal padding to be applied to a specific graphical component. */
	public abstract int getPadding();
	
}
