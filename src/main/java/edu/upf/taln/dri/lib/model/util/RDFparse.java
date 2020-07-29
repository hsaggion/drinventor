package edu.upf.taln.dri.lib.model.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.URIref;
import org.apache.jena.vocabulary.RDF;
import org.apache.log4j.Logger;

import edu.upf.taln.dri.common.util.Util;
import edu.upf.taln.dri.lib.exception.InternalProcessingException;
import edu.upf.taln.dri.lib.model.Document;
import edu.upf.taln.dri.lib.model.ext.Author;
import edu.upf.taln.dri.lib.model.ext.BabelSynsetOcc;
import edu.upf.taln.dri.lib.model.ext.Citation;
import edu.upf.taln.dri.lib.model.ext.CitationMarker;
import edu.upf.taln.dri.lib.model.ext.Header;
import edu.upf.taln.dri.lib.model.ext.Institution;
import edu.upf.taln.dri.lib.model.ext.PubIdENUM;
import edu.upf.taln.dri.lib.model.ext.Section;
import edu.upf.taln.dri.lib.model.ext.Sentence;
import edu.upf.taln.dri.lib.model.ext.SentenceSelectorENUM;
import gate.util.InvalidOffsetException;

/**
 * Collection of utility methods to generate an RDF dataset that represents the contents of a document
 * 
 *
 */
public class RDFparse {

	private static Logger logger = Logger.getLogger(RDFparse.class.getName());

	private static Random rnd = new Random();

	// URL de-duplication variables - still not used

