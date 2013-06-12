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

package org.eclipse.mylyn.reviews.core.spi.remote;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public abstract class ReviewsDataLocator extends AbstractDataLocator {

	private static final String REVIEWS_DIR = "reviews_bin";

	private static final String OBSOLETE_MODEL_DIR = "model";

	private static final String OBSOLETE_REVIEWS_XML_DIR = "reviews_xml";

	@Override
	public IPath getLocatorDataSegment() {
		return new Path(REVIEWS_DIR);
	}

	@Override
	public void migrate() {
		deleteSubDirectory(OBSOLETE_MODEL_DIR);
		deleteSubDirectory(OBSOLETE_REVIEWS_XML_DIR);
	}

	private void deleteSubDirectory(String directory) {
		File file = new File(getSystemDataPath().append(directory).toOSString());
		if (file.exists()) {
			try {
				FileUtils.deleteDirectory(file);
			} catch (IOException e) {
				//We'll ignore, because we don't want to break any calling methods, and the only harm is stray files.
			}
		}
	}
}