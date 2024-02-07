package org.bamburov.utils;

import static com.jayway.jsonpath.JsonPath.parse;
import static com.jayway.jsonpath.Configuration.defaultConfiguration;

public class JsonUtils {
    public static String getJsonNodeValue(String jsonString, String jsonPath) {
        Object jsonValue;
        Object jsonObject = defaultConfiguration().jsonProvider().parse(jsonString);
        if (jsonPath.contains("$.")) {
            jsonValue = parse(jsonObject).read(jsonPath);
        } else {
            jsonValue = parse(jsonObject).read("$." + jsonPath);
        }

        return String.valueOf(jsonValue);
    }
}
