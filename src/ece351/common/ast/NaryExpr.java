/* *********************************************************************
 * ECE351 
 * Department of Electrical and Computer Engineering 
 * University of Waterloo 
 * Term: Fall 2021 (1219)
 *
 * The base version of this file is the intellectual property of the
 * University of Waterloo. Redistribution is prohibited.
 *
 * By pushing changes to this file I affirm that I am the author of
 * all changes. I affirm that I have complied with the course
 * collaboration policy and have not plagiarized my work. 
 *
 * I understand that redistributing this file might expose me to
 * disciplinary action under UW Policy 71. I understand that Policy 71
 * allows for retroactive modification of my final grade in a course.
 * For example, if I post my solutions to these labs on GitHub after I
 * finish ECE351, and a future student plagiarizes them, then I too
 * could be found guilty of plagiarism. Consequently, my final grade
 * in ECE351 could be retroactively lowered. This might require that I
 * repeat ECE351, which in turn might delay my graduation.
 *
 * https://uwaterloo.ca/secretariat-general-counsel/policies-procedures-guidelines/policy-71
 * 
 * ********************************************************************/

package ece351.common.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.parboiled.common.ImmutableList;

import ece351.util.Examinable;
import ece351.util.Examiner;

/**
 * An expression with multiple children. Must be commutative.
 */
public abstract class NaryExpr extends Expr {

	public final ImmutableList<Expr> children;

	public NaryExpr(final Expr... exprs) {
		Arrays.sort(exprs);
		ImmutableList<Expr> c = ImmutableList.of();
		for (final Expr e : exprs) {
			c = c.append(e);
		}
    	this.children = c;
	}
	
	public NaryExpr(final List<Expr> children) {
		final ArrayList<Expr> a = new ArrayList<Expr>(children);
		Collections.sort(a);
		this.children = ImmutableList.copyOf(a);
	}

	/**
	 * Each subclass must implement this factory method to return
	 * a new object of its own type. 
	 */
	public abstract NaryExpr newNaryExpr(final List<Expr> children);

	/**
	 * Construct a new NaryExpr (of the appropriate subtype) with 
	 * one extra child.
	 * @param e the child to append
	 * @return a new NaryExpr
	 */
	public NaryExpr append(final Expr e) {
		return newNaryExpr(children.append(e));
	}

	/**
	 * Construct a new NaryExpr (of the appropriate subtype) with 
	 * the extra children.
	 * @param list the children to append
	 * @return a new NaryExpr
	 */
	public NaryExpr appendAll(final List<Expr> list) {
		final List<Expr> a = new ArrayList<Expr>(children.size() + list.size());
		a.addAll(children);
		a.addAll(list);
		return newNaryExpr(a);
	}

	/**
	 * Check the representation invariants.
	 */
	public boolean repOk() {
		// programming sanity
		assert this.children != null;
		// should not have a single child: indicates a bug in simplification
		assert this.children.size() > 1 : "should have more than one child, probably a bug in simplification";
		// check that children is sorted
		int i = 0;
		for (int j = 1; j < this.children.size(); i++, j++) {
			final Expr x = this.children.get(i);
			assert x != null : "null children not allowed in NaryExpr";
			final Expr y = this.children.get(j);
			assert y != null : "null children not allowed in NaryExpr";
			assert x.compareTo(y) <= 0 : "NaryExpr.children must be sorted";
		}
        // Note: children might contain duplicates --- not checking for that
        // ... maybe should check for duplicate children ...
		// no problems found
		return true;
	}

	/**
	 * The name of the operator represented by the subclass.
	 * To be implemented by each subclass.
	 */
	public abstract String operator();
	
	/**
	 * The complementary operation: NaryAnd returns NaryOr, and vice versa.
	 */
	abstract protected Class<? extends NaryExpr> getThatClass();
	

	/**
     * e op x = e for absorbing element e and operator op.
     * @return
     */
	public abstract ConstantExpr getAbsorbingElement();

    /**
     * e op x = x for identity element e and operator op.
     * @return
     */
	public abstract ConstantExpr getIdentityElement();


	@Override 
    public final String toString() {
    	final StringBuilder b = new StringBuilder();
    	b.append("(");
    	int count = 0;
    	for (final Expr c : children) {
    		b.append(c);
    		if (++count  < children.size()) {
    			b.append(" ");
    			b.append(operator());
    			b.append(" ");
    		}
    		
    	}
    	b.append(")");
    	return b.toString();
    }


	@Override
	public final int hashCode() {
		return 17 + children.hashCode();
	}

	@Override
	public final boolean equals(final Object obj) {
		if (!(obj instanceof Examinable)) return false;
		return examine(Examiner.Equals, (Examinable)obj);
	}
	
	@Override
	public final boolean isomorphic(final Examinable obj) {
		return examine(Examiner.Isomorphic, obj);
	}
	
