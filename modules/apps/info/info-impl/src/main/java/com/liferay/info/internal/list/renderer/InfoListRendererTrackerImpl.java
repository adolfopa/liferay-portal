/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.info.internal.list.renderer;

import com.liferay.info.internal.util.MappingServiceTrackerCustomizer;
import com.liferay.info.list.renderer.InfoListRenderer;
import com.liferay.info.list.renderer.InfoListRendererTracker;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Jorge Ferrer
 */
@Component(immediate = true, service = InfoListRendererTracker.class)
public class InfoListRendererTrackerImpl implements InfoListRendererTracker {

	@Override
	public InfoListRenderer getInfoListRenderer(String key) {
		if (Validator.isNull(key)) {
			return null;
		}

		return _infoListRenderers.get(key);
	}

	@Override
	public List<InfoListRenderer> getInfoListRenderers() {
		return new ArrayList<>(_infoListRenderers.values());
	}

	@Override
	public List<InfoListRenderer> getInfoListRenderers(String itemClassName) {
		List<InfoListRenderer> infoListRenderers =
			_itemClassNameInfoListRenderersServiceTrackerMap.getService(
				itemClassName);

		if (infoListRenderers != null) {
			return new ArrayList<>(infoListRenderers);
		}

		return Collections.emptyList();
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_itemClassNameInfoListRenderersServiceTrackerMap =
			ServiceTrackerMapFactory.openMultiValueMap(
				bundleContext, InfoListRenderer.class, "model.class.name",
				new MappingServiceTrackerCustomizer<>(
					_infoListRenderers, InfoListRenderer::getKey,
					bundleContext));
	}

	@Deactivate
	protected void deactivate() {
		_itemClassNameInfoListRenderersServiceTrackerMap.close();
	}

	private final Map<String, InfoListRenderer> _infoListRenderers =
		new ConcurrentHashMap<>();
	private ServiceTrackerMap<String, List<InfoListRenderer>>
		_itemClassNameInfoListRenderersServiceTrackerMap;

}