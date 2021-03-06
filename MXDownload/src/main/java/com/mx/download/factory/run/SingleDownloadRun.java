package com.mx.download.factory.run;

import com.mx.download.factory.SpeedInterceptor;
import com.mx.download.model.DownChipBean;
import com.mx.download.utils.Log;
import com.mx.download.utils.Utils;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 创建人： zhangmengxiong
 * 创建时间： 2016-11-22.
 * 联系方式: zmx_final@163.com
 */
public class SingleDownloadRun implements Runnable {
    private int TIME_OUT = 20 * 1000;// 超时
    private String sourceUrl;// 资源路径
    private String savePath;
    private String fileName;
    private DownChipBean chipBeen;// 下载开始位置
    private AtomicBoolean isStop = new AtomicBoolean(false);// 该线程外部停止标记
    private AtomicBoolean errorTag = new AtomicBoolean(false);// 该线程外部停止标记
    private SpeedInterceptor speedInterceptor;

    public SingleDownloadRun(String fromUrl, String savePath, DownChipBean chipBeen, SpeedInterceptor interceptor, int time_out) {
        this.sourceUrl = fromUrl;
        this.savePath = savePath;
        this.chipBeen = chipBeen;
        this.isStop.set(false);
        this.errorTag.set(false);
        this.TIME_OUT = time_out;
        speedInterceptor = interceptor;

        fileName = new File(savePath).getName();
    }

    @Override
    public void run() {
        if (chipBeen.isComplete()) {
            isStop.set(false);
            errorTag.set(false);
            return;
        }
        if (isStop.get()) {
            Log.v(fileName + " -- " + chipBeen + "被终止");
            return;
        }
        Log.v(fileName + " -- " + chipBeen + "开始执行");

        HttpURLConnection conn = null;
        InputStream is = null;
        SaveFile saveFile = null;
        try {
            saveFile = new SaveFile(savePath, chipBeen.getSeek(), 0);

            URL url = new URL(this.sourceUrl);// 创建URL对象
            conn = (HttpURLConnection) url.openConnection();// 创建URL连接
            conn.setConnectTimeout(TIME_OUT);// 设置连接超时时间为10000ms
            conn.setReadTimeout(TIME_OUT); // 设置读取数据超时时间为10000ms
            String property = "bytes=" + chipBeen.getSeek() + "-";// 开始下载位置
            conn.setRequestProperty("RANGE", property);

            int response = conn.getResponseCode();
            if (response >= HttpURLConnection.HTTP_OK && response <= HttpURLConnection.HTTP_PARTIAL) {
                is = conn.getInputStream(); // 获取文件输入流，读取文件内容
                byte[] buff = new byte[1024 * 16];// 创建缓冲区
                int length;
                while (((length = is.read(buff)) > 0)) {
                    if (isStop.get() || Thread.interrupted()) break;

                    saveFile.write(buff, length); // 写入文件内容
                    chipBeen.addDownloadSize(length);
                    if (chipBeen.isComplete()) break;
                    if (speedInterceptor != null) speedInterceptor.interceptor();
                }

                Log.v(fileName + " --> " + chipBeen);
            } else {
                throw new Exception("获取错误的状态码：" + response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.v(fileName + " -- " + chipBeen + "出现错误！即将退出线程。");
            errorTag.set(true);
        } finally {
            if (saveFile != null) {
                saveFile.close();// 关闭打开的文件
            }
            Utils.closeSilent(is);
            try {
                if (conn != null) conn.disconnect();
            } catch (Exception ignored) {
            }
        }
    }

    public boolean isDownloadOver()// 返回该线程下载是否完成的标志
    {
        return chipBeen.isComplete();
    }

    public void stop() {
        isStop.set(true);
    }

    public boolean isInError() {
        return errorTag.get();
    }
}
