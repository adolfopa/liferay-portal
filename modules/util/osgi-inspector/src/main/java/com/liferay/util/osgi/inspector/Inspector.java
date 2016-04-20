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

import alice.tuprolog.InvalidLibraryException;
import alice.tuprolog.MalformedGoalException;
import alice.tuprolog.NoMoreSolutionException;
import alice.tuprolog.NoSolutionException;
import alice.tuprolog.Prolog;
import alice.tuprolog.SolveInfo;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Carlos Sierra Andrés
 */
@Component(
	immediate = true,
	property = {
		"osgi.command.function=query", "osgi.command.scope=ins"
	},
	service = Object.class
)
public class Inspector {

	private Prolog _prolog;

	@Activate
	protected void activate(BundleContext bundleContext)
		throws InvalidLibraryException {
		_prolog = new Prolog();

		_prolog.loadLibrary("alice.tuprolog.lib.BasicLibrary");
		_prolog.loadLibrary("alice.tuprolog.lib.OOLibrary");
		_prolog.loadLibrary(_myLibrary);
	}

	public void query(String input)
		throws MalformedGoalException, NoSolutionException, NoMoreSolutionException {

		SolveInfo info = _prolog.solve(input);

		System.out.println(info.getSolution());

		try {
			while (_prolog.hasOpenAlternatives()) {
				info = _prolog.solveNext();

				System.out.println(info.getSolution());
			}

			System.out.println("true.");
		}
		catch (NoSolutionException e) {
			System.out.println("false.");
		}
		catch (NoMoreSolutionException e) {
			System.out.println("true.");
		}
	}

	@Reference
	private MyLibrary _myLibrary;
}
