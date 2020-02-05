package com.dhy.uploader

class UploaderSetting {
    public Boolean enable = true
    public Boolean debugOn = true
    public String updateLogFileName = 'DeployLog.log'
    public String updateLogFileEncoding = 'GBK'
    public String batScriptPath = null

//    public ServerSetting debug = null   //extensions
//    public ServerSetting release = null //extensions

    public String oldApkFolder = null
    public def apkVersionCode = null
    public String apkVersionName = null
}