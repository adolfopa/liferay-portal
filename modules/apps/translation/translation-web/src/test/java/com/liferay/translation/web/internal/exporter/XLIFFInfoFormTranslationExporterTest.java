package com.liferay.translation.web.internal.exporter;

import com.liferay.petra.string.StringPool;

import java.io.IOException;
import java.io.InputStream;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.xliff2.XLIFF2Filter;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Brian Wing Shun Chan
 */
public class XLIFFInfoFormTranslationExporterTest {

	@Test
	public void importXLIFF2() {
		XLIFF2Filter filter = new XLIFF2Filter();

		try (InputStream is =
				XLIFFInfoFormTranslationExporterTest.class.getResourceAsStream(
					"/com/liferay/translation/exporter/test/dependencies/test-journal-article.xlf")) {

			RawDocument rawDocument = new RawDocument(
				is, StringPool.UTF8, LocaleId.US_ENGLISH,
				LocaleId.fromString("es-ES"));

			filter.open(rawDocument);

			rawDocument.toString();

			//filter.close();
			Assert.assertTrue(rawDocument.toString(), true);
		}
		catch (IOException ioException) {
			Assert.assertTrue(ioException.getMessage(), false);

			throw new RuntimeException(ioException);
		}
	}

	//
	//	@Test
	//	public void importXLIFFExample() {
	//		XLIFF2Filter filter = new XLIFF2Filter();
	//
	//		try (InputStream is =
	//				XLIFFInfoFormTranslationExporterTest.class.getResourceAsStream(
	//					"/com/liferay/translation/exporter/test/dependencies/example_2_0.xlf")) {
	//
	//			RawDocument rawDocument = new RawDocument(
	//				is, StringPool.UTF8, LocaleId.ENGLISH, LocaleId.FRENCH);

	//
	//			filter.open(rawDocument);
	//
	//			System.out.println(filter.toString());
	//
	//			filter.close();
	//		}
	//		catch (IOException ioException) {
	//			Assert.assertTrue(ioException.getMessage(), false);
	//			throw new RuntimeException(ioException);
	//		}
	//	}

	//	@Test
	//	public void importXLIFFLyncode() {
	//		try (InputStream is =
	//				XLIFFInfoFormTranslationExporterTest.class.getResourceAsStream(
	//					"/com/liferay/translation/exporter/test/dependencies/example_1_2.xlf")) {
	//
	//			XLIFF read = XLiffUtils.read(is);

	//
	//			Collection<String> sources = read.getSources();

	//
	//			sources.toString();
	//		}
	//		catch (IOException | XliffException ioException) {
	//			Assert.assertTrue(ioException.getMessage(), false);
	//
	//			throw new RuntimeException(ioException);
	//		}
	//	}

	//	@Test
	//	public void importXLIFFReader() {
	//		try (XLIFFReader reader = new XLIFFReader(
	//				XLIFFReader.VALIDATION_MINIMAL)) {
	//
	//			File file = new File(
	//				"/com/liferay/translation/exporter/test/dependencies/test-journal-article.xlf");

	//
	//			reader.open(file);
	//
	//			Assert.assertEquals(0, reader.getWarningCount());
	//		}
	//		catch (Exception e) {
	//			Assert.assertTrue(e.getMessage(), false);
	//		}
	//	}

}