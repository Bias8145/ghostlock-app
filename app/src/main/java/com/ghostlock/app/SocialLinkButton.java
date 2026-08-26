package com.ghostlock.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;

import com.google.android.material.button.MaterialButton;

/** Material 3 text button that opens its configured social link. */
public class SocialLinkButton extends MaterialButton {
    private String url;

    public SocialLinkButton(Context context) {
        super(context);
        init();
    }

    public SocialLinkButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SocialLinkButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnClickListener(v -> openLink());
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private void openLink() {
        String target = url;
        if (target == null || target.isEmpty()) {
            target = getText().toString().equalsIgnoreCase("Telegram")
                    ? "https://t.me/VOLD_NAMESPACE"
                    : "https://github.com/Bias8145";
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(target));
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            getContext().startActivity(intent);
        } catch (RuntimeException ignored) {
            // No browser/handler available; keep the UI stable without a crash.
        }
    }
}
