package com.ghostlock.app;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import com.google.android.material.button.MaterialButton;

import java.lang.reflect.Field;

/** Clear-log action that is available only after a run has produced a final result. */
public final class ClearLogButton extends MaterialButton {
    private TextWatcher watcher;

    public ClearLogButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setVisibility(GONE);
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        post(this::bindLog);
    }

    @Override protected void onDetachedFromWindow() {
        TextViewHolder holder = findLog();
        if (holder != null && watcher != null) {
            holder.log.removeTextChangedListener(watcher);
        }
        watcher = null;
        super.onDetachedFromWindow();
    }

    private void bindLog() {
        TextViewHolder holder = findLog();
        if (holder == null) return;
        updateVisibility(holder.log.getText() == null ? "" : holder.log.getText().toString());
        watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateVisibility(s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(Editable s) { }
        };
        holder.log.addTextChangedListener(watcher);
        setOnClickListener(v -> clearLog(holder.log));
    }

    private void updateVisibility(String log) {
        String s = log.trim().toLowerCase(java.util.Locale.ROOT);
        boolean completed = s.contains("exit code=0") || s.contains("exit code=137") || s.contains("exit code=-1");
        setVisibility(completed ? VISIBLE : GONE);
    }

    private void clearLog(android.widget.TextView log) {
        log.setText("");
        try {
            Field field = MainActivity.class.getDeclaredField("logBuffer");
            field.setAccessible(true);
            StringBuilder buffer = (StringBuilder) field.get(getContext());
            synchronized (buffer) {
                buffer.setLength(0);
            }
        } catch (Throwable ignored) {
        }
        setVisibility(GONE);
    }

    private TextViewHolder findLog() {
        android.view.View root = getRootView();
        android.widget.TextView log = root.findViewById(R.id.logView);
        return log == null ? null : new TextViewHolder(log);
    }

    private static final class TextViewHolder {
        final android.widget.TextView log;
        TextViewHolder(android.widget.TextView log) { this.log = log; }
    }
}
