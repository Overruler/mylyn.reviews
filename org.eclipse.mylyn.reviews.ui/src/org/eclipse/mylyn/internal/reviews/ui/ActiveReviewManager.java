/*******************************************************************************
 * Copyright (c) 2014 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.reviews.ui;

import java.util.List;

import org.eclipse.mylyn.reviews.core.model.IReview;
import org.eclipse.mylyn.reviews.ui.spi.editor.AbstractReviewTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.IFormPage;

import com.google.common.collect.Lists;

public class ActiveReviewManager {

	private IReview review = null;

	private TaskEditor currentPart;

	private final List<IActiveReviewListener> reviewListeners = Lists.newArrayList();

	private final IPartListener editorPartListener = new IPartListener() {
		public void partOpened(IWorkbenchPart part) {
		}

		public void partDeactivated(IWorkbenchPart part) {
		}

		public void partClosed(IWorkbenchPart part) {
			if (part == currentPart) {
				currentPart = null;
				setReview(null);
			}
		}

		public void partBroughtToTop(IWorkbenchPart part) {
		}

		public void partActivated(IWorkbenchPart part) {
			if (part instanceof TaskEditor && currentPart != part) {
				TaskEditor editor = (TaskEditor) part;
				IFormPage page = editor.getActivePageInstance();
				if (page instanceof AbstractReviewTaskEditorPage) {
					currentPart = editor;
					AbstractReviewTaskEditorPage reviewPage = (AbstractReviewTaskEditorPage) page;
					setReview(reviewPage.getReview());
				}
			}
		}
	};

	private final IPageListener pageListener = new IPageListener() {

		private IWorkbenchPage activePage;

		public void pageOpened(IWorkbenchPage page) {
		}

		public void pageClosed(IWorkbenchPage page) {
			pageActivated(null);
		}

		public void pageActivated(IWorkbenchPage page) {
			if (page != activePage) {
				if (activePage != null) {
					activePage.removePartListener(editorPartListener);
				}
				if (page != null) {
					page.addPartListener(editorPartListener);
					editorPartListener.partActivated(page.getActiveEditor());
				}
				activePage = page;
			}
		}
	};

	private final IWindowListener windowListener = new IWindowListener() {

		public void windowOpened(IWorkbenchWindow window) {
		}

		public void windowDeactivated(IWorkbenchWindow window) {
			window.removePageListener(pageListener);
		}

		public void windowClosed(IWorkbenchWindow window) {
		}

		public void windowActivated(IWorkbenchWindow window) {
			window.addPageListener(pageListener);
			pageListener.pageActivated(window.getActivePage());
		}
	};

	public ActiveReviewManager() {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPageListener(pageListener);
		PlatformUI.getWorkbench().addWindowListener(windowListener);
		windowListener.windowActivated(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
	}

	public void setReview(IReview review) {
		if (this.review != review) {
			this.review = review;
			if (review != null) {
				for (IActiveReviewListener reviewListener : reviewListeners) {
					reviewListener.reviewActivated(review);
				}
			} else {
				for (IActiveReviewListener reviewListener : reviewListeners) {
					reviewListener.reviewDeactivated();
				}
			}
		}
	}

	public void addReviewListener(IActiveReviewListener listener) {
		reviewListeners.add(listener);
	}

	public void removeReviewListener(IActiveReviewListener listener) {
		reviewListeners.remove(listener);
	}

	public IReview getReview() {
		return review;
	}

	public TaskEditor getCurrentPart() {
		return currentPart;
	}
}
