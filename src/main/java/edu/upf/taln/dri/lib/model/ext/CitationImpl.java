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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.wipo.analyzers.wipokr.utils.StringUtil;

import edu.upf.taln.dri.lib.model.BaseDocumentElem;
import edu.upf.taln.dri.lib.model.DocCacheManager;

/**
 * IMPORTANT: Never instantiate directly this class! <br/>
 * CitationImpl represents a citation of the document
 * 
 *
 */
public class CitationImpl extends BaseDocumentElem implements Citation {

	private Integer id;
	private Set<CitationSourceENUM> source;
	private String text;
	private String title;
	private List<Author> authorList;
	private Map<PubIdENUM, String> pubIDmap;
	private String year;
	private String pages;
	private String firstPage;
	private String lastPage;
	private String openURL;
	private String bibsonomyURL;
	private String chapter;
	private String volume;
	private String issue;
	private String series;
	private String publisher;
	private String publisherLoc;
	private String journal;
	private String institution;
	private String edition;
	private List<Author> editorList;
	private Map<String, String> citationString;
	private List<CitationMarker> citationMarkers;


	public CitationImpl(DocCacheManager cacheManager) {
		super(cacheManager);
		this.id = null;
		this.text = null;
		this.pubIDmap = new HashMap<PubIdENUM, String>();
		this.citationMarkers = new ArrayList<CitationMarker>();
		this.authorList = new ArrayList<Author>();
		this.editorList = new ArrayList<Author>();
		this.citationString = new HashMap<String, String>();
	}

	public CitationImpl(DocCacheManager cacheManager, Integer id, String text,
			List<CitationMarker> citationMarkers) {
		super(cacheManager);
		this.id = id;
		this.text = text;
		this.pubIDmap = new HashMap<PubIdENUM, String>();
		this.citationMarkers = citationMarkers;
		this.authorList = new ArrayList<Author>();
		this.editorList = new ArrayList<Author>();
		this.citationString = new HashMap<String, String>();
	}


