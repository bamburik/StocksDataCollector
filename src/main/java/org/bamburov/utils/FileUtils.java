package org.bamburov.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    public static String readFileFromResourcesToString(String pathToFileFromResources) {
        return String.join("\n", readFileFromResourcesToList(pathToFileFromResources));
    }

    public static List<String> readFileFromResourcesToList(String pathToFileFromResources) {
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

    public static String readFileFromProject(String pathFromProjectFolder) throws IOException {
        File file = new File(pathFromProjectFolder);
        InputStream inputStream = new FileInputStream(file);
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

    public static void writeToFileFromResources(String fileName, List<String> lines) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write("ticker");
        for (String line : lines) {
            writer.newLine();
            writer.write(line);
        }
        writer.close();
    }
}
