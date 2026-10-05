/*
** Command & Conquer Generals Zero Hour(tm)
** Copyright 2025 Electronic Arts Inc.
** Licensed under GPL-3.0-or-later. See LICENSE.md.
*/

package com.generalsx.zerohour;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.KeyEvent;

import java.io.File;
import java.io.FileWriter;

/** GeneralsX @feature Codex 28/08/2026 Settings shared by Setup and the native engine. */
final class GamepadControlConfig {
    private static final String PREFS_NAME = "generalszh_gamepad_controls";

    boolean showCursor = true;
    float cursorSensitivity = 1.0f;
    float panSensitivity = 1.0f;
    float deadzone = 0.18f;

    // A/Cross, B/Circle, Start and shoulders stay fixed to the essential
    // mouse/menu/zoom actions. These seven shortcuts are intentionally
    // configurable because their useful meaning depends on the player's mod.
    int westKey = KeyEvent.KEYCODE_E;
    int northKey = KeyEvent.KEYCODE_Q;
    int backKey = KeyEvent.KEYCODE_SPACE;
    int dpadUpKey = KeyEvent.KEYCODE_1;
    int dpadRightKey = KeyEvent.KEYCODE_2;
    int dpadDownKey = KeyEvent.KEYCODE_3;
    int dpadLeftKey = KeyEvent.KEYCODE_4;

    static GamepadControlConfig defaults() {
        return new GamepadControlConfig();
    }

    static GamepadControlConfig load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        GamepadControlConfig config = defaults();
        config.showCursor = prefs.getBoolean("show_cursor", true);
        config.cursorSensitivity = clamp(prefs.getFloat("cursor_sensitivity", 1.0f), 0.5f, 2.0f);
        config.panSensitivity = clamp(prefs.getFloat("pan_sensitivity", 1.0f), 0.5f, 2.0f);
        config.deadzone = clamp(prefs.getFloat("deadzone", 0.18f), 0.05f, 0.35f);
        config.westKey = validKey(prefs.getInt("west_key", config.westKey), config.westKey);
        config.northKey = validKey(prefs.getInt("north_key", config.northKey), config.northKey);
        config.backKey = validKey(prefs.getInt("back_key", config.backKey), config.backKey);
        config.dpadUpKey = validKey(prefs.getInt("dpad_up_key", config.dpadUpKey), config.dpadUpKey);
        config.dpadRightKey = validKey(prefs.getInt("dpad_right_key", config.dpadRightKey), config.dpadRightKey);
        config.dpadDownKey = validKey(prefs.getInt("dpad_down_key", config.dpadDownKey), config.dpadDownKey);
        config.dpadLeftKey = validKey(prefs.getInt("dpad_left_key", config.dpadLeftKey), config.dpadLeftKey);
        return config;
    }

    void save(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean("show_cursor", showCursor)
            .putFloat("cursor_sensitivity", clamp(cursorSensitivity, 0.5f, 2.0f))
            .putFloat("pan_sensitivity", clamp(panSensitivity, 0.5f, 2.0f))
            .putFloat("deadzone", clamp(deadzone, 0.05f, 0.35f))
            .putInt("west_key", westKey)
            .putInt("north_key", northKey)
            .putInt("back_key", backKey)
            .putInt("dpad_up_key", dpadUpKey)
            .putInt("dpad_right_key", dpadRightKey)
            .putInt("dpad_down_key", dpadDownKey)
            .putInt("dpad_left_key", dpadLeftKey)
            .apply();
    }

    static void reset(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply();
    }

    /** Write the small native-side config before libmain.so starts. */
    static void prepareForLaunch(Context context, File gameFolder) {
        if (gameFolder == null) return;
        GamepadControlConfig config = load(context);
        File nativeConfig = new File(gameFolder, "GeneralsXGamepad.ini");
        try (FileWriter writer = new FileWriter(nativeConfig, false)) {
            writer.write("ShowCursor=" + (config.showCursor ? 1 : 0) + "\n");
            writer.write("CursorSensitivity=" + config.cursorSensitivity + "\n");
            writer.write("PanSensitivity=" + config.panSensitivity + "\n");
            writer.write("Deadzone=" + config.deadzone + "\n");
            writer.write("WestKey=" + nativeKeyName(config.westKey) + "\n");
            writer.write("NorthKey=" + nativeKeyName(config.northKey) + "\n");
            writer.write("BackKey=" + nativeKeyName(config.backKey) + "\n");
            writer.write("DpadUpKey=" + nativeKeyName(config.dpadUpKey) + "\n");
            writer.write("DpadRightKey=" + nativeKeyName(config.dpadRightKey) + "\n");
            writer.write("DpadDownKey=" + nativeKeyName(config.dpadDownKey) + "\n");
            writer.write("DpadLeftKey=" + nativeKeyName(config.dpadLeftKey) + "\n");
        } catch (Exception ignored) {
            // Native defaults are complete, so a missing file is non-fatal.
        }
    }

    private static String nativeKeyName(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_ESCAPE) return "Escape";
        if (keyCode == KeyEvent.KEYCODE_DEL) return "Backspace";
        if (keyCode == KeyEvent.KEYCODE_ENTER) return "Return";
        return TouchControlConfig.displayNameForKey(keyCode);
    }

    private static int validKey(int value, int fallback) {
        for (int code : TouchControlConfig.keyCodes()) {
            if (code == value) return value;
        }
        return fallback;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
