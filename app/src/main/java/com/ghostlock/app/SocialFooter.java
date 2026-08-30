package com.ghostlock.app;

import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

/** Centered social links kept outside the main panels. */
public class SocialFooter extends LinearLayout {
    public SocialFooter(android.content.Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ImageButton telegram = findViewById(R.id.telegramButton);
        ImageButton github = findViewById(R.id.githubButton);
        if (telegram != null) {
            telegram.setOnClickListener(v -> openUrl("https://t.me/VOLD_NAMESPACE"));
        }
        if (github != null) {
            github.setOnClickListener(v -> openUrl("https://github.com/Bias8145"));
        }
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            getContext().startActivity(intent);
        } catch (RuntimeException ignored) {
            // No browser available; keep the app stable.
        }
    }
}
