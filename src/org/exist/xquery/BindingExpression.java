/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-06 Wolfgang M. Meier
 *  wolfgang@exist-db.org
 *  http://exist.sourceforge.net
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *  
 *  $Id$
 */
package org.exist.xquery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.exist.dom.persistent.*;
import org.exist.numbering.NodeId;
import org.exist.storage.UpdateListener;
import org.exist.xquery.value.*;

/**
 * Abstract superclass for the variable binding expressions "for" and "let".
 * 
 * @author Wolfgang Meier <wolfgang@exist-db.org>
 */
public abstract class BindingExpression extends AbstractFLWORClause implements RewritableExpression {

	protected final static Logger LOG =
		LogManager.getLogger(BindingExpression.class);

    protected final static SequenceType POSITIONAL_VAR_TYPE = 
        new SequenceType(Type.INTEGER, Cardinality.EXACTLY_ONE);
    
	protected String varName;
	protected SequenceType sequenceType = null;
	protected Expression inputSequence;
	protected Expression whereExpr;

	private ExprUpdateListener listener;


    public BindingExpression(XQueryContext context) {
		super(context);
	}

	public void setVariable(String qname) {
		varName = qname;
	}

    public String getVariable() {
        return this.varName;
    }

    /**
	 * Set the sequence type of the variable (as specified in the "as" clause).
	 * 
	 * @param type
	 */
	public void setSequenceType(SequenceType type) {
		this.sequenceType = type;
	}

	public void setInputSequence(Expression sequence) {
		this.inputSequence = sequence.simplify();
	}

    public Expression getInputSequence() {
        return this.inputSequence;
    }

    public void setWhereExpression(Expression expr) {
		this.whereExpr = expr.simplify();
	}

    public Expression getWhereExpression() {
        return this.whereExpr;
    }

    /* (non-Javadoc)
             * @see org.exist.xquery.Expression#analyze(org.exist.xquery.Expression, int)
             */
    public void analyze(AnalyzeContextInfo contextInfo) throws XPathException {
        unordered = (contextInfo.getFlags() & UNORDERED) > 0;
    }

    @Override
    public Sequence postEval(Sequence seq) throws XPathException {
        if (returnExpr instanceof FLWORClause) {
            seq = ((FLWORClause)returnExpr).postEval(seq);
        }
        return super.postEval(seq);
    }

