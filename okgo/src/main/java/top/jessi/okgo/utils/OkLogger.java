/*
 * Copyright 2016 jeasonlzy(廖子尧)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package top.jessi.okgo.utils;


/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧）Github地址：https://github.com/jeasonlzy
 * 版    本：1.0
 * 创建日期：2015/10/12
 * 描    述：日志的工具类
 * 修订历史：
 * ================================================
 * 2024.06.17
 * 1.更换日志打印至ILog框架，更加灵活控制日志输出
 */
public class OkLogger {

    public static void v(String msg) {
        ILog.v(msg);
    }

    public static void v(String tag, String msg) {
        ILog.v(tag + " --- " + msg);
    }

    public static void d(String msg) {
        ILog.d(msg);
    }

    public static void d(String tag, String msg) {
        ILog.d(tag + " --- " + msg);
    }

    public static void i(String msg) {
        ILog.i(msg);
    }

    public static void i(String tag, String msg) {
        ILog.i(tag + " --- " + msg);
    }

    public static void w(String msg) {
        ILog.w(msg);
    }

    public static void w(String tag, String msg) {
        ILog.w(tag + " --- " + msg);
    }

    public static void e(String msg) {
        ILog.e(msg);
    }

    public static void e(String tag, String msg) {
        ILog.e(tag + " --- " + msg);
    }

    public static void printStackTrace(Throwable t) {
        if (t != null) t.printStackTrace();
    }
}
