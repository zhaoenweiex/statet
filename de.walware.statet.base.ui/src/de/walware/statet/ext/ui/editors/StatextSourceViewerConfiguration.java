/*******************************************************************************
 * Copyright (c) 2005-2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.editors;

import java.util.Iterator;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import de.walware.eclipsecommons.templates.TemplateVariableProcessor;
import de.walware.eclipsecommons.templates.WordFinder;
import de.walware.eclipsecommons.ui.util.ColorManager;

import de.walware.statet.base.internal.ui.StatetUIPlugin;
import de.walware.statet.ext.ui.text.StatextTextScanner;


/**
 * Configuration for SourceViewer...
 */
public abstract class StatextSourceViewerConfiguration extends TextSourceViewerConfiguration {

	
	public static IPreferenceStore createCombinedPreferenceStore(IPreferenceStore store) {
		IPreferenceStore[] stores = new IPreferenceStore[] {
			store,
			StatetUIPlugin.getDefault().getPreferenceStore(),
			EditorsUI.getPreferenceStore(),
		};
		return new ChainedPreferenceStore(stores);
	}
	
	
	private ColorManager fColorManager;
	private StatextTextScanner[] fScanners;
	

	public StatextSourceViewerConfiguration() {
	}
	
	protected void setup(IPreferenceStore preferenceStore, ColorManager colorManager) {
		fPreferenceStore = preferenceStore;
		fColorManager = colorManager;
	}
	
	/**
	 * Initializes the scanners.
	 */
	protected void setScanners(StatextTextScanner[] scanners) {
		fScanners = scanners;
	}

	public abstract boolean affectsTextPresentation(PropertyChangeEvent event);
	
	public IPreferenceStore getPreferences() {
		return fPreferenceStore;
	}
	
	protected ColorManager getColorManager() {
		return fColorManager;
	}
	
	public void handlePropertyChangeEvent(PropertyChangeEvent event) {
		if (affectsTextPresentation(event)) {
			for (StatextTextScanner scanner : fScanners) {
				scanner.adaptToPreferenceChange(event);
			}
		}
	}
	
	
/* For TemplateEditors ********************************************************/
	
	protected static class TemplateVariableTextHover implements ITextHover {

		private TemplateVariableProcessor fProcessor;

		/**
		 * @param processor the template variable processor
		 */
		public TemplateVariableTextHover(TemplateVariableProcessor processor) {
			fProcessor = processor;
		}

		public String getHoverInfo(ITextViewer textViewer, IRegion subject) {
			try {
				IDocument doc= textViewer.getDocument();
				int offset= subject.getOffset();
				if (offset >= 2 && "${".equals(doc.get(offset-2, 2))) { //$NON-NLS-1$
					String varName= doc.get(offset, subject.getLength());
					TemplateContextType contextType= fProcessor.getContextType();
					if (contextType != null) {
						Iterator iter= contextType.resolvers();
						while (iter.hasNext()) {
							TemplateVariableResolver var= (TemplateVariableResolver) iter.next();
							if (varName.equals(var.getType())) {
								return var.getDescription();
							}
						}
					}
				}				
			} catch (BadLocationException e) {
			}
			return null;
		}

		public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
			if (textViewer != null) {
				return WordFinder.findWord(textViewer.getDocument(), offset);
			}
			return null;	
		}
		
	} 
	
	protected IContentAssistant getTemplateVariableContentAssistant(ISourceViewer sourceViewer, TemplateVariableProcessor processor) {
		ContentAssistant assistant = new ContentAssistant();
		
		for (String contentType : getConfiguredContentTypes(sourceViewer)) {
			assistant.setContentAssistProcessor(processor, contentType);
		}

//		ContentAssistPreference.configure(assistant);
		
		assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));

		return assistant;
	}	
	
}