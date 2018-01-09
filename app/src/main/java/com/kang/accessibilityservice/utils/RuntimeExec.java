package com.kang.accessibilityservice.utils;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.util.Log;

import com.kang.accessibilityservice.BuildConfig;

/**
 * Android 程序执行Shell指令工具类
 * Created by kangren on 2018/1/5.
 */

public class RuntimeExec {

    public static final String TAG = RuntimeExec.class.getSimpleName();
    public static final String COMMAND_SH = "sh";
    public static final String COMMAND_LINE_END = "\n";
    public static final String COMMAND_EXIT = "exit\n";


    /**
     * 只执行，不返回结果
     * @param command 命令
     */
    public static void justExecute(String command)
    {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 执行单条命令
     *
     * @param command
     * @return
     */
    public static List<String> execute(String command) {
        return execute(new String[] { command });
    }

    /**
     * 可执行多行命令（bat）
     *
     * @param commands
     * @return
     */
    public static List<String> execute(String[] commands) {
        List<String> results = new ArrayList<String>();
        int status = -1;
        if (commands == null || commands.length == 0) {
            return null;
        }
        debug("execute command start : " + commands);
        Process process = null;
        BufferedReader successReader = null;
        BufferedReader errorReader = null;
        StringBuilder errorMsg = null;

        DataOutputStream dos = null;
        try {
            process = Runtime.getRuntime().exec(COMMAND_SH);
            dos = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }
                dos.write(command.getBytes());
                dos.writeBytes(COMMAND_LINE_END);
                dos.flush();
            }
            dos.writeBytes(COMMAND_EXIT);
            dos.flush();

            status = process.waitFor();

            errorMsg = new StringBuilder();
            successReader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            errorReader = new BufferedReader(new InputStreamReader(
                    process.getErrorStream()));
            String lineStr;
            while ((lineStr = successReader.readLine()) != null) {
                results.add(lineStr);
                debug(" command line item : " + lineStr);
            }
            while ((lineStr = errorReader.readLine()) != null) {
                errorMsg.append(lineStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (dos != null) {
                    dos.close();
                }
                if (successReader != null) {
                    successReader.close();
                }
                if (errorReader != null) {
                    errorReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (process != null) {
                process.destroy();
            }
        }
        debug(String.format(Locale.CHINA,
                "execute command end,errorMsg:%s,and status %d: ", errorMsg,
                status));
        return results;
    }

    private static void debug(String message) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message);
        }
    }

}