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
        simulateViaEmulatorConsole("gsm call 0");
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
                writer.write("auth " + token + "\n");
                writer.flush();
                readUntilOk(reader);
            }

            writer.write(command + "\n");
            writer.flush();
            readUntilOk(reader);

            writer.write("quit\n");
            writer.flush();
        }
    }

    private static String getAuthToken() throws Exception {
        String token = InstrumentationRegistry.getArguments().getString("emulatorAuthToken");
        if (token != null && !token.isEmpty()) {
            return token;
        }

        File tokenFile = new File(System.getProperty("user.home"), ".emulator_console_auth_token");
        try (BufferedReader reader = new BufferedReader(new FileReader(tokenFile))) {
            return reader.readLine().trim();
        }
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
