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

import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.UIManager;

/**
 * Class responsibility: Provide methods for interacting with the file system.
 *
 */
public class FileOps {
	// Note: the stopIt boolean and UIManager code lines are used
	// to convince the file dialogs that we don't want to
	// change a file/directory name when it is multiple-clicked and we
	// don't want to see the option... ever.

	public static File saveDialog(String directory) {
		Boolean stopIt = UIManager.getBoolean("FileChooser.readOnly");
		UIManager.put("FileChooser.readOnly", Boolean.TRUE);
		JFileChooser fc = new JFileChooser(directory);
		UIManager.put("FileChooser.readOnly", stopIt);
		fc.showSaveDialog(fc);
		return fc.getSelectedFile();
	}

	public static File openDialog(String directory) {
		Boolean stopIt = UIManager.getBoolean("FileChooser.readOnly");
		UIManager.put("FileChooser.readOnly", Boolean.TRUE);
		JFileChooser fc = new JFileChooser(directory);
		UIManager.put("FileChooser.readOnly", stopIt);
		fc.showOpenDialog(fc);
		return fc.getSelectedFile();
	}

	/**
	 * 
	 * @param file
	 *            a Java.IO.File object
	 * @return s[n] as a string such that each element is a line in the file,
	 *         each element is terminated with "\n", and each comma in each
	 *         element is replaced with "\t".
	 */
	private static String[] openDelimitedText(File file) {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader br = null;
			String sCurrentLine;
			br = new BufferedReader(new FileReader(file.getCanonicalPath()));
			while ((sCurrentLine = br.readLine()) != null) {
				sb.append(sCurrentLine.replaceAll(",", "\t"));
				sb.append("\n");
			}
			br.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		String[] rows = sb.toString().split("\n");
		return rows;
	}

	/**
	 * 
	 * @param file To be explained
	 * @return Opens a tab or comma delimited data set as as a double[][].
	 * @throws Exception To be explained
	 */
	public static double[][] openMatrix(File file) throws Exception {
		try {
			String[] sRows = openDelimitedText(file);
			int m = sRows.length, n;
			List<double[]> lColSets = new ArrayList<double[]>();
			for (int i = 0; i < m; i++) {
				String[] sColSet = sRows[i].split("\t");
				n = sColSet.length;
				double[] elements = new double[n];
				for (int j = 0; j < n; j++) {
					if (StringUtils.isNumeric(sColSet[j])) {
						elements[j] = Double.valueOf(sColSet[j]);
					}
				}
				lColSets.add(elements);
			}
			double[][] matrix = new double[m][];
			for (int i = 0; i < m; i++) {
				matrix[i] = (double[]) lColSets.get(i);
			}
			return matrix;
		} catch (Exception e) {
			throw new Exception("File not in valid format");
		}
	}

	public static void saveCsv(File fileSpec, double[][] A) throws IOException {
		String extAdd = fileSpec.getCanonicalPath() + ".csv";
		fileSpec = new File(extAdd);
		String csv = StringUtils.toCsv(A);
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileSpec));
		writer.write(csv);
		writer.close();
	}

	public static void saveString(String str, File fileSpec) {
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(fileSpec.getCanonicalPath()));
			bw.write(str);
			bw.close();
			bw = null;
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void saveImage(Image img, File file)
			throws FileNotFoundException, IOException {
		ImageIO.write((RenderedImage) img, "png", file);
	}

}
