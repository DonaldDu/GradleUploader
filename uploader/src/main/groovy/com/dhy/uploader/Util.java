package com.dhy.uploader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Util {
    public static void excuteCMD(File bat, BatParams params) throws Exception {
        String cmd = String.format("cd %s && start %s %s", bat.getParent(), bat.getAbsolutePath(), params.toEnvironmentFilePath());
        excuteCMD(cmd);
    }

    public static void excuteCMD(String cmd) throws IOException {
        String path = System.getenv("path");
        String[] envp = new String[]{"path=" + path};

        String[] cmdarray = new String[]{"cmd", "/c", cmd};
        Runtime.getRuntime().exec(cmdarray, envp);
    }

    public static String excuteCommand(String cmd) {
        StringBuilder stringBuilder = new StringBuilder();
        Process process;
        try {
            String path = System.getenv("path");
            String[] envp = new String[]{"path=" + path};

            String[] cmdarray = new String[]{"cmd", "/c", cmd};
            process = Runtime.getRuntime().exec(cmdarray, envp);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
