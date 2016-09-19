package gr.iti.mklab.reveal.forensics.util.dwt;

/**
 * Copyright 2009 Mark Bishop This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details: http://www.gnu.org/licenses
 * 
 * The author makes no warranty for the accuracy, completeness, safety, or
 * usefulness of any information provided and does not represent that its use
 * would not infringe privately owned right.
 */
/**
 * 
 * Reference: Golub, Gene H., Charles F. Van Loan, Matrix Computations 3rd
 * Edition, The Johns Hopkins University Press, Baltimore, MD, 1996.
 * 
 * 
 * Class responsibility: Provide methods for basic vector and matrix
 * computations. Note: only a few of these methods are used in the DWT demo.
 * 
 * @author mark bishop
 *
 */
public class MatrixOps {

	/**
	 * 
	 * @return gives an upper bound on the relative error due to rounding in
	 *         floating point arithmetic
	 */
	public static double machineEpsilonDouble() {
		double eps = 1.0;
		do
			eps /= 2.0;
		while ((1.0 + (eps / 2.0)) != 1.0);
		return eps;
	}

	/**
	 * 
	 * @param n To be explained
	 * @return Identity matrix I[n,n]
	 */
	public static double[][] eye(int n) {
		double[][] eye = new double[n][n];
		for (int i = 0; i < n; i++) {
			eye[i][i] = 1.0;
		}
		return eye;
	}

	public static double[] getColumnAsVector(double[][] A, int colIndex) {
		int m = A.length;
		double[] col = new double[m];
		for (int i = 0; i < m; i++) {
			col[i] = A[i][colIndex];
		}
		return col;
	}

	public static double[] getRowAsVector(double[][] A, int rowIndex) {
		int n = A[0].length;
		System.out.print(A.length + "\n");
		System.out.print(A[0].length + "\n");
		double[] col = new double[n];
		for (int i = 0; i < n; i++) {
			col[i] = A[rowIndex][i];
		}
		return col;
	}

	public static double[][] vectorToRowMatrix(double[] v) {
		int n = v.length;
		double[][] row = new double[1][n];
		for (int i = 0; i < n; i++) {
			row[0][i] = v[i];
		}
		return row;
	}

	public static double[][] vectorToColumnMatrix(double[] v) {
		int n = v.length;
		double[][] row = new double[n][1];
		for (int i = 0; i < n; i++) {
			row[i][0] = v[i];
		}
		return row;
	}

	public static double[] deepCopy(double[] v) {
		int n = v.length;
		double[] copy = new double[n];
		for (int j = 0; j < n; j++) {
			copy[j] = v[j];
		}
		return copy;
	}

