/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 * <p>
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.util.osgi.inspector;

import alice.tuprolog.Library;
import alice.tuprolog.Number;
import alice.tuprolog.Struct;
import alice.tuprolog.Term;
import alice.tuprolog.TermVisitor;
import alice.tuprolog.Var;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;

import java.util.Collection;

/**
 * @author Carlos Sierra Andrés
 */
@Component(immediate = true, service = MyLibrary.class)
public class MyLibrary extends Library {

	private BundleContext _bundleContext;

	@Activate
	protected void activate(BundleContext bundleContext) {

		_bundleContext = bundleContext;
	}

	public String getName() {
		return "My Library";
	}

	public boolean component_1(Term out) {
		System.out.println(out.getClass());
		
		return false;
	}

	@Reference
	private ServiceComponentRuntime _serviceComponentRuntime;
}
