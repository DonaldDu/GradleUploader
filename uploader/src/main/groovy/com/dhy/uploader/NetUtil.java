package com.dhy.uploader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class NetUtil {
    private static final OkHttpClient okHttpClient;

    static {
        okHttpClient = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    private static void printErrorResponse(Response res) {
        System.out.println("HTTP ERROR CODE " + res.code());
        System.out.println("error response");
        System.out.println(getBody(res));
    }

    private static String getBody(Response res) {
        ResponseBody body = res.body();
        try {
            return body != null ? body.string() : "";
        } catch (IOException e) {
            return "";
        }
    }

    static String fetchLatestApkVersion(String apkVersionUrl) throws IOException {
        System.out.println("fetchLatestApkVersion " + apkVersionUrl);
        Request request = new Request.Builder().url(apkVersionUrl).build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            return getBody(response);
        } else {
            printErrorResponse(response);
            return null;
        }
    }

    static void downloadFile(File file, String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = okHttpClient.newCall(request).execute();
        assert response.body() != null;
        InputStream inputStream = response.body().byteStream();
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] buf = new byte[1024 * 1024];//1MB
        int size;
        while ((size = inputStream.read(buf)) != -1) {
            outputStream.write(buf, 0, size);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }
}
