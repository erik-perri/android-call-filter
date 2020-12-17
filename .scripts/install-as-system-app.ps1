$appPackage = "com.novyr.callfilter"
$appName = "AndroidCallFilter"
$appMainActivity = "ui.MainActivity"
$adbPath = "adb"
$systemAppPath = "/system/priv-app"
$apkFileName = "$appName.apk"
$apkPathHost = "..\app\build\outputs\apk\debug\app-debug.apk"
$apkPathTarget = "$systemAppPath/$apkFileName"

$output = & $adbPath root | Out-String

Write-Host "Remounting system for write"
$output = & $adbPath remount | Out-String
if (!$output.Contains("remount succeeded")) {
	Write-Host "Unable to remount system"
	Exit 1
}

Write-Host "Pushing $apkPathHost to $apkPathTarget"
$output = & $adbPath push "$apkPathHost" "$apkPathTarget" | Out-String
if ($output.Contains('failed to copy') -or !$output.Contains("1 file pushed")) {
	Write-Host "Failed to push file, make sure emulator is running with -writable-system switch"
	Exit 1
}

$output = & $adbPath shell chmod 644 $apkPathTarget | Out-String
$output = & $adbPath shell mount -o remount,ro / | Out-String
$output = & $adbPath shell am force-stop $appPackage | Out-String

# This is left to Android Studio
#$output = & $adbPath shell am start -n "$appPackage/$appPackage.$appMainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER

Exit 0
