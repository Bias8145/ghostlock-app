package com.ghostlock.app;

import android.os.Bundle;
import androidx.activity.ComponentActivity;
import androidx.activity.compose.setContent;
import androidx.compose.material3.MaterialTheme;
import androidx.compose.material3.Surface;
import androidx.compose.runtime.Composable;
import com.ghostlock.app.ui.settings.SettingsScreen;
import com.ghostlock.app.ui.theme.ThemeRepository;
import com.ghostlock.app.data.GhostlockPrefs;
import androidx.lifecycle.ViewModelProvider;

public class SettingsComposeActivity extends ComponentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent {
            // Provide ThemeRepository via ViewModel
            ThemeRepository themeRepository = new ViewModelProvider(this).get(ThemeRepository.class);
            MaterialTheme(materialTheme = themeMaterialTheme(themeRepository)) {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettingsScreen(new GhostlockActions() {
                        @Override
                        public void onShizukuClick() {
                            // TODO: implement
                        }

                        @Override
                        public void onSafeModeChanged(Boolean enabled) {
                            // TODO: implement
                        }

                        @Override
                        public void onOpenAdvanced() {
                            // TODO: implement
                        }

                        @Override
                        public void onExportProfile() {
                            // TODO: implement
                        }

                        @Override
                        public void onImportOffsetsHocon(String content) {
                            // TODO: implement
                        }

                        @Override
                        public void onImportOffsetsJson(String content) {
                            // TODO: implement
                        }

                        @Override
                        public void onCloseParameters() {
                            finish();
                        }

                        @Override
                        public void onShowAbout() {
                            // TODO: implement
                        }
                    });
                }
            }
        }
    }

    private static androidx.compose.material3.MaterialTheme themeMaterialTheme(ThemeRepository repository) {
        return new androidx.compose.material3.MaterialTheme(
                repository.getActualIsDark()
                        ? androidx.compose.material3.darkColorScheme()
                        : androidx.compose.material3.lightColorScheme()
        );
    }
}