	private boolean examine(final Examiner e, final Examinable obj) {
		// basics
		if (obj == null) return false;
		// if the number of children are different, consider them not equivalent
		if (!this.getClass().equals(obj.getClass())) return false;
		final NaryExpr that = (NaryExpr) obj;
		if (this.children.size() !=  that.children.size()) return false;
		// since the n-ary expressions have the same number of children and they are sorted, just iterate and check
		// supposed to be sorted, but might not be (because repOk might not pass)
		// if they are not the same elements in the same order return false
		for(int i = 0; i < this.children.size(); i++) {
			if(!this.children.get(i).equals(that.children.get(i))) {
				return false;
			} 
		}
		// no significant differences found, return true
		return true;
	}

	// A function I made to check if a set is a subset of another set for subset absorption
	public boolean isSubset(NaryExpr set){
		if(this.equals(set)) {
			return false;
		}
		for(Expr c : children){
			if(!set.children.contains(c)){
				return false;
			}
		}
		return true;
	}
	
	@Override
	protected final Expr simplifyOnce() {
		assert repOk();
		final Expr result = 
				simplifyChildren().
				mergeGrandchildren().
				foldIdentityElements().
				foldAbsorbingElements().
				foldComplements().
				removeDuplicates().
				simpleAbsorption().
				subsetAbsorption().
				singletonify();
		assert result.repOk();
		return result;
	}
	
	/**
	 * Call simplify() on each of the children.
	 */
	private NaryExpr simplifyChildren() {
		// note: we do not assert repOk() here because the rep might not be ok
		// the result might contain duplicate children, and the children
		// might be out of order
		ImmutableList<Expr> c = ImmutableList.of();
		ImmutableList<Expr> sorted = sortChildren(this.children);
		for(Expr e : sorted) {
			c = c.append(e.simplify());
		}
		c = sortChildren(c);
		return newNaryExpr(c);
	}

	private NaryExpr mergeGrandchildren() {
		// extract children to merge using filter (because they are the same type as us)
		ImmutableList<Expr> c = ImmutableList.of();
		for(Expr e: children){
			if(e.getClass() == this.getClass()){
				// merge in the grandchildren
				for(Expr grandchild : ((NaryExpr) e).children){
					c = c.append(grandchild);
				}

			} else{
				c = c.append(e);
			}
		}
		
		// if no children to merge, then return this (i.e., no change)
		if(c.isEmpty()) {
			return this;
		}	
		c = sortChildren(c);
		NaryExpr naryC = newNaryExpr(c);
		// assert result.repOk(): this operation should always leave the AST in a legal state
		assert naryC.repOk();
		return naryC;
	}


	private NaryExpr foldIdentityElements() {
    	// if we have only one child stop now and return self
		if(children.size() == 1) {
			return this;
		} 
		// we have multiple children, remove the identity elements
    		// all children were identity elements, so now our working list is empty
    		// return a new list with a single identity element
    		// normal return
			// all items that arent the identity element
		else {
			return filter(getIdentityElement(), Examiner.Equals, false);
		}
    	// do not assert repOk(): this fold might leave the AST in an illegal state (with only one child)
    }

    private NaryExpr foldAbsorbingElements() {
		// absorbing element: 0.x=0 and 1+x=1
		// absorbing element is present: return it
		ConstantExpr absorbing = getAbsorbingElement();
		// not so fast! what is the return type of this method? why does it have to be that way?
		// no absorbing element present, do nothing
		// not so fast! what is the return type of this method? why does it have to be that way?
		if(filter(absorbing, Examiner.Equals, true).children.size() != 0) {
			return newNaryExpr(ImmutableList.of(absorbing));
		}
		return this; // TODO: replace this stub
    	// do not assert repOk(): this fold might leave the AST in an illegal state (with only one child)
	}

	private NaryExpr foldComplements() {
		// collapse complements
		// !x . x . ... = 0 and !x + x + ... = 1
		// x op !x = absorbing element
		// find all negations
		NaryExpr not = this.filter(NotExpr.class, true);
		
		// for each negation, see if we find its complement
				// found matching negation and its complement
				// return absorbing element
		for (Expr child : children) {
			for (Expr n: not.children){
				if ((new NotExpr(child)).equals(n) ){
					return newNaryExpr(ImmutableList.of(getAbsorbingElement()));
				}
			}
		}
		// no complements to fold
		return this;
    	// do not assert repOk(): this fold might leave the AST in an illegal state (with only one child)
	}

