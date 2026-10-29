SET SERVEROUTPUT ON

DECLARE
    v_cantidad NUMBER;
    v_rol TUSUARIOS.ROL%TYPE;
    v_clave TUSUARIOS.CLAVE%TYPE;

    -- Hash PBKDF2 de la clave inicial "ADMIN" (generado con
    -- java seguridad.ClaveHash ADMIN). La base nunca guarda texto plano.
    c_clave_admin CONSTANT TUSUARIOS.CLAVE%TYPE :=
        'pbkdf2$120000$TAoKTmoKzoG38O8HrY9HYA==$1aqXsAIOUQ7ZYz9hgqKg7E1YbCuTA9m08aNl4KV6kMU=';

    PROCEDURE crearCategoriaSiFalta (
        p_descripcion IN TCATEGORIAS.DESCRIPCION%TYPE
    )
    AS
        v_existentes NUMBER;
        v_id TCATEGORIAS.ID%TYPE;
    BEGIN
        SELECT COUNT(*)
        INTO v_existentes
        FROM TCATEGORIAS
        WHERE UPPER(TRIM(DESCRIPCION)) = UPPER(TRIM(p_descripcion));

        IF v_existentes = 0 THEN
            insertarCategoria(p_descripcion, v_id);

            DBMS_OUTPUT.PUT_LINE(
                'Categoria creada: '
                || v_id || ' - ' || p_descripcion
            );
        END IF;
    END crearCategoriaSiFalta;
BEGIN
    SAVEPOINT datos_iniciales;

    SELECT COUNT(*)
    INTO v_cantidad
    FROM TUSUARIOS
    WHERE ID = 'ADMIN';

    IF v_cantidad = 0 THEN
        INSERT INTO TUSUARIOS (
            ID, NOMBRE, CLAVE, ROL,
            TELEFONO, PRIMER_INGRESO
        )
        VALUES (
            'ADMIN',
            'Administrador',
            c_clave_admin,
            'ADMINISTRADOR',
            NULL,
            1
        );

        DBMS_OUTPUT.PUT_LINE(
            'Usuario inicial creado: ADMIN. Clave inicial: ADMIN.'
        );
    ELSE
        SELECT ROL, CLAVE
        INTO v_rol, v_clave
        FROM TUSUARIOS
        WHERE ID = 'ADMIN';

        IF v_rol <> 'ADMINISTRADOR' THEN
            RAISE_APPLICATION_ERROR(
                -20002,
                'El ID ADMIN ya pertenece a un funcionario.'
            );
        END IF;

        IF v_clave NOT LIKE 'pbkdf2$%' THEN
            -- Base anterior con clave en texto plano: vuelve a la inicial con hash.
            UPDATE TUSUARIOS
            SET CLAVE = c_clave_admin,
                PRIMER_INGRESO = 1
            WHERE ID = 'ADMIN';

            DBMS_OUTPUT.PUT_LINE(
                'ADMIN tenia clave sin hash: se restablecio la clave inicial ADMIN.'
            );
        ELSE
            DBMS_OUTPUT.PUT_LINE(
                'ADMIN ya existe: se conserva su clave actual.'
            );
        END IF;
    END IF;

    crearCategoriaSiFalta('Sala para 10 personas');
    crearCategoriaSiFalta('Laptop Windows');
    crearCategoriaSiFalta('Sala de juntas');

    COMMIT;

    DBMS_OUTPUT.PUT_LINE(
        'DATOS INICIALES GUARDADOS CORRECTAMENTE.'
    );

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK TO datos_iniciales;
        RAISE;
END;
/

SELECT ID, NOMBRE, ROL, PRIMER_INGRESO
FROM TUSUARIOS
WHERE ID = 'ADMIN';

SELECT ID, DESCRIPCION
FROM TCATEGORIAS
ORDER BY DESCRIPCION;