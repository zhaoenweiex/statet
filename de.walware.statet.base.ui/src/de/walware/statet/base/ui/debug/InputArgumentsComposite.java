/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.ui.debug;

import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;

import de.walware.eclipsecommons.ui.dialogs.WidgetToolsButton;
import de.walware.eclipsecommons.ui.util.LayoutUtil;
import de.walware.eclipsecommons.ui.util.PixelConverter;

import de.walware.statet.base.internal.ui.StatetMessages;


/**
 * Composite usually used in launch configuration dialogs.
 */
public class InputArgumentsComposite extends Composite {
	
	
	private Text fTextControl;
	private Button fVariablesButton;
	
	
	public InputArgumentsComposite(final Composite parent) {
		super(parent, SWT.NONE);
		
		createControls();
	}
	
	
	private void createControls() {
		final Composite container = this;
		final GridLayout layout = LayoutUtil.applyCompositeDefaults(new GridLayout(), 2);
		layout.horizontalSpacing = 0;
		container.setLayout(layout);
		
		final Label label = new Label(container, SWT.LEFT);
		label.setText(StatetMessages.InputArguments_label);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
		
		fTextControl = new Text(container, SWT.LEFT | SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = LayoutUtil.hintWidth(fTextControl, SWT.DEFAULT);
		gd.heightHint = new PixelConverter(fTextControl).convertHeightInCharsToPixels(4);
		fTextControl.setLayoutData(gd);
		
		final WidgetToolsButton tools = new WidgetToolsButton(fTextControl) {
			@Override
			protected void fillMenu(final Menu menu) {
				InputArgumentsComposite.this.fillMenu(menu);
			}
		};
		tools.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
	}
	
	protected void fillMenu(final Menu menu) {
		final MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText(StatetMessages.InsertVariable_label);
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				handleVariablesButton();
				getTextControl().setFocus();
			}
		});
	}
	
	private void handleVariablesButton() {
		final StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
		if (dialog.open() != Dialog.OK) {
			return;
		}
		final String variable = dialog.getVariableExpression();
		if (variable == null) {
			return;
		}
		fTextControl.insert(variable);
	}
	
	
	public Text getTextControl() {
		return fTextControl;
	}
	
	public String getNoteText() {
		return StatetMessages.InputArguments_note;
	}
	
}