	/**
	 * Generate RDF dataset from a document
	 * 
	 * @param doc
	 * @return
	 */
	public static String getDocRDF(Document doc) {

		try {
			
			if(doc == null || doc.getRawText() == null || doc.getRawText().equals("")) {
				logger.error("Error while generating RDF dataset from document. Document invalid or empty.");
				return null;
			}

			OntModel model = null;

			// **************************
			// ******* MODEL INIT *******
			model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
			RDFparse RDFclassInst = new RDFparse();
			InputStream baseModelStream = RDFclassInst.getClass().getClassLoader().getResourceAsStream("RDF-base.ttl");
			model = (OntModel) model.read(baseModelStream, "http://drinventor.eu/", "TTL");

			for(Entry<String, String> entry : model.getNsPrefixMap().entrySet()) {
				System.out.println("Namespace: " + entry.getKey() + " -> " + entry.getValue());
			}
			
			// *******************************
			// ******* NO PREFIXES SET *******
			// model.setNsPrefix("swrc", "http://swrc.ontoware.org/ontology-07#"); // SWRC ontology
			// model = (OntModel) model.getBaseModel();
			
			logger.info("KNOWLEDGE MODEL INITIALIZED");
			try {
				Thread.sleep(1000);
			}
			catch (Exception e) {
				Util.notifyException("Knowledge base population", e, logger);
			}

			// --------------------------------------------
			// --------------------------------------------
			// Model population

			// ********************************************
			// Get document URI
			Resource paperResource = null;
			String docDigest = computeStringDigest(doc.getRawText());

			Header head = null;
			try {
				head = doc.extractHeader();
			}
			catch(InternalProcessingException e) {
				Util.notifyException("Extracting document header", e, logger);
			}

			// Formatting paper URI
			try {
				String paperURIstr = "";

				if(head != null && head.getTitles() != null) {
					String title = head.getTitles().values().iterator().next(); // Got one random title - review in case of multiple titles
					String[] titleParts = title.split(" ");
					String titlePartsComposed = "";
					if(titleParts != null && titleParts.length > 0) {
						for(int i = 0; i < titleParts.length; i++) {
							titlePartsComposed = titleParts[i] + " ";
							if(titlePartsComposed.length() > 20) {
								break;
							}
						}
					}

					paperURIstr = (titlePartsComposed.length() > 0) ? titlePartsComposed : ((title.length() > 20) ? title.substring(0, 20) : title);
				}

				paperURIstr += "_" + docDigest;

				if(paperURIstr == null || paperURIstr.equals("")) {
					paperURIstr = "doc_" + rnd.nextInt();
				}

				// TODO: in which class we create the individual URI?
				paperResource = createIndividual(model, model.getNsPrefixURI("dripub"), paperURIstr, model.getNsPrefixURI("fabio") + "Expression");
				logger.debug("Added paper URI: " + paperResource.getURI());
			}
			catch (Exception e) {
				Util.notifyException("Document URI generation", e, logger);
			}

			// ********************************************
			// Add authors, editors and document metadata
			if(head != null) {

				// Title
				if(StringUtils.isNotEmpty(head.getTitles().values().iterator().next())) { // Got one random title - review in case of multiple titles
					paperResource.addProperty(model.getProperty(model.getNsPrefixURI("swrc") + "title"), model.createTypedLiteral(head.getTitles().values().iterator().next().trim())); // Got one random title - review in case of multiple titles
					paperResource.addProperty(model.getProperty(model.getNsPrefixURI("rdfs") + "label"), model.createTypedLiteral(head.getTitles().values().iterator().next().trim())); // Got one random title - review in case of multiple titles 
					logger.debug("Added paper title: " + head.getTitles().values().iterator().next().trim()); // Got one random title - review in case of multiple titles
				}
				
				// Authors list
				if(head.getAuthorList() != null && head.getAuthorList().size() > 0) {
					for(Author auth : head.getAuthorList()) {
						if(auth != null) {
							try {
								String authorName = processPersonName(auth.getFullName());
								if(auth.getFirstName() != null && !auth.getFirstName().equals("") && auth.getSurname() != null && !auth.getSurname().equals("")) {
									authorName = processPersonName(auth.getFirstName()) + " " + processPersonName(auth.getSurname());
								}
								Resource authorIRI = createIndividual(model, model.getNsPrefixURI("driperson"), authorName, model.getNsPrefixURI("foaf") + "Person");
								authorIRI.addProperty(model.getProperty(model.getNsPrefixURI("foaf") + "name"), model.createTypedLiteral(authorName));
								authorIRI.addProperty(model.getProperty(model.getNsPrefixURI("rdfs") + "rdfs"), model.createTypedLiteral(authorName));
								logger.debug("RDF: added author URI and name > " + authorName);

								// Create a role in time and associate with the author role, the person, and the proceedings document
								Resource roleInTime_PAPERAUTH = createIndividual(model, model.getNsPrefixURI("drirole"), authorName +"_role_" + rnd.nextInt(Integer.MAX_VALUE), model.getNsPrefixURI("pro") + "RoleInTime");			
								authorIRI.addProperty(model.getProperty(model.getNsPrefixURI("pro") + "holdsRoleInTime"), roleInTime_PAPERAUTH);
								roleInTime_PAPERAUTH.addProperty(model.getProperty(model.getNsPrefixURI("pro") + "withRole"), model.getResource(model.getNsPrefixURI("pro") + "author" ));
								logger.debug("RDF: added (pro:author) role in time of > " + authorName);
								roleInTime_PAPERAUTH.addProperty(model.getProperty(model.getNsPrefixURI("pro") + "relatesToDocument"), paperResource);
								logger.debug("RDF: linked (pro:author) to papaer > " + paperResource);

								// Author affiliation
								if(auth.getAffiliations() != null && auth.getAffiliations().size() > 0) {
									for(Institution authAffiliation : auth.getAffiliations()) {
										if(authAffiliation != null && authAffiliation.getName() != null && !authAffiliation.getName().equals("")) {
											Resource affiliation = createIndividual(model, model.getNsPrefixURI("driaffiliation"), "#" + authAffiliation.getName(), model.getNsPrefixURI("foaf") + "Organization");
											affiliation.addProperty(RDF.type, model.getResource(model.getNsPrefixURI("dbp") + "Organization"));

											/* TO ADD - AFFILIATION EXTERNAL URI
											if(affInternal.getInstituteURI() != null && !affInternal.getInstituteURI().equals("")) {
												affiliation.addProperty(RDFS.seeAlso, model.createTypedLiteral(affInternal.getInstituteURI(), model.getNsPrefixURI("xsd") + "anyURI"));
											}
											 */

											affiliation.addProperty(model.getProperty(model.getNsPrefixURI("swrc") + "name"), model.createTypedLiteral(authAffiliation.getName()));
											logger.debug("RDF: added author organization :" + authAffiliation.getName() + " > " + authorName);

											// Connect editor role to institute
											roleInTime_PAPERAUTH.addProperty(model.getProperty(model.getNsPrefixURI("pro") + "relatesToOrganization"), affiliation);

											/* TO ADD - AFFILIATION STATE AND COUNTRY URI
											// T-BOX ADD: ceurws:cityLabel and ceurws:countryLabel in T-BOX 
											if(affInternal.getLocation().isDBpedia()) {
												affiliation.addProperty(model.getProperty(model.getNsPrefixURI("dbp") + "city"), model.createTypedLiteral(affInternal.getLocation().getCityURI(), model.getNsPrefixURI("xsd") + "anyURI")); 
												affiliation.addProperty(model.getProperty(model.getNsPrefixURI("dbp") + "country"), model.createTypedLiteral(affInternal.getLocation().getStateURI(), model.getNsPrefixURI("xsd") + "anyURI"));
												logCreation("RDF: added ws dbp:city > " + affInternal.getLocation().getCityURI() + " of editor: " + editorAff.getKey());
												logCreation("RDF: added ws dbp:country > " + affInternal.getLocation().getStateURI() + " of editor: " + editorAff.getKey());

												if(affInternal.getLocation().getCityName() != null && !affInternal.getLocation().getCityName().equals("")) {
													affiliation.addProperty(model.getProperty(model.getNsPrefixURI("ceurws") + "cityLabel"), model.createTypedLiteral(affInternal.getLocation().getCityName() ));
													logCreation("RDF: added ws ceurws:cityLabel > " + affInternal.getLocation().getCityName() + " of editor: " + editorAff.getKey());
												}
												if(affInternal.getLocation().getStateName() != null && !affInternal.getLocation().getStateName().equals("")) {
													affiliation.addProperty(model.getProperty(model.getNsPrefixURI("ceurws") + "countryLabel"), model.createTypedLiteral(affInternal.getLocation().getStateName() ));
													logCreation("RDF: added ws ceurws:countryLabel > " + affInternal.getLocation().getStateName() + " of editor: " + editorAff.getKey());
												}
											}
											else if(affInternal.getLocation().isGAZ()) {
												affiliation.addProperty(model.getProperty(model.getNsPrefixURI("ceurws") + "cityLabel"), model.createTypedLiteral(affInternal.getLocation().getCityName() )); 
												affiliation.addProperty(model.getProperty(model.getNsPrefixURI("ceurws") + "countryLabel"), model.createTypedLiteral(affInternal.getLocation().getStateName() ));
												logCreation("RDF: added ws ceurws:cityLabel > " + affInternal.getLocation().getCityName());
												logCreation("RDF: added ws ceurws:countryLabel > " + affInternal.getLocation().getStateName());
											}
											 */
										}
									}
								}
							} catch (Exception e) {
								Util.notifyException("Creating author RDF", e, logger);
							}
						}
					}
				}
				else {
					logger.debug("RDF: no authors for paper > " + doc.getName());
				}

				// Editor
				if(head.getEditorList() != null && head.getEditorList().size() > 0) {
					for(Author editor : head.getEditorList()) {
						if(editor != null) {
							try {
								String editorName = processPersonName(editor.getFullName());
								if(editor.getFirstName() != null && !editor.getFirstName().equals("") && editor.getSurname() != null && !editor.getSurname().equals("")) {
									editorName = processPersonName(editor.getFirstName()) + "_" + processPersonName(editor.getSurname());
								}
								Resource authorIRI = createIndividual(model, model.getNsPrefixURI("driperson"), editorName, model.getNsPrefixURI("foaf") + "Person");
								authorIRI.addProperty(model.getProperty(model.getNsPrefixURI("foaf") + "name"), model.createTypedLiteral(editorName));
								authorIRI.addProperty(model.getProperty(model.getNsPrefixURI("rdfs") + "label"), model.createTypedLiteral(editorName));
								logger.debug("RDF: added editor URI and name > " + editorName);

								// Create a role in time and associate with the author role, the person, and the proceedings document
								Resource roleInTime_PAPERAUTH = createIndividual(model, model.getNsPrefixURI("drirole"), editorName +"_role_" + rnd.nextInt(Integer.MAX_VALUE), model.getNsPrefixURI("pro") + "RoleInTime");			
								authorIRI.addProperty(model.getProperty(model.getNsPrefixURI("pro") + "holdsRoleInTime"), roleInTime_PAPERAUTH);
								roleInTime_PAPERAUTH.addProperty(model.getProperty(model.getNsPrefixURI("pro") + "withRole"), model.getResource(model.getNsPrefixURI("pro") + "editor" ));
								logger.debug("RDF: added (pro:editor) role in time of > " + editorName);
								roleInTime_PAPERAUTH.addProperty(model.getProperty(model.getNsPrefixURI("pro") + "relatesToDocument"), paperResource);
								logger.debug("RDF: linked (pro:editor) to papaer > " + paperResource);

							} catch (Exception e) {
								Util.notifyException("Creating editor RDF", e, logger);
							}
						}
					}
				}
				else {
					logger.debug("RDF: no editors for paper > " + doc.getName());
				}
				
				// Year
				if(StringUtils.isNotEmpty(head.getYear())) {
					Integer intYear = null;
					String yearValidated = head.getYear();
					try {
						intYear = Integer.valueOf(head.getYear().trim());
						yearValidated = intYear.toString();
					}
					catch (NumberFormatException nfe) {
						logger.debug("RDF: no Integer year for paper > " + doc.getName());
					}

					paperResource.addProperty(model.getProperty(model.getNsPrefixURI("swrc") + "year"), model.createTypedLiteral(yearValidated, model.getNsPrefixURI("xsd") + "int"));
					logger.debug("RDF: added added swrc:year > " + yearValidated);
				}
				else {
					logger.debug("RDF: no year for paper > " + doc.getName());
				}

				// Pages
				if(StringUtils.isNotEmpty(head.getPages())) {
					paperResource.addProperty(model.getProperty(model.getNsPrefixURI("swrc") + "pages"), model.createTypedLiteral(head.getPages()));
					logger.debug("RDF: added paper swrc:pages > " + head.getPages());
				}
				else {
					logger.debug("RDF: no pages for paper > " + doc.getName());
				}

				// Publisher
				// TOREVIEW: is dc:publisher a literal property?
				if(StringUtils.isNotEmpty(head.getPublisher())) {
					paperResource.addProperty(model.getProperty(model.getNsPrefixURI("dc") + "publisher"), model.createTypedLiteral(head.getPublisher()));
					logger.debug("RDF: added paper dc:publisher > " + head.getPublisher());
				}
				else {
					logger.debug("RDF: no publisher for paper > " + doc.getName());
				}

				// Journal name
				if(StringUtils.isNotEmpty(head.getJournal())) {
					paperResource.addProperty(model.getProperty(model.getNsPrefixURI("swrc") + "journal"), model.createTypedLiteral(head.getJournal()));
					logger.debug("RDF: added paper swrc:journal > " + head.getJournal());
				}
				else {
					logger.debug("RDF: no journal for paper > " + doc.getName());
				}

				// Chapter name
				if(StringUtils.isNotEmpty(head.getChapter())) {
					paperResource.addProperty(model.getProperty(model.getNsPrefixURI("swrc") + "chapter"), model.createTypedLiteral(head.getChapter()));
					logger.debug("RDF: added paper swrc:chapter > " + head.getChapter());
				}
				else {
					logger.debug("RDF: no chapter for paper > " + doc.getName());
				}

				// Edition name
				if(StringUtils.isNotEmpty(head.getEdition())) {
					paperResource.addProperty(model.getProperty(model.getNsPrefixURI("swrc") + "edition"), model.createTypedLiteral(head.getEdition()));
					logger.debug("RDF: added paper swrc:edition > " + head.getEdition());
				}
				else {
					logger.debug("RDF: no edition for paper > " + doc.getName());
				}

				// Series name
				if(StringUtils.isNotEmpty(head.getSeries())) {
					paperResource.addProperty(model.getProperty(model.getNsPrefixURI("swrc") + "series"), model.createTypedLiteral(head.getSeries()));
					logger.debug("RDF: added paper swrc:series > " + head.getSeries());
				}
				else {
					logger.debug("RDF: no series for paper > " + doc.getName());
				}

				// Volume name
				if(StringUtils.isNotEmpty(head.getVolume())) {
					paperResource.addProperty(model.getProperty(model.getNsPrefixURI("swrc") + "volume"), model.createTypedLiteral(head.getVolume()));
					logger.debug("RDF: added paper swrc:volume > " + head.getVolume());
				}
				else {
					logger.debug("RDF: no swrc:volume for paper > " + doc.getName());
				}

				// Bibsonomy URL
				if(StringUtils.isNotEmpty(head.getBibsonomyURL())) {
					paperResource.addProperty(model.getProperty(model.getNsPrefixURI("rdfs") + "seeAlso"), model.createTypedLiteral(head.getBibsonomyURL(), model.getNsPrefixURI("xsd") + "anyURI"));
					logger.debug("RDF: connected paper with bibsonomy (rdfs:seeAlso) > " + head.getBibsonomyURL());
				}
				else {
					logger.debug("RDF: no bibsonomy URL for paper > " + doc.getName());
				}

				// DOI
				if(StringUtils.isNotEmpty(head.getPubID(PubIdENUM.DOI))) {
					paperResource.addProperty(model.getProperty(model.getNsPrefixURI("prism") + "doi"), model.createTypedLiteral(head.getPubID(PubIdENUM.DOI), model.getNsPrefixURI("xsd") + "anyURI"));
					logger.debug("RDF: connected paper with DOI (prism:doi) > " + head.getPubID(PubIdENUM.DOI));
				}
				else {
					logger.debug("RDF: no DOI for paper > " + doc.getName());
				}

			}


			// ********************************************
			// Add document structure
			List<Sentence> abstractSent = doc.extractSentences(SentenceSelectorENUM.ONLY_ABSTRACT);
			List<Sentence> contentSent = doc.extractSentences(SentenceSelectorENUM.ALL_EXCEPT_ABSTRACT);

			Map<Integer, Resource> sentIdResourceMap = new HashMap<Integer, Resource>();

			// Abstract - create sentence URI
			if(abstractSent != null && abstractSent.size() > 0) {
				for(Sentence sent : abstractSent) {
					if(sent != null && sent.getId() != null) {
						try {
							createSentence(sent, model, sentIdResourceMap, docDigest);
						}
						catch (Exception e) {
							Util.notifyException("Creating sentence RDF", e, logger);
						}
					}
				}
			}

			// Content - create sentence URI
			if(contentSent != null && contentSent.size() > 0) {
				for(Sentence sent : contentSent) {
					if(sent != null && sent.getId() != null) {
						try {
							createSentence(sent, model, sentIdResourceMap, docDigest);
						}
						catch (Exception e) {
							Util.notifyException("Creating sentence RDF", e, logger);
						}
					}
				}
			}

			// Create abstract URI and ordered sentences
			String abstractIRIstr = docDigest + "_" + "ABSTRACT";
			try {
				Resource abstractFrontMatterIRI = createIndividual(model, model.getNsPrefixURI("drisect"), abstractIRIstr, model.getNsPrefixURI("doco") + "FrontMatter");

				paperResource.addProperty(model.getProperty(model.getNsPrefixURI("po") + "contains"), abstractFrontMatterIRI);
				
				if(abstractSent != null && abstractSent.size() > 0) {
					int addedSent = 0;
					Resource lastBlankNodeInSequence = null;
					for(int k = 0; k < abstractSent.size();  k++) {
						Sentence sent = abstractSent.get(k);
						if(sent != null && sent.getId() != null && sentIdResourceMap.containsKey(sent.getId())) {
							try {
								abstractFrontMatterIRI.addProperty(model.getProperty(model.getNsPrefixURI("po") + "contains"), sentIdResourceMap.get(sent.getId()));

								// List creation logic - http://www.sparontologies.net/examples#doco_1
								if(addedSent == 0) {
									// First of the list
									Resource blankNode = model.createResource();

									abstractFrontMatterIRI.addProperty(model.getProperty(model.getNsPrefixURI("co") + "firstItem"), blankNode);
									blankNode.addProperty(model.getProperty(model.getNsPrefixURI("co") + "itemContent"), sentIdResourceMap.get(sent.getId()));
									lastBlankNodeInSequence = blankNode;
								}
								else {
									// After first of the list
									Resource blankNode = model.createResource();
									lastBlankNodeInSequence.addProperty(model.getProperty(model.getNsPrefixURI("co") + "nextItem"), blankNode);
									blankNode.addProperty(model.getProperty(model.getNsPrefixURI("co") + "itemContent"), sentIdResourceMap.get(sent.getId()));

									lastBlankNodeInSequence = blankNode;
								}
								addedSent++;
							}
							catch (Exception e) {
								Util.notifyException("Adding sentence RDF to abstract", e, logger);
							}
						}
					}
				}

			} catch (InvalidOffsetException e) {
				Util.notifyException("Creating abstrac structur", e, logger);
			}


			// Create bodyMatter URI and ordered sentences
			String bodyIRIstr = docDigest + "_" + "BODY";
			try {
				Resource bodyFrontMatterIRI = createIndividual(model, model.getNsPrefixURI("drisect"), bodyIRIstr, model.getNsPrefixURI("doco") + "BodyMatter");

				paperResource.addProperty(model.getProperty(model.getNsPrefixURI("po") + "contains"), bodyFrontMatterIRI);
				
				List<Section> rootSectList = doc.extractSections(true);

				if(rootSectList != null && rootSectList.size() > 0) {
					expandSection(rootSectList, bodyFrontMatterIRI, sentIdResourceMap, model, docDigest);
				}

			} catch (InvalidOffsetException e) {
				Util.notifyException("Creating abstrac structure", e, logger);
			}

			// ********************************************
			// Add bibliography
			List<Citation> citList = doc.extractCitations();

			if(citList != null && citList.size() > 0) {

				try {
					// Reference list URI
					String refListURI = docDigest + "_cit_RefList";
					Resource referenceListResource = createIndividual(model, model.getNsPrefixURI("dricit"), refListURI, model.getNsPrefixURI("biro") + "ReferenceList");
					referenceListResource.addProperty(model.getProperty(model.getNsPrefixURI("frbr") + "part"),  paperResource);
					
					Resource previousListItem = null;
					for(int y = 0; y < citList.size(); y++) {
						Citation cit = citList.get(y);

						if(cit != null && cit.getId() != null) {
							try {
								String titleCita = cit.getTitle();
								if(titleCita == null) {
									titleCita = "";
								}

								String citIRIstr = docDigest + "_" + ((titleCita.length() > 0) ? titleCita : rnd.nextInt());
								Resource citationElemResource = createIndividual(model, model.getNsPrefixURI("dricit"), citIRIstr, model.getNsPrefixURI("biro") + "BibliographicReference");
								
								// The paper references the bibliographic entry
								paperResource.addProperty(model.getProperty(model.getNsPrefixURI("dcterms") + "references"),  citationElemResource);
								
								// Create and link the reference list item
								String refListItemStr = docDigest + "_cit_RefListItem_" + y + "_" + ((titleCita.length() > 0) ? titleCita : rnd.nextInt());
								Resource refListItem = createIndividual(model, model.getNsPrefixURI("dricit"), refListItemStr, model.getNsPrefixURI("biro") + "ListItem");
								refListItem.addProperty(model.getProperty(model.getNsPrefixURI("co") + "itemContent"),  citationElemResource);
								if(previousListItem != null) {
									refListItem.addProperty(model.getProperty(model.getNsPrefixURI("co") + "nextItem"),  previousListItem);
									previousListItem = refListItem;
								}
								if(y == 0) referenceListResource.addProperty(model.getProperty(model.getNsPrefixURI("co") + "hasFirstItem"),  refListItem);
								if(y == (citList.size() - 1)) 	referenceListResource.addProperty(model.getProperty(model.getNsPrefixURI("co") + "hasLastItem"),  refListItem);
								
								
								// Set bibliographic citation text
								if(StringUtils.isNotEmpty(cit.getText())) {
									citationElemResource.addProperty(model.getProperty(model.getNsPrefixURI("dcterms") + "bibliographicCitation"),  model.createTypedLiteral(cit.getText()));
								}

								// Set bibliographic marker
								List<CitationMarker> citMarkers = cit.getCitaitonMarkers();
								if(citMarkers != null && citMarkers.size() > 0) {
									int count = 0;
									for(CitationMarker citMarker : citMarkers) {
										count++;
										Integer citedSentId = citMarker.getSentenceId();

										if(sentIdResourceMap.containsKey(citedSentId)) {
											// Retrieve sentence with citation
											Resource citedSentURI = sentIdResourceMap.get(citedSentId);

											// Create new citation in sentence URI
											String citationSentElemStrURI = docDigest + "_cit_CitingSent_" + ((titleCita.length() > 0) ? titleCita : rnd.nextInt());
											Resource citationSentElemResource = createIndividual(model, model.getNsPrefixURI("dricit"), citationSentElemStrURI, model.getNsPrefixURI("deo") + "Reference");

											// The sentence with citation contains the citation in sentence URI and the citation in sentence URI references the biblio entry
											citedSentURI.addProperty(model.getProperty(model.getNsPrefixURI("po") + "contains"),  citationSentElemResource);
											citationSentElemResource.addProperty(model.getProperty(model.getNsPrefixURI("dcterms") + "refrences"),  citationElemResource);

											// The citation in sentence URI has content the citation marker
											if(StringUtils.isNotEmpty(citMarker.getReferenceText())) {
												citationSentElemResource.addProperty(model.getProperty(model.getNsPrefixURI("c4o") + "hasContent"),  model.createTypedLiteral(citMarker.getReferenceText()));
											}
										}
									}

									if(count > 0) {
										citationElemResource.addProperty(model.getProperty(model.getNsPrefixURI("dcterms") + "bibliographicCitation"),  model.createTypedLiteral(count, model.getNsPrefixURI("xsd") + "int"));
									}
								}


								// Get document URI
								String citedPaperIRIstr = "citedPaper_" + ((titleCita.length() > 0) ? titleCita : rnd.nextInt());
								Resource citedPaperResource = createIndividual(model, model.getNsPrefixURI("dripub"), citedPaperIRIstr, model.getNsPrefixURI("fabio") + "Expression");
								logger.debug("Added cited paper URI: " + citedPaperResource.getURI());							

								// Connect BibliographicReference to paperURI
								citationElemResource.addProperty(model.getProperty(model.getNsPrefixURI("biro") + "references"), citedPaperResource);

								// Adding paper title
								if(StringUtils.isNotEmpty(cit.getTitle())) {
									citedPaperResource.addProperty(model.getProperty(model.getNsPrefixURI("swrc") + "title"), titleCita);
								}

								// Authors list
								if(cit.getAuthorList() != null && cit.getAuthorList().size() > 0) {
									for(Author auth : cit.getAuthorList()) {
										if(auth != null) {
											try {
												String authorName = processPersonName(auth.getFullName());
												if(auth.getFirstName() != null && !auth.getFirstName().equals("") && auth.getSurname() != null && !auth.getSurname().equals("")) {
													authorName = processPersonName(auth.getFirstName()) + " " + processPersonName(auth.getSurname());
												}
												Resource authorIRI = createIndividual(model, model.getNsPrefixURI("driperson"), authorName, model.getNsPrefixURI("foaf") + "Person");
												authorIRI.addProperty(model.getProperty(model.getNsPrefixURI("foaf") + "name"), model.createTypedLiteral(authorName));
												authorIRI.addProperty(model.getProperty(model.getNsPrefixURI("rdfs") + "label"), model.createTypedLiteral(authorName));
												logger.debug("RDF: added author URI and name > " + authorName);

												// Create a role in time and associate with the author role, the person, and the proceedings document
												Resource roleInTime_PAPERAUTH = createIndividual(model, model.getNsPrefixURI("drirole"), authorName +"_role_" + rnd.nextInt(Integer.MAX_VALUE), model.getNsPrefixURI("pro") + "RoleInTime");			
												authorIRI.addProperty(model.getProperty(model.getNsPrefixURI("pro") + "holdsRoleInTime"), roleInTime_PAPERAUTH);
												roleInTime_PAPERAUTH.addProperty(model.getProperty(model.getNsPrefixURI("pro") + "withRole"), model.getResource(model.getNsPrefixURI("pro") + "author" ));
												logger.debug("RDF: added (pro:author) role in time of > " + authorName);
												roleInTime_PAPERAUTH.addProperty(model.getProperty(model.getNsPrefixURI("pro") + "relatesToDocument"), citedPaperResource);
												logger.debug("RDF: linked (pro:author) to papaer > " + citedPaperResource);
											} catch (Exception e) {
												Util.notifyException("Creating author RDF (cited paper)", e, logger);
											}
										}
									}
								}
								else {
									logger.debug("RDF: no authors for cited paper > " + doc.getName());
								}

								// Editor
								if(cit.getEditorList() != null && cit.getEditorList().size() > 0) {
									for(Author editor : cit.getEditorList()) {
										if(editor != null) {
											try {
												String editorName = processPersonName(editor.getFullName());
												if(editor.getFirstName() != null && !editor.getFirstName().equals("") && editor.getSurname() != null && !editor.getSurname().equals("")) {
													editorName = processPersonName(editor.getFirstName()) + "_" + processPersonName(editor.getSurname());
												}
												Resource editorIRI = createIndividual(model, model.getNsPrefixURI("driperson"), editorName, model.getNsPrefixURI("foaf") + "Person");
												editorIRI.addProperty(model.getProperty(model.getNsPrefixURI("foaf") + "name"), model.createTypedLiteral(editorName));
												editorIRI.addProperty(model.getProperty(model.getNsPrefixURI("rdfs") + "label"), model.createTypedLiteral(editorName));
												logger.debug("RDF: added editor URI and name > " + editorName);

												// Create a role in time and associate with the author role, the person, and the proceedings document
												Resource roleInTime_PAPERAUTH = createIndividual(model, model.getNsPrefixURI("drirole"), editorName +"_role_" + rnd.nextInt(Integer.MAX_VALUE), model.getNsPrefixURI("pro") + "RoleInTime");			
												editorIRI.addProperty(model.getProperty(model.getNsPrefixURI("pro") + "holdsRoleInTime"), roleInTime_PAPERAUTH);
												roleInTime_PAPERAUTH.addProperty(model.getProperty(model.getNsPrefixURI("pro") + "withRole"), model.getResource(model.getNsPrefixURI("pro") + "editor" ));
												logger.debug("RDF: added (pro:editor) role in time of > " + editorName);
												roleInTime_PAPERAUTH.addProperty(model.getProperty(model.getNsPrefixURI("pro") + "relatesToDocument"), citedPaperResource);
												logger.debug("RDF: linked (pro:editor) to papaer > " + citedPaperResource);

											} catch (Exception e) {
												Util.notifyException("Creating editor RDF (cited paper)", e, logger);
											}
										}
									}
								}
								else {
									logger.debug("RDF: no editors for cited paper > " + doc.getName());
								}

								// Adding year
								if(cit.getYear() != null && !cit.getYear().equals("")) {
									Integer intYear = null;
									String yearValidated = cit.getYear();
									try {
										intYear = Integer.valueOf(cit.getYear().trim());
										yearValidated = intYear.toString();
									}
									catch (NumberFormatException nfe) {
										logger.debug("RDF: no Integer year for citation > " + cit.getText());
									}

									citedPaperResource.addProperty(model.getProperty(model.getNsPrefixURI("swrc") + "year"), model.createTypedLiteral(yearValidated, model.getNsPrefixURI("xsd") + "int"));
									logger.debug("RDF: (cit) added swrc:year > " + yearValidated);
								}
								else {
									logger.debug("RDF: no year for citation > " + cit.getText());
								}

								// Add pages
								if(cit.getPages() != null && !cit.getPages().equals("")) {
									citedPaperResource.addProperty(model.getProperty(model.getNsPrefixURI("swrc") + "pages"), model.createTypedLiteral(cit.getPages()));
									logger.debug("RDF: (cit) added swrc:pages > " + cit.getPages());
								}
								else {
									logger.debug("RDF: no pages for citation > " + cit.getText());
								}

								// Add publisher
								// TOREVIEW: is dc:publisher a literal property?
								if(cit.getPublisher() != null && !cit.getPublisher().equals("")) {
									citedPaperResource.addProperty(model.getProperty(model.getNsPrefixURI("dc") + "publisher"), model.createTypedLiteral(cit.getPublisher()));
									logger.debug("RDF: (cit) added dc:publisher > " + cit.getPublisher());
								}
								else {
									logger.debug("RDF: no publisher for citation > " + cit.getText());
								}

								// Add journal name
								if(cit.getJournal() != null && !cit.getJournal().equals("")) {
									citedPaperResource.addProperty(model.getProperty(model.getNsPrefixURI("swrc") + "journal"), model.createTypedLiteral(cit.getJournal()));
									logger.debug("RDF: (cit) added swrc:journal > " + cit.getJournal());
								}
								else {
									logger.debug("RDF: no journal for citation > " + cit.getText());
								}

								// Add chapter name
								if(cit.getChapter() != null && !cit.getChapter().equals("")) {
									citedPaperResource.addProperty(model.getProperty(model.getNsPrefixURI("swrc") + "chapter"), model.createTypedLiteral(cit.getChapter()));
									logger.debug("RDF: (cit) added swrc:chapter > " + cit.getChapter());
								}
								else {
									logger.debug("RDF: no chapter for citation > " + cit.getText());
								}

								// Add edition name
								if(cit.getEdition() != null && !cit.getEdition().equals("")) {
									citedPaperResource.addProperty(model.getProperty(model.getNsPrefixURI("swrc") + "edition"), model.createTypedLiteral(cit.getEdition()));
									logger.debug("RDF: (cit) added swrc:edition > " + cit.getEdition());
								}
								else {
									logger.debug("RDF: no edition for citation > " + cit.getText());
								}

								// Add series name
								if(cit.getSeries() != null && !cit.getSeries().equals("")) {
									citedPaperResource.addProperty(model.getProperty(model.getNsPrefixURI("swrc") + "series"), model.createTypedLiteral(cit.getSeries()));
									logger.debug("RDF: (cit) added swrc:series > " + cit.getSeries());
								}
								else {
									logger.debug("RDF: no series for citation > " + cit.getText());
								}

								// Add volume name
								if(cit.getVolume() != null && !cit.getVolume().equals("")) {
									citedPaperResource.addProperty(model.getProperty(model.getNsPrefixURI("swrc") + "volume"), model.createTypedLiteral(cit.getVolume()));
									logger.debug("RDF: (cit) added swrc:volume > " + cit.getVolume());
								}
								else {
									logger.debug("RDF: no chapter for citation > " + cit.getText());
								}

								// Add bibsonomy URL
								if(cit.getBibsonomyURL() != null && !cit.getBibsonomyURL().equals("")) {
									citedPaperResource.addProperty(model.getProperty(model.getNsPrefixURI("rdfs") + "seeAlso"), model.createTypedLiteral(cit.getBibsonomyURL(), model.getNsPrefixURI("xsd") + "anyURI"));
									logger.debug("RDF: (cit) connected citation with bibsonomy (rdfs:seeAlso) > " + cit.getBibsonomyURL());
								}
								else {
									logger.debug("RDF: no bibsonomy URL for citation > " + cit.getText());
								}

								// Add DOI
								if(cit.getPubID(PubIdENUM.DOI) != null && !cit.getPubID(PubIdENUM.DOI).equals("")) {
									citedPaperResource.addProperty(model.getProperty(model.getNsPrefixURI("prism") + "doi"), model.createTypedLiteral(cit.getPubID(PubIdENUM.DOI), model.getNsPrefixURI("xsd") + "anyURI"));
									logger.debug("RDF: (cit) connected citation with DOI (prism:doi) > " + cit.getPubID(PubIdENUM.DOI));
								}
								else {
									logger.debug("RDF: no DOI for citation > " + cit.getText());
								}

							} catch (Exception e) {
								Util.notifyException("Creating citation structure", e, logger);
							}
						}
					}
				} catch (Exception e) {
					Util.notifyException("Creating citation structure", e, logger);
				}
			}

			
			// ***********************************
			// ********* GENERATE STRING *********
			OutputStream output = new OutputStream() {
				private StringBuilder string = new StringBuilder();
				@Override
				public void write(int b) throws IOException {
					this.string.append((char) b );
				}

				public String toString(){
					return this.string.toString();
				}
			};
			model.write(output, "TTL");
			// ***********************************
			// ***********************************
			
			return output.toString();

		} catch (InternalProcessingException e) {
			Util.notifyException("RDF dataset generation", e, logger);
		}

		return null;
	}

