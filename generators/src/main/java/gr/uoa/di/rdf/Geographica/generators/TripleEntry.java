package gr.uoa.di.rdf.Geographica.generators;

public class TripleEntry {

	public static enum EntryType {
		Iri, Literal, TypedLiteral
	};

	public EntryType type;
	public String value;
	public String typeIri;


	public TripleEntry(EntryType type, String value, String typeIri) {
		this.type = type;
		this.value = value;
		this.typeIri = typeIri;
	}
	
	public TripleEntry(EntryType type, String value) {
		this.type = type;
		this.value = value;
		this.typeIri = null;
	}
	
	public TripleEntry() {
		this.type = null;
		this.value = null;
		this.typeIri = null;
	}
	

	public String toString() {
		switch (type) {
		case Iri:
			return "<" + value + ">";
		case TypedLiteral:
			return "\"" + value + "\"^^<" + typeIri + ">";
		default:
			// Literal
			return "\"" + value + "\"";
		}
	}
}
