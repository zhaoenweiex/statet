/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.TextConsole;

import de.walware.eclipsecommons.ICommonStatusConstants;
import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.r.launching.IRCodeLaunchConnector;
import de.walware.statet.r.ui.RUI;


/**
 * Connector for classic Eclipse console.
 */
public class TextConsoleConnector implements IRCodeLaunchConnector {
	
	
	public static final String ID = "de.walware.statet.r.rCodeLaunchConnector.EclipseTextConsole"; //$NON-NLS-1$
	
	
	public TextConsoleConnector() {
	}
	
	public boolean submit(final String[] rCommands, final boolean gotoConsole) throws CoreException {
		UIAccess.checkedSyncExec(new UIAccess.CheckedRunnable() {
			public void run() throws CoreException {
				IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(true);
				IWorkbenchPart activePart = page.getActivePart();
		
				try {
					TextConsole console = getAndShowConsole();
					if (console == null) {
						handleNoConsole();
					}

					IDocument doc = console.getDocument();
					try {
						for (int i = 0; i < rCommands.length; i++) {
							doc.replace(doc.getLength(), 0, rCommands[i]+'\n');
						}
						if (gotoConsole) {
							activePart = null;
						}
					} catch (BadLocationException e) {
						throw new CoreException(new Status(
								IStatus.ERROR,
								RUI.PLUGIN_ID,
								ICommonStatusConstants.LAUNCHING_ERROR,
								RLaunchingMessages.TextConsoleConnector_error_Other_message,
								e));
					}
				}
				finally {
					if (activePart != null) {
						page.activate(activePart);
					}
				}
			}
		});
		return true; // otherwise, we throw exception
	}
	
	public void gotoConsole() throws CoreException {
		UIAccess.checkedSyncExec(new UIAccess.CheckedRunnable() {
			public void run() throws CoreException {
				TextConsole console = getAndShowConsole();
				if (console == null) {
					handleNoConsole();
				}
			}
		});
	}
	
	private void handleNoConsole() throws CoreException {
		throw new CoreException(new Status(
				IStatus.WARNING,
				RUI.PLUGIN_ID,
				ICommonStatusConstants.LAUNCHING_ERROR,
				RLaunchingMessages.TextConsoleConnector_error_NoConsole_message,
				null));
	}
	
	private TextConsole getAndShowConsole() throws CoreException {
		IConsoleView view = getConsoleView(true);
		IConsole console = view.getConsole();
		if (console instanceof TextConsole) {
			return ((TextConsole) console);
		}
		return null;
	}
	
	private IConsoleView getConsoleView(boolean activateConsoleView) throws PartInitException {
		IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(false);
		IConsoleView view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
		if (activateConsoleView) {
			page.activate(view);
		}
		return view;
	}
	
}
