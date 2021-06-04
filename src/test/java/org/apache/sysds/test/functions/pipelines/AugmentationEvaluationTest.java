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
package org.apache.sysds.test.functions.pipelines;

import org.apache.sysds.runtime.matrix.data.MatrixValue;
import org.apache.sysds.test.AutomatedTestBase;
import org.apache.sysds.test.TestConfiguration;
import org.apache.sysds.test.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class AugmentationEvaluationTest extends AutomatedTestBase {
	private final static String TEST_NAME = "evaluate_augmentation_pipeline";
	private final static String TEST_DIR = "functions/builtin/";
	private final static String TEST_CLASS_DIR = TEST_DIR + AugmentationEvaluationTest.class.getSimpleName() + "/";

	@Override
	public void setUp() {
		addTestConfiguration(TEST_NAME, new TestConfiguration(TEST_CLASS_DIR, TEST_NAME, new String[] {"B"}));
	}

	@Test
	public void testEvaluationNoRuntimeExceptions() throws Exception {
		loadTestConfiguration(getTestConfiguration(TEST_NAME));
		final int w = 5, h = 3, n = 100;
		double[][] X_train = getRandomMatrix(n, w*h, 0, 255, 1, 7);
		double[][] y_train = getRandomMatrix(n, 1, 0, 1, 1, 11);
		double[][] X_test = getRandomMatrix(n/10, w*h, 0, 255, 1, 13);
		double[][] y_test = getRandomMatrix(n/10, 1, 0, 1, 1, 19);
		double[][] pipeline = new double[][] {
				{1, 0.50, 0.20, 2, 0.75, 0.40, 3, 0.80, 0.60},
				{2, 0.10, 0.80, 1, 0.30, 0.10, 0, 0.00, 0.00}
		};

		String HOME = SCRIPT_DIR + TEST_DIR;
		fullDMLScriptName = HOME + TEST_NAME + ".dml";
		programArgs = new String[] {
				"-nvargs",
				"pipeline=" + input("A"),
				"X_train=" + input("B"),
				"y_train=" + input("C"),
				"X_test=" + input("D"),
				"y_test=" + input("E"),
				"width=" + w,
				"height=" + h,
				"n_train=" + n,
				"n_test=" + n/10,
				"n_policies=" + pipeline.length,
				"n_transforms=" + pipeline[0].length,
				"out_file=" + output("F")
		};

		writeInputMatrixWithMTD("A", pipeline, false);
		writeInputMatrixWithMTD("B", X_train, false);
		writeInputMatrixWithMTD("C", y_train, false);
		writeInputMatrixWithMTD("D", X_test, false);
		writeInputMatrixWithMTD("E", y_test, false);
		runTest(true, false, null, -1);

		HashMap<MatrixValue.CellIndex, Double> dmlfile = readDMLScalarFromOutputDir("F");

		Assert.assertEquals(1, dmlfile.keySet().size());
		double value = dmlfile.values().stream().findFirst().get();
		Assert.assertTrue(0 <= value && value <= 1);
	}
}
