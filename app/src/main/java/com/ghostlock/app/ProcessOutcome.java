package com.ghostlock.app;

/** Describes the native process result without claiming that root access was verified. */
public final class ProcessOutcome {
    public enum Kind { EXIT_ZERO_UNVERIFIED, NON_ZERO }

    private final Kind kind;
    private final int exitCode;

    private ProcessOutcome(Kind kind, int exitCode) {
        this.kind = kind;
        this.exitCode = exitCode;
    }

    public static ProcessOutcome fromExitCode(int exitCode) {
        return new ProcessOutcome(
                exitCode == 0 ? Kind.EXIT_ZERO_UNVERIFIED : Kind.NON_ZERO,
                exitCode);
    }

    public Kind kind() { return kind; }
    public int exitCode() { return exitCode; }

    public String logMessage() {
        if (kind == Kind.EXIT_ZERO_UNVERIFIED) {
            return "process completed with exit code 0; root-manager access remains unverified";
        }
        return "process returned non-zero exit code " + exitCode + "; inspect stage logs";
    }
}