package com.github.lihang941.v2ray.tool;

import io.vertx.core.json.JsonObject;

import java.io.*;

/**
 * @author : lihang941
 * @since : 2019/4/17
 */
public final class FileTool {

    public static JsonObject readFileToJson(String path) throws IOException {
        return new JsonObject(readFile(path));
    }

    public static String readFile(String path) throws IOException {
        try (FileReader fileReader = new FileReader(path)) {
            return readString(fileReader);
        }
    }


    public static String readFile(InputStream inputStream) throws IOException {
        try (InputStreamReader fileReader = new InputStreamReader(inputStream)) {
            return readString(fileReader);
        }
    }

    private static String readString(InputStreamReader fileReader) throws IOException {
        StringBuilder str = new StringBuilder();
        int len;
        char[] buf = new char[1024];
        while ((len = fileReader.read(buf)) != -1) {
            str.append(new String(buf, 0, len));
        }
        return str.toString();
    }


    public static void wirte(String path, String content) throws IOException {
        try (FileWriter fileWriter = new FileWriter(path)) {
            fileWriter.write(content);
            fileWriter.flush();
        }
    }

}
