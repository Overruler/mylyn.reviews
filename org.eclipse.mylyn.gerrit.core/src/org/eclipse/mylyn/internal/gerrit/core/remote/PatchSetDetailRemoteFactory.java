/*******************************************************************************
 * Copyright (c) 2013 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Miles Parker, Tasktop Technologies - initial API and implementation
 *     Steffen Pingel, Tasktop Technologies - original GerritUtil implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.gerrit.core.remote;

import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.internal.gerrit.core.client.GerritChange;
import org.eclipse.mylyn.reviews.core.model.IRepository;
import org.eclipse.mylyn.reviews.core.model.IReview;
import org.eclipse.mylyn.reviews.core.model.IReviewItemSet;
import org.eclipse.mylyn.reviews.core.model.IReviewsFactory;
import org.eclipse.mylyn.reviews.core.spi.remote.emf.RemoteEmfConsumer;
import org.eclipse.mylyn.reviews.core.spi.remote.review.ReviewItemSetRemoteFactory;
import org.eclipse.osgi.util.NLS;

import com.google.gerrit.common.data.PatchSetDetail;
import com.google.gerrit.reviewdb.PatchSet;

/**
 * Converts patch set details to review sets. Does not retrive actual patch set content. Does not require a remote
 * invocation, as the neccesary data is collected as part of {@link GerritReviewRemoteFactory} API call.
 * 
 * @author Miles Parker
 * @author Steffen Pingel
 */
public class PatchSetDetailRemoteFactory extends ReviewItemSetRemoteFactory<PatchSetDetail, PatchSetDetail> {

	public PatchSetDetailRemoteFactory(GerritRemoteFactoryProvider gerritRemoteFactoryProvider) {
		super(gerritRemoteFactoryProvider);
	}

	@Override
	public PatchSetDetail pull(IReview parent, PatchSetDetail remoteKey, IProgressMonitor monitor) throws CoreException {
		return remoteKey;
	}

	@Override
	public void push(PatchSetDetail remoteObject, IProgressMonitor monitor) throws CoreException {
		//noop, push not supported in Gerrit
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}

	@Override
	public boolean isPullNeeded(IReview parent, IReviewItemSet object, PatchSetDetail remote) {
		return object == null || remote == null;
	}

	@Override
	public IReviewItemSet createModel(IReview review, PatchSetDetail patchSetDetail) {
		PatchSet patchSet = patchSetDetail.getPatchSet();
		IReviewItemSet itemSet = IReviewsFactory.INSTANCE.createReviewItemSet();
		itemSet.setName(NLS.bind("Patch Set {0}", patchSet.getPatchSetId()));
		itemSet.setCreationDate(patchSet.getCreatedOn());
		itemSet.setId(patchSet.getPatchSetId() + "");
		itemSet.setReference(patchSet.getRefName());
		itemSet.setRevision(patchSet.getRevision().get());
		review.getSets().add(itemSet);
		return itemSet;
	}

	@Override
	public PatchSetDetail getRemoteKey(PatchSetDetail remoteObject) {
		return remoteObject;
	}

	@Override
	public String getLocalKeyForRemoteObject(PatchSetDetail remoteObject) {
		return remoteObject.getPatchSet().getPatchSetId() + "";
	}

	@Override
	public String getLocalKeyForRemoteKey(PatchSetDetail remoteKey) {
		return getLocalKeyForRemoteObject(remoteKey);
	}

	@Override
	public PatchSetDetail getRemoteObjectForLocalKey(IReview parentObject, String localKey) {
		GerritReviewRemoteFactory reviewFactory = ((GerritRemoteFactoryProvider) getFactoryProvider()).getReviewFactory();
		RemoteEmfConsumer<IRepository, IReview, String, GerritChange, String, Date> reviewConsumer = reviewFactory.getConsumerForModel(
				parentObject.getRepository(), parentObject);
		try {
			int index = Integer.parseInt(localKey) - 1;
			if (reviewConsumer != null) {
				GerritChange change = reviewConsumer.getRemoteObject();
				if (change != null) {
					if (change.getPatchSetDetails().size() > index) {
						return change.getPatchSetDetails().get(index);
					}
				}
			}
		} catch (NumberFormatException e) {
			//ignore;
		} finally {
			if (reviewConsumer != null) {
				reviewConsumer.release();
			}
		}
		return null;
	}
}
