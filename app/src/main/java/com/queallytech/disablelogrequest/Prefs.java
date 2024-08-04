package com.queallytech.disablelogrequest;

import android.content.Context;
import android.content.SharedPreferences;

import de.robv.android.xposed.XSharedPreferences;

class Prefs {
    public static final String PREFS_NAME = "disable_logs_request";
    public static final String PREFS_KEY_DISABLE_LOGS = "disable_logs";

    private SharedPreferences prefs;

    public Prefs(Context context) throws SecurityException {
        if (context == null) {
            prefs = new XSharedPreferences(PREFS_NAME);
        } else {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_READABLE);
        }
    }

    public void reload() {
        if (prefs instanceof XSharedPreferences) {
            ((XSharedPreferences) prefs).reload();
        }
    }

    public boolean isLogsDisabled() {
        return prefs.getBoolean(PREFS_KEY_DISABLE_LOGS, true);
    }

    public void setLogsDisabled(boolean disabled) {
        prefs.edit().putBoolean(PREFS_KEY_DISABLE_LOGS, disabled).apply();
    }
}
