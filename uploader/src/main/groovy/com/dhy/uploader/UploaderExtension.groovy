package com.dhy.uploader

import com.alibaba.fastjson.JSONObject
import okhttp3.Request

class UploaderExtension {
    public Boolean fetchToken = true
    public Boolean enable = true
    public Boolean debugOn = true
    public String KEY_FILE = 'file'
    public String apkFile = null
    public String url = null
    public Map<String, ?> extras = new HashMap()
    public def onGetApk = { fileName, filePath, extras -> }
    public def initRequest = { Request.Builder reqBuilder -> }
    public def initFetchToken = { JSONObject tokenJson, Request.Builder reqBuilder -> }
}