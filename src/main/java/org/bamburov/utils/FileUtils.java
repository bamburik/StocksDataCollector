package org.bamburov.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    public static String readFileToString(String pathToFileFromResources) {
        return String.join("\n", readFileToList(pathToFileFromResources));
    }

    public static List<String> readFileToList(String pathToFileFromResources) {
        InputStream inputStream = FileUtils.class.getClassLoader().getResourceAsStream(pathToFileFromResources);
        List<String> result = new ArrayList<>();
        try (InputStreamReader streamReader =
                     new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
