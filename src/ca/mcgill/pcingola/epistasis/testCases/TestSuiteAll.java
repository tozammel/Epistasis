package ca.mcgill.pcingola.epistasis.testCases;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Invoke all test cases
 *
 * @author pcingola
 */
public class TestSuiteAll {

	public static boolean compareCdsTestsEnable = false;

	public static void main(String args[]) {
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		suite.addTestSuite(TestCaseMutualInformation.class);
		suite.addTestSuite(TestCasesPhylo.class);
		suite.addTestSuite(TestCasesPhyloLikelihood.class);
		suite.addTestSuite(TestTransitionMatrix.class);

		return suite;
	}
}