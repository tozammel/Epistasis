package ca.mcgill.pcingola.epistasis.phylotree;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Calculate transition matrix
 * This trivial implementation simply returns exactly the same matrix for any 'time'
 *
 * @author pcingola
 */
public class TransitionMatrix extends Array2DRowRealMatrix {

	private static final long serialVersionUID = 1L;
	protected EigenDecomposition eigen;

	public static final double ACCEPTED_ERROR = 1e-4;
	boolean checkNegativeLambda;

	/**
	 * Load from file
	 * @param fileName
	 */
	public static double[][] load(String fileName) {
		String file = Gpr.readFile(fileName);
		String lines[] = file.split("\n");

		int numRows = lines.length;
		int numCols = lines[0].split("\t").length;
		double d[][] = new double[numRows][numCols];

		int row = 0;
		for (String line : lines) {
			String recs[] = line.split("\t");
			for (int col = 0; col < recs.length; col++)
				d[row][col] = Gpr.parseDoubleSafe(recs[col]);
			row++;
		}

		return d;
	}

	public TransitionMatrix(double matrix[][]) {
		super(matrix);
	}

	public TransitionMatrix(RealMatrix m) {
		super(m.getData());
	}

	public TransitionMatrix(String fileName) {
		super(load(fileName));
	}

	/**
	 * Matrix exponentiation
	 * @param time
	 * @return
	 */
	public RealMatrix exp(double time) {
		// Did we already perform eigendecomposition?
		if (eigen == null) eigen = new EigenDecomposition(this);

		// Exponentiate the diagonal
		RealMatrix D = eigen.getD().copy();
		double maxLambda = Double.NEGATIVE_INFINITY;
		int dim = D.getColumnDimension();
		for (int i = 0; i < dim; i++) {
			double lambda = D.getEntry(i, i);
			maxLambda = Math.max(maxLambda, lambda);
			D.setEntry(i, i, Math.exp(lambda * time));
		}

		if (checkNegativeLambda && maxLambda > ACCEPTED_ERROR) throw new RuntimeException("All eigenvalues should be negative: max(lambda) = " + maxLambda);

		// Perform matrix exponential
		return eigen.getV().multiply(D).multiply(eigen.getVT());
	}

	/**
	 * Matrix log (natural log) times 1/time
	 * @param time
	 * @return
	 */
	public RealMatrix log() {
		// Did we already perform eigendecomposition?
		if (eigen == null) eigen = new EigenDecomposition(this);

		// Exponentiate the diagonal
		RealMatrix D = eigen.getD().copy();
		int dim = D.getColumnDimension();
		for (int i = 0; i < dim; i++) {
			double lambda = D.getEntry(i, i);
			D.setEntry(i, i, Math.log(lambda));
		}

		// Perform matrix exponential
		return eigen.getV().multiply(D).multiply(eigen.getVT());
	}

	public RealMatrix matrix(double time) {
		return this;
	}

	/**
	 * Save data to file
	 * @param fileName
	 * @return
	 */
	public void save(String fileName) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < getRowDimension(); i++) {
			for (int j = 0; j < getColumnDimension(); j++) {
				if (j > 0) sb.append('\t');
				sb.append(getEntry(i, j));
			}
			sb.append('\n');
		}

		Gpr.toFile(fileName, sb.toString());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < getRowDimension(); i++) {
			sb.append("| ");
			for (int j = 0; j < getColumnDimension(); j++) {
				if (j > 0) sb.append(", ");
				double val = getEntry(i, j);
				double aval = Math.abs(val);
				if (aval < 1000000 && aval >= 100000.0) sb.append(String.format("% 6.2f", val));
				else if (aval < 100000 && aval >= 10000.0) sb.append(String.format("% 5.2f ", val));
				else if (aval < 10000 && aval >= 1000.0) sb.append(String.format("% 4.2f  ", val));
				else if (aval < 1000 && aval >= 100.0) sb.append(String.format("% 3.2f   ", val));
				else if (aval < 100 && aval >= 10.0) sb.append(String.format("% 2.2f    ", val));
				else if (aval < 10 && aval >= 1.0) sb.append(String.format("% 1.3f    ", val));
				else if (aval < 1.0 && aval >= 0.01) sb.append(String.format("% 1.3f    ", val));
				else if (aval < 1.0 && aval >= 0.001) sb.append(String.format("% 1.4f   ", val));
				else if (aval < 1.0 && aval >= 0.000001) sb.append(String.format("% 1.6f ", val));
				else if (aval < 1.0 && aval >= 0.0000001) sb.append(String.format("% 1.7f", val));
				else if (val == 0.0) sb.append(String.format(" 0        ", val));
				else sb.append(String.format("% 1.3e", val));
			}
			sb.append(" |\n");
		}

		return sb.toString();
	}

}
