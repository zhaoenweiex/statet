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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import de.walware.statet.r.internal.debug.RLaunchingMessages;
import de.walware.statet.r.launching.RCodeLaunchRegistry;


public class RScriptDirectLaunchShortcut implements ILaunchShortcut {

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
			AbstractTextEditor redt = (AbstractTextEditor) editor;
			IDocument doc = redt.getDocumentProvider().getDocument(editor.getEditorInput() );
			int countLines = doc.getNumberOfLines();
			String[] lines = new String[countLines];
			for (int i = 0; i < countLines; i++) {
				int lineLength = doc.getLineLength(i);
				String lineDelimiter = doc.getLineDelimiter(i);
				if (lineDelimiter != null)
					lineLength -= lineDelimiter.length();
				lines[i] = doc.get(doc.getLineOffset(i), lineLength);
			}
			RCodeLaunchRegistry.runRCodeDirect(lines);
		}
		catch (Exception e) {
			LaunchShortcutUtil.handleRLaunchException(e, 
					RLaunchingMessages.RScriptLaunch_error_message);
		}
	}
	
	
	private void doRun(IFile file) throws Exception {

		InputStream input = file.getContents();
		
		String charset = file.getCharset();
		BufferedReader reader = new BufferedReader(new InputStreamReader(input, charset));
		List<String> lines = new ArrayList<String>();
		String line;
		while ((line = reader.readLine()) != null) {
			lines.add(line);
		}
		RCodeLaunchRegistry.runRCodeDirect(lines.toArray(new String[lines.size()]));
	}
}