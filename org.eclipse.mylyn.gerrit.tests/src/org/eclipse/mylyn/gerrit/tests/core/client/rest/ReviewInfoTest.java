/*******************************************************************************
 * Copyright (c) 2013 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.gerrit.tests.core.client.rest;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.mylyn.commons.sdk.util.CommonTestUtil;
import org.eclipse.mylyn.internal.gerrit.core.client.JSonSupport;
import org.eclipse.mylyn.internal.gerrit.core.client.rest.ReviewInfo;
import org.junit.Test;

public class ReviewInfoTest extends TestCase {

	@Test
	public void testFromEmptyJson() throws Exception {
		ReviewInfo reviewInfo = parseFile("testdata/EmptyWithMagic.json");

		assertNotNull(reviewInfo);
		assertNull(reviewInfo.getLabels());
	}

	@Test
	public void testFromInvalid() throws Exception {
		ReviewInfo reviewInfo = parseFile("testdata/InvalidWithMagic.json");

		assertNotNull(reviewInfo);
		assertNull(reviewInfo.getLabels());
	}

	@Test
	public void testFromCodeReviewMinusOne() throws Exception {
		ReviewInfo reviewInfo = parseFile("testdata/ReviewInfo_codeReviewMinusOne.json");

		assertNotNull(reviewInfo);
		assertNotNull(reviewInfo.getLabels());
		assertFalse(reviewInfo.getLabels().isEmpty());
		assertNotNull(reviewInfo.getLabels().get("Code-Review"));
		assertEquals(-1, reviewInfo.getLabels().get("Code-Review").shortValue());
	}

	@Test
	public void testFromVerifyZero() throws Exception {
		ReviewInfo reviewInfo = parseFile("testdata/ReviewInfo_verifyZero.json");

		assertNotNull(reviewInfo);
		assertNotNull(reviewInfo.getLabels());
		assertFalse(reviewInfo.getLabels().isEmpty());
		assertNotNull(reviewInfo.getLabels().get("Verify"));
		assertEquals(0, reviewInfo.getLabels().get("Verify").shortValue());
	}

	private ReviewInfo parseFile(String path) throws IOException {
		File file = CommonTestUtil.getFile(this, path);
		String content = CommonTestUtil.read(file);
		return new JSonSupport().parseResponse(content, ReviewInfo.class);
	}

}
