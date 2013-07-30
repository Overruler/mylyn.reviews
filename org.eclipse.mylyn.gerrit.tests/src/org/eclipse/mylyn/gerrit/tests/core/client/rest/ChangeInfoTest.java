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

import static org.eclipse.mylyn.gerrit.tests.core.client.rest.IsEmpty.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.eclipse.mylyn.commons.sdk.util.CommonTestUtil;
import org.eclipse.mylyn.internal.gerrit.core.client.JSonSupport;
import org.eclipse.mylyn.internal.gerrit.core.client.rest.AccountInfo;
import org.eclipse.mylyn.internal.gerrit.core.client.rest.ApprovalInfo;
import org.eclipse.mylyn.internal.gerrit.core.client.rest.ApprovalUtil;
import org.eclipse.mylyn.internal.gerrit.core.client.rest.ChangeInfo;
import org.eclipse.mylyn.internal.gerrit.core.client.rest.LabelInfo;
import org.eclipse.mylyn.internal.gerrit.core.client.rest.RevisionInfo;
import org.hamcrest.Matchers;
import org.junit.Test;

import com.google.gerrit.common.data.ApprovalDetail;
import com.google.gerrit.common.data.ApprovalType;
import com.google.gerrit.reviewdb.ApprovalCategory.Id;
import com.google.gerrit.reviewdb.ApprovalCategoryValue;
import com.google.gerrit.reviewdb.Change;
import com.google.gerrit.reviewdb.PatchSetApproval;

public class ChangeInfoTest extends TestCase {
	@Test
	public void testFromEmptyJson() throws Exception {
		ChangeInfo changeInfo = parseFile("testdata/EmptyWithMagic.json");

		assertNotNull(changeInfo);
		assertNull(changeInfo.getKind());
		assertNull(changeInfo.getId());
		assertNull(changeInfo.getProject());
		assertNull(changeInfo.getBranch());
		assertNull(changeInfo.getChangeId());
		assertNull(changeInfo.getSubject());
		assertNull(changeInfo.getStatus());
		assertNull(changeInfo.getCreated());
		assertNull(changeInfo.getUpdated());
		assertEquals(false, changeInfo.isReviewed());
		assertEquals(false, changeInfo.isMergeable());
		assertNull(changeInfo.getOwner());
	}

	@Test
	public void testFromInvalid() throws Exception {
		ChangeInfo changeInfo = parseFile("testdata/InvalidWithMagic.json");

		assertNotNull(changeInfo);
		assertNotNull(changeInfo);
		assertNull(changeInfo.getKind());
		assertNull(changeInfo.getId());
		assertNull(changeInfo.getProject());
		assertNull(changeInfo.getBranch());
		assertNull(changeInfo.getChangeId());
		assertNull(changeInfo.getSubject());
		assertNull(changeInfo.getStatus());
		assertNull(changeInfo.getCreated());
		assertNull(changeInfo.getUpdated());
		assertEquals(false, changeInfo.isReviewed());
		assertEquals(false, changeInfo.isMergeable());
		assertNull(changeInfo.getOwner());
	}

	@Test
	public void testFromAbandoned() throws Exception {
		ChangeInfo changeInfo = parseFile("testdata/ChangeInfo_abandoned.json");

		assertNotNull(changeInfo);
		assertEquals(changeInfo.getKind(), "gerritcodereview#change");
		assertEquals(changeInfo.getId(), "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");
		assertEquals(changeInfo.getProject(), "myProject");
		assertEquals(changeInfo.getBranch(), "master");
		assertEquals("I8473b95934b5732ac55d26311a706c9c2bde9940", changeInfo.getChangeId());
		assertEquals("Implementing Feature X", changeInfo.getSubject());
		assertEquals(Change.Status.ABANDONED, changeInfo.getStatus());
		assertEquals(timestamp("2013-02-01 09:59:32.126"), changeInfo.getCreated());
		assertEquals(timestamp("2013-02-21 11:16:36.775"), changeInfo.getUpdated());
		assertEquals(true, changeInfo.isReviewed());
		assertEquals(true, changeInfo.isMergeable());
		AccountInfo changeOwner = changeInfo.getOwner();
		assertNotNull(changeOwner);
		assertEquals("John Doe", changeOwner.getName());
		assertNull(changeOwner.getEmail());
		assertNull(changeOwner.getUsername());
		assertEquals(-1, changeOwner.getId());
	}

