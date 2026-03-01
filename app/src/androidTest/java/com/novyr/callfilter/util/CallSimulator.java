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
import java.util.LinkedHashSet;
import java.util.Set;

public class CallSimulator {
    private static final String TAG = "CallSimulator";
    private static final int SOCKET_TIMEOUT_MS = 2000;

    private static String sCachedHost = null;
    private static int sCachedPort = -1;

    private CallSimulator() {
        //
    }

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
        // Retry logic is implicit: findConsoleSocket() scans multiple candidates.
        // If the connection drops during execution, we invalidate cache and throw.
        try (Socket socket = findConsoleSocket()) {
            socket.setSoTimeout(5000); // Longer timeout for actual command execution

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            String greeting = readResponse(reader);

            if (greeting.contains("Authentication required")) {
                String token = InstrumentationRegistry.getArguments().getString("emulatorAuthToken");
                if (token != null && !token.isEmpty()) {
                    writer.write("auth " + token + "\n");
                    writer.flush();
                    readResponse(reader);
                }
            }

            writer.write(command + "\n");
            writer.flush();
            readResponse(reader);

            writer.write("quit\n");
            writer.flush();
        } catch (Exception e) {
            // If we failed, clear cache so the next call rescans
            sCachedHost = null;
            sCachedPort = -1;
            throw new RuntimeException("Failed to execute console command: " + command, e);
        }
    }

    /**
     * Scans for the emulator console.
     * Environments like GMD and Orchestrator make the IP/Port unpredictable.
     * We scan the most likely candidates and return the first working connection.
     */
    private static Socket findConsoleSocket() throws RuntimeException {
        // 1. Try Cached Connection first
        if (sCachedHost != null) {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(sCachedHost, sCachedPort), SOCKET_TIMEOUT_MS);
                return socket;
            } catch (Exception e) {
                Log.w(TAG, "Cached connection to " + sCachedHost + ":" + sCachedPort + " failed. Rescanning...");
                sCachedHost = null;
            }
        }

        // 2. Build Candidate List
        Set<String> hosts = new LinkedHashSet<>();
        hosts.add("10.0.2.2"); // Standard
        String gateway = detectGateway();
        if (gateway != null) {
            hosts.add(gateway);
        }

        Set<Integer> ports = new LinkedHashSet<>();
        int detectedPort = detectPort();
        if (detectedPort > 0) {
            ports.add(detectedPort);
        }
        ports.add(5554);
        ports.add(5556);
        ports.add(5558);

        // 3. Scan
        for (String host : hosts) {
            for (int port : ports) {
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(host, port), SOCKET_TIMEOUT_MS);

                    Log.i(TAG, "Connected to Emulator Console at " + host + ":" + port);

                    sCachedHost = host;
                    sCachedPort = port;
                    return socket;
                } catch (Exception ignored) {
                    // Continue scanning
                }
            }
        }

        throw new RuntimeException("Could not connect to Emulator Console. Scanned hosts: " + hosts + ", ports: " + ports);
    }

    private static String detectGateway() {
        try {
            UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            // "ip route" output example: "default via 192.168.232.1 dev eth0"
            String output = device.executeShellCommand("ip route").trim();
            for (String part : output.split("\\s+")) {
                // Return first valid non-zero IP
                if (part.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")
                        && !part.equals("0.0.0.0")) {
                    return part;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to execute ip route", e);
        }

        return null;
    }

    private static int detectPort() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        try {
            String val = device.executeShellCommand("getprop ro.boot.qemu.console.port").trim();
            if (!val.isEmpty()) return Integer.parseInt(val);
        } catch (Exception e) {
            Log.w(TAG, "Failed to read ro.boot.qemu.console.port", e);
        }

        try {
            String val = device.executeShellCommand("getprop ro.serialno").trim();
            if (val.startsWith("emulator-")) {
                return Integer.parseInt(val.split("-")[1]);
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to read ro.serialno", e);
        }

        return -1;
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