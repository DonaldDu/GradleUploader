package com.dhy.uploader;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Util {
    public static void excuteCMD(File bat, BatParams params, Map<String, ?> extraEnvs) throws Exception {
        String cmd = String.format("cd /d %s && start %s %s", bat.getParent(), bat.getAbsolutePath(), params.toEnvironmentFilePath(extraEnvs));
        excuteCMD(cmd);
    }

    public static void excuteCMD(String cmd) throws IOException {
        String path = System.getenv("path");
        String[] envp = new String[]{"path=" + path};

        String[] cmdarray = new String[]{"cmd", "/c", cmd};
        Runtime.getRuntime().exec(cmdarray, envp);
    }
}
