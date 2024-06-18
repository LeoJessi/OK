package top.jessi.okgo.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;


/**
 * 日志类
 */
public class ILog {

    /**
     * 用于自定义TAG
     */
    private static final String LOG_TAG = "OK";
    /**
     * 日志前缀
     */
    private static final String LOG_PRE = " <||> ";
    /**
     * 是否输出日志
     */
    private static boolean IS_SECURITY_LOG = false;
    /**
     * 是否输出Log的位置，true:输出；false:不输出
     */
    private static boolean IS_LOG_POSITION = false;
    /**
     * 日志分隔字符
     */
    private static final String LOG_SPLIT = "  \t<==>  ";

    String PATH_LOGCAT = "";

    public void init(Context context) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中
            PATH_LOGCAT = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + File.separator + "Log";
        } else {// 如果SD卡不存在，就保存到本应用的目录下
            PATH_LOGCAT = context.getFilesDir().getAbsolutePath()
                    + File.separator + "Log";
        }
        File file = new File(PATH_LOGCAT);
        if (!file.exists()) {
            boolean b = file.mkdirs();
        }
    }

    /**
     * 输出浏览级别Log
     *
     * @param tag 标签
     * @param msg 信息
     */
    public static void v(String tag, String msg) {
        if (!IS_SECURITY_LOG) {
            return;
        }
        tag = (tag != null ? tag : LOG_TAG);
        String logMsg = (msg == null ? "" : msg);

        if (IS_LOG_POSITION) {
            logMsg = getPositionInfo() + LOG_SPLIT + logMsg;
        }
        Log.v(LOG_PRE + tag, logMsg);
    }

    public static void v(String msg) {
        if (!IS_SECURITY_LOG) {
            return;
        }
        String logMsg = (msg == null ? "" : msg);
        if (IS_LOG_POSITION) {
            logMsg = getPositionInfo() + LOG_SPLIT + logMsg;
        }
        Log.v(LOG_PRE + LOG_TAG, logMsg);

    }

    /**
     * 输出调试级别Log
     *
     * @param tag 标签
     * @param msg 信息
     */
    public static void d(String tag, String msg) {
        if (!IS_SECURITY_LOG) {
            return;
        }
        tag = (tag != null ? tag : LOG_TAG);
        String logMsg = (msg == null ? "" : msg);

        if (IS_LOG_POSITION) {
            logMsg = getPositionInfo() + LOG_SPLIT + logMsg;
        }
        Log.d(LOG_PRE + tag, logMsg);
    }

    public static void d(String msg) {
        if (!IS_SECURITY_LOG) {
            return;
        }
        String logMsg = (msg == null ? "" : msg);
        if (IS_LOG_POSITION) {
            logMsg = getPositionInfo() + LOG_SPLIT + logMsg;
        }
        Log.d(LOG_PRE + LOG_TAG, logMsg);

    }

    /**
     * 输出信息级别
     *
     * @param tag tag
     * @param msg 信息
     */
    public static void i(String tag, String msg) {
        if (!IS_SECURITY_LOG) {
            return;
        }
        tag = (tag != null ? tag : LOG_TAG);
        String logMsg = (msg == null ? "" : msg);

        if (IS_LOG_POSITION) {
            logMsg = getPositionInfo() + LOG_SPLIT + logMsg;
        }
        Log.i(LOG_PRE + tag, logMsg);
    }

    public static void i(String msg) {
        if (!IS_SECURITY_LOG) {
            return;
        }
        String logMsg = (msg == null ? "" : msg);
        if (IS_LOG_POSITION) {
            logMsg = getPositionInfo() + LOG_SPLIT + logMsg;
        }
        Log.i(LOG_PRE + LOG_TAG, logMsg);
    }

    /**
     * 输出警告级别Log
     *
     * @param tag 标签
     * @param msg 信息
     */
    public static void w(String tag, String msg) {
        if (!IS_SECURITY_LOG) {
            return;
        }
        tag = (tag != null ? tag : LOG_TAG);
        String logMsg = (msg == null ? "" : msg);

        if (IS_LOG_POSITION) {
            logMsg = getPositionInfo() + LOG_SPLIT + logMsg;
        }
        Log.w(LOG_PRE + tag, logMsg);
    }

    public static void w(String msg) {
        if (!IS_SECURITY_LOG) {
            return;
        }
        String logMsg = (msg == null ? "" : msg);
        if (IS_LOG_POSITION) {
            logMsg = getPositionInfo() + LOG_SPLIT + logMsg;
        }
        Log.w(LOG_PRE + LOG_TAG, logMsg);

    }

    /**
     * 输出错误级别Log
     *
     * @param tag tag
     * @param msg 信息
     */
    public static void e(String tag, String msg) {
        if (!IS_SECURITY_LOG) {
            return;
        }
        tag = (tag != null ? tag : LOG_TAG);
        String logMsg = (msg == null ? "" : msg);

        if (IS_LOG_POSITION) {
            logMsg = getPositionInfo() + LOG_SPLIT + logMsg;
        }
        Log.e(LOG_PRE + tag, logMsg);
    }

    public static void e(String msg) {
        if (!IS_SECURITY_LOG) {
            return;
        }
        String logMsg = (msg == null ? "" : msg);
        if (IS_LOG_POSITION) {
            logMsg = getPositionInfo() + LOG_SPLIT + logMsg;
        }
        Log.e(LOG_PRE + LOG_TAG, logMsg);
    }


    /**
     * 获取Log信息
     *
     * @return log信息
     */
    public static String getPositionInfo() {
        StackTraceElement element = new Throwable().getStackTrace()[2];
        return element.getFileName() + " -- Line：" + element.getLineNumber() + " -- Method: " + element.getMethodName();
    }

    public static boolean isIsSecurityLog() {
        return IS_SECURITY_LOG;
    }

    public static void setIsSecurityLog(boolean isSecurityLog) {
        IS_SECURITY_LOG = isSecurityLog;
    }

    public static boolean isIsLogPosition() {
        return IS_LOG_POSITION;
    }

    public static void setIsLogPosition(boolean isLogPosition) {
        IS_LOG_POSITION = isLogPosition;
    }
}

