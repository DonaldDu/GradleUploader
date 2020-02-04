package com.dhy.uploader;

import java.io.File;
import java.io.IOException;

import io.sigpipe.jbsdiff.ui.FileUI;

public class BSDiffUtil {
    public static File diff(String oldApkFolderPath, File patchFolder, File newApk, int newApkVersionCode) {
        AppVersion appVersion = NetUtil.fetchLatestApkVersion();
        if (appVersion == null) {
            System.out.println("********** no old app version found **********");
            return null;
        }

        File oldApkFolder = new File(oldApkFolderPath);
        if (!oldApkFolder.exists()) oldApkFolder.mkdirs();

        String apkFileName = getApkFileName(appVersion.url);
        File oldApk = new File(oldApkFolder, apkFileName);
        if (!oldApk.exists() || (oldApk.length() != appVersion.packagesize)) {
            if (oldApk.exists()) oldApk.delete();
            try {
                NetUtil.downloadFile(oldApk, appVersion.url);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        String newApkMD5 = SignUtils.getMd5ByFile(newApk);
        String patchFileName = formatPatchFileName(newApkMD5, appVersion.versioncode, newApkVersionCode);
        File patch = new File(patchFolder, patchFileName);
        try {
            FileUI.diff(oldApk, newApk, patch);
            return patch;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getApkFileName(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    private static String formatPatchFileName(String newApkMD5, int oldVersion, int newVersion) {
        return String.format("%s_%dv%d.patch.apk", newApkMD5, oldVersion, newVersion);
    }
}
