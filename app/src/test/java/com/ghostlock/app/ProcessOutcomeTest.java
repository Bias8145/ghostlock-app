package com.ghostlock.app;

import org.junit.Test;
import org.json.JSONObject;
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
    @Test public void legacyHistorySuccessFieldRemainsReadable() throws Exception {
        JSONObject legacy = new JSONObject();
        legacy.put("timestamp", 123L);
        legacy.put("success", true);
        legacy.put("kernel", "test-kernel");
        legacy.put("cpuPair", "0,1");

        AnalyticsManager.RunRecord record = AnalyticsManager.RunRecord.fromJSON(legacy);
        assertTrue(record.processExitZero);
        assertEquals(123L, record.timestamp);
    }

    @Test public void newHistoryUsesExplicitProcessExitField() throws Exception {
        AnalyticsManager.RunRecord record =
                new AnalyticsManager.RunRecord(true, "test-kernel", "0,1", null);
        JSONObject json = record.toJSON();

        assertTrue(json.getBoolean("processExitZero"));
        assertEquals(false, json.has("success"));
    }

}