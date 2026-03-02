package com.novyr.callfilter.util;

import android.os.Build;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.util.List;

public class UiDumpOnFailureRule extends TestWatcher {
    private final UiDevice device;

    public UiDumpOnFailureRule(UiDevice device) {
        this.device = device;
    }

    @Override
    protected void failed(Throwable e, Description description) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== Screen dump (").append(description.getMethodName()).append(") ===\n");
        sb.append("API level: ").append(Build.VERSION.SDK_INT).append("\n");
        sb.append("Current package: ").append(device.getCurrentPackageName()).append("\n\n");
        try {
            List<UiObject2> textNodes = device.findObjects(By.textStartsWith(""));
            if (textNodes != null) {
                for (UiObject2 obj : textNodes) {
                    sb.append("  text=\"").append(obj.getText()).append("\"");
                    sb.append("  class=").append(obj.getClassName());
                    if (obj.getResourceName() != null) {
                        sb.append("  res=").append(obj.getResourceName());
                    }
                    sb.append("\n");
                }
            }
        } catch (Exception ex) {
            sb.append("(dump failed: ").append(ex.getMessage()).append(")\n");
        }
        System.err.println(sb);
    }
}
