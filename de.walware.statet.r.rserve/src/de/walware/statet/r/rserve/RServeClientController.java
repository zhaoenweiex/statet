/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * v2.1 or newer, which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.rserve;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.rosuda.JRclient.REXP;
import org.rosuda.JRclient.RSrvException;
import org.rosuda.JRclient.Rconnection;

import de.walware.eclipsecommons.preferences.PreferencesUtil;

import de.walware.statet.nico.core.NicoPreferenceNodes;
import de.walware.statet.nico.core.runtime.IToolEventHandler;
import de.walware.statet.nico.core.runtime.IToolRunnable;
import de.walware.statet.nico.core.runtime.Prompt;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.r.internal.rserve.launchconfigs.ConnectionConfig;
import de.walware.statet.r.nico.AbstractRController;
import de.walware.statet.r.nico.BasicR;
import de.walware.statet.r.nico.IBasicRAdapter;
import de.walware.statet.r.nico.ISetupRAdapter;
import de.walware.statet.r.nico.IncompleteInputPrompt;
import de.walware.statet.r.nico.RWorkspace;
import de.walware.statet.r.nico.ui.tools.RQuitRunnable;


public class RServeClientController
		extends AbstractRController<IBasicRAdapter, RWorkspace> {

	
	private class RServeAdapter extends AbstractRAdapter implements IBasicRAdapter, ISetupRAdapter, IAdaptable {
		
		@Override
		protected Prompt doSubmit(String input, IProgressMonitor monitor) {
			String completeInput = input;
			if ((fPrompt.meta & BasicR.META_PROMPT_INCOMPLETE_INPUT) != 0) {
				completeInput = ((IncompleteInputPrompt) fPrompt).previousInput + input;
			}
			monitor.subTask(fDefaultPrompt.text + " " + completeInput);  //$NON-NLS-1$
			try {
				REXP rx = fRconnection.eval(completeInput);
				if (rx != null) {
					fDefaultOutputStream.append(rx.toString()+fLineSeparator, fCurrentRunnable.getSubmitType(), 0);
				}
				else {
					fErrorOutputStream.append("[RServe] Warning: Server returned null."+fLineSeparator, fCurrentRunnable.getSubmitType(), 0);
				}
				return fDefaultPrompt;
			}
			catch (RSrvException e) {
				if (e.getRequestReturnCode() == 2) {
					return createIncompleteInputPrompt(fPrompt, input);
				}
				fErrorOutputStream.append("[RServe] Error: "+e.getLocalizedMessage()+"."+fLineSeparator, fCurrentRunnable.getSubmitType(), 0);
				if (!fRconnection.isConnected() || e.getRequestReturnCode() == -1) {
					killTool(new NullProgressMonitor());
					return Prompt.NONE;
				}
				else {
					return fDefaultPrompt;
				}
			}
		}
	}
	
	
	private ConnectionConfig fConfig;
	private Rconnection fRconnection;
	
	
	public RServeClientController(ToolProcess process, ConnectionConfig config) {
		super(process);
		fConfig = config;
		
		fWorkspaceData = new RWorkspace(this);
		fRunnableAdapter = new RServeAdapter();
	}
	
	@Override
	protected void startTool(IProgressMonitor monitor) throws CoreException {
		try {
    		int timeout = PreferencesUtil.getInstancePrefs().getPreferenceValue(NicoPreferenceNodes.KEY_DEFAULT_TIMEOUT);
			fRconnection = new Rconnection(
					fConfig.getServerAddress(), fConfig.getServerPort(),
					timeout);
			
			if (!fRconnection.isConnected()) {
				throw new CoreException(new Status(
						IStatus.ERROR,
						RServePlugin.PLUGIN_ID,
						0,
						"Cannot connect to RServe server.",
						null));
			}
			
			fInfoStream.append("[RServe] Server version: "+fRconnection.getServerVersion()+"."+fWorkspaceData.getLineSeparator(), SubmitType.OTHER, 0);
			
			if (fRconnection.needLogin()) {
				String[] login = new String[] { "guest", "guest" };
				int result = IToolEventHandler.OK;
				IToolEventHandler handler = getEventHandler(LOGIN_EVENT_ID);
				if (handler != null) {
					result = handler.handle(fRunnableAdapter, login);
				}
				if (result == IToolEventHandler.OK) {
					fRconnection.login(login[0], login[1]);
				}
				else {
					killTool(new NullProgressMonitor());
				}
			}

//			ISetupRAdapter system = (RServeAdapter) fRunnableAdapter;
//			system.setDefaultPromptText("> ");
//			system.setIncompletePromptText("+ ");
//			system.setLineSeparator("\n");
		}
		catch (RSrvException e) {
			throw new CoreException(new Status(
					IStatus.ERROR,
					RServePlugin.PLUGIN_ID,
					0,
					"Error when connecting to RServe server.",
					e));
		}
	}
	
	@Override
	protected void interruptTool(int hardness) {
		if (hardness == 0) {
			return;
		}
		getControllerThread().interrupt();
	}
	
	@Override
	protected boolean isToolAlive() {
		Rconnection con = fRconnection;
		if (con != null && con.isConnected()) {
			return true;
		}
		return false;
	}
	
	@Override
	protected IToolRunnable createQuitRunnable() {
		return new RQuitRunnable() {
			@Override
			public void run(IBasicRAdapter tools, IProgressMonitor monitor)
					throws InterruptedException, CoreException {
				fRconnection.close();
				markAsTerminated();
			}
		};
	}
	
	@Override
	protected void killTool(IProgressMonitor monitor) {
		Rconnection con = fRconnection;
		if (con != null) {
			con.close();
			fRconnection = null;
		}
		markAsTerminated();
	}

}
