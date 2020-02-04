package com.dhy.uploader;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class NetUtil {
    public static ServerSetting setting;
    private static final OkHttpClient okHttpClient;

    static {
        okHttpClient = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public static String uploadFile(Map<String, Object> params) throws IOException {
        String token = fetchToken(setting.loginUrl, setting.loginToken);
        return uploadFile(setting.uploadUrl, params, token);
    }

    private static String uploadFile(String url, Map<String, Object> params, String token) throws IOException {
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for (String key : params.keySet()) {
            Object v = params.get(key);
            if (v instanceof File) {
                File file = (File) v;
                MediaType mediaType = MediaType.parse("application/octet-stream");
                bodyBuilder.addFormDataPart(key, file.getName(), RequestBody.create(mediaType, file));
            } else {
                bodyBuilder.addFormDataPart(key, v.toString());
            }
        }

        Request.Builder uploadReqBuilder = new Request.Builder().url(url).post(bodyBuilder.build());
        uploadReqBuilder.header("Authorization", token);

        final Request request = uploadReqBuilder.build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            return getBody(response);
        } else {
            printErrorResponse(response);
            return null;
        }
    }

    private static String fetchToken(String url, String token) throws IOException {
        Request.Builder tokenReqBuilder = new Request.Builder().url(url);
        tokenReqBuilder.header("Authorization", token);
        tokenReqBuilder.post(RequestBody.create(null, ""));
        Request request = tokenReqBuilder.build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            JSONObject json = JSONObject.parseObject(getBody(response));
            return "Bearer " + json.getString("access_token");
        } else {
            printErrorResponse(response);
            return null;
        }
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

    public static AppVersion fetchLatestApkVersion() {
        try {
            return fetchLatestApkVersion(setting.appId, setting.appType);
        } catch (IOException e) {
            return null;
        }
    }

    private static AppVersion fetchLatestApkVersion(String appId, int appType) throws IOException {
        String url = String.format(setting.apkVersionUrl + "?packagename=%s&versiontype=%s", appId, appType);
        System.out.println("fetchLatestApkVersion " + url);
        Request request = new Request.Builder().url(url).build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            String json = getBody(response);
            return new Gson().fromJson(json, AppVersion.Response.class).data;
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
