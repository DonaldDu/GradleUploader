package com.dhy.uploader

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.serializer.SerializerFeature
import com.google.gson.Gson
import org.apache.commons.io.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class UploaderPlugin implements Plugin<Project> {
    def pluginName = 'uploader'
    private Project project = null

    @Override
    void apply(Project project) {
        this.project = project
        // 接收外部参数
        project.extensions.create("uploader", UploaderSetting)
        project.uploader.extensions.create("debug", ServerSetting)
        project.uploader.extensions.create("release", ServerSetting)
        createUpdateLogFile()
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
                createUploadTask(variant).dependsOn project.tasks["assemble${variantName}"]
                createUploadApkAndPatchTask(variant).dependsOn project.tasks["assemble${variantName}"]
            }
        }
    }

    private Task createUploadApkAndPatchTask(Object variant) {
        String variantName = variant.name.capitalize()
        Task task = project.tasks.create("upload${variantName}Apk&Patch").doLast {
            println("\n*****************************************************************************")
            NetUtil.setting = getServer(variant)
            UploaderSetting setting = getSetting()
            File apkFile = variant.outputs[0].outputFile
            def patchFile = BSDiffUtil.diff(setting.oldApkFolder, apkFile.parentFile, apkFile, setting.apkVersionCode)
            UploadInfo patchInfo = generateUploadInfo(variant)
            def newApkParams = patchInfo.copyParams()
            patchInfo.setApkFile(patchFile)
            patchInfo.markeAsPatch()
            uploadApk(patchInfo)
            println("*****************************************************************************\n")

            UploadInfo newApkInfo = new UploadInfo()
            newApkInfo.params.putAll(newApkParams)
            uploadApk(newApkInfo)
        }
        task.group = 'upload'
        return task
    }

    UploadInfo generateUploadInfo(Object variant) {
        UploadInfo uploadInfo = new UploadInfo()
        uploadInfo.init(getServer(variant))
        File apkFile = variant.outputs[0].outputFile
        uploadInfo.setApkFile(apkFile)
        return uploadInfo
    }

    private void createUpdateLogFile() {
        def setting = getSetting()
        if (setting.enable && setting.updateLogFileName != null) {
            def logFile = new File(project.projectDir, setting.updateLogFileName)
            if (!logFile.exists()) logFile.createNewFile()
        }
    }

    private String getUpdateLog() {
        def setting = getSetting()
        if (setting.updateLogFileName != null) {
            def logFile = new File(project.projectDir, setting.updateLogFileName)
            def log = FileUtils.readFileToString(logFile, setting.updateLogFileEncoding)
            if (log.length() == 0) log = "empty log"
            return log
        } else {
            return null
        }
    }

    private ServerSetting getServer(Object variant) {
        if (isDebug(variant)) {
            ServerSetting s = getSetting().debug
            s.isDebug = true
            return s
        } else {
            return getSetting().release
        }
    }

    private static boolean isDebug(Object variant) {
        return variant.name.contains("debug")
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
            NetUtil.setting = getServer(variant)
            uploadApk(generateUploadInfo(variant))
            println("*****************************************************************************\n")
        }
        uploadTask.group = 'upload'
        return uploadTask
    }

    boolean uploadApk(UploadInfo info) {
        println("$pluginName: start uploading....")
        def res = post(info.params)
        if (!res) {
            project.logger.error("$pluginName: Failed to upload!")
            return false
        } else {
            println("$pluginName: upload apk success !")
            return true
        }
    }

    static boolean post(Map<String, ?> params) {
        String result = NetUtil.uploadFile(params)
        if (result != null) {
            def gson = new Gson()
            AppVersion.Response res = gson.fromJson(result, AppVersion.Response)
            if (res.isOK()) {
                println(jsonFormat(gson.toJson(res.data)))
            } else println(jsonFormat(result))
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

    private class UploadInfo {
        public final Map<String, ?> params = new HashMap<>()
        public ServerSetting server

        void init(ServerSetting server) {
            this.server = server
            def setting = getSetting()

            params.put("packagename", server.appId)
            params.put("versiontype", server.appType)
            params.put("versionname", getApkVersionName(setting))
            params.put("versioncode", setting.apkVersionCode)
            params.put("message", getUpdateLog())
        }

        Map<String, ?> copyParams() {
            def map = new HashMap()
            map.putAll(params)
            return map
        }

        private String getApkVersionName(UploaderSetting setting) {
            if (server.isDebug) {
                return setting.apkVersionName + '.' + setting.apkVersionCode
            } else {
                return setting.apkVersionName
            }
        }

        void setApkFile(File apk) {
            params.put("file", apk)
        }

        void markeAsPatch() {
            params.put("versiontype", server.appType + 10_000)
        }
    }
}
