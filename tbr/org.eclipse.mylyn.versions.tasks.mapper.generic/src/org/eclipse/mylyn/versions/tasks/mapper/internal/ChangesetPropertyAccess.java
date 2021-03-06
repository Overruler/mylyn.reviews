/*******************************************************************************
 * Copyright (c) 2012 Research Group for Industrial Software (INSO), Vienna University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Research Group for Industrial Software (INSO), Vienna University of Technology - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.versions.tasks.mapper.internal;
import org.eclipse.mylyn.versions.core.ChangeSet;

/**
 * 
 * @author Kilian Matt
 *
 */
public enum ChangesetPropertyAccess {

	REVISION() {
		public String getValue(ChangeSet cs) {
			return cs.getId();
		}
	},
	REPOSITORY() {
		public String getValue(ChangeSet cs) {
			return cs.getRepository().getUrl();
		}
	},
	COMMIT_MESSAGE() {
		public String getValue(ChangeSet cs) {
			return cs.getMessage();
		}
	};

	public abstract String getValue(ChangeSet cs);

}
