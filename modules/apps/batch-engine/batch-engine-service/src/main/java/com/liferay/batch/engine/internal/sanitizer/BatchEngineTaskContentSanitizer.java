/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.sanitizer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.liferay.batch.engine.BatchEngineTaskContentType;
import com.liferay.batch.engine.configuration.BatchEngineTaskCompanyConfiguration;
import com.liferay.batch.engine.internal.util.PasswordFieldUtil;
import com.liferay.batch.engine.model.BatchEngineImportTask;
import com.liferay.petra.io.unsync.UnsyncBufferedReader;
import com.liferay.petra.io.unsync.UnsyncByteArrayOutputStream;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author Adolfo Pérez
 */
public class BatchEngineTaskContentSanitizer {

	public BatchEngineTaskContentSanitizer(
		BatchEngineImportTask batchEngineImportTask,
		ConfigurationProvider configurationProvider,
		Set<String> passwordFieldNames) {

		_batchEngineImportTask = batchEngineImportTask;
		_configurationProvider = configurationProvider;

		_parameters = GetterUtil.getObject(
			batchEngineImportTask.getParameters(), HashMap::new);

		_passwordFieldNames = passwordFieldNames;
	}

	public byte[] sanitize(InputStream inputStream) throws Exception {
		UnsyncByteArrayOutputStream unsyncByteArrayOutputStream =
			new UnsyncByteArrayOutputStream();

		try (ZipInputStream zipInputStream = new ZipInputStream(inputStream);
			ZipOutputStream zipOutputStream = new ZipOutputStream(
				unsyncByteArrayOutputStream)) {

			ZipEntry zipEntry = zipInputStream.getNextEntry();

			zipOutputStream.putNextEntry(new ZipEntry(zipEntry.getName()));

			zipOutputStream.write(_sanitizeInputStream(zipInputStream));

			zipOutputStream.closeEntry();
		}

		return unsyncByteArrayOutputStream.toByteArray();
	}

	private void _copyCell(
		Cell cell, Map<Short, CellStyle> cellStyles, Cell newCell,
		XSSFWorkbook newXSSFWorkbook) {

		CellStyle cellStyle = cell.getCellStyle();

		newCell.setCellStyle(
			cellStyles.computeIfAbsent(
				cellStyle.getIndex(),
				index -> {
					CellStyle newCellStyle = newXSSFWorkbook.createCellStyle();

					newCellStyle.cloneStyleFrom(cellStyle);

					return newCellStyle;
				}));

		CellType cellType = cell.getCellType();

		if (cellType == CellType.FORMULA) {
			cellType = cell.getCachedFormulaResultType();
		}

		if (cellType == CellType.BOOLEAN) {
			newCell.setCellValue(cell.getBooleanCellValue());
		}
		else if (cellType == CellType.ERROR) {
			newCell.setCellErrorValue(cell.getErrorCellValue());
		}
		else if (cellType == CellType.NUMERIC) {
			newCell.setCellValue(cell.getNumericCellValue());
		}
		else if (cellType == CellType.STRING) {
			newCell.setCellValue(cell.getStringCellValue());
		}
	}

	private String _getCSVFileColumnDelimiter() throws Exception {
		BatchEngineTaskCompanyConfiguration
			batchEngineTaskCompanyConfiguration =
				_configurationProvider.getCompanyConfiguration(
					BatchEngineTaskCompanyConfiguration.class,
					_batchEngineImportTask.getCompanyId());

		return batchEngineTaskCompanyConfiguration.csvFileColumnDelimiter();
	}

	private String _getFieldName(String name) {
		Map<String, Serializable> fieldNameMapping =
			_batchEngineImportTask.getFieldNameMapping();

		if (MapUtil.isEmpty(fieldNameMapping)) {
			return name;
		}

		String fieldName = (String)fieldNameMapping.get(name);

		if (Validator.isNotNull(fieldName)) {
			return fieldName;
		}

		int index = name.indexOf(CharPool.PERIOD);

		if (index == -1) {
			return name;
		}

		String mappedFieldName = (String)fieldNameMapping.get(
			name.substring(0, index));

		if (Validator.isNotNull(mappedFieldName)) {
			return mappedFieldName + name.substring(index);
		}

		return name;
	}

