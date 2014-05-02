<#assign liferay_ui = taglibLiferayHash["/WEB-INF/tld/liferay-ui.tld"] />
<#assign group=themeDisplay.getScopeGroup()>
<#assign apiKey=group.getLiveParentTypeSettingsProperty("googleMapsKey")!"">

<#if apiKey = "">
	<div class="alert alert-warning">
		${languageUtil.get(
			locale, "please-configure-your-google-maps-key-in-the-site-settings")}
	</div>
<#else>
	<#assign minHeight="400px">

	<#assign namespace=renderResponse.getNamespace()>

	<#-- Pin images to use for each asset type -->
	<#assign images={
		"com.liferay.portlet.journal.model.JournalArticle":
			"http://maps.google.com/mapfiles/ms/icons/blue-dot.png",
		"com.liferay.portlet.documentlibrary.model.DLFileEntry":
			"http://maps.google.com/mapfiles/ms/icons/green-dot.png",
		"com.liferay.portlet.dynamicdatalists.model.DDLRecord":
			"http://maps.google.com/mapfiles/ms/icons/red-dot.png",
		"default":
			"http://maps.google.com/mapfiles/ms/icons/yellow-dot.png"
	}>

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

			<#-- Pin image -->
			<#if images?keys?seq_contains(entry.getClassName())>
				<#assign _=marker.put("icon", images[entry.getClassName()])>
			<#else>
				<#assign _=marker.put("icon", images["default"])>
			</#if>

			<#assign _=markers.put(marker)>
		</#list>
	</#list>

	<#assign showEditURL=paramUtil.getBoolean(renderRequest, "showEditURL", true)>
	<#assign hasEditPermissions=assetRenderer.hasEditPermission(permissionChecker)>
	<#assign showEditControls=showEditURL && hasEditPermissions>

	<style type="text/css">
		.asset-entry-abstract .asset-entry-abstract-image {
			float: left;
		}

		.asset-entry-abstract .asset-entry-abstract-image img {
			display: block;
		}

		.asset-entry-abstract .asset-entry-abstract-content {
			margin-left: 10em;
		}

		.asset-entry-abstract .taglib-icon {
			float: right;
		}
	</style>

	<script type="text/javascript"
			src="http://maps.googleapis.com/maps/api/js?key=${apiKey}&sensor=true">
	</script>

	<script type="text/javascript">
		(function () {
			function putMarkers(map) {
				var points = ${markers};
				var len = points.length;
				var bounds = new google.maps.LatLngBounds();

				for (var i = 0; i < len; i++) {
					var point = points[i];

					var marker = new google.maps.Marker({
						position: new google.maps.LatLng(
								point["latitude"], point["longitude"]),
						map: map,
						title: point["title"],
						icon: point["icon"]
					});

					bounds.extend(marker.position);

					(function (marker) {
						var infoWindow = new google.maps.InfoWindow({
							content: point["abstract"] || point["title"]
						});

						google.maps.event.addListener(marker, 'click', function () {
							infoWindow.open(map, marker);
						});
					})(marker);
				}

				return bounds;
			}

			function initialize() {
				var mapOptions = { zoom: 8 };

				var map = new google.maps.Map(
						document.getElementById("${namespace}map-canvas"), mapOptions);

				var bounds = putMarkers(map);

				map.fitBounds(bounds);
				map.panToBounds(bounds);
			}

			google.maps.event.addDomListener(window, 'load', initialize);
		})();
	</script>

	<div id="${namespace}map-canvas" style="min-height: ${minHeight};"/>

	<#macro getAbstract asset>
		<#assign assetRenderer=asset.getAssetRenderer()>
		<#assign popUpWindowState=windowStateFactory.getWindowState("POP_UP")>
		<#assign
			redirectURL=renderResponse.createLiferayPortletURL(
				themeDisplay.getPlid(), themeDisplay.getPortletDisplay().getId(),
				"RENDER_PHASE", false)>
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
			<@liferay_ui.icon
				image="edit"
				label=true
				message="edit"
				url=taglibEditURL
			/>
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
</#if>