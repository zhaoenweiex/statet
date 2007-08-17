/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rsource.ast;

import de.walware.statet.r.core.rlang.RTerminal;



public abstract class Relational extends StdBinary {
	

	static class LT extends Relational {
		
		@Override
		public final RTerminal getOperator() {
			return RTerminal.REL_LT;
		}

	}
	
	static class LE extends Relational {
		
		@Override
		public final RTerminal getOperator() {
			return RTerminal.REL_LE;
		}

	}

	static class EQ extends Relational {
		
		@Override
		public final RTerminal getOperator() {
			return RTerminal.REL_EQ;
		}

	}

	static class GE extends Relational {
		
		@Override
		public final RTerminal getOperator() {
			return RTerminal.REL_GE;
		}

	}

	static class GT extends Relational {
		
		@Override
		public final RTerminal getOperator() {
			return RTerminal.REL_GT;
		}

	}

	static class NE extends Relational {
		
		@Override
		public final RTerminal getOperator() {
			return RTerminal.REL_NE;
		}

	}

	
	@Override
	public final NodeType getNodeType() {
		return NodeType.RELATIONAL;
	}
	
	public abstract RTerminal getOperator();
	
	@Override
	public final void accept(RAstVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public final boolean equalsSingle(RAstNode element) {
		return (element.getNodeType() == NodeType.RELATIONAL);
	}

}
