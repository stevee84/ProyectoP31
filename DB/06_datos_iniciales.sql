SET SERVEROUTPUT ON

DECLARE
    v_cantidad NUMBER;
    v_rol TUSUARIOS.ROL%TYPE;

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
        WHERE DESCRIPCION = p_descripcion;

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
            'ADMIN',
            'ADMINISTRADOR',
            NULL,
            1
        );

        DBMS_OUTPUT.PUT_LINE(
            'Usuario inicial creado: ADMIN. Clave inicial: ADMIN.'
        );
    ELSE
        SELECT ROL
        INTO v_rol
        FROM TUSUARIOS
        WHERE ID = 'ADMIN';

        IF v_rol <> 'ADMINISTRADOR' THEN
            RAISE_APPLICATION_ERROR(
                -20002,
                'El ID ADMIN ya pertenece a un funcionario.'
            );
        END IF;

        DBMS_OUTPUT.PUT_LINE(
            'ADMIN ya existe: se conserva su clave actual.'
        );
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