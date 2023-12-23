package com.example.kvp.traindriver.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Switch;

import com.example.kvp.traindriver.MainContext;
import com.example.kvp.traindriver.R;

public class SettingsActivity extends AppCompatActivity
{
    public static String PreferencesKey_CompactMode = "CompactMode";

    private MainContext mainContext;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mainContext = MainContext.getMainContext(this);

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("settings", MODE_PRIVATE);
        Switch compactMode = findViewById(R.id.switchCompactMode);
        compactMode.setChecked(preferences.getBoolean(PreferencesKey_CompactMode, false));
        compactMode.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(PreferencesKey_CompactMode, isChecked);
            editor.commit();
        });
    }

    public static boolean isCompactMode(Context context)
    {
        SharedPreferences preferences = context.getApplicationContext().getSharedPreferences("settings", MODE_PRIVATE);
        return preferences.getBoolean(PreferencesKey_CompactMode, false);
    }

}

