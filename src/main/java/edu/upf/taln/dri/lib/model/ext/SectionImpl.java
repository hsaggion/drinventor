/*
 * ******************************************************************************************************
 * Dr. Inventor Text Mining Framework Java Library
 * 
 * This code has been developed by the Natural Language Processing Group of the
 * Universitat Pompeu Fabra in the context of the FP7 European Project Dr. Inventor
 * Call: FP7-ICT-2013.8.1 - Agreement No: 611383
 * 
 * Dr. Inventor Text Mining Framework Java Library is available under an open licence, GPLv3, for non-commercial applications.
 * ******************************************************************************************************
 */
package edu.upf.taln.dri.lib.model.ext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import edu.upf.taln.dri.lib.model.BaseDocumentElem;
import edu.upf.taln.dri.lib.model.DocCacheManager;
import edu.upf.taln.dri.lib.model.util.ObjectGenerator;

/**
 * IMPORTANT: Never instantiate directly this class! <br/>
 * Sentence of a document together with its descriptive features.
 * 
 *
 */
public class SectionImpl extends BaseDocumentElem implements Section {

	private Integer id;
	private String name;
	private Integer level;
	private Integer parentSectionId;
	private LangENUM language;
	private List<Integer> childrenSectionId = new ArrayList<Integer>();
	private List<Integer> sentencesId = new ArrayList<Integer>();

	// Constructor
	public SectionImpl(DocCacheManager cacheManager) {
		super(cacheManager);
		this.id = null;
		this.name = null;
		this.level = null;
		this.parentSectionId = null;
		this.language = LangENUM.UNSPECIFIED;
		this.childrenSectionId = new ArrayList<Integer>();
		this.sentencesId = new ArrayList<Integer>();
	}

	// Setters and getters
	public Integer getId() {
		return (this.id != null) ? new Integer(this.id) : null;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return (this.name != null) ? new String(this.name) : null;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getLevel() {
		return (this.level != null) ? new Integer(this.level) : null;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Integer getParentSectionId() {
		return (this.parentSectionId != null) ? new Integer(this.parentSectionId) : null;
	}

	public void setParentSectionId(Integer parentSectionId) {
		this.parentSectionId = parentSectionId;
	}

	public List<Integer> getChildrenSectionId() {
		return (this.childrenSectionId != null) ? Collections.unmodifiableList(this.childrenSectionId) : null;
	}

	public void setChildrenSectionId(List<Integer> childrenSectionId) {
		this.childrenSectionId = childrenSectionId;
	}

	public List<Integer> getSentencesId() {
		return (this.sentencesId != null) ? Collections.unmodifiableList(this.sentencesId) : null;
	}

	public void setSentencesId(List<Integer> sentencesId) {
		this.sentencesId = sentencesId;
	}

	// Other interface implementations
	public boolean addSentenceId(Integer newSentId) {
		if(newSentId == null || sentencesId.contains(newSentId)) {
			return false;
		}
		else {
			sentencesId.add(newSentId);
			return true;
		}
	}

	public boolean addChildSectionId(Integer newChildSectId) {
		if(newChildSectId == null || childrenSectionId.contains(newChildSectId)) {
			return false;
		}
		else {
			childrenSectionId.add(newChildSectId);
			return true;
		}
	}

	@Override
	public Section getParentSection() {
		return ObjectGenerator.getSectionFromId(this.parentSectionId, this.cacheManager);
	}

	@Override
	public List<Section> getSubsections() {
		List<Section> result = new ArrayList<Section>();

		if(!CollectionUtils.isEmpty(this.childrenSectionId)) {
			for(Integer subsectionId : this.childrenSectionId) {
				Section subsection = ObjectGenerator.getSectionFromId(subsectionId, this.cacheManager);
				if(subsection != null) {
					result.add(subsection);
				}
			}
		}

		return result;
	}

	@Override
	public List<Sentence> getSentences() {
		List<Sentence> result = new ArrayList<Sentence>();

		if(!CollectionUtils.isEmpty(this.sentencesId)) {
			for(Integer sentenceId : this.sentencesId) {
				Sentence sentence = ObjectGenerator.getSentenceFromId(sentenceId, this.cacheManager);
				if(sentence != null) {
					result.add(sentence);
				}
			}
		}

		return result;
	}
	
	public void setLanguage(LangENUM languge) {
		this.language = languge;
	}

	@Override
	public LangENUM getLanguage() {
		switch(this.language) {
		case ES:
			return LangENUM.EN;
		case EN:
			return LangENUM.ES;
		default:
			return LangENUM.UNSPECIFIED;
		} 
	}

	@Override
	public String asString(boolean compactOutput) {
		String sectionStr = "";

		sectionStr += "[SECTION] Full name: '" + ((this.id != null) ? this.id + "" : "-") + "'" +
				((this.name != null || !compactOutput) ? ", Name: '" + StringUtils.defaultString(this.name, "-") + "'" : "") +
				((this.level != null || !compactOutput) ? ", Level: '" + ((this.level != null) ? this.level + "" : "-") + "'" : "") +
				((this.parentSectionId != null || !compactOutput) ? ", Parent section ID: '" + ((this.parentSectionId != null) ? this.parentSectionId + "" : "-") + "'" : "") +
				((this.childrenSectionId != null || !compactOutput) ? ", Children sections IDs: '" + ((this.childrenSectionId != null) ? this.childrenSectionId + "" : "-") + "'" : "") +
				((this.sentencesId != null || !compactOutput) ? ", Sentences IDs: '" + ((this.sentencesId != null) ? this.sentencesId + "" : "-") + "'" : "") + "\n";

		return sectionStr;
	}

}
