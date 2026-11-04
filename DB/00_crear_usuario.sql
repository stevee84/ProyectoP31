-- Crea el usuario del proyecto. Ejecutar UNA vez conectado como SYSTEM.
-- Pide la clave del nuevo usuario; usar la misma en oracle.properties.
-- Después, conectado como RESERVAS_P2, ejecutar 01, 02 y 06.

CREATE USER RESERVAS_P2 IDENTIFIED BY "&clave_reservas_p2"
    DEFAULT TABLESPACE USERS
    QUOTA UNLIMITED ON USERS;

GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE, CREATE VIEW,
      CREATE TRIGGER, CREATE PROCEDURE, CREATE TYPE
    TO RESERVAS_P2;
