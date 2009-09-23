/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.launchconfigs;

import java.util.Collections;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.walware.ecommons.databinding.NumberValidator;
import de.walware.ecommons.databinding.SWTMultiEnabledObservable;
import de.walware.ecommons.debug.ui.LaunchConfigTabWithDbc;
import de.walware.ecommons.templates.TemplateVariableProcessor;
import de.walware.ecommons.ui.text.sourceediting.SnippetEditor;
import de.walware.ecommons.ui.text.sourceediting.SnippetEditorObservable;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.launching.RConsoleLaunching;
import de.walware.statet.r.ui.editors.RSourceViewerConfigurator;
import de.walware.statet.r.ui.editors.RTemplateSourceViewerConfigurator;


/**
 * Launch config tab for common R console options
 * <ul>
 *   <li>History (not yet implemented)</li>
 *   <li>Option to pin console page</li>
 *   <li>Option for a startup snippet</li>
 * </ul>
 */
public class RConsoleOptionsTab extends LaunchConfigTabWithDbc {
	
	
	private Button fPinControl;
	private SnippetEditor fStartupSnippetEditor;
	
	private WritableValue fPinValue;
	private WritableValue fStartupSnippetValue;
	
	private Button fObjectDBEnabledControl;
	private WritableValue fObjectDBEnabledValue;
	private Button fObjectDBAutoEnabledControl;
	private WritableValue fObjectDBAutoEnabledValue;
	private Text fObjectDBListsChildrenControl;
	private WritableValue fObjectDBListsChildrenValue;
	private Text fObjectDBEnvsChildrenControl;
	private WritableValue fObjectDBEnvsChildrenValue;
	
	
	public RConsoleOptionsTab() {
	}
	
	
	public String getName() {
		return RLaunchingMessages.RConsole_OptionsTab_name;
	}
	
	
	public void createControl(final Composite parent) {
		final Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainComposite.setLayout(GridLayoutFactory.swtDefaults().create());
		
		{	// Console options:
			final Group group = new Group(mainComposite, SWT.NONE);
			group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			group.setText(RLaunchingMessages.RConsole_MainTab_ConsoleOptions_label);
			createConsoleOptions(group);
		}
		
		{	// Snippet options:
			final Group group = new Group(mainComposite, SWT.NONE);
			group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			group.setText("R snippet run after startup:");
			createSnippetOptions(group);
		}
		
		{	// Object DB options:
			final Group group = new Group(mainComposite, SWT.NONE);
			group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			group.setText("Object DB (for Object Browser etc.):");
			createObjectDBOptions(group);
		}
		
		Dialog.applyDialogFont(parent);
		initBindings();
	}
	
