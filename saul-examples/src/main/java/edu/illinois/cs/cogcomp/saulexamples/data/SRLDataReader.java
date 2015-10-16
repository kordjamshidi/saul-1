package edu.illinois.cs.cogcomp.saulexamples.data;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.PropbankReader;
import edu.illinois.cs.cogcomp.saulexamples.nlp.SRL.SRLDataModel;

import java.util.ArrayList;
import java.util.List;

/**
 * A reader that interfaces with {@link PropbankReader} and collects all the entities required by {@link SRLDataModel}
 *
 * @author Christos Christodoulopoulos
 */
public class SRLDataReader {
	private final PropbankReader reader;
	public List<TextAnnotation> textAnnotations;

	public SRLDataReader(String treebankHome, String propbankHome) throws Exception {
        textAnnotations = new ArrayList<>();
		reader = new PropbankReader(treebankHome, propbankHome, ViewNames.SRL_VERB, true);
	}

	public void readData() {
		while (reader.hasNext()) {
			TextAnnotation ta = reader.next();
			assert ta.hasView(ViewNames.SRL_VERB);

			textAnnotations.add(ta);
		}
	}
}
