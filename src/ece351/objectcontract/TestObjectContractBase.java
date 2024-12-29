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

package ece351.objectcontract;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

/**
 * Some basic controls for the object contract tests.
 * Do not edit this file: edit the subclass instead.
 */
public abstract class TestObjectContractBase {

	/**
	 * Use java.lang.String to verify that
	 * the property check methods work properly.
	 */
	@Test
	public void testString() {
		assertTrue(checkNotEqualsNull("foo"));

		assertTrue(checkEqualsIsReflexive("foo"));

		assertTrue(checkEqualsIsSymmetric("foo", "foo"));
		assertTrue(checkEqualsIsSymmetric("foo", "bar"));
		
		assertTrue(checkEqualsIsTransitive("foo", "foo", "foo"));
		assertFalse(checkEqualsIsTransitive("foo", "foo", "bar"));
		assertFalse(checkEqualsIsTransitive("foo", "bar", "foo"));
		
		assertTrue(checkHashcodeIsConsistent("foo", "foo"));
		assertTrue(checkHashcodeIsConsistent("foo", "bar"));
	}

	/**
	 * Use java.lang.Integer to verify that
	 * the property check methods work.
	 */
	@Test
	public void testInteger() {
		// construct 
		final Integer a = new Integer(-1);
		final Integer b = new Integer(0);
		final Integer c = new Integer(1);
	
		// reflexivity
		assertTrue(checkEqualsIsReflexive(a));
		assertTrue(checkEqualsIsReflexive(b));
		assertTrue(checkEqualsIsReflexive(c));
	
		// symmetry
		assertTrue(checkEqualsIsSymmetric(a,b));
		assertTrue(checkEqualsIsSymmetric(b,c));
		assertTrue(checkEqualsIsSymmetric(a,c));
	
		// transitivity
		assertFalse(checkEqualsIsTransitive(a,b,c));
		assertTrue(checkEqualsIsTransitive(a,a,a));
		assertTrue(checkEqualsIsTransitive(b,b,b));
		assertTrue(checkEqualsIsTransitive(c,c,c));
		
		assertTrue(checkEqualsIsTransitive(a, new Integer(a), new Integer(a)));
		assertTrue(checkEqualsIsTransitive(b, new Integer(b), new Integer(b)));
		assertTrue(checkEqualsIsTransitive(c, new Integer(c), new Integer(c)));
		
		// hashcode consistency
		assertTrue(checkHashcodeIsConsistent(a, b));
		assertTrue(checkHashcodeIsConsistent(a, c));
		assertTrue(checkHashcodeIsConsistent(b, c));
		assertTrue(checkHashcodeIsConsistent(a, a));
		assertTrue(checkHashcodeIsConsistent(b, b));
		assertTrue(checkHashcodeIsConsistent(c, c));
	}

	/**
	 * Use random java.lang.Integer objects to verify
	 * that the property check methods work.
	 */
	@Test
	public void testIntegerRandom() {
		final Random random = new Random();
		for (int i = 0; i < 1000000; i++) {
			final Integer a = random.nextInt();
			final Integer b = random.nextInt();
			final Integer c = random.nextInt();
	
			// reflexivity
			assertTrue(checkEqualsIsReflexive(a));
			assertTrue(checkEqualsIsReflexive(b));
			assertTrue(checkEqualsIsReflexive(c));
	
			// symmetry
			assertTrue(checkEqualsIsSymmetric(a,b));
			assertTrue(checkEqualsIsSymmetric(b,c));
			assertTrue(checkEqualsIsSymmetric(a,c));
			assertTrue(checkEqualsIsSymmetric(a,a));
			assertTrue(checkEqualsIsSymmetric(b,b));
			assertTrue(checkEqualsIsSymmetric(c,c));
	
			// transitivity
			assertTrue(checkEqualsIsTransitive(a,a,a));
			assertTrue(checkEqualsIsTransitive(b,b,b));
			assertTrue(checkEqualsIsTransitive(c,c,c));
			// small chance that this line fails erroneously
			// if all three random ints are in fact equals
			assertFalse(checkEqualsIsTransitive(a,b,c)); 
			
			assertTrue(checkEqualsIsTransitive(a, new Integer(a), new Integer(a)));
			assertTrue(checkEqualsIsTransitive(b, new Integer(b), new Integer(b)));
			assertTrue(checkEqualsIsTransitive(c, new Integer(c), new Integer(c)));
			
			// hashcode consistency
			assertTrue(checkHashcodeIsConsistent(a, b));
			assertTrue(checkHashcodeIsConsistent(a, c));
			assertTrue(checkHashcodeIsConsistent(b, c));
			assertTrue(checkHashcodeIsConsistent(a, a));
			assertTrue(checkHashcodeIsConsistent(b, b));
			assertTrue(checkHashcodeIsConsistent(c, c));
		}
	}

