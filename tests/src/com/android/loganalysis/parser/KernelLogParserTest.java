/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.loganalysis.parser;

import com.android.loganalysis.item.KernelLogItem;
import com.android.loganalysis.item.MiscKernelLogItem;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link KernelLogParser}.
 */
public class KernelLogParserTest extends TestCase {
    /**
     * Test that log lines formatted by last kmsg are able to be parsed.
     */
    public void testParseLastKmsg() {
        List<String> lines = Arrays.asList(
                "[    0.000000] Start",
                "[    1.000000] Kernel panic",
                "[    2.000000] End");

        KernelLogItem kernelLog = new KernelLogParser().parse(lines);
        assertNotNull(kernelLog);
        assertEquals(0.0, kernelLog.getStartTime(), 0.0000005);
        assertEquals(2.0, kernelLog.getStopTime(), 0.0000005);
        assertEquals(1, kernelLog.getEvents().size());
        assertEquals(1, kernelLog.getMiscEvents(KernelLogParser.KERNEL_RESET).size());

        MiscKernelLogItem item = kernelLog.getMiscEvents(KernelLogParser.KERNEL_RESET).get(0);
        assertEquals(1.0, item.getEventTime(), 0.0000005);
        assertEquals("[    0.000000] Start", item.getPreamble());
        assertEquals("Kernel panic", item.getMessage());
    }

    /**
     * Test that log lines formatted by dmsg are able to be parsed.
     */
    public void testParseDmesg() {
        List<String> lines = Arrays.asList(
                "<1>[    0.000000] Start",
                "<1>[    1.000000] Kernel panic",
                "<1>[    2.000000] End");

        KernelLogItem kernelLog = new KernelLogParser().parse(lines);
        assertNotNull(kernelLog);
        assertEquals(0.0, kernelLog.getStartTime(), 0.0000005);
        assertEquals(2.0, kernelLog.getStopTime(), 0.0000005);
        assertEquals(1, kernelLog.getEvents().size());
        assertEquals(1, kernelLog.getMiscEvents(KernelLogParser.KERNEL_RESET).size());

        MiscKernelLogItem item = kernelLog.getMiscEvents(KernelLogParser.KERNEL_RESET).get(0);
        assertEquals(1.0, item.getEventTime(), 0.0000005);
        assertEquals("<1>[    0.000000] Start", item.getPreamble());
        assertEquals("Kernel panic", item.getMessage());
    }

    /**
     * Test that last boot reasons are parsed.
     */
    public void testParseLastMessage() {
        List<String> lines = Arrays.asList(
                "[    0.000000] Start",
                "[    2.000000] End",
                "Last boot reason: hw_reset");

        KernelLogItem kernelLog = new KernelLogParser().parse(lines);
        assertNotNull(kernelLog);
        assertEquals(0.0, kernelLog.getStartTime(), 0.0000005);
        assertEquals(2.0, kernelLog.getStopTime(), 0.0000005);
        assertEquals(1, kernelLog.getEvents().size());
        assertEquals(1, kernelLog.getMiscEvents(KernelLogParser.KERNEL_RESET).size());

        MiscKernelLogItem item = kernelLog.getMiscEvents(KernelLogParser.KERNEL_RESET).get(0);
        assertEquals(2.0, item.getEventTime(), 0.0000005);
        assertEquals("[    0.000000] Start\n[    2.000000] End", item.getPreamble());
        assertEquals("Last boot reason: hw_reset", item.getMessage());
    }

    /**
     * Test that reset reasons don't crash if times are set.
     */
    public void testNoPreviousLogs() {
        List<String> lines = Arrays.asList(
                "Last boot reason: hw_reset");

        KernelLogItem kernelLog = new KernelLogParser().parse(lines);
        assertNotNull(kernelLog);
        assertNull(kernelLog.getStartTime());
        assertNull(kernelLog.getStopTime());
        assertEquals(1, kernelLog.getEvents().size());
        assertEquals(1, kernelLog.getMiscEvents(KernelLogParser.KERNEL_RESET).size());

        MiscKernelLogItem item = kernelLog.getMiscEvents(KernelLogParser.KERNEL_RESET).get(0);
        assertNull(item.getEventTime());
        assertEquals("", item.getPreamble());
        assertEquals("Last boot reason: hw_reset", item.getMessage());
    }
}
