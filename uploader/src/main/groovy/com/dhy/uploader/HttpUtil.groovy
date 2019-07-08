package com.dhy.uploader

import okhttp3.*

class HttpUtil {
    static String upload(String url, Map<String, Object> params) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
        for (String key : params.keySet()) {
            Object v = params.get(key)
            if (v instanceof File) {
                File file = (File) v
                MediaType mediaType = MediaType.parse("application/octet-stream")
                builder.addFormDataPart(key, file.getName(), RequestBody.create(mediaType, file))
            } else {
                builder.addFormDataPart(key, v.toString())
            }
        }
        final Request request = new Request.Builder()
                .url(url)
                .post(builder.build())
                .build()
        OkHttpClient okHttpClient = new OkHttpClient()
        Response response = okHttpClient.newCall(request).execute()
        if (response.isSuccessful()) {
            return response.body()?.string()
        } else {
            printf("HTTP ERROR CODE " + response.code())
            printf("error response")
            printf(response.body()?.string())
            return null
        }
    }
}
