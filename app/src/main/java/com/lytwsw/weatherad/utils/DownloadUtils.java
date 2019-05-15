package com.lytwsw.weatherad.utils;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 * author: Created by lixiaotong on 2018/10/17
 * e-mail: 516030811@qq.com
 */
public class DownloadUtils {
    //下载器
    private DownloadManager downloadManager;
    private Context mContext;
    //下载的ID
    private long downloadId;
    private String name;
    private String pathstr;
    //构造函数
    public DownloadUtils(Context context, String url, String name) {
        this.mContext = context;
        downloadAPK(url, name);
        this.name = name;
    }
    //下载apk
    private void downloadAPK(String url, String name) {
        //创建下载任务
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedOverRoaming(false);//移动网络情况下是否允许漫游
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);//在通知栏中显示，默认就是显示的
        request.setTitle("下载");
        request.setDescription("新版点检下载中...");
        request.setVisibleInDownloadsUi(true);

        //设置下载的路径
        File file = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), name);
        request.setDestinationUri(Uri.fromFile(file));
        pathstr = file.getAbsolutePath();
        DeleteFile(pathstr); //根据路径删除已经存在的文件
        //获取DownloadManager
        if (downloadManager == null)
            downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        //将下载请求加入下载队列，加入下载队列后会给该任务返回一个long型的id，通过该id可以取消任务，重启任务、获取下载的文件等等
        if (downloadManager != null) {
            downloadId = downloadManager.enqueue(request);//执行下载任务
        }
        //注册广播接收者，监听下载状态
        mContext.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
    //广播监听下载的各个状态
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkStatus();
        }
    };
    //检查下载状态
    private void checkStatus() {
        DownloadManager.Query query = new DownloadManager.Query();//通过DownloadManager类获取现在进程信息
        //通过下载的id查找
        query.setFilterById(downloadId);
        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                case DownloadManager.STATUS_PAUSED://下载暂停
                    break;
                case DownloadManager.STATUS_PENDING://下载延迟
                    break;
                case DownloadManager.STATUS_RUNNING://正在下载
                    break;
                case DownloadManager.STATUS_SUCCESSFUL://下载完成
                    installAPK();//下载完成安装APK
                    cursor.close();
                    mContext.unregisterReceiver(receiver);//移除广播
                    break;
                case DownloadManager.STATUS_FAILED://下载失败
                    Toast.makeText(mContext, "下载失败", Toast.LENGTH_SHORT).show();
                    cursor.close();
                    mContext.unregisterReceiver(receiver);//移除广播
                    break;
            }
        }
    }
    //安装apk
    private void installAPK() {
        setPermission(pathstr);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // 由于没有在Activity环境下启动Activity,设置下面的标签
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //Android 7.0以上要使用FileProvider
        if (Build.VERSION.SDK_INT >= 24) {
            File file = (new File(pathstr));
            //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
            Uri apkUri = FileProvider.getUriForFile(mContext, "com.zzas.demo01.fileprovider", file);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            File file = new File(pathstr);
            Uri uri = Uri.fromFile(file);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        }
        mContext.startActivity(intent);
    }
    //修改文件权限
    private void setPermission(String absolutePath) {
        String command = "chmod " + "777" + " " + absolutePath;
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    //根据路径删除已经存在的文件7.0以上
    private void DeleteFile(String apkPath){
        File file = new File(apkPath);
        Boolean off = file.exists();
        if(off){//如果存在就删除对应文件
            if (Build.VERSION.SDK_INT >= 24) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri apkUri = FileProvider.getUriForFile(mContext, "com.zzas.demo01.fileprovider", file);
                //添加这一句表示对目标应用临时授权该Uri所代表的文件
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                mContext.getContentResolver().delete(apkUri,null,null);
            }else {
                file.delete();
            }
        }
    }
}