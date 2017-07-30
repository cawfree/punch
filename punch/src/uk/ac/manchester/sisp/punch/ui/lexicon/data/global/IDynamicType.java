package uk.ac.manchester.sisp.punch.ui.lexicon.data.global;

import uk.ac.manchester.sisp.punch.ui.lexicon.data.IDataType;

/* Declares an interface which allows an implemetor to specify an array of associated types.  */
public interface IDynamicType {
	/* Returns the associated DataTypes. Conventionally in the form [SINK, SOURCE, BIDIRECTIONAL] to match the EDataDirection's ordinal(). */
	public abstract IDataType<?>[] getDataTypes();
}