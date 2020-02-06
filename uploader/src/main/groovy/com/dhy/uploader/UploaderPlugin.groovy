package com.dhy.uploader

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
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
                createUploadApkTask(variant).dependsOn project.tasks["assemble${variantName}"]
                createUploadApkAndPatchTask(variant).dependsOn project.tasks["assemble${variantName}"]
            }
        }
    }

    private Task createUploadApkTask(Object variant) {
        String variantName = variant.name.capitalize()
        Task task = project.tasks.create("upload${variantName}Apk").doLast {
            File apkFile = variant.outputs[0].outputFile
            def server = getServer(variant)
            startBatScript(server, apkFile, null)
        }
        task.group = 'upload'
        return task
    }

    private Task createUploadApkAndPatchTask(Object variant) {
        String variantName = variant.name.capitalize()
        Task task = project.tasks.create("upload${variantName}Apk&Patch").doLast {
            def server = getServer(variant)
            def appVersionJson = NetUtil.fetchLatestApkVersion(server.apkVersionUrl)
            JSONObject appVersion = JSON.parse(appVersionJson)
            String OLD_APK_URL = "OLD_APK_URL"
            String OLD_APK_VERSION_CODE = "OLD_APK_VERSION_CODE"
            server.onGetVersionResponse(appVersion, OLD_APK_URL, OLD_APK_VERSION_CODE)
            String oldApkUrl = appVersion.getString(OLD_APK_URL)
            int oldApkVersionCode = appVersion.getIntValue(OLD_APK_VERSION_CODE)
            File apkFile = variant.outputs[0].outputFile
            File patchFolder = apkFile.parentFile
            File patchFile = BSDiffUtil.diff(patchFolder, setting.oldApkFolder, oldApkUrl, oldApkVersionCode, apkFile, setting.apkVersionCode)
            startBatScript(server, apkFile, patchFile)
        }
        task.group = 'upload'
        return task
    }

    private void startBatScript(ServerSetting server, File apkFile, File patchFile) {
        BatParams params = new BatParams(server)
        params.setVersion(setting.apkVersionName, setting.apkVersionCode as int)
        params.apkFilePath = apkFile.absolutePath
        params.patchFilePath = patchFile?.absolutePath
        params.updateLog = getUpdateLog()
        Util.excuteCMD(new File(setting.batScriptPath), params, server.extraEnvs)
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
        ServerSetting s
        if (isDebug(variant)) {
            s = getSetting().debug
            s.isDebug = true
        } else {
            s = getSetting().release
        }
        if (s.batScriptPath == null) s.batScriptPath = setting.batScriptPath
        return s
    }

    private static boolean isDebug(Object variant) {
        return variant.name.contains("debug")
    }

    UploaderSetting getSetting() {
        return project.uploader
    }
}
