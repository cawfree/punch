package uk.ac.manchester.sisp.punch.liason;

public interface ILiason<A, B> { 
	
	/* Defines a Negation of a specified Liason. */
	public static abstract class Inverted<A> implements ILiason<A, Boolean> {
		/* Returns the opposite response to the concrete Liasons' interpretation of Alice. */
		@Override public final Boolean isLiason(final A pAlice) { return !this.getLiason().isLiason(pAlice); }
		/* Abstract getter definition. */
		public abstract ILiason<A, Boolean> getLiason();
	};
	
	/* Determines if a Liason exists for Alice. */
	public abstract B isLiason(final A pAlice);
	
}