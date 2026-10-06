/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.upgrade.v7_4_x.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.change.tracking.test.util.BaseCTUpgradeProcessTestCase;
import com.liferay.exportimport.kernel.staging.MergeLayoutPrototypesThreadLocal;
import com.liferay.layout.test.util.ContentLayoutTestUtil;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.portal.kernel.cache.MultiVMPool;
import com.liferay.portal.kernel.dao.orm.EntityCache;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutSetPrototype;
import com.liferay.portal.kernel.model.change.tracking.CTModel;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.LayoutSetLocalService;
import com.liferay.portal.kernel.service.LayoutSetPrototypeLocalService;
import com.liferay.portal.kernel.service.change.tracking.CTService;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.upgrade.v7_4_x.LayoutLayoutSetPrototypeLayoutExternalReferenceCodeUpgradeProcess;
import com.liferay.sites.kernel.util.Sites;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Adolfo Pérez
 */
@RunWith(Arquillian.class)
public class
	LayoutLayoutSetPrototypeLayoutExternalReferenceCodeUpgradeProcessTest
		extends BaseCTUpgradeProcessTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_layoutSetPrototype = LayoutTestUtil.addLayoutSetPrototype(
			RandomTestUtil.randomString());

		_sites.updateLayoutSetPrototypesLinks(
			_group, _layoutSetPrototype.getLayoutSetPrototypeId(), 0, true,
			true);
	}

	@Test
	@TestInfo("LPP-65915")
	public void testUpgrade() throws Exception {
		_testUpgradeWithExternalReferenceCodeInUse();
		_testUpgradeWithExternalReferenceCodeMismatch();
	}

	@Override
	protected CTModel<?> addCTModel() throws Exception {
		Layout layout = _addLayout();

		_mergeLayoutSetPrototypeLayouts();

		return _layoutLocalService.getLayoutByFriendlyURL(
			_group.getGroupId(), false, layout.getFriendlyURL());
	}

	@Override
	protected void deleteCTModel(long primaryKey) throws Exception {
		Layout layout = _layoutLocalService.getLayout(primaryKey);

		Layout layoutSetPrototypeLayout = layout.getLayoutSetPrototypeLayout();

		_layoutLocalService.deleteLayout(layout);

		if (layoutSetPrototypeLayout != null) {
			_layoutLocalService.deleteLayout(layoutSetPrototypeLayout);
		}
	}

	@Override
	protected CTService<?> getCTService() {
		return _layoutLocalService;
	}

	@Override
	protected void runUpgrade() throws Exception {
		UpgradeProcess upgradeProcess =
			new LayoutLayoutSetPrototypeLayoutExternalReferenceCodeUpgradeProcess();

		upgradeProcess.upgrade();

		_entityCache.clearCache();
		_multiVMPool.clear();
	}

	@Override
	protected CTModel<?> updateCTModel(CTModel<?> ctModel) throws Exception {
		Layout layout = (Layout)ctModel;

		layout.setPriority(RandomTestUtil.randomInt());

		return _layoutLocalService.updateLayout(layout);
	}

	private Layout _addLayout() throws Exception {
		Layout layout = LayoutTestUtil.addTypeContentLayout(
			_layoutSetPrototype.getGroup(), true, false);

		ContentLayoutTestUtil.publishLayout(layout.fetchDraftLayout(), layout);

		return _layoutLocalService.getLayout(layout.getPlid());
	}

	private Layout _addLinkedLayout() throws Exception {
		Layout layoutSetPrototypeLayout = _addLayout();

		_mergeLayoutSetPrototypeLayouts();

		return _layoutLocalService.getLayoutByFriendlyURL(
			_group.getGroupId(), false,
			layoutSetPrototypeLayout.getFriendlyURL());
	}

	private void _mergeLayoutSetPrototypeLayouts() throws Exception {
		_layoutSetPrototype =
			_layoutSetPrototypeLocalService.getLayoutSetPrototype(
				_layoutSetPrototype.getLayoutSetPrototypeId());

		_layoutSetPrototype.setModifiedDate(new Date());

		_layoutSetPrototype =
			_layoutSetPrototypeLocalService.updateLayoutSetPrototype(
				_layoutSetPrototype);

		MergeLayoutPrototypesThreadLocal.clearMergeComplete();

		_sites.mergeLayoutSetPrototypeLayouts(
			_layoutSetLocalService.getLayoutSet(_group.getGroupId(), false));
	}

	private void _testUpgradeWithExternalReferenceCodeInUse() throws Exception {
		Layout layout = _addLinkedLayout();

		String externalReferenceCode = String.valueOf(layout.getPlid());

		layout.setExternalReferenceCode(externalReferenceCode);

		layout = _layoutLocalService.updateLayout(layout);

		Layout otherLayout = LayoutTestUtil.addTypePortletLayout(_group);

		otherLayout.setExternalReferenceCode(
			layout.getLayoutSetPrototypeLayoutERC());

		_layoutLocalService.updateLayout(otherLayout);

		runUpgrade();

		layout = _layoutLocalService.getLayout(layout.getPlid());

		Assert.assertEquals(
			externalReferenceCode, layout.getExternalReferenceCode());
	}

	private void _testUpgradeWithExternalReferenceCodeMismatch()
		throws Exception {

		Layout layout = _addLinkedLayout();

		layout.setExternalReferenceCode(String.valueOf(layout.getPlid()));

		layout = _layoutLocalService.updateLayout(layout);

		Layout draftLayout = layout.fetchDraftLayout();

		draftLayout.setExternalReferenceCode(
			String.valueOf(draftLayout.getPlid()));

		draftLayout = _layoutLocalService.updateLayout(draftLayout);

		Assert.assertNotEquals(
			layout.getLayoutSetPrototypeLayoutERC(),
			layout.getExternalReferenceCode());
		Assert.assertNotEquals(
			draftLayout.getLayoutSetPrototypeLayoutERC(),
			draftLayout.getExternalReferenceCode());

		runUpgrade();

		layout = _layoutLocalService.getLayout(layout.getPlid());

		Assert.assertEquals(
			layout.getLayoutSetPrototypeLayoutERC(),
			layout.getExternalReferenceCode());

		draftLayout = _layoutLocalService.getLayout(draftLayout.getPlid());

		Assert.assertEquals(
			draftLayout.getLayoutSetPrototypeLayoutERC(),
			draftLayout.getExternalReferenceCode());

		Layout layoutSetPrototypeLayout = layout.getLayoutSetPrototypeLayout();

		String name = RandomTestUtil.randomString();

		_layoutLocalService.updateName(
			layoutSetPrototypeLayout.getPlid(), name,
			LocaleUtil.toLanguageId(LocaleUtil.getSiteDefault()));

		_mergeLayoutSetPrototypeLayouts();

		layout = _layoutLocalService.getLayout(layout.getPlid());

		Assert.assertEquals(name, layout.getName(LocaleUtil.getSiteDefault()));
	}

	@Inject
	private EntityCache _entityCache;

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private LayoutLocalService _layoutLocalService;

	@Inject
	private LayoutSetLocalService _layoutSetLocalService;

	@DeleteAfterTestRun
	private LayoutSetPrototype _layoutSetPrototype;

	@Inject
	private LayoutSetPrototypeLocalService _layoutSetPrototypeLocalService;

	@Inject
	private MultiVMPool _multiVMPool;

	@Inject
	private Sites _sites;

}