CREATE USER azurebotuser FROM LOGIN $(BOT_USER_NM);

EXEC sp_addrolemember 'db_datawriter', 'azurebotuser';
EXEC sp_addrolemember 'db_datareader', 'azurebotuser';
GRANT CREATE TABLE TO azurebotuser;
GRANT SELECT, INSERT, DELETE, ALTER, EXECUTE, CONTROL ON SCHEMA::dbo TO azurebotuser;
