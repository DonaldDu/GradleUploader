package com.dhy.uploader

class UploaderExtension {
    public Boolean enable = true
    public Boolean debugOn = true
    public String KEY_FILE = 'file'
    public String apkFile = null
    public String url = null
    public Map<String, ?> extras = new HashMap()
    public def onGetApk = { fileName, filePath, extras -> }
}