	private NaryExpr removeDuplicates() {
		// remove duplicate children: x.x=x and x+x=x
		// since children are sorted this is fairly easy
			// no changes
			// removed some duplicates
		ImmutableList<Expr> uniqueList = ImmutableList.of();
		for (Expr c: children){
			if (!uniqueList.contains(c)){
				uniqueList = uniqueList.append(c);
			}
		}
		uniqueList = sortChildren(uniqueList); // prob dont' need this
		return newNaryExpr(uniqueList); // TODO: replace this stub
    	// do not assert repOk(): this fold might leave the AST in an illegal state (with only one child)
	}

	private NaryExpr simpleAbsorption() {
		// (x.y) + x ... = x ...
		// check if there are any conjunctions that can be removed
		ImmutableList<Expr> itemsToRemove = ImmutableList.of();
		int i = 0;
		int j = 0;
		for(Expr c1 : children){
			i++;
			for(Expr c2 : children ){
				j++;
				if (i ==j) continue; // Since c1 will = c2 always when they are the same element

				if (c2 instanceof NaryExpr && !(c1 instanceof NaryExpr)){
					if(((NaryExpr) c2).children.contains(c1)){
						itemsToRemove = itemsToRemove.append(c2);
					}
				}
			}
		}
		return removeAll(itemsToRemove, Examiner.Equals);
    	// do not assert repOk(): this operation might leave the AST in an illegal state (with only one child)
	}

	private NaryExpr subsetAbsorption() {
        // check if there are any conjunctions that are supersets of others
        // e.g., ( a . b . c ) + ( a . b ) = a . b
        ImmutableList<Expr> itemsToRemove = ImmutableList.of();
        ImmutableList<Expr> listOfChildren = ImmutableList.of();

        for(Expr c : children) {
            if(c instanceof NaryExpr) {
                listOfChildren = listOfChildren.append(c);
            }
        }

        NaryExpr sets = newNaryExpr(listOfChildren);
		// Checking all the sets and determining whether the set is a subset of the superset of all children
        for (Expr set : sets.children) {
            for(Expr set2 : sets.children) {
                if(((NaryExpr) set).isSubset((NaryExpr) set2)) {
                    itemsToRemove = itemsToRemove.append(set2);
                }
            }

            for(Expr grandchildSet : ((NaryExpr) set).children) {
                if(grandchildSet instanceof NaryExpr) {
                    if(((NaryExpr) grandchildSet).isSubset(this)) {
                        itemsToRemove = itemsToRemove.append(set);
                        break;
                    }
                } else {
					break;
				}    
            }
        }
        NaryExpr out = newNaryExpr(sortChildren(removeAll(itemsToRemove, Examiner.Equals).children));
        return out;
        // do not assert repOk(): this operation might leave the AST in an illegal state (with only one child)
    }

	/**
	 * If there is only one child, return it (the containing NaryExpr is unnecessary).
	 */
	private Expr singletonify() {
		// if we have only one child, return it
		// having only one child is an illegal state for an NaryExpr
		// multiple children; nothing to do; return self
		if(children.size() == 1) {
			return children.get(0);
		} else {
			return this;
		}
	}

	/**
	 * Return a new NaryExpr with only the children of a certain type, 
	 * or excluding children of a certain type.
	 * @param filter
	 * @param shouldMatchFilter
	 * @return
	 */
	public final NaryExpr filter(final Class<? extends Expr> filter, final boolean shouldMatchFilter) {
		ImmutableList<Expr> l = ImmutableList.of();
		for (final Expr child : children) {
			if (child.getClass().equals(filter)) {
				if (shouldMatchFilter) {
					l = l.append(child);
				}
			} else {
				if (!shouldMatchFilter) {
					l = l.append(child);
				}
			}
		}
		return newNaryExpr(l);
	}

	// Making the sorting a function since I didn't want to keep writing it out
	public ImmutableList<Expr> sortChildren(ImmutableList<Expr> children){
        Object[] arr =(children.toArray());
        Arrays.sort(arr);
        ImmutableList<Expr> out = ImmutableList.of();
        for (Object c: arr){
            out = out.append((Expr)c);
        }
        return out;
    }

	public final NaryExpr filter(final Expr filter, final Examiner examiner, final boolean shouldMatchFilter) {
		ImmutableList<Expr> l = ImmutableList.of();
		for (final Expr child : children) {
			if (examiner.examine(child, filter)) {
				if (shouldMatchFilter) {
					l = l.append(child);
				}
			} else {
				if (!shouldMatchFilter) {
					l = l.append(child);
				}
			}
		}
		return newNaryExpr(l);
	}

	public final NaryExpr removeAll(final List<Expr> toRemove, final Examiner examiner) {
		NaryExpr result = this;
		for (final Expr e : toRemove) {
			result = result.filter(e, examiner, false);
		}
		return result;
	}

	public final boolean contains(final Expr expr, final Examiner examiner) {
		for (final Expr child : children) {
			if (examiner.examine(child, expr)) {
				return true;
			}
		}
		return false;
	}

}
