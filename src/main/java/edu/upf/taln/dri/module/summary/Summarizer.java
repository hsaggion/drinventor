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
package edu.upf.taln.dri.module.summary;

import org.apache.log4j.Logger;

import edu.upf.taln.dri.module.DRIModule;
import gate.ProcessingResource;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.metadata.CreoleResource;



/**
 * Generic summarizer.
 * 
 *
 */
@CreoleResource(name = "DRI Modules - Summary generator")
public class Summarizer extends AbstractLanguageAnalyser implements ProcessingResource, DRIModule {

	private static Logger logger = Logger.getLogger(Summarizer.class);	

	private static final long serialVersionUID = 1L;

	@Override
	public Resource init() {
		return this;
	}
	

	public void execute() throws ExecutionException {
		
		// CURRENT SUMMARIZATION APPROACHES ARE IMPLEMENTED AS XGAPP
		// THIS IS A TEMPLATE FOR NEW SUMMARIZATION APPROACHES
			
	}
	
	@Override
	public boolean resetAnnotations() {
		
		return false;
	}

}
