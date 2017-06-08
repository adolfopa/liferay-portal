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

package com.liferay.wiki.internal.asset.validator;

import com.liferay.asset.kernel.validator.AggregateAssetEntryValidator;
import com.liferay.asset.kernel.validator.AggregateAssetEntryValidatorFactory;
import com.liferay.asset.kernel.validator.AssetEntryValidator;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portlet.asset.validator.AssetEntryValidatorRegistry;
import com.liferay.wiki.configuration.WikiGroupServiceConfiguration;
import com.liferay.wiki.model.WikiPage;
import com.liferay.wiki.model.WikiPageConstants;
import com.liferay.wiki.service.WikiPageLocalService;

import java.util.Dictionary;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Roberto Díaz
 */
@Component(
	configurationPid = "com.liferay.wiki.configuration.WikiGroupServiceConfiguration",
	configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true,
	property = {"model.class.name=com.liferay.wiki.model.WikiPage"},
	service = AggregateAssetEntryValidatorFactory.class
)
public class WikiPageAggregateAssetEntryValidatorFactory
	implements AggregateAssetEntryValidatorFactory {

	@Activate
	@Modified
	public void activate(ComponentContext componentContext) {
		BundleContext bundleContext = componentContext.getBundleContext();

		_serviceTrackerMap = ServiceTrackerMapFactory.openMultiValueMap(
			bundleContext, AssetEntryValidator.class, "model.class.name");

		Dictionary<String, Object> properties =
			componentContext.getProperties();

		_wikiGroupServiceConfiguration = ConfigurableUtil.createConfigurable(
			WikiGroupServiceConfiguration.class, properties);
	}

	@Deactivate
	public void deactivate() {
		_serviceTrackerMap.close();
	}

	@Override
	public AggregateAssetEntryValidator getAggregateAssetEntryValidator(
		long groupId, String className, long classPK, long classTypePK,
		long[] categoryIds, String[] tagNames) {

		WikiPage wikiPage = _wikiPageLocalService.fetchWikiPage(classPK);

		if (wikiPage == null) {
			wikiPage = _wikiPageLocalService.fetchPage(classPK);
		}

		if (wikiPage == null) {
			try {
				wikiPage = _wikiPageLocalService.getPage(classPK, false);

				if (StringUtil.equals(
						wikiPage.getTitle(),
						_wikiGroupServiceConfiguration.frontPageName()) &&
					(wikiPage.getVersion() ==
						WikiPageConstants.VERSION_DEFAULT)) {

					return null;
				}
			}
			catch (PortalException pe) {
				if (_log.isWarnEnabled()) {
					_log.warn(pe, pe);
				}
			}
		}

		List<AssetEntryValidator> assetEntryValidators =
			_assetEntryValidatorRegistry.getAssetEntryValidators(className);

		return new AggregateAssetEntryValidator(
			assetEntryValidators.toArray(
				new AssetEntryValidator[assetEntryValidators.size()]));
	}

	private static final Log _log = LogFactoryUtil.getLog(
		WikiPageAggregateAssetEntryValidatorFactory.class);

	@Reference(unbind = "-")
	private AssetEntryValidatorRegistry _assetEntryValidatorRegistry;

	private ServiceTrackerMap<String, List<AssetEntryValidator>>
		_serviceTrackerMap;
	private WikiGroupServiceConfiguration _wikiGroupServiceConfiguration;

	@Reference(unbind = "-")
	private WikiPageLocalService _wikiPageLocalService;

}