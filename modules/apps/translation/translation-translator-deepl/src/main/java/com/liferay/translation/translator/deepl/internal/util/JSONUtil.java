package com.liferay.translation.translator.deepl.internal.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * JSON Utility
 *
 * @author Yasuyuki Takeo
 */
public class JSONUtil {

  public static <T> T toObject(
      final String jsonString,
      final TypeReference<T> type ) throws JsonProcessingException {
    T data = new ObjectMapper().readValue(jsonString, type);
    return data;
  }

  /**
   * Map json to the actual object
   */
  public static <T> T toObject(final String jsonString, final Class<T> clazz)
      throws JsonParseException, JsonMappingException, IOException {

    ObjectMapper mapper = new ObjectMapper();

    //just ignore unknown properties in json
    //mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.enable(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES);

    return mapper.readValue(jsonString, clazz);
  }

  /**
   * Write JSON file
   */
  public static <T> void writer(String fullPath, T obj) throws IOException, URISyntaxException {
    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    ow.writeValue(new File(fullPath), obj);
  }

  /**
   * Write JSON Value as String
   */
  public static <T> String writeValueAsString(T obj) throws JsonProcessingException {
    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    return ow.writeValueAsString(obj);
  }
}

