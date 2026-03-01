package com.novyr.callfilter.util;

import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class CallSimulator {
    private static final String TAG = CallSimulator.class.getSimpleName();
    private static final int SOCKET_TIMEOUT_MS = 10000;

    private CallSimulator() {
    }

    public static void simulateIncomingCall(String phoneNumber) throws Exception {
        simulateViaEmulatorConsole("gsm call " + phoneNumber);
    }

    public static void simulatePrivateCall() throws Exception {
        simulateViaEmulatorConsole("gsm call #");
    }

    public static void cancelCall(String phoneNumber) throws Exception {
        simulateViaEmulatorConsole("gsm cancel " + phoneNumber);
    }

    private static int getConsolePort() {
        UiDevice device = UiDevice.getInstance(
                InstrumentationRegistry.getInstrumentation());

        // Try the dedicated console port property (set by newer emulator images)
        try {
            String portProp = device.executeShellCommand(
                    "getprop ro.boot.qemu.console.port").trim();
            if (!portProp.isEmpty()) {
                int port = Integer.parseInt(portProp);
                Log.d(TAG, "Detected console port from property: " + port);
                return port;
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to detect console port from property", e);
        }

        // Derive from the emulator serial (format: "emulator-XXXX" where XXXX
        // is the console port). Works on all API levels since ro.serialno and
        // ro.boot.serialno are always set by the emulator.
        String[] serialProps = {"ro.boot.serialno", "ro.serialno"};
        for (String prop : serialProps) {
            try {
                String serial = device.executeShellCommand("getprop " + prop).trim();
                if (serial.startsWith("emulator-")) {
                    int port = Integer.parseInt(serial.substring("emulator-".length()));
                    Log.d(TAG, "Derived console port from " + prop + ": " + port);
                    return port;
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to parse serial from " + prop, e);
            }
        }

        // Try the instrumentation argument
        String portArg = InstrumentationRegistry.getArguments().getString("emulatorConsolePort");
        if (portArg != null && !portArg.isEmpty()) {
            try {
                int port = Integer.parseInt(portArg);
                Log.d(TAG, "Using console port from instrumentation arg: " + port);
                return port;
            } catch (NumberFormatException e) {
                Log.w(TAG, "Invalid port in instrumentation arg: " + portArg);
            }
        }

        Log.d(TAG, "Using default console port 5554");
        return 5554;
    }

    private static void simulateViaEmulatorConsole(String command) throws Exception {
        int port = getConsolePort();
        Log.d(TAG, "Connecting to emulator console at 10.0.2.2:" + port);

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("10.0.2.2", port), SOCKET_TIMEOUT_MS);
            socket.setSoTimeout(SOCKET_TIMEOUT_MS);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream()));

            String greeting = readUntilOk(reader);

            if (greeting.contains("Authentication required")) {
                String token = getAuthToken();
                if (!token.isEmpty()) {
                    writer.write("auth " + token + "\n");
                    writer.flush();
                    readUntilOk(reader);
                }
            }

            writer.write(command + "\n");
            writer.flush();
            readUntilOk(reader);

            writer.write("quit\n");
            writer.flush();
        }
    }

    private static String getAuthToken() {
        String token = InstrumentationRegistry.getArguments().getString("emulatorAuthToken");
        if (token != null && !token.isEmpty()) {
            return token;
        }

        // Try standard locations for the auth token file
        String[] candidates = {
                System.getProperty("user.home") + "/.emulator_console_auth_token",
                "/data/local/tmp/.emulator_console_auth_token",
                System.getenv("HOME") + "/.emulator_console_auth_token",
        };

        for (String path : candidates) {
            if (path == null) continue;
            File tokenFile = new File(path);
            if (tokenFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(tokenFile))) {
                    String line = reader.readLine();
                    if (line != null && !line.trim().isEmpty()) {
                        return line.trim();
                    }
                    // Empty token file means auth is disabled
                    return "";
                } catch (Exception e) {
                    Log.w(TAG, "Failed to read token from " + path, e);
                }
            }
        }

        return "";
    }

    private static String readUntilOk(BufferedReader reader) throws Exception {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            Log.d(TAG, "Console: " + line);
            sb.append(line).append('\n');
            if (line.startsWith("OK")) {
                return sb.toString();
            }
            if (line.startsWith("KO:")) {
                throw new RuntimeException("Emulator console error: " + line);
            }
        }
        throw new RuntimeException(
                "Emulator console connection closed without OK. Received: " + sb.toString());
    }
}
