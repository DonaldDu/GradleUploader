package com.devilwwj.plugintest

import okhttp3.*
import org.junit.Test
import java.io.File
import java.util.concurrent.TimeUnit

class UploadTest {
    @Test
    fun upload() {
        val file = File("D:\\ANETU3\\app\\build\\outputs\\apk\\debug\\PUCHE-debug_v3.0.2.4.162-c162_master-1ea6ddd.apk")
        val url = BuildConfig.UPLOAD_APK_URL
        if (file.exists() && url.isNotEmpty()) {
            val params: MutableMap<String, Any> = mutableMapOf()
            params["packagename"] = "packagename"
            params["versiontype"] = "0"
            params["versionname"] = "1.0"
            params["versioncode"] = "1"
            params["file"] = file
            val response = upload(url, params)
            println(response)
        } else {
            println("upload canceled")
        }
    }

    private fun upload(url: String, params: Map<String, Any>): String? {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        for (key in params.keys) {
            val v = params[key]
            if (v is File) {
                val mediaType = MediaType.parse("application/octet-stream")
                builder.addFormDataPart(key, v.name, RequestBody.create(mediaType, v))
            } else {
                builder.addFormDataPart(key, v.toString())
            }
        }
        val reqBuilder = Request.Builder().url(url).post(builder.build())
        reqBuilder.header("Authorization", BuildConfig.UPLOAD_APK_AUTHORIZATION)
        val request = reqBuilder.build()
        val okHttpClient = OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()
        val response = okHttpClient.newCall(request).execute()
        return if (response.isSuccessful) {
            response.body()?.string()
        } else {
            println("HTTP ERROR CODE " + response.code())
            println("error response")
            println(response.body()?.string())
            return null
        }
    }
}
