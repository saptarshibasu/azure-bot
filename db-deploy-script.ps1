param([string] $serverInstance, [string] $database, [string] $adminUsername, [string] $adminPassword, [string] $userPassword, [string] $userLogin)
$StringArray = "BOT_USER_PWD=$userPassword","BOT_USER_NM=$userLogin"
$serverInstanceUrl = "$serverInstance.database.windows.net"
find-module sqlserver | Install-Module -AllowClobber -Force
Invoke-Sqlcmd -Query
    "CREATE LOGIN $(BOT_USER_NM) WITH password = '$(BOT_USER_PWD)';" 
    -Variable $StringArray -ServerInstance $serverInstanceUrl 
    -Database master -Username $adminUsername -Password $adminPassword
Invoke-Sqlcmd -Query 
    "
    CREATE USER azurebotuser FROM LOGIN $(BOT_USER_NM);
    EXEC sp_addrolemember 'db_datawriter', 'azurebotuser';
    EXEC sp_addrolemember 'db_datareader', 'azurebotuser';
    GRANT CREATE TABLE TO azurebotuser;
    GRANT SELECT, INSERT, DELETE, ALTER, EXECUTE, CONTROL ON SCHEMA::dbo TO azurebotuser;
    " 
    -Variable $StringArray -ServerInstance $serverInstanceUrl 
    -Database $database -Username $adminUsername -Password $adminPassword
Invoke-Sqlcmd -Query 
    "
    CREATE TABLE TICKET(
    TICKET_ID VARCHAR(10),
    STATUS VARCHAR(10));
    INSERT INTO TICKET VALUES ('TKT1234567', 'CLSD');
    INSERT INTO TICKET VALUES ('TKT1234568', 'OPN');
    INSERT INTO TICKET VALUES ('TKT1234569', 'NEW');
    " 
    -ServerInstance $serverInstanceUrl -Database $database 
    -Username $userLogin -Password $userPassword
