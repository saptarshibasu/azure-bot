CREATE LOGIN azurebot WITH password = '@zureBot@123';

CREATE USER azurebotuser FROM LOGIN azurebot;

EXEC sp_addrolemember 'db_datawriter', 'azurebotuser';
EXEC sp_addrolemember 'db_datareader', 'azurebotuser';

CREATE TABLE TICKET(
    TICKET_ID VARCHAR(10),
    STATUS VARCHAR(10)
);

INSERT INTO TICKET VALUES ('TKT1234567', 'CLSD');
INSERT INTO TICKET VALUES ('TKT1234568', 'OPN');
INSERT INTO TICKET VALUES ('TKT1234569', 'NEW');