	@Test
	public void testNewChangeInfo() {
		ChangeInfo changeInfo = new ChangeInfo();

		assertThat(changeInfo.getLabels(), nullValue());
		// reviews, no labels = no reviews
		assertThat(changeInfo.convertToApprovalDetails(), empty());
		assertThat(changeInfo.convertToApprovalTypes(), nullValue());
		assertThat(changeInfo.getRevisions(), nullValue());
	}

	@Test
	public void testNoReviews() throws Exception {
		ChangeInfo changeInfo = parseFile("testdata/ChangeInfo_NoReviews.json");

		assertHasCodeReviewLabels(changeInfo);
		assertThat(changeInfo.getLabels().get("Code-Review").getAll(), nullValue());
		assertThat(changeInfo.convertToApprovalDetails(), empty());
		assertHasCodeReviewApprovalType(changeInfo.convertToApprovalTypes());
		assertHasRevisions(changeInfo, 1);
	}

	@Test
	public void testCodeReviewMinusOne() throws IOException {
		ChangeInfo changeInfo = parseFile("testdata/ChangeInfo_CodeReviewMinusOne.json");

		assertHasCodeReviewLabels(changeInfo);
		assertHasApprovalInfo(changeInfo.getLabels().get("Code-Review").getAll(), -1);
		assertHasApprovalDetail(changeInfo.convertToApprovalDetails(), -1);
		assertHasCodeReviewApprovalType(changeInfo.convertToApprovalTypes());
		assertHasRevisions(changeInfo, 1);
	}

	@Test
	public void testTwoRevisions() throws IOException {
		ChangeInfo changeInfo = parseFile("testdata/ChangeInfo_TwoRevisions.json");

		assertHasCodeReviewLabels(changeInfo);
		assertHasApprovalInfo(changeInfo.getLabels().get("Code-Review").getAll(), 0);
		assertHasApprovalDetail(changeInfo.convertToApprovalDetails(), 0);
		assertHasCodeReviewApprovalType(changeInfo.convertToApprovalTypes());
		assertHasRevisions(changeInfo, 2);
	}

	// Utility methods

	public static void assertHasCodeReviewLabels(ChangeInfo changeInfo) {
		assertThat(changeInfo, notNullValue());
		Map<String, LabelInfo> labels = changeInfo.getLabels();
		assertThat(labels, not(empty()));
		assertThat(labels.size(), is(1));
		assertThat(labels, Matchers.<String, LabelInfo> hasKey("Code-Review"));
		Map<String, String> values = labels.get("Code-Review").getValues();
		assertThat(values, not(empty()));
		assertThat(values.size(), is(5));
		assertThat(values, Matchers.<String, String> hasKey("-2"));
		assertThat(values, Matchers.<String, String> hasKey("-1"));
		assertThat(values, Matchers.<String, String> hasKey(" 0"));
		assertThat(values, Matchers.<String, String> hasKey("+1"));
		assertThat(values, Matchers.<String, String> hasKey("+2"));
		assertThat(values.get("-2"), equalTo(ApprovalUtil.CRVW.getValue((short) -2).getName()));
		assertThat(values.get("-1"), equalTo(ApprovalUtil.CRVW.getValue((short) -1).getName()));
		assertThat(values.get(" 0"), equalTo(ApprovalUtil.CRVW.getValue((short) 0).getName()));
		assertThat(values.get("+1"), equalTo(ApprovalUtil.CRVW.getValue((short) 1).getName()));
		assertThat(values.get("+2"), equalTo(ApprovalUtil.CRVW.getValue((short) 2).getName()));
	}