	@Override
	public Integer getId() {
		return (this.id != null) ? new Integer(this.id) : null;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public String getText() {
		return (this.text != null) ? new String(this.text) : null; 
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public Set<CitationSourceENUM> getSource() {
		return (this.source != null) ? Collections.unmodifiableSet(this.source) : null; 
	}

	public void addSource(CitationSourceENUM source) {
		if(this.source == null) {
			this.source = new HashSet<CitationSourceENUM>();
		}

		if(source != null) {
			this.source.add(source);
		}
	}

	public void setPubID(PubIdENUM pubIDtype, String pubIDvalue) {
		if(pubIDtype != null && pubIDvalue != null) {
			this.pubIDmap.put(pubIDtype, pubIDvalue);
		}
	}

	@Override
	public List<CitationMarker> getCitaitonMarkers() {
		return (this.citationMarkers != null) ? Collections.unmodifiableList(this.citationMarkers) : null;
	}

	public void setNewCitationMarker(CitationMarker citMarker) {
		this.citationMarkers.add(citMarker);
	}

	@Override
	public String getTitle() {
		return (this.title != null) ? new String(this.title) : null;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public List<Author> getAuthorList() {
		return (this.authorList != null) ? Collections.unmodifiableList(this.authorList) : null;
	}

	public void setAuthor(Author author) {
		this.authorList.add(author);
	}

	@Override
	public String getYear() {
		return (this.year != null) ? new String(this.year) : null;
	}

	public void setYear(String year) {
		this.year = year;
	}

	@Override
	public String getPages() {
		return (this.pages != null) ? new String(this.pages) : null;
	}

	public void setPages(String pages) {
		this.pages = pages;
	}

	@Override
	public String getFirstPage() {
		return (this.firstPage != null) ? new String(this.firstPage) : null;
	}

	public void setFirstPage(String firstPage) {
		this.firstPage = firstPage;
	}

	@Override
	public String getLastPage() {
		return (this.lastPage != null) ? new String(this.lastPage) : null;
	}

	public void setLastPage(String lastPage) {
		this.lastPage = lastPage;
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

	@Override
	public String getBibsonomyURL() {
		return (this.bibsonomyURL != null) ? new String(this.bibsonomyURL) : null;
	}

	public void setBibsonomyURL(String bibsonomyURL) {
		this.bibsonomyURL = bibsonomyURL;
	}

	@Override
	public String getChapter() {
		return (this.chapter != null) ? new String(this.chapter) : null;
	}

	public void setChapter(String chapter) {
		this.chapter = chapter;
	}

	@Override
	public String getVolume() {
		return (this.volume != null) ? new String(this.volume) : null;
	}

	public void setVolume(String volume) {
		this.volume = volume;
	}

	@Override
	public String getIssue() {
		return (this.issue != null) ? new String(this.issue) : null;
	}

	public void setIssue(String issue) {
		this.issue = issue;
	}

	@Override
	public String getSeries() {
		return (this.series != null) ? new String(this.series) : null;
	}

	public void setSeries(String series) {
		this.series = series;
	}

	@Override
	public String getPublisher() {
		return (this.publisher != null) ? new String(this.publisher) : null;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	@Override
	public String getPublisherLoc() {
		return (this.publisherLoc != null) ? new String(this.publisherLoc) : null;
	}

	public void setPublisherLoc(String publisherLoc) {
		this.publisherLoc = publisherLoc;
	}

	@Override
	public String getEdition() {
		return (this.edition != null) ? new String(this.edition) : null;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}

	@Override
	public List<Author> getEditorList() {
		return (this.editorList != null) ? Collections.unmodifiableList(this.editorList) : null;
	}

	public void setEditor(Author editor) {
		this.editorList.add(editor);
	}

	public void setCitationStringEntry(String citationStyle, String citationContents) {
		if(citationStyle != null && !citationStyle.equals("") && citationContents != null && !citationContents.equals("")) {
			this.citationString.put(citationStyle, citationContents);
		}
	}

	public Map<String, String> getCitationString() {
		return Collections.unmodifiableMap(this.citationString);
	}

	@Override
	public String getJournal() {
		return (this.journal != null) ? new String(this.journal) : null;
	}

	public void setJournal(String journal) {
		this.journal = journal;
	}

	@Override
	public String getInstitution() {
		return (this.institution != null) ? new String(this.institution) : null;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}

	@Override
	public String getOpenURL() {
		return (this.openURL != null) ? new String(this.openURL) : null;
	}

	public void setOpenURL(String openURL) {
		this.openURL = openURL;
	}
	

	@Override
	public String asString(boolean compactOutput) {
		String citationStr = "";

		citationStr += "[CITATION] ID: '" + ((this.id != null) ? this.id + "" : "-") + "'" +
				((this.source != null || !compactOutput) ? ", Source: '" + ((this.source != null) ? this.source + "" : "-") + "'" : "") +
				((this.title != null || !compactOutput) ? ", Title: '" + StringUtils.defaultString(this.title, "-") + "'" : "") +
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
				((this.institution != null || !compactOutput) ? ", Institution: '" + StringUtils.defaultString(this.institution, "-") + "'" : "") +
				((this.edition != null || !compactOutput) ? ", Edition: '" + StringUtils.defaultString(this.edition, "-") + "'" : "") +
				((this.text != null || !compactOutput) ? ", Text: '" + StringUtils.defaultString(this.text, "-") + "'" : "") + "\n";


		if(this.authorList != null && this.authorList.size() > 0) {
			for(Author author : this.authorList) {
				citationStr += "   " + author.asString(compactOutput);
			}
		}
		else {
			citationStr += "   NO AUTHORS ASSOCIATED\n";
		}

		if(this.editorList != null && this.editorList.size() > 0) {
			for(Author editor : this.editorList) {
				citationStr += "   " + editor.asString(compactOutput);
			}
		}
		else {
			citationStr += "   NO EDITORS ASSOCIATED\n";
		}

		if(this.citationString != null && this.citationString.size() > 0) {
			for(Map.Entry<String, String> citEntry : this.citationString.entrySet()) {
				citationStr += "   CIT STYLE: " + StringUtil.defaultString(citEntry.getKey(), "-") + " - VALUE: " + StringUtil.defaultString(citEntry.getValue(), "-");
			}
		}
		else {
			citationStr += "   NO CITATION STYLES ASSOCIATED\n";
		}

		if(this.pubIDmap != null && this.pubIDmap.size() > 0) {
			for(Entry<PubIdENUM, String> pubIDentry : this.pubIDmap.entrySet()) {
				citationStr += "   PUB ID TYPE: " + ((pubIDentry.getKey() != null) ? pubIDentry.getKey() + "" : "-") + " - VALUE: " + StringUtil.defaultString(pubIDentry.getValue(), "-");
			}
		}
		else {
			citationStr += "   NO PUB IDs ASSOCIATED\n";
		}

		if(this.citationMarkers != null && this.citationMarkers.size() > 0) {
			for(CitationMarker citMarker : this.citationMarkers) {
				citationStr += "   " + citMarker.asString(compactOutput);
			}
		}
		else {
			citationStr += "   NO CITATION MARKERS ASSOCIATED\n";
		}

		return citationStr;
	}

}
