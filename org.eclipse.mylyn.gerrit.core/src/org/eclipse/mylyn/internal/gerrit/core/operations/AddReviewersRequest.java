/*******************************************************************************
 * Copyright (c) 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.gerrit.core.operations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.internal.gerrit.core.client.GerritClient;
import org.eclipse.mylyn.internal.gerrit.core.client.GerritException;

import com.google.gerrit.common.data.ReviewerResult;

/**
 * @author Steffen Pingel
 */
public class AddReviewersRequest extends AbstractRequest<ReviewerResult> {

	private final List<String> reviewers;

	private final String reviewId;

	public AddReviewersRequest(String reviewId, List<String> reviewers) {
		Assert.isNotNull(reviewId);
		Assert.isNotNull(reviewers);
		this.reviewId = reviewId;
		this.reviewers = Collections.unmodifiableList(new ArrayList<String>(reviewers));
	}

	public List<String> getReviewers() {
		return reviewers;
	}

	public String getReviewId() {
		return reviewId;
	}

	@Override
	protected ReviewerResult execute(GerritClient client, IProgressMonitor monitor) throws GerritException {
		return client.addReviewers(getReviewId(), getReviewers(), monitor);
	}

	@Override
	public String getOperationName() {
		return Messages.GerritOperation_Adding_Reviewers;
	}

}
