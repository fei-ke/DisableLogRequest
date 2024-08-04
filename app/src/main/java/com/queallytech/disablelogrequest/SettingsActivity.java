package com.queallytech.disablelogrequest;


import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.Switch;

public class SettingsActivity extends Activity {

    private Prefs prefs;
    private Switch switchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        switchView = new Switch(this);
        switchView.setText(R.string.auto_decline);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        root.addView(switchView, params);

        try {
            prefs = new Prefs(this);
            switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.setLogsDisabled(isChecked);
            });
        } catch (SecurityException e) {
            switchView.setEnabled(false);
        }

        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (prefs != null) {
            switchView.setChecked(prefs.isLogsDisabled());
        }
    }
}
