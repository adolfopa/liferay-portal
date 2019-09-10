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

import React, {useEffect, useRef} from 'react';
import PropTypes from 'prop-types';
import ClayButton from '@clayui/button';
import {ClayInputWithMultiSelect} from '@clayui/form';
import {useResource} from '@clayui/data-provider';

import {ItemSelectorDialog} from 'frontend-js-web';

function usePrevious(value) {
	const ref = useRef();

	useEffect(() => {
		ref.current = value;
	}, [value]);

	return ref.current;
}

function AssetTagsSelector({
	addCallback,
	eventName,
	groupIds = [],
	inputName,
	inputValue,
	label,
	onInputValueChange = () => {},
	onSelectedItemsChange = () => {},
	portletURL,
	removeCallback,
	selectedItems = [],
	showSelectButton
}) {
	const {resource, refetch} = useResource({
		fetchOptions: {
			body: Liferay.Util.objectToFormData({
				cmd: JSON.stringify({
					'/assettag/search': {
						end: 20,
						groupIds,
						name: `%${inputValue === '*' ? '' : inputValue}%`,
						start: 0,
						tagProperties: ''
					}
				}),
				p_auth: Liferay.authToken
			}),
			method: 'POST'
		},
		link: `${window.location.origin}${themeDisplay.getPathContext()}
				/api/jsonws/invoke`
	});

	const previousInputValue = usePrevious(inputValue);

	useEffect(() => {
		if (inputValue && inputValue !== previousInputValue) {
			refetch();
		}
	}, [inputValue, previousInputValue, refetch]);

	const handleItemsChange = items => {
		const current = new Set(items);

		const selected = new Set(selectedItems);

		const addedItems = items.filter(item => !selected.has(item));

		const removedItems = selectedItems.filter(item => !current.has(item));

		onSelectedItemsChange([...current]);

		addedItems.forEach(item => callGlobalCallback(addCallback, item));

		removedItems.forEach(item => callGlobalCallback(removeCallback, item));
	};

	const callGlobalCallback = (callback, item) => {
		if (callback && typeof window[callback] === 'function') {
			window[callback](item);
		}
	};

	const handleSelectButtonClick = () => {
		const sub = (str, obj) => str.replace(/\{([^}]+)\}/g, (_, m) => obj[m]);

		const url = sub(decodeURIComponent(portletURL), {
			selectedTagNames: selectedItems.join()
		});

		const itemSelectorDialog = new ItemSelectorDialog({
			buttonAddLabel: Liferay.Language.get('done'),
			eventName,
			title: Liferay.Language.get('tags'),
			url
		});

		itemSelectorDialog.open();

		itemSelectorDialog.on('selectedItemChange', event => {
			const dialogSelectedItems = event.selectedItem;

			if (dialogSelectedItems) {
				const newValues =
					dialogSelectedItems.items.length > 0
						? dialogSelectedItems.items.split(',')
						: [];

				const newValuesSet = new Set(newValues);

				const addedItems = newValues.filter(
					item => !newValuesSet.has(item)
				);

				const removedItems = selectedItems.filter(
					item => !newValuesSet.has(item)
				);

				onSelectedItemsChange(newValues);

				addedItems.forEach(item =>
					callGlobalCallback(addCallback, item)
				);

				removedItems.forEach(item =>
					callGlobalCallback(removeCallback, item)
				);
			}
		});
	};

	/*
	const _handleInputBlur = () => {
		const filteredItems = event.target.filteredItems;

		if (!filteredItems || (filteredItems && filteredItems.length === 0)) {
			const inputValue = event.target.inputValue;

			if (inputValue) {
				const existingTag = this.selectedItems.find(
					tag => tag.value === inputValue
				);

				if (existingTag) {
					return;
				}

				const item = {
					label: inputValue,
					value: inputValue
				};

				this.selectedItems = this.selectedItems.concat(item);
				this.tagNames = this._getTagNames();

				this._notifyItemsChanged('itemAdded', this.addCallback, item);
			}
		}
	}
*/

	return (
		<div className="lfr-tags-selector-content">
			{selectedItems.map((item, i) => {
				return (
					<input
						key={i}
						name={inputName}
						type="hidden"
						value={item}
					/>
				);
			})}

			<ClayInputWithMultiSelect
				inputValue={inputValue}
				items={selectedItems}
				label={label || Liferay.Language.get('tags')}
				//				onInputBlur={_handleInputBlur}		//TODO IN CLAY
				onInputChange={onInputValueChange}
				onItemsChange={handleItemsChange}
				sourceItems={resource && resource.map(tag => tag.value)}
			/>

			{showSelectButton && (
				<ClayButton
					displayType="secondary"
					onClick={handleSelectButtonClick}
				>
					{Liferay.Language.get('select')}
				</ClayButton>
			)}
		</div>
	);
}

AssetTagsSelector.propTypes = {
	addCallback: PropTypes.string,
	eventName: PropTypes.string,
	groupIds: PropTypes.array,
	inputName: PropTypes.string,
	inputValue: PropTypes.string,
	label: PropTypes.string,
	onInputValueChange: PropTypes.func,
	onSelectedItemsChange: PropTypes.func,
	portletURL: PropTypes.string,
	removeCallback: PropTypes.string,
	selectedItems: PropTypes.array
};

export default AssetTagsSelector;
