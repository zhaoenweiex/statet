/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.sweave.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.core.impl.GenericResourceSourceUnit2;

import de.walware.docmlet.tex.core.TexCore;
import de.walware.docmlet.tex.core.model.ILtxWorkspaceSourceUnit;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RProjects;
import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.sweave.ILtxRweaveSourceUnit;
import de.walware.statet.r.sweave.ITexRweaveCoreAccess;
import de.walware.statet.r.sweave.Sweave;
import de.walware.statet.r.sweave.TexRweaveCoreAccess;


public class LtxRweaveSourceUnit extends GenericResourceSourceUnit2<LtxRweaveSuModelContainer> 
		implements ILtxRweaveSourceUnit, ILtxWorkspaceSourceUnit, IRWorkspaceSourceUnit {
	
	
	private ITexRweaveCoreAccess coreAccess;
	
	
	public LtxRweaveSourceUnit(final String id, final IFile file) {
		super(id, file);
	}
	
	@Override
	protected LtxRweaveSuModelContainer createModelContainer() {
		return new LtxRweaveSuModelContainer(this);
	}
	
	
	@Override
	public String getModelTypeId() {
		return Sweave.LTX_R_MODEL_TYPE_ID;
	}
	
	@Override
	public String getContentTypeId() {
		return Sweave.LTX_R_CONTENT_ID;
	}
	
	
	@Override
	public ITexRweaveCoreAccess getRCoreAccess() {
		ITexRweaveCoreAccess coreAccess = this.coreAccess;
		if (coreAccess == null) {
			final IRProject rProject = RProjects.getRProject(getResource().getProject());
			coreAccess = new TexRweaveCoreAccess(TexCore.getWorkbenchAccess(),
					(rProject != null) ? rProject : RCore.getWorkbenchAccess() );
			synchronized (this) {
				if (isConnected()) {
					this.coreAccess = coreAccess;
				}
			}
		}
		return coreAccess;
	}
	
	@Override
	public IREnv getREnv() {
		return RCore.getREnvManager().getDefault();
	}
	
	@Override
	public ITexRweaveCoreAccess getTexCoreAccess() {
		return getRCoreAccess();
	}
	
	
	@Override
	protected void register() {
		super.register();
		
		final IModelManager rManager = RCore.getRModelManager();
		if (rManager != null) {
			rManager.deregisterDependentUnit(this);
		}
	}
	
	@Override
	protected void unregister() {
		final IModelManager rManager = RCore.getRModelManager();
		if (rManager != null) {
			rManager.deregisterDependentUnit(this);
		}
		
		super.unregister();
		
		this.coreAccess = null;
	}
	
	
	@Override
	public void reconcileRModel(final int reconcileLevel, final IProgressMonitor monitor) {
	}
	
	
	@Override
	public Object getAdapter(final Class required) {
		if (required.equals(IRCoreAccess.class)) {
			return getRCoreAccess();
		}
		return super.getAdapter(required);
	}
	
}
