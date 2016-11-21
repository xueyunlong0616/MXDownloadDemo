package com.mx.download.utils;

import com.mx.download.model.DownloadStatus;

public abstract class IDownLoadCall {

    /**
     * 开始初始化
     *
     * @param url
     */
    public void onPrepare(String url) {
    }

    /**
     * 开始下载
     *
     * @param status 下载的信息
     */
    public void onStart(DownloadStatus status) {
    }

    /**
     * 下载失败
     *
     * @param th 错误消息
     */
    public void onError(Throwable th) {
    }

    /**
     * 下载进度更新
     *
     * @param status
     */
    public void onProgressUpdate(DownloadStatus status) {
    }

    /**
     * 下载进程结束
     *
     * @param url
     */
    public void onFinish(String url) {
    }

    /**
     * 下载被手动终止
     *
     * @param url
     */
    public void onCancel(String url) {
    }
}
