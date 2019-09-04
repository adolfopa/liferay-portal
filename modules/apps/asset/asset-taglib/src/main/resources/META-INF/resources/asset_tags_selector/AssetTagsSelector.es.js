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

import React, {useEffect, useRef, useState} from 'react';
import PropTypes from 'prop-types';
import ClayButton from '@clayui/button';
import {ClayInputWithMultiSelect} from '@clayui/form';
import {useResource} from '@clayui/data-provider';

import {ItemSelectorDialog} from 'frontend-js-web';

function usePrevious(value) {
	const ref = useRef();

	useEffect(() => {
		ref.current = value;
	}, [value]); // Only re-run if value changes

	return ref.current;
}

function AssetTagsSelector({
	addCallback,
	eventName,
	groupIds = [],
	inputName,
	inputValue,
	label,
	onInputValueChange,
	onSelectedItemsChange,
	portletURL,
	removeCallback,
	selectedItems = [],
	showSelectButton
}) {
	const [innerSelectedItems, setInnerSelectedItems] = useState(selectedItems);
	const [innerInputValue, setInnerInputValue] = useState(inputValue);
	const [sourceItems, setSourceItems] = useState([]);

	const {resource, refetch} = useResource({
		fetchOptions: {
			body: Liferay.Util.objectToFormData({
				cmd: JSON.stringify({
					'/assettag/search': {
						end: 20,
						groupIds,
						name: `%${
							innerInputValue === '*' ? '' : innerInputValue
						}%`,
						start: 0,
						tagProperties: ''
					}
				}),
				p_auth: Liferay.authToken
			}),
			method: 'POST'
		},
		link:
			window.location.origin +
			themeDisplay.getPathContext() +
			'/api/jsonws/invoke'
	});

	const previousInnerInputValue = usePrevious(innerInputValue);

	useEffect(() => {
		if (innerInputValue !== previousInnerInputValue) {
			if (onInputValueChange) {
				onInputValueChange(innerInputValue);
			}
		}
	}, [innerInputValue, onInputValueChange, previousInnerInputValue]);

	const previousInnerSelectedItems = usePrevious(innerInputValue);

	useEffect(() => {
		if (innerSelectedItems !== previousInnerSelectedItems) {
			if (onSelectedItemsChange) {
				onSelectedItemsChange(innerSelectedItems);
			}
		}
	}, [innerSelectedItems, onSelectedItemsChange, previousInnerSelectedItems]);

	useEffect(() => {
		if (innerInputValue && innerInputValue !== previousInnerInputValue) {
			refetch();
		}
	}, [innerInputValue, previousInnerInputValue, refetch]);

	useEffect(() => {
		if (resource) {
			setSourceItems(resource.map(tag => tag.value));
		}
	}, [resource]);

	useEffect(() => {
		setInnerSelectedItems(selectedItems);
	}, [selectedItems]);

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
	const handleItemsChange = items => {
		const current = new Set(items);

		const selected = new Set(innerSelectedItems);

		const addedItems = items.filter(item => !selected.has(item));

		const removedItems = innerSelectedItems.filter(
			item => !current.has(item)
		);

		setInnerSelectedItems([...current]);

		callGlobalCallback(addCallback, addedItems);

		callGlobalCallback(removeCallback, removedItems);
	};

	const handleSelectButtonClick = () => {
		const sub = (str, obj) => str.replace(/\{([^}]+)\}/g, (_, m) => obj[m]);

		const url = sub(decodeURIComponent(portletURL), {
			selectedTagNames: innerSelectedItems.join()
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

				const removedItems = innerSelectedItems.filter(
					item => !newValuesSet.has(item)
				);

				setInnerSelectedItems(newValues);

				addedItems.forEach(item =>
					callGlobalCallback(addCallback, item)
				);

				removedItems.forEach(item =>
					callGlobalCallback(removeCallback, item)
				);
			}
		});
	};

	const callGlobalCallback = (callback, item) => {
		if (callback && typeof window[callback] === 'function') {
			window[callback](item);
		}
	};

	return (
		<div className="lfr-tags-selector-content">
			{innerSelectedItems.map((item, i) => {
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
				inputValue={innerInputValue}
				items={innerSelectedItems}
				label={label || Liferay.Language.get('tags')}
				//				onInputBlur={_handleInputBlur}		//TODO IN CLAY
				onInputChange={setInnerInputValue}
				onItemsChange={handleItemsChange}
				sourceItems={sourceItems}
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
