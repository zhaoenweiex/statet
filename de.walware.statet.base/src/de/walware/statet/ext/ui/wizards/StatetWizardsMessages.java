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

package de.walware.statet.ext.ui.wizards;

import org.eclipse.osgi.util.NLS;


public class StatetWizardsMessages extends NLS {
	
	private static final String BUNDLE_NAME = StatetWizardsMessages.class.getName();


	public static String NewElementWizard_error_DuringOperation_message;

	public static String NewProjectReferencePage_title;
	public static String NewProjectReferencePage_description;
	
	public static String ResourceGroup_NewFile_label;
	public static String ResourceGroup_error_EmptyName;
	public static String ResourceGroup_error_InvalidFilename;
	public static String ResourceGroup_error_ResourceExists;




	static {
		NLS.initializeMessages(BUNDLE_NAME, StatetWizardsMessages.class);
	}
	
}