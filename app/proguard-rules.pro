# The themed Tools UI dispatches these handlers through reflection.
# Keep their names/signatures stable in release builds so Parse Boot,
# Import and Parse Link continue to resolve after R8 obfuscation.
-keepclassmembers class com.ghostlock.app.MainActivity {
    void pickDocument(int);
    void pickParseBoot(boolean);
    void runExtract(java.lang.String, java.io.File);
}
