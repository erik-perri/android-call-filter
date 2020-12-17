# Helper Scripts

## call-emulator.ps1

This calls the emulator via telnet. If no argument is
provided the call will be from a private number, if an
argument is provided that number will be used (can only
contain 0-9 # +).

## install-as-system-app.ps1

This installs the application on the emulator as a system
app. This is used to test Android versions < 21 where the
app must be a system app to cancel calls. It should not be
used on newer versions.

## Run as system app.run.xml

This is an Android Studio run configuration which runs the
install-as-system-app script before launch. For it to work
you must setup the external tool named "Install as System
App".

Location on Windows:
`%AppData%\Google\AndroidStudioX.X\tools\External Tools.xml`

```xml
<toolSet name="External Tools">
  <tool name="Install as system app" showInMainMenu="false" showInEditor="false" showInProject="false" showInSearchPopup="false" disabled="false" useConsole="true" showConsoleOnStdOut="false" showConsoleOnStdErr="false" synchronizeAfterRun="true">
    <exec>
      <option name="COMMAND" value="powershell.exe" />
      <option name="PARAMETERS" value="&amp; Set-ExecutionPolicy Unrestricted -Scope Process -Force; .\install-as-system-app.ps1;" />
      <option name="WORKING_DIRECTORY" value="$ProjectFileDir$/.scripts" />
    </exec>
  </tool>
</toolSet>
```
