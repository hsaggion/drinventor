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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.upf.taln.dri.lib.model.BaseDocumentElem;
import edu.upf.taln.dri.lib.model.DocCacheManager;

/**
 * IMPORTANT: Never instantiate directly this class! <br/>
 * HeaderImpl represents the header of a paper
 * 
 *
 */
public class HeaderImpl extends BaseDocumentElem implements Header {

	private String plainText;
	private Map<LangENUM, String> titles;
	private Map<LangENUM, List<String>> keywords;
	public List<Author> authorList;
	private String year;
	private String pages;
	private String firstPage;
	private String lastPage;
	private Map<PubIdENUM, String> pubIDmap;
	private String openURL;
	private String bibsonomyURL;
	private String chapter;
	private String volume;
	private String issue;
	private String series;
	private String publisher;
	private String publisherLoc;
	private String journal;
	private String edition;
	private List<Author> editorList;
	public List<Institution> institutions;

	// Constructors
	public HeaderImpl(DocCacheManager cacheManager) {
		super(cacheManager);
		this.plainText = null;
		this.titles =  new HashMap<LangENUM, String>();
		this.keywords = new HashMap<LangENUM, List<String>>();
		this.authorList = new ArrayList<Author>();
		this.year = null;
		this.pages =  null;
		this.pubIDmap =  new HashMap<PubIdENUM, String>();
		this.openURL =  null;
		this.bibsonomyURL =  null;
		this.chapter =  null;
		this.volume =  null;
		this.series =  null;
		this.publisher = null;
		this.journal =  null;
		this.edition =  null;
		this.editorList = new ArrayList<Author>(); 
		this.institutions = new ArrayList<Institution>();
	}


	// Setters and getters
	public String getPlainText() {
		return (this.plainText != null) ? new String(this.plainText) : null;
	}

	public void setPlainText(String plainText) {
		this.plainText = plainText;
	}

	public Map<LangENUM, String> getTitles() {
		return (this.titles != null) ? Collections.unmodifiableMap(this.titles) : null;
	}

	public void addTitle(LangENUM language, String title) {
		this.titles.put(language, title);
	}
	
	public Map<LangENUM, List<String>> getKeywords() {
		return Collections.unmodifiableMap(keywords);
	}

	public void setKeyword(LangENUM lang, List<String> keywords) {
		this.keywords.put(lang, keywords);
	}

	public List<Author> getAuthorList() {
		return (this.authorList != null) ? Collections.unmodifiableList(this.authorList) : null;
	}

	public void setAuthorList(List<Author> authorList) {
		this.authorList = authorList;
	}

