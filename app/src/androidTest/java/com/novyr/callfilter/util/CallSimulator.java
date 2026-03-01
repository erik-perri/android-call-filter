package com.novyr.callfilter.util;

import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class CallSimulator {
    private static final String TAG = "CallSimulator";
    private static final int SOCKET_TIMEOUT_MS = 5000;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;

    private CallSimulator() {}

    public static void simulateIncomingCall(String phoneNumber) {
        executeCommand("gsm call " + phoneNumber);
    }

    public static void simulatePrivateCall() {
        executeCommand("gsm call #");
    }

    public static void cancelCall(String phoneNumber) {
        executeCommand("gsm cancel " + phoneNumber);
    }

    private static void executeCommand(String command) {
        String host = detectConsoleIp();
        int port = detectConsolePort();

        Exception lastException = null;

        // Retry loop to handle "Connection Refused" flaky starts in CI
        for (int i = 0; i < MAX_RETRIES; i++) {
            Log.d(TAG, "Connecting to " + host + ":" + port + " (Attempt " + (i + 1) + ")");

            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), SOCKET_TIMEOUT_MS);
                socket.setSoTimeout(SOCKET_TIMEOUT_MS);

                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                String greeting = readResponse(reader);

                // Handle Auth if required
                if (greeting != null && greeting.contains("Authentication required")) {
                    String token = InstrumentationRegistry.getArguments().getString("emulatorAuthToken");
                    if (token != null && !token.isEmpty()) {
                        writer.write("auth " + token + "\n");
                        writer.flush();
                        readResponse(reader);
                    }
                }

                // Send Command
                writer.write(command + "\n");
                writer.flush();
                readResponse(reader);

                writer.write("quit\n");
                writer.flush();
                return; // Success

            } catch (Exception e) {
                lastException = e;
                try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ignored) {}
            }
        }

        throw new RuntimeException("Failed to connect to Emulator Console at " + host + ":" + port, lastException);
    }

    /**
     * Detects the host IP.
     * Standard Emulator: 10.0.2.2
     * GMD/Orchestrator: The Default Gateway (e.g., 192.168.232.1)
     */
    private static String detectConsoleIp() {
        try {
            UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            String output = device.executeShellCommand("ip route").trim();

            // Look for a line like: "default via 192.168.232.1 dev eth0"
            for (String line : output.split("\n")) {
                if (line.trim().startsWith("default via")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length > 2) {
                        return parts[2]; // The IP address
                    }
                }
            }
        } catch (Exception ignored) {
            Log.w(TAG, "Failed to detect gateway, defaulting to 10.0.2.2");
        }
        return "10.0.2.2";
    }

    /**
     * Detects the console port.
     * Checks system property (Reliable on modern API)
     * Checks serial number (Reliable on all APIs, e.g. "emulator-5556" -> 5556)
     * Falls back to 5554
     */
    private static int detectConsolePort() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // 1. Try property
        try {
            String port = device.executeShellCommand("getprop ro.boot.qemu.console.port").trim();
            if (!port.isEmpty()) return Integer.parseInt(port);
        } catch (Exception ignored) {}

        // 2. Try Serial Number (Critical for GMD/Parallel execution)
        try {
            String serial = device.executeShellCommand("getprop ro.serialno").trim();
            // Serial format is usually "emulator-5554", "emulator-5556"
            if (serial.startsWith("emulator-")) {
                String portPart = serial.split("-")[1];
                return Integer.parseInt(portPart);
            }
        } catch (Exception ignored) {}

        return 5554;
    }

    private static String readResponse(BufferedReader reader) throws Exception {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append('\n');
            if (line.startsWith("OK")) {
                return sb.toString();
            }
            if (line.startsWith("KO:")) {
                throw new RuntimeException("Console Error: " + line);
            }
        }
        return sb.toString();
    }
}