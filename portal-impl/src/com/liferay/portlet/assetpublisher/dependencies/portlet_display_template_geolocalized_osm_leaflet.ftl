<#assign liferay_aui = taglibLiferayHash["/WEB-INF/tld/liferay-aui.tld"] />
<#assign liferay_ui = taglibLiferayHash["/WEB-INF/tld/liferay-ui.tld"] />

<#assign minHeight="400px">
<#assign maxWidth=500>

<#assign namespace=renderResponse.getNamespace()>

<#assign markers=jsonFactoryUtil.createJSONArray()>

<#list entries as entry>
	<#assign assetRenderer=entry.getAssetRenderer()>
	<#assign ddmReader=assetRenderer.getDDMFieldReader()>

	<#assign fields=ddmReader.getFields("geolocation")>

	<#list fields.iterator() as field>
		<#-- Latitude and longitude -->
		<#assign marker=jsonFactoryUtil.createJSONObject(field.getValue())>

		<#-- Title -->
		<#assign _=marker.put("title", assetRenderer.getTitle(locale))>

		<#-- Abstract -->
		<#assign entryAbstract>
			<@getAbstract asset=entry />
		</#assign>
		<#assign _=marker.put("abstract", entryAbstract)>

		<#assign _=markers.put(marker)>
	</#list>
</#list>

<style type="text/css">
	.asset-entry-abstract .asset-entry-abstract-image {
		float: left;
		margin-right: 2em;
	}

	.asset-entry-abstract .asset-entry-abstract-image img {
		display: block;
	}

	.asset-entry-abstract .taglib-icon {
		float: right;
	}
</style>

<div id="${namespace}map-canvas" style="min-height: ${minHeight};"/>

<link rel="stylesheet" href="http://cdn.leafletjs.com/leaflet-0.7.2/leaflet.css" />

<script src="http://cdn.leafletjs.com/leaflet-0.7.2/leaflet.js"></script>

<@liferay_aui.script>
	(function () {
		var map = L.map('${namespace}map-canvas');

		L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
			attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
		}).addTo(map);

		var bounds = L.latLngBounds([]);
		var points = ${markers};
		var len = points.length;

		for (var i = 0; i < len; i++) {
			var point = points[i];
			var latLng = L.latLng(point['latitude'], point['longitude']);

			L.marker(latLng)
				.addTo(map)
				.bindPopup(point['abstract'], {
					maxWidth: ${maxWidth}
				});

			bounds.extend(latLng);
		}

		map.fitBounds(bounds);
	})();
</@liferay_aui.script>

<#macro getAbstract asset>
	<#assign assetRenderer=asset.getAssetRenderer()>
	<#assign popUpWindowState=windowStateFactory.getWindowState("POP_UP")>
	<#assign
		redirectURL=renderResponse.createLiferayPortletURL(
			themeDisplay.getPlid(), themeDisplay.getPortletDisplay().getId(),
			"RENDER_PHASE", false)
	>
	<#assign
		_=redirectURL.setParameter(
			"struts_action", "/asset_publisher/add_asset_redirect")
	>

	<div class="asset-entry-abstract">
		<#assign
			editPortletURL=assetRenderer.getURLEdit(
				renderRequest, renderResponse, popUpWindowState, redirectURL)
		>
		<#assign
			taglibEditURL="javascript:Liferay.Util.openWindow({id: '" +
				renderResponse.getNamespace() + "editAsset', title: '" +
				htmlUtil.escapeJS(languageUtil.format(
					locale, "edit-x",
					htmlUtil.escape(assetRenderer.getTitle(locale)),
					false)) +
				"', uri:'" + htmlUtil.escapeJS(editPortletURL.toString()) +
				"'});"
		>

		<#if assetRenderer.hasEditPermission(permissionChecker)>
			<@liferay_ui.icon
				image="edit"
				label=true
				message="edit"
				url=taglibEditURL
			/>
		</#if>

		<div class="asset-entry-abstract-image">
			<img src="${assetRenderer.getThumbnailPath(renderRequest)}"/>
		</div>

		<#assign
			assetURL=assetPublisherHelper.getAssetViewURL(
				renderRequest, renderResponse, asset)
		>

		<div class="asset-entry-abstract-content">
			<h3><a href="${assetURL}">${assetRenderer.getTitle(locale)}</a></h3>

			<div>
			${assetRenderer.getSummary(renderRequest, renderResponse)}
			</div>
		</div>

		<div class="asset-entry-abstract-footer">
			<a href="${assetURL}">${languageUtil.get(locale, "read-more")} &raquo;</a>
		</div>
	</div>
</#macro>