    protected Sequence applyWhereExpression(Sequence contextSequence) throws XPathException {
		if (contextSequence != null &&
				Type.subTypeOf(contextSequence.getItemType(), Type.NODE) &&
                contextSequence.isPersistentSet() &&
                //We might not be sure of the return type at this level
				Type.subTypeOf(whereExpr.returnsType(), Type.ITEM)) {
			final Sequence seq = whereExpr.eval(contextSequence);
			//But *now*, we are ;-)
			if (Type.subTypeOf(whereExpr.returnsType(), Type.NODE)) {
				final NodeSet nodes = seq.toNodeSet(); 
				// if the where expression returns a node set, check the context
				// node of each node in the set           
				final NodeSet contextSet = contextSequence.toNodeSet();
				final boolean contextIsVirtual = contextSet instanceof VirtualNodeSet;            
				final NodeSet result = new ExtArrayNodeSet();
				DocumentImpl lastDoc = null;

				for (final NodeProxy current : nodes) {
					int sizeHint = Constants.NO_SIZE_HINT;
					if(lastDoc == null || current.getOwnerDocument() != lastDoc) {
						lastDoc = current.getOwnerDocument();
						sizeHint = nodes.getSizeHint(lastDoc);
					}
					ContextItem	context = current.getContext();                
					if (context == null) {               
						throw new XPathException(this, "Internal evaluation error: context node is missing for node " +
								current.getNodeId() + "!");
					}
	//				LOG.debug(current.debugContext());
					while (context != null) {
	                    //TODO : Is this the context we want ? Not sure... would have prefered the LetExpr.
						if (context.getContextId() == whereExpr.getContextId()) {
							final NodeProxy contextNode = context.getNode();                    
							if(contextIsVirtual || contextSet.contains(contextNode)) {
	                            contextNode.addMatches(current);
								result.add(contextNode, sizeHint);
							}
						}
	                    context = context.getNextDirect();
					}
				}
				return result;
			}
		}
		
		if (contextSequence == null) {
			final Sequence innerSeq = whereExpr.eval(null);
			return innerSeq.effectiveBooleanValue() ? BooleanValue.TRUE : BooleanValue.FALSE;
		} else {
			// general where clause: just check the effective boolean value
			final ValueSequence result = new ValueSequence();
			int p = 0;			
			for (final SequenceIterator i = contextSequence.iterate(); i.hasNext(); p++) {
				final Item item = i.nextItem();
				context.setContextSequencePosition(p, contextSequence);
                final Sequence innerSeq = whereExpr.eval(contextSequence, item);                
				if (innerSeq.effectiveBooleanValue())
					{result.add(item);}                    
			}
			return result;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.exist.xquery.Expression#preselect(org.exist.dom.persistent.DocumentSet, org.exist.xquery.StaticContext)
	 */
	public DocumentSet preselect(DocumentSet in_docs) throws XPathException {
		return in_docs;
	}

	/* (non-Javadoc)
	 * @see org.exist.xquery.AbstractExpression#resetState()
	 */
	public void resetState(boolean postOptimization) {
		super.resetState(postOptimization);
		inputSequence.resetState(postOptimization);
		if(whereExpr != null) {whereExpr.resetState(postOptimization);}
		returnExpr.resetState(postOptimization);
	}
	
	protected final static void setContext(int contextId, Sequence seq) throws XPathException {
		if (seq instanceof VirtualNodeSet) {
			((VirtualNodeSet)seq).setInPredicate(true);
            ((VirtualNodeSet)seq).setSelfIsContext();
		} else {
			Item next;
			for (final SequenceIterator i = seq.unorderedIterator(); i.hasNext();) {
				next = i.nextItem(); 
				if (next instanceof NodeProxy)
					 {((NodeProxy) next).addContextNode(contextId, (NodeProxy) next);}
			}
		}
	}
	
	protected final static void clearContext(int contextId, Sequence seq) throws XPathException {
		if (seq != null && !(seq instanceof VirtualNodeSet)) {
            seq.clearContext(contextId);
		}
	}

    protected void registerUpdateListener(final Sequence sequence) {
        if (listener == null) {
            listener = new ExprUpdateListener(sequence);
            context.registerUpdateListener(listener);
        } else
            {listener.setSequence(sequence);}
    }

    private class ExprUpdateListener implements UpdateListener {
        private Sequence sequence;

        public ExprUpdateListener(Sequence sequence) {
            this.sequence = sequence;
        }

        public void setSequence(Sequence sequence) {
            this.sequence = sequence;
        }
        
        @Override
        public void documentUpdated(DocumentImpl document, int event) {
        }

        @Override
        public void nodeMoved(NodeId oldNodeId, NodeHandle newNode) {
            sequence.nodeMoved(oldNodeId, newNode);
        }

        @Override
        public void unsubscribe() {
            BindingExpression.this.listener = null;
        }

        @Override
        public void debug() {
        }
    }

    @Override
    public int returnsType() {
        //TODO: let must return "return expression type"
        if (sequenceType != null) {
            return sequenceType.getPrimaryType();
        }
        return super.returnsType();
    }

	/* RewritableExpression API */
	
	@Override
	public void replace(Expression oldExpr, Expression newExpr) {
		if (inputSequence == oldExpr)
			{inputSequence = newExpr;}
		else if (whereExpr == oldExpr)
			{whereExpr = newExpr;}
		else if (returnExpr == oldExpr)
			{returnExpr = newExpr;}
	}
	
	@Override
	public Expression getPrevious(Expression current) {
		return null;
	}
	
	@Override
	public Expression getFirst() {
		return null;
	}
	
	@Override
	public void remove(Expression oldExpr) throws XPathException {
	}
	
	/* END RewritableExpression API */
}
