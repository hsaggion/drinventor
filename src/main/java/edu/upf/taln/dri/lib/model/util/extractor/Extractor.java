package edu.upf.taln.dri.lib.model.util.extractor;

import edu.upf.taln.dri.lib.model.DocCacheManager;
import edu.upf.taln.dri.lib.model.ext.Author;

import java.util.List;

public abstract class Extractor {

	abstract public List<Author> extract(DocCacheManager cacheManager);

	public static String normalizeText(String inputText) {
		if(inputText != null) {
			inputText = inputText.replaceAll("\t", " ");
			inputText = inputText.replaceAll("\\s+", " ");
			inputText = inputText.trim();
		}
		return inputText;
	}
}
