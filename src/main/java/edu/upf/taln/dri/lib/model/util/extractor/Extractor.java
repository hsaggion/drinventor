package edu.upf.taln.dri.lib.model.util.extractor;

import edu.upf.taln.dri.lib.model.DocCacheManager;
import edu.upf.taln.dri.lib.model.ext.Author;

import java.util.List;

public interface Extractor {

	List<Author> extract(DocCacheManager cacheManager);


}
