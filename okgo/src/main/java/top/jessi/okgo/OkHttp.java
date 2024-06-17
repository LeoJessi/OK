package top.jessi.okgo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.webkit.WebSettings;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import top.jessi.ilog.ILog;

/**
 * Created by Jessi on 2022/8/13 10:41
 * Email：17324719944@189.cn
 * Describe：okhttp请求工具类
 */
public class OkHttp {
    // okhttp客户端
    private static volatile OkHttpClient sOkHttpClient = null;
    // okhttp请求信号量
    private static volatile Semaphore sSemaphore = null;
    // OkHttp的requestHeader参数map
    private Map<String, String> mHeaderMap;
    // OkHttp的request参数map
    // private Map<String, String> paramMap;
    private Map<String, Object> mParamMap;   // 传入的value是object可以防止数据类型乱码  如json转字符串会加"\"
    // OkHttp的请求url
    private String mUrl;
    // OkHttp的Request
    private Request.Builder mRequest;

    /**
     * 初始化okhttpClient，并允许https访问
     *
     * @param context 上下文   主要用户获取设备 User-Agent
     */
    private OkHttp(Context context) {
        if (sOkHttpClient == null) {
            synchronized (OkHttp.class) {
                if (sOkHttpClient == null) {
                    TrustManager[] trustManagers = buildTrustManagers();
                    sOkHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(15, TimeUnit.SECONDS)   // 连接超时时间
                            .writeTimeout(15, TimeUnit.SECONDS)     // 设置读超时
                            .readTimeout(15, TimeUnit.SECONDS)      // 设置写超时
                            .sslSocketFactory(createSSLSocketFactory(trustManagers),
                                    (X509TrustManager) trustManagers[0])
                            .hostnameVerifier(((hostname, session) -> true))
                            .retryOnConnectionFailure(true)     // 是否自动重连
                            .build();
                    // 给Okhttp设置User-Agent
                    // addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36
                    // (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
                    addHeader("User-Agent", getUserAgent(context));
                }
            }
        }
    }

    /**
     * 用于异步请求时，控制访问线程数，返回结果
     *
     * @return 信息量
     */
    private static Semaphore getSemaphoreInstance() {
        /*只能1个线程同时访问*/
        synchronized (OkHttp.class) {
            if (sSemaphore == null) sSemaphore = new Semaphore(0);
        }
        return sSemaphore;
    }

    /**
     * 创建OkHttp
     *
     * @param context 上下文
     * @return Okhttp对象
     */
    public static OkHttp builder(Context context) {
        return new OkHttp(context);
    }

    /**
     * 添加url
     *
     * @param url 请求地址
     * @return 当前实例
     */
    public OkHttp url(String url) {
        ILog.d("OkHttp request url", url);
        this.mUrl = url;
        return this;
    }

    /**
     * 添加请求头
     *
     * @param key   参数名
     * @param value 参数值
     * @return 当前实例
     */
    public OkHttp addHeader(String key, String value) {
        if (mHeaderMap == null) {
            mHeaderMap = new LinkedHashMap<>(16);
        }
        mHeaderMap.put(key, value);
        return this;
    }

    /*
     * 添加参数
     * key   参数名
     * value 参数值
     * */

    /**
     * 添加参数
     *
     * @param key   参数名
     * @param value 参数值
     * @return 当前实例
     */
    public OkHttp addParam(String key, Object value) {
        if (mParamMap == null) {
            mParamMap = new LinkedHashMap<>(16);
        }
        mParamMap.put(key, value);
        return this;
    }

