package gr.iti.mklab.reveal.forensics.util.dwt;

/**
 * Copyright 2014 Mark Bishop This program is free software: you can
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

import java.util.ArrayList;

/**
 * Class responsibility: Provide methods for string csv conversions.
 *
 */

public class StringUtils {

	public static String toCsv(int[] v) {
		int n = v.length;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) {
			sb.append(v[i]);
			if (i < n - 1) {
				sb.append(",");
			}
		}
		sb.append("\n");
		return sb.toString();
	}

	public static String toCsv(double[] v) {
		int n = v.length;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) {
			sb.append(v[i]);
			sb.append("\n");
		}
		return sb.toString();
	}

	public static String toCsv(ArrayList<double[]> columnVectors) {
		StringBuilder sb = new StringBuilder();
		int n = columnVectors.size();
		int m = columnVectors.get(0).length;

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				sb.append(columnVectors.get(j)[i]);
				if (j < n - 1) {
					sb.append(",");
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public static String toCsv(double[][] A) {
		int m = A.length;
		int n = A[0].length;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				sb.append(A[i][j]);
				if (j < n - 1) {
					sb.append(",");
				}
			}
			sb.append("\n");
		}
		String result = sb.toString();
		return result;
	}

	/**
	 * 
	 * @param str
	 *            a string
	 * @return True if pattern is regex match "-?\\d+(\\.\\d+)?(E-?\\d+)?" i.e.
	 *         a number with optional '-', decimal point or E+/-.
	 */
	public static boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?(E-?\\d+)?");
	}
}