	public static double[][] deepCopy(double[][] A) {
		int m = A.length;
		int n = A[0].length;
		double[][] copy = new double[m][n];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				copy[i][j] = A[i][j];
			}
		}
		return copy;
	}

	public static double[][] transpose(double[][] A) {
		int m = A.length;
		int n = A[0].length;
		double[][] AT = new double[n][m];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				AT[i][j] = A[j][i];
			}
		}
		return AT;
	}

	public static double[][] scale(double alpha, double[][] A) {
		int m = A.length;
		int n = A[0].length;
		double[][] alphaA = new double[m][n];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				alphaA[i][j] = alpha * A[i][j];
			}
		}
		return alphaA;
	}

	public static double[] scale(double alpha, double[] v) {
		int n = v.length;
		double[] alphaV = new double[n];
		for (int i = 0; i < n; i++) {
			alphaV[i] = alpha * v[i];
		}
		return alphaV;
	}

	public static double[][] add(double[][] A, double[][] B) {
		int m = A.length;
		int n = A[0].length;
		double[][] C = new double[m][n];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				C[i][j] = (A[i][j] + B[i][j]);
			}
		}
		return C;
	}

	public static double[] add(double[] v1, double[] v2) {
		int n = v1.length;
		double[] v1Pv2 = new double[n];
		for (int i = 0; i < n; i++) {
			v1Pv2[i] = v1[i] + v2[i];
		}
		return v1Pv2;
	}

	public static double[][] multiply(double[][] A, double[][] B) {
		int m = A.length;
		int p = A[0].length;
		int n = B[0].length;
		double[][] C = new double[m][n];
		for (int i = 0; i < m; i++) {
			for (int k = 0; k < p; k++) {
				for (int j = 0; j < n; j++) {
					C[i][j] = C[i][j] + A[i][k] * B[k][j];
				}
			}
		}
		return C;
	}

	public static double[] multiply(double[][] A, double[] b) {
		int m = A.length;
		int n = A[0].length;
		double[] v = new double[m];
		for (int k = 0; k < n; k++) {
			for (int i = 0; i < m; i++) {
				v[i] += A[i][k] * b[k];
			}
		}
		return v;
	}

	public static double[][] hadamard(double[][] A, double[][] B) {
		int m = A.length;
		int n = A[0].length;
		double[][] C = new double[m][n];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				C[i][j] = A[i][j] * B[i][j];
			}
		}
		return C;
	}

	public static double[] hadamard(double[] v1, double[] v2) {
		int m = v1.length;
		double[] c = new double[m];
		for (int i = 0; i < m; i++) {
			c[i] = v1[i] * v2[i];
		}
		return c;
	}

	public static double vectorInnerProduct(double[] v1, double[] v2) {
		int n = v1.length;
		double ab = 0;
		for (int i = 0; i < n; i++) {
			ab += v1[i] * v2[i];
		}
		return ab;
	}

	public static double[][] vectorTensorProduct(double[] v1, double[] v2) {
		int m = v1.length;
		int n = v2.length;
		double[][] c = new double[m][n];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				c[i][j] += v1[i] * v2[j];
			}
		}
		return c;
	}

	/**
	 * 
	 * @param A
	 *            A(m,n)
	 * @return maximum row sum
	 */
	public static double matrixInfinityNorm(double[][] A) {
		int m = A.length;
		double lFinity = 0;
		for (int i = 0; i < m; i++) {
			double vIN = vectorInfinityNorm(A[i]);
			if (vIN > lFinity)
				lFinity = vIN;
		}
		return lFinity;
	}

	public static double vector2Norm(double[] v) {
		int n = v.length;
		double norm = 0;
		for (int i = 0; i < n; i++) {
			norm += Math.pow(v[i], 2);
		}
		norm = Math.pow(norm, 0.5);
		return norm;
	}

	public static double matrixFNorm(double[][] A) {
		int m = A.length;
		int n = A[0].length;
		double norm = 0;
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				norm += (Math.pow(A[i][j], 2));
			}
		}
		norm = Math.pow(norm, 0.5);
		return norm;
	}

	/**
	 * 
	 * @param v
	 *            vector v[n] as double[]
	 * @return InfinityNorm(v) ... absolute value of highest magnitude element
	 */
	public static double vectorInfinityNorm(double[] v) {
		double lFinity = 0;
		int n = v.length;
		lFinity = Math.abs(v[0]);
		for (int i = 1; i < n; i++) {
			double abs = Math.abs(v[i]);
			if (abs > lFinity) {
				lFinity = abs;
			}
		}
		return lFinity;
	}

	/**
	 * Golub, Algorithm 3.1.2
	 * 
	 * @param A
	 *            A[m,n] where A is upper triangular
	 * @param b
	 *            b[m]
	 * @return x[n] where Ax = b
	 */
	public static double[] backCalculateX(double[][] upperTriangularMatrix,
			double[] b) {
		int n = upperTriangularMatrix[0].length;
		b[n - 1] = b[n - 1] / upperTriangularMatrix[n - 1][n - 1];
		for (int i = n - 2; i >= 0; i += -1) {
			double temp = 0;
			for (int j = n - 1; j >= i + 1; j += -1) {
				temp = temp - upperTriangularMatrix[i][j] * b[j];
			}
			temp = temp + b[i];
			b[i] = temp / upperTriangularMatrix[i][i];
		}
		return b;
	}

	/**
	 * 
	 * @param x
	 *            x(m)
	 * @param order To be explained
	 * @return V(m, order+1)such that V(i,j) = x(i)^j
	 */
	public static double[][] createVandermonde(double[] x, int order) {
		int m = x.length;
		int n = order;
		double[][] V = new double[m][n + 1];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j <= n; j++) {
				V[i][j] = Math.pow(x[i], (double) j);
			}
		}
		return V;
	}
}
