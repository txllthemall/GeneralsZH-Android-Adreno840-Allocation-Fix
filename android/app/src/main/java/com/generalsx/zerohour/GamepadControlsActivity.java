/*
** Command & Conquer Generals Zero Hour(tm)
** Copyright 2025 Electronic Arts Inc.
** Licensed under GPL-3.0-or-later. See LICENSE.md.
*/

package com.generalsx.zerohour;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

/** GeneralsX @feature Codex 28/08/2026 Editor for the Android RTS gamepad profile. */
public class GamepadControlsActivity extends Activity {
    private GamepadControlConfig config;
    private LinearLayout mappings;
    private CheckBox cursorCheck;
    private TextView cursorSpeedValue;
    private TextView panSpeedValue;
    private TextView deadzoneValue;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.gamepad_editor_title);
        config = GamepadControlConfig.load(this);
        buildUi();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(12), dp(16), dp(24));
        scroll.addView(root, new ScrollView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(scroll);
        InsetUtil.applySafeInsets(root);

        TextView intro = new TextView(this);
        intro.setText(R.string.gamepad_editor_help);
        intro.setPadding(0, 0, 0, dp(12));
        root.addView(intro);

        cursorCheck = new CheckBox(this);
        cursorCheck.setText(R.string.gamepad_show_cursor);
        cursorCheck.setChecked(config.showCursor);
        root.addView(cursorCheck);

        cursorSpeedValue = addSlider(root, R.string.gamepad_cursor_speed, 50, 200,
            Math.round(config.cursorSensitivity * 100f), value -> config.cursorSensitivity = value / 100f);
        panSpeedValue = addSlider(root, R.string.gamepad_pan_speed, 50, 200,
            Math.round(config.panSensitivity * 100f), value -> config.panSensitivity = value / 100f);
        deadzoneValue = addSlider(root, R.string.gamepad_deadzone, 5, 35,
            Math.round(config.deadzone * 100f), value -> config.deadzone = value / 100f);

        TextView fixed = new TextView(this);
        fixed.setText(R.string.gamepad_fixed_mapping_help);
        fixed.setPadding(0, dp(14), 0, dp(8));
        root.addView(fixed);

        mappings = new LinearLayout(this);
        mappings.setOrientation(LinearLayout.VERTICAL);
        root.addView(mappings);
        rebuildMappings();

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.END);
        actions.setPadding(0, dp(16), 0, 0);
        Button defaults = new Button(this);
        defaults.setText(R.string.gamepad_defaults_button);
        defaults.setOnClickListener(v -> resetDefaults());
        actions.addView(defaults);
        Button save = new Button(this);
        save.setText(R.string.gamepad_save_button);
        save.setOnClickListener(v -> saveAndFinish());
        actions.addView(save);
        root.addView(actions);
    }

    private interface ValueListener { void onValue(int value); }

    private TextView addSlider(LinearLayout root, int titleRes, int min, int max,
                               int initial, ValueListener listener) {
        TextView label = new TextView(this);
        label.setPadding(0, dp(8), 0, 0);
        label.setText(getString(titleRes, initial));
        root.addView(label);
        SeekBar seek = new SeekBar(this);
        seek.setMin(min);
        seek.setMax(max);
        seek.setProgress(initial);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int value, boolean fromUser) {
                listener.onValue(value);
                label.setText(getString(titleRes, value));
            }
            @Override public void onStartTrackingTouch(SeekBar bar) {}
            @Override public void onStopTrackingTouch(SeekBar bar) {}
        });
        label.setTag(seek);
        root.addView(seek);
        return label;
    }

    private interface KeyGetter { int get(); }
    private interface KeySetter { void set(int keyCode); }

    private void addMapping(int labelRes, KeyGetter getter, KeySetter setter) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView label = new TextView(this);
        label.setText(labelRes);
        row.addView(label, new LinearLayout.LayoutParams(0,
            ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        Button value = new Button(this);
        value.setText(TouchControlConfig.displayNameForKey(getter.get()));
        value.setOnClickListener(v -> chooseKey(value, setter));
        row.addView(value);
        mappings.addView(row);
    }

    private void chooseKey(Button button, KeySetter setter) {
        String[] names = TouchControlConfig.keyNames();
        int[] codes = TouchControlConfig.keyCodes();
        new AlertDialog.Builder(this)
            .setTitle(R.string.gamepad_choose_key)
            .setItems(names, (dialog, which) -> {
                setter.set(codes[which]);
                button.setText(TouchControlConfig.displayNameForKey(codes[which]));
            })
            .setNegativeButton(R.string.common_cancel, null)
            .show();
    }

    private void rebuildMappings() {
        mappings.removeAllViews();
        addMapping(R.string.gamepad_map_west, () -> config.westKey, value -> config.westKey = value);
        addMapping(R.string.gamepad_map_north, () -> config.northKey, value -> config.northKey = value);
        addMapping(R.string.gamepad_map_back, () -> config.backKey, value -> config.backKey = value);
        addMapping(R.string.gamepad_map_dpad_up, () -> config.dpadUpKey, value -> config.dpadUpKey = value);
        addMapping(R.string.gamepad_map_dpad_right, () -> config.dpadRightKey, value -> config.dpadRightKey = value);
        addMapping(R.string.gamepad_map_dpad_down, () -> config.dpadDownKey, value -> config.dpadDownKey = value);
        addMapping(R.string.gamepad_map_dpad_left, () -> config.dpadLeftKey, value -> config.dpadLeftKey = value);
    }

    private void resetDefaults() {
        config = GamepadControlConfig.defaults();
        cursorCheck.setChecked(config.showCursor);
        ((SeekBar)cursorSpeedValue.getTag()).setProgress(Math.round(config.cursorSensitivity * 100f));
        ((SeekBar)panSpeedValue.getTag()).setProgress(Math.round(config.panSensitivity * 100f));
        ((SeekBar)deadzoneValue.getTag()).setProgress(Math.round(config.deadzone * 100f));
        rebuildMappings();
    }

    private void saveAndFinish() {
        config.showCursor = cursorCheck.isChecked();
        config.save(this);
        String gamePath = SetupActivity.getSavedGamePath(this);
        if (gamePath != null) GamepadControlConfig.prepareForLaunch(this, new File(gamePath));
        Toast.makeText(this, R.string.gamepad_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
