package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Utils {
    public static String load(String path) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(path)))) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append('\n');
            reader.close();
        } catch (IOException ignore) {
        }
        return sb.toString();
    }

    public static String join(String str) {
        return str.replaceAll("[\n\r]", " ").trim();
    }

    public static List<Object> asList(Object... objects) {
        List list = new ArrayList();
        for (Object o : objects) list.add(o);
        return list;
    }
}
