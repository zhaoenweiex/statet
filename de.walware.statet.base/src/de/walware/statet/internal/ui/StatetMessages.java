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

package de.walware.statet.internal.ui;

import org.eclipse.osgi.util.NLS;


public class StatetMessages extends NLS {
	

	public static String ErrorDialog_title;
	public static String InternalError_UnexpectedException;
	
	public static String TaskPriority_High;
	public static String TaskPriority_Normal;
	public static String TaskPriority_Low;

	public static String CoreUtility_Build_Job_title;
	public static String CoreUtility_Build_AllTask_name;
	public static String CoreUtility_Build_ProjectTask_name;
	
	public static String CopyToClipboard_error_title;
	public static String CopyToClipboard_error_message;

	
	private static final String BUNDLE_NAME = StatetMessages.class.getName();

	static {
		NLS.initializeMessages(BUNDLE_NAME, StatetMessages.class);
	}
	
}