	private Set<String> _getNestedPasswordFieldNames(String fieldName) {
		Set<String> nestedPasswordFieldNames = new HashSet<>();

		String prefix = fieldName + StringPool.PERIOD;

		for (String passwordFieldName : _passwordFieldNames) {
			if (passwordFieldName.startsWith(prefix)) {
				nestedPasswordFieldNames.add(
					passwordFieldName.substring(prefix.length()));
			}
		}

		return nestedPasswordFieldNames;
	}

	private byte[] _sanitizeCSVInputStream(InputStream inputStream)
		throws Exception {

		String enclosingCharacter = (String)_parameters.getOrDefault(
			"enclosingCharacter", StringPool.QUOTE);

		CSVFormat csvFormat = CSVFormat.Builder.create(
		).setDelimiter(
			(String)_parameters.getOrDefault(
				"delimiter", _getCSVFileColumnDelimiter())
		).setIgnoreEmptyLines(
			true
		).setQuote(
			enclosingCharacter.charAt(0)
		).build();

		UnsyncByteArrayOutputStream unsyncByteArrayOutputStream =
			new UnsyncByteArrayOutputStream();

		try (CSVParser csvParser = CSVParser.parse(
				new InputStreamReader(
					BOMInputStream.builder(
					).setByteOrderMarks(
						ByteOrderMark.UTF_8, ByteOrderMark.UTF_16LE,
						ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_32LE,
						ByteOrderMark.UTF_32BE
					).setInputStream(
						inputStream
					).setInclude(
						false
					).get(),
					StandardCharsets.UTF_8),
				csvFormat);
			CSVPrinter csvPrinter = new CSVPrinter(
				new OutputStreamWriter(
					unsyncByteArrayOutputStream, StandardCharsets.UTF_8),
				csvFormat)) {

			Iterator<CSVRecord> iterator = csvParser.iterator();

			List<String> headers = new ArrayList<>();

			if (GetterUtil.getBoolean(
					_parameters.getOrDefault(
						"containsHeaders", StringPool.TRUE))) {

				if (iterator.hasNext()) {
					CSVRecord csvRecord = iterator.next();

					headers = csvRecord.toList();

					csvPrinter.printRecord(headers);
				}
			}
			else {
				for (int i = 0; i < 100; i++) {
					headers.add(String.valueOf(i));
				}
			}

			Set<Integer> indexes = new HashSet<>();

			for (int i = 0; i < headers.size(); i++) {
				if (_passwordFieldNames.contains(
						_getFieldName(headers.get(i)))) {

					indexes.add(i);
				}
			}

			while (iterator.hasNext()) {
				CSVRecord csvRecord = iterator.next();

				List<String> values = csvRecord.toList();

				for (int index : indexes) {
					if (index < values.size()) {
						values.set(index, StringPool.BLANK);
					}
				}

				csvPrinter.printRecord(values);
			}
		}

		return unsyncByteArrayOutputStream.toByteArray();
	}

	private byte[] _sanitizeInputStream(InputStream inputStream)
		throws Exception {

		BatchEngineTaskContentType batchEngineTaskContentType =
			BatchEngineTaskContentType.valueOf(
				_batchEngineImportTask.getContentType());

		if (batchEngineTaskContentType == BatchEngineTaskContentType.CSV) {
			return _sanitizeCSVInputStream(inputStream);
		}

		if ((batchEngineTaskContentType == BatchEngineTaskContentType.JSON) ||
			(batchEngineTaskContentType == BatchEngineTaskContentType.JSONT)) {

			return _sanitizeJSONInputStream(inputStream);
		}

		if (batchEngineTaskContentType == BatchEngineTaskContentType.JSONL) {
			return _sanitizeJSONLInputStream(inputStream);
		}

		if ((batchEngineTaskContentType == BatchEngineTaskContentType.XLS) ||
			(batchEngineTaskContentType == BatchEngineTaskContentType.XLSX)) {

			return _sanitizeXLSInputStream(inputStream);
		}

		throw new IllegalArgumentException(
			"Unsupported batch engine task content type " +
				batchEngineTaskContentType);
	}

