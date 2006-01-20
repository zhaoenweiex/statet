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

package de.walware.statet.r.internal.debug.launcher;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

import de.walware.statet.r.internal.debug.RLaunchingMessages;
import de.walware.statet.r.launching.RCodeLaunchRegistry;


public class RScriptViaSourceLaunchShortcut implements ILaunchShortcut {

	public void launch(ISelection selection, String mode) {

		assert mode.equals("run");
		
		try {
			IStructuredSelection sel = (IStructuredSelection) selection;
			Object firstElement = sel.getFirstElement();
			if (firstElement instanceof IFile) {
				doRun((IFile) firstElement);
			}
		}
		catch (Exception e) {
			LaunchShortcutUtil.handleRLaunchException(e, 
					RLaunchingMessages.RScriptLaunch_error_message);
		}
	}

	public void launch(IEditorPart editor, String mode) {

		assert mode.equals("run");
		
		try {
			IFileEditorInput input = (IFileEditorInput) editor.getEditorInput();
			doRun(input.getFile());
		}
		catch (Exception e) {
			LaunchShortcutUtil.handleRLaunchException(e, 
					RLaunchingMessages.RScriptLaunch_error_message);
		}
	}

	
	private void doRun(IFile file) throws CoreException {

		RCodeLaunchRegistry.runRFileViaSource(file);
	}
	
}