	public String getYear() {
		return (this.year != null) ? new String(this.year) : null;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getPages() {
		return (this.pages != null) ? new String(this.pages) : null;
	}

	public void setPages(String pages) {
		this.pages = pages;
	}

	public String getFirstPage() {
		return (this.firstPage != null) ? new String(this.firstPage) : null;
	}

	public void setFirstPage(String firstPage) {
		this.firstPage = firstPage;
	}

	public String getLastPage() {
		return (this.lastPage != null) ? new String(this.lastPage) : null;
	}

	public void setLastPage(String lastPage) {
		this.lastPage = lastPage;
	}

	public void setPubID(PubIdENUM pubIDtype, String pubIDvalue) {
		if(pubIDtype != null && pubIDvalue != null) {
			this.pubIDmap.put(pubIDtype, pubIDvalue);
		}
	}

	public String getOpenURL() {
		return (this.openURL != null) ? new String(this.openURL) : null;
	}

	public void setOpenURL(String openURL) {
		this.openURL = openURL;
	}

	@Override
	public String getPubID(PubIdENUM pubIDtype) {
		if(pubIDtype != null) {
			switch(pubIDtype) {
			case DOI:
				return this.pubIDmap.get(PubIdENUM.DOI);
			case PMID:
				return this.pubIDmap.get(PubIdENUM.PMID);
			default:
				return null;
			}
		}

		return null;
	}

	public String getBibsonomyURL() {
		return (this.bibsonomyURL != null) ? new String(this.bibsonomyURL) : null;
	}

	public void setBibsonomyURL(String bibsonomyURL) {
		this.bibsonomyURL = bibsonomyURL;
	}

	public String getChapter() {
		return (this.chapter != null) ? new String(this.chapter) : null;
	}

	public void setChapter(String chapter) {
		this.chapter = chapter;
	}

	public String getVolume() {
		return (this.volume != null) ? new String(this.volume) : null;
	}

	public void setVolume(String volume) {
		this.volume = volume;
	}

	public String getIssue() {
		return (this.issue != null) ? new String(this.issue) : null;
	}

	public void setIssue(String issue) {
		this.issue = issue;
	}

	public String getSeries() {
		return (this.series != null) ? new String(this.series) : null;
	}

	public void setSeries(String series) {
		this.series = series;
	}

	public String getPublisher() {
		return (this.publisher != null) ? new String(this.publisher) : null;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getPublisherLoc() {
		return (this.publisherLoc != null) ? new String(this.publisherLoc) : null;
	}

	public void setPublisherLoc(String publisherLoc) {
		this.publisherLoc = publisherLoc;
	}

	public String getJournal() {
		return (this.journal != null) ? new String(this.journal) : null;
	}

	public void setJournal(String journal) {
		this.journal = journal;
	}

	public String getEdition() {
		return (this.edition != null) ? new String(this.edition) : null;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}

	public List<Author> getEditorList() {
		return (this.editorList != null) ? Collections.unmodifiableList(this.editorList) : null;
	}

	public void setEditorList(List<Author> editorList) {
		this.editorList = editorList;
	}

	public List<Institution> getInstitutions() {
		return (this.institutions != null) ? Collections.unmodifiableList(this.institutions) : null;
	}

	public void setInstitutions(List<Institution> institutions) {
		this.institutions = institutions;
	}

	// Other methods
	public void addAuthor(Author auth) {
		if(auth != null) {
			if (this.authorList == null) {
				this.authorList = new ArrayList<Author>();
			}
			this.authorList.add(auth);
		}
	}

	public void addAuthors(List<Author> authorList){
		if (authorList != null) {
			if (this.authorList == null) {
				this.authorList = new ArrayList<>();
			}
			this.authorList.addAll(authorList);
		}

	}

	public void addInstitution(Institution inst) {
		if(inst != null) {
			if(this.institutions == null) {
				this.institutions = new ArrayList<Institution>();
			}
			this.institutions.add(inst);
		}
	}


	@Override
	public String asString(boolean compactOutput) {
		String headerStr = "";

		headerStr += "[HEADER] Title: '" + ((this.titles != null) ? this.titles : "NULL") + "'" +
				((this.year != null || !compactOutput) ? ", Year: '" + StringUtils.defaultString(this.year, "-") + "'" : "") +
				((this.pages != null || !compactOutput) ? ", Pages: '" + StringUtils.defaultString(this.pages, "-") + "'" : "") +
				((this.firstPage != null || !compactOutput) ? ", First page: '" + StringUtils.defaultString(this.firstPage, "-") + "'" : "") +
				((this.lastPage != null || !compactOutput) ? ", Last page: '" + StringUtils.defaultString(this.lastPage, "-") + "'" : "") +
				((this.openURL != null || !compactOutput) ? ", Open URL: '" + StringUtils.defaultString(this.openURL, "-") + "'" : "") +
				((this.bibsonomyURL != null || !compactOutput) ? ", Bibsonomy URL: '" + StringUtils.defaultString(this.bibsonomyURL, "-") + "'" : "") +
				((this.chapter != null || !compactOutput) ? ", Chapter: '" + StringUtils.defaultString(this.chapter, "-") + "'" : "") +
				((this.volume != null || !compactOutput) ? ", Volume: '" + StringUtils.defaultString(this.volume, "-") + "'" : "") +
				((this.issue != null || !compactOutput) ? ", Issue: '" + StringUtils.defaultString(this.issue, "-") + "'" : "") +
				((this.series != null || !compactOutput) ? ", Series: '" + StringUtils.defaultString(this.series, "-") + "'" : "") +
				((this.publisher != null || !compactOutput) ? ", Publisher: '" + StringUtils.defaultString(this.publisher, "-") + "'" : "") +
				((this.publisherLoc != null || !compactOutput) ? ", Publisher loc: '" + StringUtils.defaultString(this.publisherLoc, "-") + "'" : "") +
				((this.journal != null || !compactOutput) ? ", Journal: '" + StringUtils.defaultString(this.journal, "-") + "'" : "") +
				((this.edition != null || !compactOutput) ? ", Edition: '" + StringUtils.defaultString(this.edition, "-") + "'" : "") +
				((this.plainText != null || !compactOutput) ? ", Plain text: '" + StringUtils.defaultString(this.plainText, "-") + "'" : "") + "\n";

		if(this.authorList != null && this.authorList.size() > 0) {
			for(Author author : this.authorList) {
				headerStr += "   " + author.asString(compactOutput);
			}
		}
		else {
			headerStr += "   NO AUTHORS ASSOCIATED\n";
		}

		if(this.editorList != null && this.editorList.size() > 0) {
			for(Author editor : this.editorList) {
				headerStr += "   " + editor.asString(compactOutput);
			}
		}
		else {
			headerStr += "   NO EDITORS ASSOCIATED\n";
		}

		if(this.institutions != null && this.institutions.size() > 0) {
			for(Institution inst : this.institutions) {
				headerStr += "   " + inst.asString(compactOutput);
			}
		}
		else {
			headerStr += "   NO AFFILIATIONS ASSOCIATED\n";
		}

		return headerStr;
	}

}
