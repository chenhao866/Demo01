package com.zzas.demo01;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.lytwsw.weatherad.utils.APKVersionCodeUtils;
import com.lytwsw.weatherad.utils.DownloadUtils;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button myButton;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myButton = findViewById(R.id.myButton);
        myButton.setOnClickListener(this);
    }
    //执行更新操作
    protected void OnUpdateApp(){
        int versionCode = APKVersionCodeUtils.getVersionCode(this);//获取当前版本号
        String versionName = APKVersionCodeUtils.getVerName(this);//获取当前版本名称
        /*正常步骤为对通过接口请求服务端版本号信息，如果服务端版本号大于客户端版本号，那么提示更新*/
        //从服务端下载apk
        String url = "http://192.168.1.85:8080/vus/app-release.apk";//下载链接
        String appName ="app-release.apk";//apk名称
        new DownloadUtils(this, url, appName);//apk更新类
    }
    //点击事件
    @Override
    public void onClick(View v) {
        if (v.equals(myButton)){
            OnUpdateApp();
        }
    }
}
