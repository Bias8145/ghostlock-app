package com.ghostlock.app;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProcessOutcomeTest {
    @Test public void zeroExitIsNotReportedAsVerifiedRoot() {
        ProcessOutcome outcome = ProcessOutcome.fromExitCode(0);
        assertEquals(ProcessOutcome.Kind.EXIT_ZERO_UNVERIFIED, outcome.kind());
        assertTrue(outcome.logMessage().contains("unverified"));
        assertEquals(0, outcome.exitCode());
    }

    @Test public void nonZeroExitRemainsFailure() {
        ProcessOutcome outcome = ProcessOutcome.fromExitCode(17);
        assertEquals(ProcessOutcome.Kind.NON_ZERO, outcome.kind());
        assertTrue(outcome.logMessage().contains("17"));
        assertEquals(17, outcome.exitCode());
    }
}