	private static Timestamp timestamp(String date) throws ParseException {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		cal.setTime(sdf.parse(date));
		cal.add(Calendar.MILLISECOND, TimeZone.getDefault().getRawOffset());
		return new Timestamp(cal.getTimeInMillis());
	}

	private ChangeInfo parseFile(String path) throws IOException {
		File file = CommonTestUtil.getFile(this, path);
		String content = CommonTestUtil.read(file);
		return new JSonSupport().parseResponse(content, ChangeInfo.class);
	}

	private static void assertHasCodeReviewApprovalType(Set<ApprovalType> approvalTypes) {
		assertThat(approvalTypes, not(empty()));
		assertThat(approvalTypes.size(), is(1));
		ApprovalType approvalType = approvalTypes.iterator().next();
		assertThat(approvalType.getCategory().getId(), equalTo(ApprovalUtil.CRVW.getCategory().getId()));
		assertThat(approvalType.getCategory().getName(), is("Code Review"));
		assertThat(approvalType.getValues().size(), is(5));
		for (ApprovalCategoryValue approvalCategoryValue : ApprovalUtil.CRVW.getValues()) {
			assertHasItem(approvalType.getValues(), ApprovalCategoryValueComparator.INSTANCE, approvalCategoryValue);
		}
	}

	private static void assertHasApprovalInfo(List<ApprovalInfo> all, int value) {
		assertThat(all, not(empty()));
		assertThat(all.size(), is(1));
		ApprovalInfo approvalInfo = all.get(0);
		assertThat(approvalInfo, notNullValue());
		assertThat(approvalInfo.getValue(), is((short) value));
		assertThat(approvalInfo.getEmail(), equalTo("tests@mylyn.eclipse.org"));
	}

	private static void assertHasApprovalDetail(Set<ApprovalDetail> approvalDetails, int value) {
		assertThat(approvalDetails, not(empty()));
		assertThat(approvalDetails.size(), is(1));
		ApprovalDetail approvalDetail = approvalDetails.iterator().next();
		assertThat(approvalDetail, notNullValue());
		Map<Id, PatchSetApproval> approvalMap = approvalDetail.getApprovalMap();
		assertThat(approvalMap, notNullValue());
		assertThat(approvalMap, Matchers.<Id, PatchSetApproval> hasKey(ApprovalUtil.CRVW.getCategory().getId()));
		PatchSetApproval patchSetApproval = approvalMap.get(ApprovalUtil.CRVW.getCategory().getId());
		assertThat(patchSetApproval.getValue(), is((short) value));
	}

	private static void assertHasRevisions(ChangeInfo changeInfo, int patchSetNr) {
		assertThat(changeInfo, notNullValue());
		String currentRevision = changeInfo.getCurrentRevision();
		assertThat(currentRevision, notNullValue());
		Map<String, RevisionInfo> revisions = changeInfo.getRevisions();
		assertThat(revisions, not(empty()));
		assertThat(revisions.size(), is(1));
		RevisionInfo currentRevisionInfo = revisions.get(currentRevision);
		assertThat(currentRevisionInfo, notNullValue());
		assertThat(currentRevisionInfo.isDraft(), is(false));
		assertThat(currentRevisionInfo.getNumber(), is(patchSetNr));
	}

	private static <T> void assertHasItem(Collection<T> collection, Comparator<T> comparator, T itemToFind) {
		for (T item : collection) {
			if (comparator.compare(item, itemToFind) == 0) {
				return;
			}
		}
		fail("Item " + itemToFind + " not found in " + collection);
	}

	private static class ApprovalCategoryValueComparator implements Comparator<ApprovalCategoryValue> {
		private final static Comparator<ApprovalCategoryValue> INSTANCE = new ApprovalCategoryValueComparator();

		public int compare(ApprovalCategoryValue acv1, ApprovalCategoryValue acv2) {
			return acv1.format().compareTo(acv2.format());
		}
	}
}