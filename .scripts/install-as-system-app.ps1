$appPackage = "com.novyr.callfilter"
$appName = "AndroidCallFilter"
#$appMainActivity = "ui.loglist.LogListActivity"
$adbPath = "adb"
$apkFileName = "$appName.apk"
$apkPathHost = "..\app\build\outputs\apk\debug\app-debug.apk"
$targetApiVersion = $null # If null will attempt to determine

if ($targetApiVersion -eq $null) {
	$emulatorProcesses = Get-WmiObject Win32_Process -Filter "name = 'emulator.exe'" | Select-Object CommandLine
	$scriptName = $MyInvocation.MyCommand.Name

	if ($emulatorProcesses.Length -eq 0) {
		Write-Host "Unabled to find emulator process`n"
		Write-Host "Launch an emulator with an AVD containing ""API ##"" in the name, or set the version you're targetting manually in $scriptName"
		Exit 1
	} elseif ($emulatorProcesses.Length -gt 1) {
		Write-Host "Multiple emulator processes found`n"
		Write-Host "Launch a single emulator with an AVD containing ""API ##"" in the name, or set the version you're targetting manually in $scriptName"
		Exit 1
	}

	if ($emulatorProcesses[0] -match '-avd\s+[^A\s]+API_([0-9]+)') {
		$targetApiVersion = $Matches.1
	} else {
		Write-Host "Failed to determine API version`n"
		Write-Host "Launch an emulator with an AVD containing ""API ##"" in the name, or set the version you're targetting manually in $scriptName"
		Exit 1
	}

	Write-Host "Found emulator running API $targetApiVersion"
}

if ($targetApiVersion -ge 21) {
	Write-Host "This should only be used for API versions below 21 (L)"
	Exit 1
} elseif ($targetApiVersion -ge 18) {
	$systemAppPath = "/system/priv-app"
} else {
	$systemAppPath = "/system/app"
}

$apkPathTarget = "$systemAppPath/$apkFileName"

$discardedOutput = & $adbPath root | Out-String

$output = & $adbPath remount | Out-String
if (!$output.Contains("remount succeeded")) {
	Write-Host "Unable to remount system, make sure emulator is running with -writable-system switch`n"
	Write-Host $output
	Exit 1
}

Write-Host "Pushing $apkPathHost to $apkPathTarget"
$output = & $adbPath push "$apkPathHost" "$apkPathTarget" | Out-String
if ($output.Contains('failed to copy') -or !$output.Contains("1 file pushed")) {
	Write-Host "Failed to push file, make sure emulator is running with -writable-system switch"
	Write-Host $output
	Exit 1
}

$output = & $adbPath shell chmod 644 $apkPathTarget | Out-String
if ($output.Length -gt 0) {
	Write-Host "Unexpected output while adjusting permissions`n"
	Write-Host $output
}

$output = & $adbPath shell mount -o remount,ro / | Out-String
if ($output.Length -gt 0) {
	Write-Host "Unexpected output while remounting`n"
	Write-Host $output
}

$discardedOutput = & $adbPath shell am force-stop $appPackage | Out-String

# This is left to Android Studio
#$output = & $adbPath shell am start -n "$appPackage/$appPackage.$appMainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER

Exit 0
