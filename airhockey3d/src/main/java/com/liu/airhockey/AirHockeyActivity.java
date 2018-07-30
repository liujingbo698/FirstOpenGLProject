package com.liu.airhockey;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

public class AirHockeyActivity extends Activity {
    private GLSurfaceView glSurfaceView;
    private boolean rendererSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glSurfaceView = new GLSurfaceView(this);

        // 判断当前 Android版本是否支持 OpenGL 2.0
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        // 这段代码不能再模拟器上工作，因为GPU模拟部分缺陷
//        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

        // 适配模拟器
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000
                // 版本SDK版本号大于等于15并且后面的至少有个为真
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                // 以下至少一个为真
                && (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")));

        if (supportsEs2) {
            // 请求一个 OpenGL ES 2.0 兼容上下文
            glSurfaceView.setEGLContextClientVersion(2);

            // 分配渲染器
            glSurfaceView.setRenderer(new AirHockeyRenderer(this));
            rendererSet = true;

        } else {
            Toast.makeText(this, "该设备部支持 OpenGLES2.0", Toast.LENGTH_SHORT).show();
            return;
        }

        setContentView(glSurfaceView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rendererSet)
            glSurfaceView.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rendererSet)
            glSurfaceView.onResume();
    }
}