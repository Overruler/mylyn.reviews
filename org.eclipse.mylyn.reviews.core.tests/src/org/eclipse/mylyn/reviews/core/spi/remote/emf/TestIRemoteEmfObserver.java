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

package org.eclipse.mylyn.reviews.core.spi.remote.emf;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;

final class TestIRemoteEmfObserver<P extends EObject, T, L, C> implements IRemoteEmfObserver<P, T, L, C> {

	static final int TEST_TIMEOUT = 100;

	T createdObject;

	int updated;

	int responded;

	int sent;

	IStatus failure;

	AbstractRemoteEmfFactory<P, T, ?, ?, L, C> factory;

	TestIRemoteEmfObserver(AbstractRemoteEmfFactory<P, T, ?, ?, L, C> factory) {
		this.factory = factory;
	}

	public void created(P object, T child) {
		createdObject = child;
	}

	public void updating(P parent, T object) {
	}

	public void sent(P parentObject, T modelObject) {
		responded++;
		sent++;
	}

	@Override
	public void sending(P parentObject, T modelObject) {
	}

	public void updated(P object, T child, boolean modified) {
		responded++;
		if (modified) {
			updated++;
		}
	}

	public void failed(P object, T child, IStatus status) {
		failure = status;
	}

	protected void waitForResponse(int expectedResponses, int expectedUpdate) {
		waitForResponse(expectedResponses, expectedUpdate, 0);
	}

	protected void waitForResponse(int expectedResponses, int expectedUpdate, int expectedSents) {
		long delay;
		delay = 0;
		while (delay < TEST_TIMEOUT) {
			if (responded < expectedResponses || updated < expectedUpdate || sent < expectedSents) {
				try {
					Thread.sleep(10);
					delay += 10;
				} catch (InterruptedException e) {
				}
			} else {
				break;
			}
		}
		try {
			//wait extra to ensure there aren't remaining jobs
			Thread.sleep(25);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		assertThat("Wrong # responses: " + responded + ", updated: " + updated, responded, is(expectedResponses));
		assertThat("Wrong # updates" + updated, updated, is(expectedUpdate));
		assertThat("Wrong # sents" + sent, sent, is(expectedSents));
		if (factory != null) {
			assertThat(factory.getService().isActive(), is(false));
		}
	}

	protected void waitForFailure() {
		long delay = 0;
		while (delay < TEST_TIMEOUT) {
			if (failure == null) {
				try {
					Thread.sleep(10);
					delay += 10;
				} catch (InterruptedException e) {
				}
			} else {
				break;
			}
		}
	}

	void clear() {
		createdObject = null;
		updated = 0;
		failure = null;
	}
}