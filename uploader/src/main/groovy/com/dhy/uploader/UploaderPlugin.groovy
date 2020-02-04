package com.dhy.uploader

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.serializer.SerializerFeature
import okhttp3.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

import java.util.concurrent.TimeUnit

class UploaderPlugin implements Plugin<Project> {
    def pluginName = 'uploader'
    private Project project = null

    @Override
    void apply(Project project) {
        this.project = project
        // 接收外部参数
        project.extensions.create("uploader", UploaderSetting)
        project.uploader.extensions.create("debugServer", ServerSetting)
        project.uploader.extensions.create("releaseServer", ServerSetting)

        // 取得外部参数
        if (project.android.hasProperty("applicationVariants")) { // For android application.
            project.android.applicationVariants.all { variant ->
                String variantName = variant.name.capitalize()

                // Check for execution
                if (!getSetting().enable) {
                    project.logger.error("$pluginName gradle enable is false")
                    return
                }

                // Create task.
                Task betaTask = createUploadTask(variant)
                betaTask.dependsOn project.tasks["assemble${variantName}"]
            }
        }
    }

    UploadInfo generateUploadInfo(Object variant) {
        UploadInfo uploadInfo = new UploadInfo()
        uploadInfo.extras = getSetting().extras

        // if you not set apkFile, default get the assemble output file
        if (getSetting().apkFile != null) {
            uploadInfo.sourceFile = getSetting().apkFile
            println("$pluginName: you has set the custom apkFile")
            println("$pluginName: your apk absolutepath :" + getSetting().apkFile)
        } else {
            File apkFile = variant.outputs[0].outputFile
            uploadInfo.sourceFile = apkFile.getAbsolutePath()
            println("$pluginName: the apkFile is default set to build file")
            println("$pluginName: your apk absolutepath :" + apkFile.getAbsolutePath())
        }
        if (variant.name.contains("debug")) {
            uploadInfo.server = getSetting().debugServer
        } else {
            uploadInfo.server = getSetting().releaseServer
        }
        println 'uploadUrl:' + uploadInfo.server.uploadUrl
        return uploadInfo
    }

    private Task createUploadTask(Object variant) {
        String variantName = variant.name.capitalize()
        Task uploadTask = project.tasks.create("upload${variantName}Apk").doLast {
            // if debug model and debugOn = false no execute upload
            if (variantName.contains("Debug") && !getSetting().debugOn) {
                println("$pluginName: the option debugOn is closed, if you want to upload apk file on debug model, you can set debugOn = true to open it")
                return
            }
            println("\n*****************************************************************************")
            uploadApk(generateUploadInfo(variant))
            println("*****************************************************************************\n")
        }
        uploadTask.group = 'upload'
        return uploadTask
    }

    boolean uploadApk(UploadInfo uploadInfo) {
        println("$pluginName: start uploading....")
        NetUtil.server = uploadInfo.server
        def res = post(uploadInfo.sourceFile, uploadInfo.extras)
        if (!res) {
            project.logger.error("$pluginName: Failed to upload!")
            return false
        } else {
            println("$pluginName: upload apk success !")
            return true
        }
    }

    boolean post(String filePath, Map<String, ?> params) {
        def apk = new File(filePath)
        def setting = getSetting()
        params.put(setting.KEY_FILE, apk)
        if (setting.onGetApk != null) setting.onGetApk.call(apk.name, apk.absolutePath, params)

        if (params != null) {
            params.keySet().forEach {
                println("Parameter $it: ${params[it]}")
            }
        }
        String result = NetUtil.uploadFile(params)
        if (result != null) {
            println(jsonFormat(result))
            return true
        }
        return false
    }

    static String jsonFormat(String jsonString) {
        JSONObject object = JSONObject.parseObject(jsonString)
        jsonString = JSON.toJSONString(object,
                SerializerFeature.PrettyFormat,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat)
        return jsonString
    }

    UploaderSetting getSetting() {
        return project.uploader
    }

    private static class UploadInfo {
        // Name of apk file to upload.
        public String sourceFile = null
        public Map<String, ?> extras = null
        public ServerSetting server = null
    }
}
