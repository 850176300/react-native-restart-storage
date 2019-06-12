package com.avishayil.rnrestart;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Avishay on 7/17/16.
 */
public class ReactNativeRestart extends ReactContextBaseJavaModule {

    private static final String REACT_APPLICATION_CLASS_NAME = "com.facebook.react.ReactApplication";
    private static final String REACT_NATIVE_HOST_CLASS_NAME = "com.facebook.react.ReactNativeHost";

    private LifecycleEventListener mLifecycleEventListener = null;

    public ReactNativeRestart(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    private void loadBundleLegacy() {
        final Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            // The currentActivity can be null if it is backgrounded / destroyed, so we simply
            // no-op to prevent any null pointer exceptions.
            return;
        }

        currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                currentActivity.recreate();
            }
        });
    }

    private void loadBundle() {
        clearLifecycleEventListener();
        try {
            final ReactInstanceManager instanceManager = resolveInstanceManager();
            if (instanceManager == null) {
                return;
            }

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    try {
                        instanceManager.recreateReactContextInBackground();
                    } catch (Exception e) {
                        loadBundleLegacy();
                    }
                }
            });

        } catch (Exception e) {
            loadBundleLegacy();
        }
    }

    private static ReactInstanceHolder mReactInstanceHolder;

    static ReactInstanceManager getReactInstanceManager() {
        if (mReactInstanceHolder == null) {
            return null;
        }
        return mReactInstanceHolder.getReactInstanceManager();
    }

    private ReactInstanceManager resolveInstanceManager() throws NoSuchFieldException, IllegalAccessException {
        ReactInstanceManager instanceManager = getReactInstanceManager();
        if (instanceManager != null) {
            return instanceManager;
        }

        final Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            return null;
        }

        ReactApplication reactApplication = (ReactApplication) currentActivity.getApplication();
        instanceManager = reactApplication.getReactNativeHost().getReactInstanceManager();

        return instanceManager;
    }


    private void clearLifecycleEventListener() {
        // Remove LifecycleEventListener to prevent infinite restart loop
        if (mLifecycleEventListener != null) {
            getReactApplicationContext().removeLifecycleEventListener(mLifecycleEventListener);
            mLifecycleEventListener = null;
        }
    }

    @ReactMethod
    public void Restart() {
        loadBundle();
    }


    @ReactMethod
    public void StoreStatus(String newValue){
        SharedPreferences dataBase = getCurrentActivity().getSharedPreferences("AppCacheData", Activity.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = dataBase.edit();
        mEditor.putString("appStatus", newValue);
        mEditor.commit();
    }

    @ReactMethod
    public void GetAppStatus(Promise p){
        SharedPreferences dataBase = getCurrentActivity().getSharedPreferences("AppCacheData", Activity.MODE_PRIVATE);
        String appStatus = dataBase.getString("appStatus", "normal");
        p.resolve(appStatus);
    }

    @ReactMethod
    public void StoreValue(String key, String value){
        SharedPreferences dataBase = getCurrentActivity().getSharedPreferences("AppCacheData", Activity.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = dataBase.edit();
        mEditor.putString(key, value);
        mEditor.commit();
    }

    @ReactMethod
    public void FetchCache(String key, Promise p){
        SharedPreferences dataBase = getCurrentActivity().getSharedPreferences("AppCacheData", Activity.MODE_PRIVATE);
        String value = dataBase.getString(key, "null");
        p.resolve(value);
    }

    @ReactMethod
    public void FetchAllStorageData(Promise p) {
        SharedPreferences dataBase = getCurrentActivity().getSharedPreferences("AppCacheData", Activity.MODE_PRIVATE);
        Map<String, ?> allDatas = dataBase.getAll();
        if (allDatas.size() > 0){
            JSONObject json = new JSONObject();
            for (Map.Entry<String, ?> entry : allDatas.entrySet()) {
                {
                    try {
                        json.put(entry.getKey(), entry.getValue());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        p.reject("100", "json数据初始化错误");
                    }
                }
            }
            p.resolve(json.toString());
        }else {
            p.resolve("null");
        }
    }

    @ReactMethod
    public void RemoveOneKey(String key){
        SharedPreferences dataBase = getCurrentActivity().getSharedPreferences("AppCacheData", Activity.MODE_PRIVATE);
        if (dataBase.contains(key)){
            SharedPreferences.Editor mEditor = dataBase.edit();
            mEditor.remove(key);
            mEditor.commit();
        }
    }

    @ReactMethod
    public void ClearAllData(){
        SharedPreferences dataBase = getCurrentActivity().getSharedPreferences("AppCacheData", Activity.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = dataBase.edit();
        mEditor.clear();
        mEditor.commit();
    }

    @Override
    public String getName() {
        return "RNRestart";
    }

}
