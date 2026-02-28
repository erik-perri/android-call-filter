package com.novyr.callfilter.util;

import android.os.Build;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class CallSimulator {
    private static final String TAG = CallSimulator.class.getSimpleName();

    private CallSimulator() {
    }

    public static void simulateIncomingCall(String phoneNumber) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            simulateViaEmulatorConsole("gsm call " + phoneNumber);
        } else {
            simulateViaBroadcast(phoneNumber);
        }
    }

    public static void simulatePrivateCall() throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            simulateViaEmulatorConsole("gsm call 0");
        } else {
            UiDevice device = UiDevice.getInstance(
                    InstrumentationRegistry.getInstrumentation());
            device.executeShellCommand(
                    "am broadcast -a android.intent.action.PHONE_STATE --es state RINGING");
        }
    }

    public static void cancelCall(String phoneNumber) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            simulateViaEmulatorConsole("gsm cancel " + phoneNumber);
        } else {
            UiDevice device = UiDevice.getInstance(
                    InstrumentationRegistry.getInstrumentation());
            device.executeShellCommand(
                    "am broadcast -a android.intent.action.PHONE_STATE --es state IDLE");
        }
    }

    private static void simulateViaBroadcast(String phoneNumber) throws IOException {
        UiDevice device = UiDevice.getInstance(
                InstrumentationRegistry.getInstrumentation());
        device.executeShellCommand(
                "am broadcast -a android.intent.action.PHONE_STATE"
                        + " --es state RINGING"
                        + " --es incoming_number " + phoneNumber);
    }

    private static void simulateViaEmulatorConsole(String command) throws Exception {
        String authToken = readAuthToken();

        try (Socket socket = new Socket("10.0.2.2", 5554)) {
            socket.setSoTimeout(5000);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream()));

            // Read the greeting
            readUntilOk(reader);

            // Authenticate
            writer.write("auth " + authToken + "\n");
            writer.flush();
            readUntilOk(reader);

            // Send command
            writer.write(command + "\n");
            writer.flush();
            readUntilOk(reader);

            writer.write("quit\n");
            writer.flush();
        }
    }

    private static String readAuthToken() throws IOException {
        File tokenFile = new File(System.getProperty("user.home"), ".emulator_console_auth_token");
        try (BufferedReader reader = new BufferedReader(new FileReader(tokenFile))) {
            return reader.readLine().trim();
        }
    }

    private static void readUntilOk(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            Log.d(TAG, "Console: " + line);
            if (line.startsWith("OK")) {
                return;
            }
        }
    }
}
