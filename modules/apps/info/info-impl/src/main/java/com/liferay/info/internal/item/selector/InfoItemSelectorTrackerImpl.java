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

package com.liferay.info.internal.item.selector;

import com.liferay.info.internal.util.MappingServiceTrackerCustomizer;
import com.liferay.info.item.selector.InfoItemSelector;
import com.liferay.info.item.selector.InfoItemSelectorTracker;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Eudaldo Alonso
 */
@Component(immediate = true, service = InfoItemSelectorTracker.class)
public class InfoItemSelectorTrackerImpl implements InfoItemSelectorTracker {

	@Override
	public InfoItemSelector getInfoItemSelector(String key) {
		if (Validator.isNull(key)) {
			return null;
		}

		return _infoItemSelectors.get(key);
	}

	@Override
	public List<InfoItemSelector> getInfoItemSelectors() {
		return new ArrayList<>(_infoItemSelectors.values());
	}

	@Override
	public List<InfoItemSelector> getInfoItemSelectors(String itemClassName) {
		List<InfoItemSelector> infoItemSelectors =
			_itemClassNameInfoItemSelectorsServiceTrackerMap.getService(
				itemClassName);

		if (infoItemSelectors != null) {
			return new ArrayList<>(infoItemSelectors);
		}

		return Collections.emptyList();
	}

	@Override
	public Set<String> getInfoItemSelectorsClassNames() {
		return _itemClassNameInfoItemSelectorsServiceTrackerMap.keySet();
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_itemClassNameInfoItemSelectorsServiceTrackerMap =
			ServiceTrackerMapFactory.openMultiValueMap(
				bundleContext, InfoItemSelector.class, "model.class.name",
				new MappingServiceTrackerCustomizer<>(
					_infoItemSelectors, InfoItemSelector::getKey,
					bundleContext));
	}

	@Deactivate
	protected void deactivate() {
		_itemClassNameInfoItemSelectorsServiceTrackerMap.close();
	}

	private final Map<String, InfoItemSelector> _infoItemSelectors =
		new ConcurrentHashMap<>();
	private ServiceTrackerMap<String, List<InfoItemSelector>>
		_itemClassNameInfoItemSelectorsServiceTrackerMap;

}