	/**
	 * Create a new URI for an individual and store it in the model as a type of the specified class URI
	 * 
	 * @param inModel
	 * @param inName
	 * @param inClass
	 * @return
	 * @throws InvalidOffsetException
	 */
	private static Resource createIndividual(Model inModel, String namespace, String inName, String inClass) throws InvalidOffsetException {
		String individualURI = getUriFromName(namespace, inName);

		Resource individual = inModel.createResource(individualURI);
		individual.addProperty(RDF.type, inModel.getResource(inClass));

		return individual;
	}

	/**
	 * Given a namespace prefix and a name, generates the URI string
	 * 
	 * @param namespace
	 * @param name
	 * @return
	 */
	private static String getUriFromName(String namespace, String name) {
		String retURI = "";
		if(namespace != null && !namespace.equals("") && name != null && !name.equals("")) {
			name = name.replace('(', '_').replace(')', '_').replace('[', '_').replace(']', '_').replace('{', '_').replace('}', '_');
			name = name.replace(':', '_').replace('.', '_').replace(',', '_').replace(';', '_').replace('-', '_').replace('¨', '_');
			name = name.replace('\'', '_').replace('=', '_').replace('-', '_').replace('?', '_').replace('!', '_').replace('|', '_');
			retURI = namespace + URIref.encode(name.replaceAll("\\s", "_").replaceAll("'", "_"));
		}
		return retURI;
	}

