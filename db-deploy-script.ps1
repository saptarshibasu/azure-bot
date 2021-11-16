param([string] $serverInstance, [string] $database, [string] $adminUsername, [string] $adminPassword, [string] $userPassword, [string] $userLogin)
$StringArray = "BOT_USER_PWD=$userPassword","BOT_USER_NM=$userLogin"
$serverInstanceUrl = "$serverInstance.database.windows.net"
find-module sqlserver | Install-Module -AllowClobber -Force
Invoke-Sqlcmd -InputFile ".\db-script-masterdb.sql" -Variable $StringArray -ServerInstance $serverInstanceUrl -Database master -Username $adminUsername -Password $adminPassword
Invoke-Sqlcmd -InputFile ".\db-script-azurebotdb.sql" -Variable $StringArray -ServerInstance $serverInstanceUrl -Database $database -Username $adminUsername -Password $adminPassword
Invoke-Sqlcmd -InputFile ".\db-script-azurebotuser.sql" -ServerInstance $serverInstanceUrl -Database $database -Username $userLogin -Password $userPassword
