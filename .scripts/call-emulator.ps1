$sendFrom = '#' # hash = Private number
$emuHost = "localhost"
$emuPort = 5554
$authToken = [IO.File]::ReadAllText("$env:USERPROFILE\.emulator_console_auth_token")

if ($args[0] -ne $null) {
	$sendFrom = $args[0]
}

$tcpConnection = New-Object System.Net.Sockets.TcpClient($emuHost, $emuPort)
$tcpStream = $tcpConnection.GetStream()
$reader = New-Object System.IO.StreamReader($tcpStream)
$writer = New-Object System.IO.StreamWriter($tcpStream)
$writer.AutoFlush = $true

if ($tcpConnection.Connected -ne $true) {
	Write-Host "Failed to connect to $emuHost@$emuPort"
	Exit 1
}

$response = ""
while ($tcpStream.DataAvailable -or $reader.Peek() -ne -1) {
	$response = $response + "`n" + $reader.ReadLine()
}
if (!$response.Contains('Authentication required')) {
	Write-Host "Initial response did not contain expected authentication request"
	Write-Host $response
	Exit 1
}

$commands = @(
	"auth $authToken",
	"gsm call $sendFrom",
	"exit"
)

foreach ($command in $commands) {
	if ($tcpConnection.Connected -ne $true) {
		Write-Host "Lost connection to emulator before running all commands"
		Exit 1
	}

	$writer.WriteLine($command)
	Start-Sleep -Milliseconds 100

	$response = ""
	while ($tcpStream.DataAvailable -or $reader.Peek() -ne -1) {
		$response = $response + "`n" + $reader.ReadLine()
	}
	if ($response.Length -gt 0 -and !$response.Contains("OK")) {
		Write-Host "Unexpected response during command $command"
		Write-Host $response
		Exit 1
	}
}

$client.Close | Out-Null
Exit 0
