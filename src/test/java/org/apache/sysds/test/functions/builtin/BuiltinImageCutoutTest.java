/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sysds.test.functions.builtin;

import org.apache.sysds.common.Types.ExecMode;
import org.apache.sysds.lops.LopProperties.ExecType;
import org.apache.sysds.runtime.matrix.data.MatrixValue;
import org.apache.sysds.test.AutomatedTestBase;
import org.apache.sysds.test.TestConfiguration;
import org.apache.sysds.test.TestUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Random;

public class BuiltinImageCutoutTest extends AutomatedTestBase {
	private final static String TEST_NAME = "image_cutout";
	private final static String TEST_DIR = "functions/builtin/";
	private final static String TEST_CLASS_DIR = TEST_DIR + BuiltinImageCutoutTest.class.getSimpleName() + "/";

	private final static double eps = 1e-10;
	private final static double spSparse = 0.1;
	private final static double spDense = 0.9;
	private final static Random random = new Random();

	@Override public void setUp() {
		addTestConfiguration(TEST_NAME, new TestConfiguration(TEST_CLASS_DIR, TEST_NAME, new String[] {"B"}));
	}

	@Test public void testImageTranslateMatrixDenseCP() { runImageCutoutTest(false, ExecType.CP); }

	@Test public void testImageTranslateMatrixSparseCP() {
		runImageCutoutTest(true, ExecType.CP);
	}

	@Test public void testImageTranslateMatrixDenseSP() {
		runImageCutoutTest(false, ExecType.SPARK);
	}

	@Test public void testImageTranslateMatrixSparseSP() {
		runImageCutoutTest(false, ExecType.SPARK);
	}

	private void runImageCutoutTest(boolean sparse, ExecType instType) {
		ExecMode platformOld = setExecMode(instType);
		disableOutAndExpectedDeletion();

		int rows = random.nextInt(1000) + 1;
		int cols = random.nextInt(1000) + 1;
		int x = random.nextInt(cols);
		int y = random.nextInt(rows);
		int width = random.nextInt(cols - x) + 1;
		int height = random.nextInt(rows - y) + 1;
		int fill_color = random.nextInt(256);

		try {
			loadTestConfiguration(getTestConfiguration(TEST_NAME));
			double sparsity = sparse ? spSparse : spDense;

			String HOME = SCRIPT_DIR + TEST_DIR;
			fullDMLScriptName = HOME + TEST_NAME + ".dml";
			programArgs = new String[] {"-nvargs", "in_file=" + input("A"), "out_file=" + output("B"), "width=" + cols,
				"height=" + rows, "x=" + (x+1), "y=" + (y+1), "w=" + width, "h=" + height, "fill_color=" + fill_color};

			//generate actual dataset
			double[][] A = getRandomMatrix(rows, cols, 0, 255, sparsity, 7);
			writeInputMatrixWithMTD("A", A, true);

			double[][] ref = new double[rows][cols];
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					ref[i][j] = A[i][j];
					if (y <= i && i < y + height && x <= j && j < x + width) {
						ref[i][j] = fill_color;
					}
				}
			}

			runTest(true, false, null, -1);

			//compare matrices
			HashMap<MatrixValue.CellIndex, Double> dmlfile = readDMLMatrixFromOutputDir("B");
			double[][] dml_res = TestUtils.convertMatrix(dmlfile, rows, cols);
			TestUtils.compareMatrices(ref, dml_res, eps, "Java vs. DML");
		}
		finally {
			rtplatform = platformOld;
		}
	}
}
