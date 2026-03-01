package com.novyr.callfilter.util;

import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class CallSimulator {
    private static final String TAG = CallSimulator.class.getSimpleName();

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

    private static void simulateViaEmulatorConsole(String command) throws Exception {
        try (Socket socket = new Socket("10.0.2.2", 5554)) {
            socket.setSoTimeout(5000);
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
        }
        return sb.toString();
    }
}