	/**
	 * Use the simple control objects to verify that
	 * the property check methods work properly.
	 * We need to observe cases where the property check methods
	 * are expected to return false to know that they really work.
	 * Otherwise they could be implemented as 'return true'.
	 */
	@Test
	public void testSimpleControls() {
		final Object t = constructAlwaysTrue();
		final Object f = constructAlwaysFalse();

		assertFalse(checkNotEqualsNull(t));
	
		assertFalse(checkEqualsIsReflexive(f));
		assertTrue(checkEqualsIsReflexive(t));
	
		final Object toggler = constructToggler();
		assertFalse(checkEqualsIsSymmetric(toggler, toggler));
	}

	@Test
	public void testHashcodeConsistency() {
		final Object[] o = constructHashcodeConsistencyViolators();
		// check that the returned array is sane
		assertEquals(o.length, 2);
		assertNotNull(o[0]);
		assertNotNull(o[1]);
		// check that hashcode() is not consistent with equals
		assertFalse(checkHashcodeIsConsistent(o[0], o[1]));
	}
	
	/**
	 * Use the SymmetryBreaker control objects to verify
	 * that the property check methods work properly.
	 */
	@Test
	public void testSymmetryBreakerControl() {
		// ask students to construct the symmetry breaker objects
		final SymmetryBreaker[] b = constructSymmetryBreakers();
		// check that the returned array is sane
		assertEquals(b.length, 2);
		assertNotNull(b[0]);
		assertNotNull(b[1]);
		// check that symmetry is violated
		assertTrue(b[0].equals(b[1]));
		assertFalse(b[1].equals(b[0])); // symmetry is violated
		assertFalse(checkEqualsIsSymmetric(b[0], b[1])); // verify the property check method
	}

	/**
	 * Use the TransitivityBreaker control objects to verify
	 * that the property check methods work properly.
	 */
	@Test
	public void testTransitivityBreakerControl() {
		// ask students to construct the transitivity breaker objects
		final TransitivityBreaker[] b = constructTransitivityBreakers();
		// check that the returned array is sane
		assertEquals(b.length, 3);
		assertNotNull(b[0]);
		assertNotNull(b[1]);
		assertNotNull(b[2]);
		// check that transitivity is broken
		assertTrue(b[0].equals(b[1]));
		assertTrue(b[1].equals(b[2]));
		assertFalse(b[0].equals(b[2]));
		assertFalse(checkEqualsIsTransitive(b[0], b[1], b[2]));
	}
	
	/**
	 * A control object designed to break transitivity.
	 */
	static class TransitivityBreaker {
		final static double epsilon = 0.0000001;
		final double state;
		TransitivityBreaker(final double state) {
			this.state = state;
		}
		/**
		 * Returns true if the values are within epsilon of each other.
		 */
		@Override
		public boolean equals(final Object obj) {
			if (!(obj instanceof TransitivityBreaker)) return false;
			final TransitivityBreaker that = (TransitivityBreaker) obj;
			final double diff = Math.abs(this.state - that.state);
			return diff < epsilon;
		}
	}

	/**
	 * A control object designed to break symmetry.
	 */
	protected static class SymmetryBreaker {
		private final int state;
		private final SymmetryBreaker contrarian;
		SymmetryBreaker(final int state, final SymmetryBreaker contrarian) {
			this.state = state;
			this.contrarian = contrarian;
		}
		@Override
		public boolean equals(final Object obj) {
			if (!(obj instanceof SymmetryBreaker)) return false;
			final SymmetryBreaker that = (SymmetryBreaker) obj;
			if (that == contrarian) return false;
			return this.state == that.state;
		}
	}
	
	/*
	 * These methods are to be implemented by the student in the subclass. 
	 */
	abstract Object constructAlwaysFalse();
	abstract Object constructAlwaysTrue();
	abstract Object constructToggler();

	abstract SymmetryBreaker[] constructSymmetryBreakers();
	abstract TransitivityBreaker[] constructTransitivityBreakers();
	abstract Object[] constructHashcodeConsistencyViolators();
	
	abstract boolean checkNotEqualsNull(final Object obj);
	abstract boolean checkEqualsIsReflexive(final Object obj);
	abstract boolean checkEqualsIsSymmetric(final Object o1, final Object o2);
	abstract boolean checkEqualsIsTransitive(final Object o1, final Object o2, final Object o3);
	abstract boolean checkHashcodeIsConsistent(final Object o1, final Object o2);


}
