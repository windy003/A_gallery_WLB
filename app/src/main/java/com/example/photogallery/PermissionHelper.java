package com.example.photogallery;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.content.ContextCompat;

public class PermissionHelper {
    
    /**
     * 获取读取媒体文件所需的权限
     * @return 根据安卓版本返回相应的权限
     */
    public static String getReadMediaPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            return Manifest.permission.READ_EXTERNAL_STORAGE;
        }
    }
    
    /**
     * 检查是否有读取媒体文件的权限
     * @param context 上下文
     * @return 是否有权限
     */
    public static boolean hasReadMediaPermission(Context context) {
        String permission = getReadMediaPermission();
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * 检查是否需要请求权限
     * @param context 上下文
     * @return 是否需要请求权限
     */
    public static boolean shouldRequestPermission(Context context) {
        return !hasReadMediaPermission(context);
    }
    
    /**
     * 获取当前安卓版本信息
     * @return 版本信息字符串
     */
    public static String getAndroidVersionInfo() {
        return "Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")";
    }
    
    /**
     * 检查是否为安卓8.0及以上版本 (API 26+)
     * @return 是否为安卓8.0+
     */
    public static boolean isAndroid8OrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }
    
    /**
     * 检查是否为安卓11及以上版本 (API 30+)
     * @return 是否为安卓11+
     */
    public static boolean isAndroid11OrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
    }
    
    /**
     * 检查是否为安卓13及以上版本 (API 33+)
     * @return 是否为安卓13+
     */
    public static boolean isAndroid13OrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
    }
} 