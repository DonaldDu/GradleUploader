package com.dhy.uploader

import com.alibaba.fastjson.JSONObject

class ServerSetting {
    public String apkVersionUrl
    public String appId
    public boolean isDebug
    public Map<String, ?> extraEnvs
    public String batScriptPath = null
    public Closure onGetVersionResponse = { JSONObject json, String KEY_APK_URL, String KEY_APK_VERSION_CODE ->
        json.getJSONArray("").getString("")
        json.put(KEY_APK_URL, null)
        json.put(KEY_APK_VERSION_CODE, null)
    }
}
