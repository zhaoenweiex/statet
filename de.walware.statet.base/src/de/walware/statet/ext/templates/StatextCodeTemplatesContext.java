/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.templates;

import java.util.Iterator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateTranslator;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

import de.walware.statet.base.core.StatetProject;


public class StatextCodeTemplatesContext extends TemplateContext implements IStatetContext {
	
	private String fLineDelimiter;
	private StatetProject fProject;

	
	public StatextCodeTemplatesContext(
			TemplateContextType contextType, 
			StatetProject project, 
			String lineDelim) {
		super(contextType);
		fLineDelimiter = lineDelim;
		fProject = project;
	}

	/* 
	 * @see de.walware.statet.ext.codegeneration.IStatetContext#getStatetProject()
	 */
	public StatetProject getStatetProject() {
		
		return fProject;
	}

	public TemplateBuffer evaluate(Template template) throws BadLocationException, TemplateException {
		// test that all variables are defined
		Iterator iterator = getContextType().resolvers();
		while (iterator.hasNext()) {
			TemplateVariableResolver var = (TemplateVariableResolver) iterator.next();
			if (var instanceof StatextCodeTemplatesContextType.CodeTemplatesVariableResolver) {
				Assert.isNotNull(getVariable(var.getType()), "Variable " + var.getType() + "not defined"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		if (!canEvaluate(template))
			return null;
			
		String pattern = changeLineDelimiter(template.getPattern(), fLineDelimiter);
		
		TemplateTranslator translator= new TemplateTranslator();
		TemplateBuffer buffer= translator.translate(pattern);
		getContextType().resolve(buffer, this);
		return buffer;
	}
	
	private static String changeLineDelimiter(String code, String lineDelim) {
		try {
			ILineTracker tracker= new DefaultLineTracker();
			tracker.set(code);
			int nLines= tracker.getNumberOfLines();
			if (nLines == 1) {
				return code;
			}
			
			StringBuffer buf= new StringBuffer();
			for (int i= 0; i < nLines; i++) {
				if (i != 0) {
					buf.append(lineDelim);
				}
				IRegion region = tracker.getLineInformation(i);
				String line= code.substring(region.getOffset(), region.getOffset() + region.getLength());
				buf.append(line);
			}
			return buf.toString();
		} catch (BadLocationException e) {
			// can not happen
			return code;
		}
	}		

	public boolean canEvaluate(Template template) {
		
		return true;
	}
	
}
