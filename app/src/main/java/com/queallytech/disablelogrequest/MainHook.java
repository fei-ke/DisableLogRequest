package com.queallytech.disablelogrequest;

import android.os.Message;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    private static final String LOG_TAG = "DisableLogsRequest";
    private static final String LOGCAT_PACKAGE = "com.android.server.logcat.LogcatManagerService";

    private static Object mActivityManagerInternal;
    private static Object mLogcatManagerService;
    private static Prefs mPrefs = new Prefs(null);

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        Log.d(LOG_TAG, "handleLoadPackage()");
        if (lpparam.packageName.equals("android")) {
            try {
                XposedHelpers.findAndHookMethod(LOGCAT_PACKAGE, lpparam.classLoader, "onStart", onStartHook());

                Class<?> handlerClass = XposedHelpers.findClass(LOGCAT_PACKAGE + "$LogAccessRequestHandler", lpparam.classLoader);
                XposedHelpers.findAndHookMethod(handlerClass, "handleMessage", Message.class, processNewLogAccessRequestHook());
            } catch (Throwable t) {
                Log.e(LOG_TAG, "Failed to hook LogcatManagerService methods", t);
            }
        }
    }

    private static XC_MethodHook onStartHook() {
        return new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    Log.d(LOG_TAG, "hook after onStart()");

                    mLogcatManagerService = param.thisObject;
                    mActivityManagerInternal = XposedHelpers.getObjectField(mLogcatManagerService, "mActivityManagerInternal");
                } catch (Throwable t) {
                    Log.e(LOG_TAG, "Failed to hook onStart()", t);
                }
            }
        };
    }

    private static XC_MethodHook processNewLogAccessRequestHook() {
        return new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    Message msg = (Message) param.args[0];
                    if (msg.what != 0) return;

                    Log.d(LOG_TAG, "hook before processNewLogAccessRequest()");

                    Object client = XposedHelpers.callMethod(mLogcatManagerService, "getClientForRequest", msg.obj);
                    if (client == null || mActivityManagerInternal == null) return;

                    int uid = XposedHelpers.getIntField(client, "mUid");
                    String packageName = (String) XposedHelpers.getObjectField(client, "mPackageName");

                    mPrefs.reload();
                    boolean logsDisabled = mPrefs.isLogsDisabled();
                    if (logsDisabled) {
                        XposedHelpers.callMethod(mLogcatManagerService, "onAccessDeclinedForClient", client);
                    } else {
                        XposedHelpers.callMethod(mLogcatManagerService, "onAccessApprovedForClient", client);
                    }
                    Log.i(LOG_TAG, "bypass for package=" + packageName + " uid=" + uid + " logsDisabled=" + logsDisabled);
                    param.setResult(null);
                } catch (Throwable t) {
                    Log.e(LOG_TAG, "Failed to override before processNewLogAccessRequest()", t);
                }
            }
        };
    }
}