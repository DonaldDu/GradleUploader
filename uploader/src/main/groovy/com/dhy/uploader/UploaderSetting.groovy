package com.dhy.uploader

class UploaderSetting {
    public Boolean enable = true
    public Boolean debugOn = true
    public String KEY_FILE = 'file'
    public String apkFile = null

//    public ServerSetting debugServer = null   //extensions
//    public ServerSetting releaseServer = null //extensions

    public String oldApkFolder = null
    public Map<String, ?> extras = new HashMap()
    public def onGetApk = { fileName, filePath, extras -> }
}