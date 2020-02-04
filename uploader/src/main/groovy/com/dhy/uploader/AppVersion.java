package com.dhy.uploader;

public class AppVersion {
    public int id;
    public String url;
    public String packagename;
    public String versiontype;
    public String message;
    public int versioncode;
    public String versionname;
    public long packagesize;

    static class Response {
        int code;
        AppVersion data;

        public boolean isOK() {
            return code == 0;
        }
    }
}