    /**
     * 为request添加请求头
     *
     * @param request 请求头
     */
    private void setHeader(Request.Builder request) {
        if (mHeaderMap != null) {
            try {
                for (Map.Entry<String, String> entry : mHeaderMap.entrySet()) {
                    request.addHeader(entry.getKey(), entry.getValue());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化get方法
     *
     * @return 当前实例
     */
    public OkHttp get() {
        mRequest = new Request.Builder().get();
        StringBuilder urlBuilder = new StringBuilder(mUrl);
        if (mParamMap != null) {
            urlBuilder.append("?");
            try {
                for (Map.Entry<String, Object> entry : mParamMap.entrySet()) {
                    urlBuilder.append(URLEncoder.encode(entry.getKey(), "utf-8"))
                            .append("=")
                            //.append(URLEncoder.encode(entry.getValue(), "utf-8"))
                            .append(URLEncoder.encode(entry.getValue().toString(), "utf-8"))    // 将map的value改为object
                            // 后需要转换类型
                            .append("&");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        }
        mRequest.url(urlBuilder.toString());
        ILog.d("OkHttp get to send", urlBuilder.toString());
        return this;
    }

    /**
     * 初始化post方法
     *
     * @param isJsonPost true等于json的方式提交数据，类似postman里post方法的raw
     *                   false等于普通的表单提交
     * @return 当前实例
     */
    public OkHttp post(boolean isJsonPost) {
        RequestBody requestBody;
        if (isJsonPost) {
            String json = "";
            if (mParamMap != null) {
                JSONObject jsonObject = new JSONObject(mParamMap);
                json = String.valueOf(jsonObject);
                // json = Json.convertObjectToJSON(paramMap);
            }
            ILog.d("OkHttp post to send", json);
            requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json);
        } else {
            FormBody.Builder formBody = new FormBody.Builder();
            if (mParamMap != null) {
                /*编译版本需Android7以上*/
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    // paramMap.forEach(formBody::add);  //add方法在forEach时不能转换类型
                    for (Map.Entry<String, String> entry : mHeaderMap.entrySet()) {
                        formBody.add(entry.getKey(), entry.getValue());
                    }
                }
            }
            ILog.d("OkHttp post to send", formBody.toString());
            requestBody = formBody.build();
        }
        mRequest = new Request.Builder().post(requestBody).url(mUrl);
        return this;
    }

    /**
     * 上传文件
     *
     * @param file     文件对象
     * @param fileName 文件名
     * @return 当前实例
     */
    public OkHttp uploadFile(String fileName, File file) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, RequestBody.create(MediaType.parse("multipart/form-data"), file))
                .build();
        mRequest = new Request.Builder().post(requestBody).url(mUrl);
        return this;
    }

    /**
     * 多文件上传
     *
     * @param filesMap 文件对象map
     * @return 当前实例
     */
    public OkHttp uploadFile(HashMap<String, File> filesMap) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        for (Map.Entry<String, File> entry : filesMap.entrySet()) {
            builder.addFormDataPart("file", entry.getKey(),
                    RequestBody.create(MediaType.parse("multipart/form-data"), entry.getValue()));
        }
        RequestBody requestBody = builder.setType(MultipartBody.FORM)
                .build();
        mRequest = new Request.Builder().post(requestBody).url(mUrl);
        return this;
    }

    /**
     * 同步请求
     *
     * @return 请求响应
     */
    public String sync() {
        setHeader(mRequest);
        try {
            Response response = sOkHttpClient.newCall(mRequest.build()).execute();
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                String res = responseBody.string();
                ILog.d("OkHttp sync response", res);
                return res;
            }
            ILog.w("OkHttp sync response", "No data returned");
            return "No data returned";
        } catch (IOException e) {
            e.printStackTrace();
            ILog.w("OkHttp sync response", e.getMessage());
            return "Request failed：" + e.getMessage();
        }
    }

    /**
     * 异步请求，有返回值
     * 直接返回结果
     *
     * @return 请求响应
     */
    public String async() {
        StringBuilder buffer = new StringBuilder();
        setHeader(mRequest);
        sOkHttpClient.newCall(mRequest.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                buffer.append("Request failed：").append(e.getMessage());
                // 后加，控制发布线程
                getSemaphoreInstance().release();
                ILog.w("OkHttp async response", buffer.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.body() != null) {
                    buffer.append(response.body().string());
                    getSemaphoreInstance().release();
                    ILog.d("OkHttp async response", buffer.toString());
                } else {
                    ILog.w("OkHttp async response", "No data returned");
                }
            }
        });
        try {
            // 获取线程
            getSemaphoreInstance().acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }

    /**
     * 异步请求，带有接口回调，可在接口做更多处理
     *
     * @param callBack 回调接口
     */
    public void async(CallBack callBack) {
        setHeader(mRequest);
        sOkHttpClient.newCall(mRequest.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callBack.onFailure(call, e.getMessage());
                ILog.w("OkHttp async response", e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                /*响应体response.body只能被使用一次  因为response.body().string()之后，response中的流会被关闭，程序会报错*/
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    String res = responseBody.string();
                    callBack.onSuccessful(call, res);
                    ILog.d("OkHttp async response", res);
                } else {
                    ILog.w("OkHttp async response", "No data returned");
                }
            }
        });
    }

    /**
     * 生成安全套接字工厂，用于https请求的证书跳过
     *
     * @param trustManagers TrustManager
     * @return SSLSocketFactory
     */
    private static SSLSocketFactory createSSLSocketFactory(TrustManager[] trustManagers) {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustManagers, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ssfFactory;
    }

    private TrustManager[] buildTrustManagers() {
        return new TrustManager[]{new X509TrustManager() {
            @SuppressLint("TrustAllX509TrustManager")
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @SuppressLint("TrustAllX509TrustManager")
            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }
        };
    }

    /**
     * 回调接口
     */
    public interface CallBack {
        // 请求成功处理
        void onSuccessful(Call call, String data);

        // 请求失败处理
        void onFailure(Call call, String errorMsg);
    }

    /**
     * 获取设备User-Agent
     *
     * @param context 传入当前上下文
     * @return 设备User-Agent
     */
    public static String getUserAgent(Context context) {
        String userAgent;
        try {
            userAgent = WebSettings.getDefaultUserAgent(context);
        } catch (Exception e) {
            userAgent = System.getProperty("http.agent");
        }
        StringBuilder sb = new StringBuilder();
        if (userAgent == null) return "";
        /*对返回的数据进行过滤，如是中文则转码*/
        for (int i = 0, len = userAgent.length(); i < len; i++) {
            char c = userAgent.charAt(i);
            if (c <= '\u001f' || c >= '\u007f') {
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
