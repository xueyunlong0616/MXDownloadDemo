package com.mx.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mx.download.MXDownload;
import com.mx.download.model.DownloadStatus;
import com.mx.download.utils.IDownLoadCall;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends Activity {

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.curSize)
    TextView curSize;

    @BindView(R.id.maxSize)
    TextView maxSize;

    @BindView(R.id.start)
    Button start;

    @BindView(R.id.stop)
    Button stop;

    MXDownload mxDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mxDownload == null) {
                    new File("/sdcard/weixin.apk").delete();
                    mxDownload = MXDownload.getInstance()
                            .download("http://a6.pc6.com/kha5/laojiumen.360.apk", "/sdcard/weixin.apk")
                            .maxThread(6)
                            .maxRetryCount(6)
                            .addMainThreadCall(new IDownLoadCall() {
                                @Override
                                public void onPrepare(String url) {
                                    progressBar.setProgress(0);
                                    progressBar.setMax(100);
                                }

                                @Override
                                public void onStart(DownloadStatus status) {
                                    progressBar.setProgress((int) (status.getPercent() * 100));
                                }

                                @Override
                                public void onError(Throwable th) {
                                }

                                @Override
                                public void onProgressUpdate(DownloadStatus status) {
                                    Log.v("proc", status.getFormatStatusString());
                                    progressBar.setProgress((int) (status.getPercent() * 100));
                                    curSize.setText(status.getFormatDownloadSize());
                                    maxSize.setText(status.getFormatTotalSize());
                                }

                                @Override
                                public void onFinish(String url) {

                                }

                                @Override
                                public void onCancel(String url) {

                                }
                            })
                            .start();
                } else {
                    mxDownload.start();
                }
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mxDownload != null) mxDownload.cancel();
            }
        });
    }
}