	private void createConsoleOptions(final Composite container) {
		container.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 1));
		
		fPinControl = new Button(container, SWT.CHECK);
		fPinControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fPinControl.setText(RLaunchingMessages.RConsole_MainTab_ConsoleOptions_Pin_label);
		
		LayoutUtil.addSmallFiller(container, false);
		
		{	final Label label = new Label(container, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			label.setText("Load/Save Command &History Automatically:");
			
			final Button disabled = new Button(container, SWT.RADIO);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			gd.horizontalIndent = LayoutUtil.defaultIndent();
			disabled.setLayoutData(gd);
			disabled.setText("Disabled");
			
			final Button rwd = new Button(container, SWT.RADIO);
			gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			gd.horizontalIndent = LayoutUtil.defaultIndent();
			rwd.setLayoutData(gd);
			rwd.setText("'.RHistory' in R working directory");
			
			final Button user = new Button(container, SWT.RADIO);
			gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			gd.horizontalIndent = LayoutUtil.defaultIndent();
			user.setLayoutData(gd);
			user.setText("'.RHistory' in user home directory");
			
			final Button custom = new Button(container, SWT.RADIO);
			gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			gd.horizontalIndent = LayoutUtil.defaultIndent();
			custom.setLayoutData(gd);
			custom.setText("Specified file: ");
			
			disabled.setSelection(true);
			rwd.setEnabled(false);
			user.setEnabled(false);
			custom.setEnabled(false);
		}
	}
	
	private void createSnippetOptions(final Composite container) {
		container.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 1));
		
		final TemplateVariableProcessor templateVariableProcessor = new TemplateVariableProcessor();
		final RSourceViewerConfigurator configurator = new RTemplateSourceViewerConfigurator(RCore.getWorkbenchAccess(), templateVariableProcessor);
		fStartupSnippetEditor = new SnippetEditor(configurator);
		fStartupSnippetEditor.create(container, SnippetEditor.DEFAULT_MULTI_LINE_STYLE);
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = LayoutUtil.hintHeight(fStartupSnippetEditor.getSourceViewer().getTextWidget(), 5);
		fStartupSnippetEditor.getControl().setLayoutData(gd);
	}
	
	private void createObjectDBOptions(final Composite container) {
		container.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 2));
		
		{	fObjectDBEnabledControl = new Button(container, SWT.CHECK);
			fObjectDBEnabledControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
			fObjectDBEnabledControl.setText("Enable");
		}
		
		{	fObjectDBAutoEnabledControl = new Button(container, SWT.CHECK);
			final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
			gd.horizontalIndent = LayoutUtil.defaultIndent();
			fObjectDBAutoEnabledControl.setLayoutData(gd);
			fObjectDBAutoEnabledControl.setText("Refresh DB automatically (initial setting).");
		}
		{	final Label label = new Label(container, SWT.NONE);
			label.setText("Max length of R lists to fetch:");
			final GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
			gd.horizontalIndent = LayoutUtil.defaultIndent();
			label.setLayoutData(gd);
		}
		{	fObjectDBListsChildrenControl = new Text(container, SWT.BORDER);
			fObjectDBListsChildrenControl.setTextLimit(10);
			final GridData gd = new GridData(SWT.LEFT, SWT.FILL, true, false);
			gd.widthHint = LayoutUtil.hintWidth(fObjectDBListsChildrenControl, 10);
			fObjectDBListsChildrenControl.setLayoutData(gd);
		}
		{	final Label label = new Label(container, SWT.NONE);
			label.setText("Max length of R environments to fetch:");
			final GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
			gd.horizontalIndent = LayoutUtil.defaultIndent();
			label.setLayoutData(gd);
		}
		{	fObjectDBEnvsChildrenControl = new Text(container, SWT.BORDER);
			fObjectDBEnvsChildrenControl.setTextLimit(10);
			final GridData gd = new GridData(SWT.LEFT, SWT.FILL, true, false);
			gd.widthHint = LayoutUtil.hintWidth(fObjectDBEnvsChildrenControl, 10);
			fObjectDBEnvsChildrenControl.setLayoutData(gd);
		}
	}
	
	@Override
	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
		fPinValue = new WritableValue(realm, Boolean.class);
		dbc.bindValue(SWTObservables.observeSelection(fPinControl), fPinValue, null, null);
		
		fStartupSnippetValue = new WritableValue(realm, String.class);
		dbc.bindValue(new SnippetEditorObservable(realm, fStartupSnippetEditor, SWT.Modify), fStartupSnippetValue, null, null);
		
		fObjectDBEnabledValue = new WritableValue(realm, Boolean.class);
		fObjectDBAutoEnabledValue = new WritableValue(realm, Boolean.class);
		fObjectDBListsChildrenValue = new WritableValue(realm, Integer.class);
		fObjectDBEnvsChildrenValue = new WritableValue(realm, Integer.class);
		final ISWTObservableValue dbObs = SWTObservables.observeSelection(fObjectDBEnabledControl);
		dbc.bindValue(dbObs, fObjectDBEnabledValue, null, null);
		dbc.bindValue(SWTObservables.observeSelection(fObjectDBAutoEnabledControl), fObjectDBAutoEnabledValue, null, null);
		dbc.bindValue(SWTObservables.observeText(fObjectDBListsChildrenControl, SWT.Modify), fObjectDBListsChildrenValue, 
				new UpdateValueStrategy().setAfterGetValidator(new NumberValidator(100, Integer.MAX_VALUE, "Invalid max value for length of R lists to fetch (100-).")), null);
		dbc.bindValue(SWTObservables.observeText(fObjectDBEnvsChildrenControl, SWT.Modify), fObjectDBEnvsChildrenValue, 
				new UpdateValueStrategy().setAfterGetValidator(new NumberValidator(100, Integer.MAX_VALUE, "Invalid max value for length of R environments to fetch (100-).")), null);
		
		dbc.bindValue(new SWTMultiEnabledObservable(realm, fObjectDBEnabledControl.getParent().getChildren(), Collections.singletonList(fObjectDBEnabledControl)),
				dbObs, null, null);
	}
	
	
	public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
	}
	
	@Override
	protected void doInitialize(final ILaunchConfiguration configuration) {
		boolean pin;
		try {
			pin = configuration.getAttribute(RConsoleLaunching.ATTR_PIN_CONSOLE, false);
		}
		catch (final CoreException e) {
			pin = false;
			logReadingError(e);
		}
		fPinValue.setValue(pin);
		
		String startupSnippet;
		try {
			startupSnippet = configuration.getAttribute(RConsoleLaunching.ATTR_INIT_SCRIPT_SNIPPET, ""); //$NON-NLS-1$
		}
		catch (final CoreException e) {
			startupSnippet = ""; //$NON-NLS-1$
			logReadingError(e);
		}
		fStartupSnippetValue.setValue(startupSnippet);
		
		{	boolean enabled = true;
			try {
				enabled = configuration.getAttribute(RConsoleLaunching.ATTR_OBJECTDB_ENABLED, enabled);
			}
			catch (final CoreException e) {
				logReadingError(e);
			}
			fObjectDBEnabledValue.setValue(enabled);
		}
		{	boolean enabled = true;
			try {
				enabled = configuration.getAttribute(RConsoleLaunching.ATTR_OBJECTDB_AUTOREFRESH_ENABLED, enabled);
			}
			catch (final CoreException e) {
				logReadingError(e);
			}
			fObjectDBAutoEnabledValue.setValue(enabled);
		}
		{	int max = 10000;
			try {
				max = configuration.getAttribute(RConsoleLaunching.ATTR_OBJECTDB_LISTS_MAX_LENGTH, 10000);
			}
			catch (final CoreException e) {
				logReadingError(e);
			}
			fObjectDBListsChildrenValue.setValue(max);
		}
		{	int max = 10000;
			try {
				max = configuration.getAttribute(RConsoleLaunching.ATTR_OBJECTDB_ENVS_MAX_LENGTH, 10000);
			}
			catch (final CoreException e) {
				logReadingError(e);
			}
			fObjectDBEnvsChildrenValue.setValue(max);
		}
	}
	
	@Override
	protected void doSave(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(RConsoleLaunching.ATTR_PIN_CONSOLE, ((Boolean) fPinValue.getValue()).booleanValue());
		
		final String startupSnippet = (String) fStartupSnippetValue.getValue();
		if (startupSnippet != null && startupSnippet.length() > 0) {
			configuration.setAttribute(RConsoleLaunching.ATTR_INIT_SCRIPT_SNIPPET, startupSnippet);
		}
		else {
			configuration.removeAttribute(RConsoleLaunching.ATTR_INIT_SCRIPT_SNIPPET);
		}
		
		{	final Boolean enabled = (Boolean) fObjectDBEnabledValue.getValue();
			configuration.setAttribute(RConsoleLaunching.ATTR_OBJECTDB_ENABLED, enabled.booleanValue());
		}
		{	final Boolean enabled = (Boolean) fObjectDBAutoEnabledValue.getValue();
			configuration.setAttribute(RConsoleLaunching.ATTR_OBJECTDB_AUTOREFRESH_ENABLED, enabled.booleanValue());
		}
		{	final Integer max = (Integer) fObjectDBListsChildrenValue.getValue();
			configuration.setAttribute(RConsoleLaunching.ATTR_OBJECTDB_LISTS_MAX_LENGTH, max.intValue());
		}
		{	final Integer max = (Integer) fObjectDBEnvsChildrenValue.getValue();
		configuration.setAttribute(RConsoleLaunching.ATTR_OBJECTDB_ENVS_MAX_LENGTH, max.intValue());
		}
	}
	
}