	private byte[] _sanitizeJSONInputStream(InputStream inputStream)
		throws Exception {

		UnsyncByteArrayOutputStream unsyncByteArrayOutputStream =
			new UnsyncByteArrayOutputStream();

		try (JsonParser jsonParser = _objectMapper.createParser(inputStream);
			JsonGenerator jsonGenerator = _objectMapper.createGenerator(
				unsyncByteArrayOutputStream)) {

			if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
				throw new IllegalArgumentException(
					"Input stream is not a JSON array");
			}

			jsonGenerator.writeStartArray();

			while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
				jsonGenerator.writeTree(
					_sanitizeJSONNode(jsonParser.readValueAsTree()));
			}

			jsonGenerator.writeEndArray();
		}

		return unsyncByteArrayOutputStream.toByteArray();
	}

	private byte[] _sanitizeJSONLInputStream(InputStream inputStream)
		throws Exception {

		UnsyncByteArrayOutputStream unsyncByteArrayOutputStream =
			new UnsyncByteArrayOutputStream();

		try (UnsyncBufferedReader unsyncBufferedReader =
				new UnsyncBufferedReader(new InputStreamReader(inputStream));
			Writer writer = new OutputStreamWriter(
				unsyncByteArrayOutputStream, StandardCharsets.UTF_8)) {

			String line = null;

			while ((line = unsyncBufferedReader.readLine()) != null) {
				if (!Validator.isBlank(line)) {
					line = _objectMapper.writeValueAsString(
						_sanitizeJSONNode(_objectMapper.readTree(line)));
				}

				writer.write(line);
				writer.write(StringPool.NEW_LINE);
			}
		}

		return unsyncByteArrayOutputStream.toByteArray();
	}

	private JsonNode _sanitizeJSONNode(JsonNode jsonNode) {
		if (!jsonNode.isObject()) {
			return jsonNode;
		}

		ObjectNode objectNode = (ObjectNode)jsonNode;

		List<String> names = new ArrayList<>();

		Iterator<String> iterator = objectNode.fieldNames();

		iterator.forEachRemaining(names::add);

		for (String name : names) {
			String fieldName = _getFieldName(name);

			if (_passwordFieldNames.contains(fieldName)) {
				objectNode.remove(name);

				continue;
			}

			Set<String> nestedPasswordFieldNames = _getNestedPasswordFieldNames(
				fieldName);

			if (!nestedPasswordFieldNames.isEmpty()) {
				PasswordFieldUtil.removePasswordFields(
					objectNode.get(name), nestedPasswordFieldNames);
			}
		}

		return jsonNode;
	}

	private byte[] _sanitizeXLSInputStream(InputStream inputStream)
		throws Exception {

		UnsyncByteArrayOutputStream unsyncByteArrayOutputStream =
			new UnsyncByteArrayOutputStream();

		try (XSSFWorkbook xssfWorkbook = new XSSFWorkbook(inputStream);
			XSSFWorkbook newXSSFWorkbook = new XSSFWorkbook()) {

			Sheet sheet = xssfWorkbook.getSheetAt(0);

			Sheet newSheet = newXSSFWorkbook.createSheet(sheet.getSheetName());

			Map<Short, CellStyle> cellStyles = new HashMap<>();
			Set<Integer> indexes = new HashSet<>();

			for (Row row : sheet) {
				Row newRow = newSheet.createRow(row.getRowNum());

				int index = 0;

				for (Cell cell : row) {
					Cell newCell = newRow.createCell(cell.getColumnIndex());

					if (row.getRowNum() == sheet.getFirstRowNum()) {
						if ((cell.getCellType() == CellType.STRING) &&
							_passwordFieldNames.contains(
								_getFieldName(cell.getStringCellValue()))) {

							indexes.add(index);
						}
					}
					else if (indexes.contains(index)) {
						index++;

						continue;
					}

					_copyCell(cell, cellStyles, newCell, newXSSFWorkbook);

					index++;
				}
			}

			newXSSFWorkbook.write(unsyncByteArrayOutputStream);
		}

		return unsyncByteArrayOutputStream.toByteArray();
	}

	private static final ObjectMapper _objectMapper = new ObjectMapper() {
		{
			enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
			setNodeFactory(JsonNodeFactory.withExactBigDecimals(true));
		}
	};

	private final BatchEngineImportTask _batchEngineImportTask;
	private final ConfigurationProvider _configurationProvider;
	private final Map<String, Serializable> _parameters;
	private final Set<String> _passwordFieldNames;

}