	/**
	 * Store model to file
	 * 
	 * @param model
	 * @param fileName
	 */
	private void writeModeltoFile(OntModel model, String fileName) {
		// Write to file
		fileName = (fileName != null && !fileName.equals("")) ? fileName : "output_file_RDF.ttl";
		try {
			logger.info("Storing knowledge base to file: " + fileName);
			FileOutputStream outStream = new FileOutputStream(fileName);
			model.write(outStream, "TTL");
		}
		catch(Exception e) {
			logger.info("ERROR WHILE WRITING TO FILE: " + fileName);
		}
		finally {
			model.close();
		}
	}

	/**
	 * Generate a string digest (MD5) from a string
	 * 
	 * @param inputStr
	 * @return
	 */
	private static String computeStringDigest(String inputStr) {
		StringBuffer hexString = new StringBuffer();

		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(inputStr.getBytes());
			byte[] hash = md.digest();

			for (int i = 0; i < hash.length; i++) {
				hexString.append(Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1));
			}
		} catch (NoSuchAlgorithmException e) {
			logger.error("Digest computation error - " + e.getMessage());
			e.printStackTrace();
		}

		return hexString.toString();
	}


	private static void expandSection(List<Section> sectList, Resource container, Map<Integer, Resource> sentIdResourceMap, OntModel model, String docDigest) {
		if(sectList != null && sectList.size() > 0 && container != null && sentIdResourceMap != null) {

			int addedSect = 0;
			Resource lastBlankNodeInSequence = null;
			for(int k = 0; k < sectList.size();  k++) {
				Section rootSect = sectList.get(k);
				if(rootSect != null && rootSect.getId() != null) {
					try {
						// Create section and link to container
						String sectName = (rootSect.getName() != null) ? ((rootSect.getName().length() > 20) ? rootSect.getName().substring(0, 20) : rootSect.getName()) : null;
						String rootSectIRI = docDigest + "_sect_" + ((sectName != null) ? sectName : rnd.nextInt());
						Resource rootSectResource = createIndividual(model, model.getNsPrefixURI("drisect"), rootSectIRI, model.getNsPrefixURI("doco") + "Section");

						// Link to container
						container.addProperty(model.getProperty(model.getNsPrefixURI("po") + "contains"), rootSectResource);

						// Add title
						if(StringUtils.isNotEmpty(rootSect.getName())) {
							String rootSectTitleIRI = rootSectIRI + "_title";
							Resource rootSectTitleResource = createIndividual(model, model.getNsPrefixURI("drisect"), rootSectTitleIRI, model.getNsPrefixURI("doco") + "SectionTitle");
							rootSectResource.addProperty(model.getProperty(model.getNsPrefixURI("po") + "containsAsHeader"), rootSectTitleResource);
							rootSectTitleResource.addProperty(model.getProperty(model.getNsPrefixURI("c4o") + "hasContent"), model.createTypedLiteral(rootSect.getName()));
						}

						// Add sentences
						List<Sentence> sectionSentence = rootSect.getSentences();
						if(sectionSentence != null && sectionSentence.size() > 0) {
							int addedSent_int = 0;
							Resource lastBlankNodeInSequence_INT = null;
							for(int w = 0; w < sectionSentence.size();  w++) {
								Sentence sent = sectionSentence.get(w);
								if(sent != null && sent.getId() != null && sentIdResourceMap.containsKey(sent.getId())) {
									try {
										rootSectResource.addProperty(model.getProperty(model.getNsPrefixURI("po") + "contains"), sentIdResourceMap.get(sent.getId()));

										// List creation logic - http://www.sparontologies.net/examples#doco_1
										if(addedSent_int == 0) {
											// First of the list
											Resource blankNode = model.createResource();

											rootSectResource.addProperty(model.getProperty(model.getNsPrefixURI("co") + "firstItem"), blankNode);
											blankNode.addProperty(model.getProperty(model.getNsPrefixURI("co") + "itemContent"), sentIdResourceMap.get(sent.getId()));
											lastBlankNodeInSequence_INT = blankNode;
										}
										else {
											// After first of the list
											Resource blankNode = model.createResource();
											lastBlankNodeInSequence_INT.addProperty(model.getProperty(model.getNsPrefixURI("co") + "nextItem"), blankNode);
											blankNode.addProperty(model.getProperty(model.getNsPrefixURI("co") + "itemContent"), sentIdResourceMap.get(sent.getId()));

											lastBlankNodeInSequence_INT = blankNode;
										}
										addedSent_int++;
									}
									catch (Exception e) {
										Util.notifyException("Adding sentence RDF to section", e, logger);
									}
								}
							}
						}

						// Section list creation logic - http://www.sparontologies.net/examples#doco_1
						if(addedSect == 0) {
							// First of the list
							Resource blankNode = model.createResource();

							container.addProperty(model.getProperty(model.getNsPrefixURI("co") + "firstItem"), blankNode);
							blankNode.addProperty(model.getProperty(model.getNsPrefixURI("co") + "itemContent"), rootSectResource);
							lastBlankNodeInSequence = blankNode;
						}
						else {
							// After first of the list
							Resource blankNode = model.createResource();
							lastBlankNodeInSequence.addProperty(model.getProperty(model.getNsPrefixURI("co") + "nextItem"), blankNode);
							blankNode.addProperty(model.getProperty(model.getNsPrefixURI("co") + "itemContent"), rootSectResource);

							lastBlankNodeInSequence = blankNode;
						}

						if(rootSect.getSubsections() != null && rootSect.getSubsections().size() > 0) {
							expandSection(rootSect.getSubsections(), rootSectResource, sentIdResourceMap, model, docDigest);
						}

						addedSect++;
					}
					catch (Exception e) {
						Util.notifyException("Adding sentence RDF to abstract", e, logger);
					}
				}
			}
		}

	}


	private static void createSentence(Sentence sent, OntModel model, Map<Integer, Resource> sentIdResourceMap, String docDigest) throws InvalidOffsetException {
		String sentURIstr = docDigest + "_sent_" + sent.getId();
		Resource sentResource = createIndividual(model, model.getNsPrefixURI("drisent"), sentURIstr, model.getNsPrefixURI("doco") + "Sentence");
		sentIdResourceMap.put(sent.getId(), sentResource);
		logger.debug("Added sentence URI: " + sentResource.getURI());

		// Add contents
		if(sent.getText() != null) {
			sentResource.addProperty(model.getProperty(model.getNsPrefixURI("c4o") + "hasContent"), model.createTypedLiteral(sent.getText()));
			logger.debug("RDF: added sentence c4o:hasContent > " + sent.getText());
		}
		else {
			logger.debug("RDF: impossible to retrieve sentence content > sentence ID: " + sent.getId());
		}

		// Add rhetorical class
		if(sent.getRhetoricalClass() != null) {
			switch (sent.getRhetoricalClass()) {
			case DRI_Approach:
				sentResource.addProperty(RDF.type, model.getResource(model.getNsPrefixURI("drionto") + "Approach"));
				break;
			case DRI_Background:
				sentResource.addProperty(RDF.type, model.getResource(model.getNsPrefixURI("drionto") + "Background"));
				break;
			case DRI_Challenge:
				sentResource.addProperty(RDF.type, model.getResource(model.getNsPrefixURI("drionto") + "Challenge"));
				break;
			case DRI_FutureWork:
				sentResource.addProperty(RDF.type, model.getResource(model.getNsPrefixURI("drionto") + "FutureWork"));
				break;
			case DRI_Outcome:
				sentResource.addProperty(RDF.type, model.getResource(model.getNsPrefixURI("drionto") + "Outcome"));
				break;
			case DRI_Unspecified:
				sentResource.addProperty(RDF.type, model.getResource(model.getNsPrefixURI("drionto") + "Unspecified"));
				break;
			case STILL_NOT_EXECUTED_RHETORICAL_CLASSIFICATION:
				break;
			default:

			}
			logger.debug("RDF: added sentence rhetorical class > " + sent.getRhetoricalClass());
		}
		else {
			logger.debug("RDF: impossible to retrieve sentence content > sentence ID: " + sent.getId());
		}

		// Add Babelnet info
		if(sent.getBabelSynsetsOcc() != null) {
			int senseCount = 1;
			for(BabelSynsetOcc synOcc : sent.getBabelSynsetsOcc()) {
				if(synOcc != null && synOcc.getBabelURL() != null && synOcc.getScore() != null) {
					String sentenceSenseURIstr = sentURIstr + "_sense_" + senseCount++;
					Resource sentSenseResource = createIndividual(model, model.getNsPrefixURI("drisent"), sentenceSenseURIstr, model.getNsPrefixURI("drionto") + "Sense");

					sentResource.addProperty(model.getProperty(model.getNsPrefixURI("dc") + "references"), sentSenseResource);
					if(StringUtils.isNotEmpty(synOcc.getBabelURL())) {
						sentSenseResource.addProperty(model.getProperty(model.getNsPrefixURI("dc") + "identifier"), model.createTypedLiteral(synOcc.getBabelURL(), model.getNsPrefixURI("xsd") + "anyURI"));
					}

					if(StringUtils.isNotEmpty(synOcc.getDbpediaURL())) {
						sentSenseResource.addProperty(model.getProperty(model.getNsPrefixURI("dc") + "identifier"), model.createTypedLiteral(synOcc.getDbpediaURL(), model.getNsPrefixURI("xsd") + "anyURI"));
					}

					if(synOcc.getScore() != null) {
						sentSenseResource.addProperty(model.getProperty(model.getNsPrefixURI("drionto") + "linkScore"), model.createTypedLiteral(synOcc.getScore(), model.getNsPrefixURI("xsd") + "anyURI"));
					}

					logger.debug("RDF: added sentence Babel synset link > " + synOcc.getId());
				}
				else {
					logger.debug("RDF: incorrect Babel synset id information > sentence ID: " + sent.getId());
				}
			}
		}
		else {
			logger.debug("RDF: no Babel synset links present for sentence > sentence ID: " + sent.getId());
		}
	}
	
	private static String processPersonName(String personName) {
		if(StringUtils.isNotEmpty(personName)) {
			personName = personName.replace(",",  " ").replace("_",  " ").replace("  ",  " ").replace("  ",  " ").replace("  ",  " ").trim();
			return personName;
		}
		else {
			return personName;
		}
	}
}
