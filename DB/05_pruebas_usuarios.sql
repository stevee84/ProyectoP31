SET SERVEROUTPUT ON

DECLARE
    v_id TUSUARIOS.ID%TYPE :=
        'PRUEBA-F-' || RAWTOHEX(SYS_GUID());

    v_id_admin TUSUARIOS.ID%TYPE :=
        'PRUEBA-A-' || RAWTOHEX(SYS_GUID());

    v_codigo TRECURSOS.CODIGO%TYPE :=
        'PRUEBA-R-' || RAWTOHEX(SYS_GUID());

    v_categoria TCATEGORIAS.ID%TYPE;
    v_reserva TRESERVAS.ID%TYPE;

    v_clave_nueva TUSUARIOS.CLAVE%TYPE :=
        'NuevaClave_Prueba!';

    v_cursor types.ref_cursor;
    v_id_leido TUSUARIOS.ID%TYPE;
    v_nombre TUSUARIOS.NOMBRE%TYPE;
    v_telefono TUSUARIOS.TELEFONO%TYPE;
    v_rol TUSUARIOS.ROL%TYPE;
    v_primer_ingreso TUSUARIOS.PRIMER_INGRESO%TYPE;
    v_cantidad NUMBER;
    v_rechazado BOOLEAN;

    PROCEDURE comprobar (
        p_condicion IN BOOLEAN,
        p_mensaje IN VARCHAR2
    )
    AS
    BEGIN
        IF p_condicion IS NULL OR NOT p_condicion THEN
            RAISE_APPLICATION_ERROR(-20901, p_mensaje);
        END IF;

        DBMS_OUTPUT.PUT_LINE('[OK] ' || p_mensaje);
    END comprobar;
BEGIN
    SAVEPOINT prueba_usuarios;

    -- Registrar y buscar.
    insertarFuncionario(
        v_id,
        '  Funcionario de prueba  ',
        ' 8888-1111 '
    );

    v_cursor := buscarFuncionario(v_id);

    FETCH v_cursor INTO v_id_leido, v_nombre, v_telefono;

    comprobar(
        v_cursor%FOUND,
        'Buscar encuentra el funcionario nuevo.'
    );

    comprobar(
        v_id_leido = v_id
        AND v_nombre = 'Funcionario de prueba'
        AND v_telefono = '8888-1111',
        'Sus datos personales se guardan correctamente.'
    );

    CLOSE v_cursor;

    -- Acceso inicial.
    v_cursor := validarUsuario(v_id, v_id);

    FETCH v_cursor INTO
        v_id_leido, v_nombre, v_rol, v_primer_ingreso;

    comprobar(
        v_cursor%FOUND,
        'La clave inicial igual al ID permite el ingreso.'
    );

    comprobar(
        v_id_leido = v_id
        AND v_rol = 'FUNCIONARIO'
        AND v_primer_ingreso = 1,
        'El rol y el primer ingreso son correctos.'
    );

    CLOSE v_cursor;

    -- Credenciales incorrectas.
    v_cursor := validarUsuario(v_id, 'Clave_incorrecta');

    FETCH v_cursor INTO
        v_id_leido, v_nombre, v_rol, v_primer_ingreso;

    comprobar(
        v_cursor%NOTFOUND,
        'Una clave incorrecta no devuelve un usuario.'
    );

    CLOSE v_cursor;

    -- Cambio de clave.
    cambiarClaveUsuario(v_id, v_clave_nueva);
    v_cursor := validarUsuario(v_id, v_clave_nueva);

    FETCH v_cursor INTO
        v_id_leido, v_nombre, v_rol, v_primer_ingreso;

    comprobar(
        v_cursor%FOUND,
        'La nueva clave permite el ingreso.'
    );

    comprobar(
        v_primer_ingreso = 0,
        'El cambio de clave completa el primer ingreso.'
    );

    CLOSE v_cursor;

    v_cursor := validarUsuario(v_id, v_id);

    FETCH v_cursor INTO
        v_id_leido, v_nombre, v_rol, v_primer_ingreso;

    comprobar(
        v_cursor%NOTFOUND,
        'La clave anterior ya no permite el ingreso.'
    );

    CLOSE v_cursor;

    -- Rechazar clave vacia.
    v_rechazado := FALSE;

    BEGIN
        cambiarClaveUsuario(v_id, '   ');
    EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -20002 THEN
                v_rechazado := TRUE;
            ELSE
                RAISE;
            END IF;
    END;

    comprobar(
        v_rechazado,
        'Se rechaza una clave formada solo por espacios.'
    );

    -- Modificar datos personales.
    modificarFuncionario(
        v_id,
        'Funcionario modificado',
        '8888-2222'
    );

    v_cursor := buscarFuncionario(v_id);

    FETCH v_cursor INTO v_id_leido, v_nombre, v_telefono;

    comprobar(
        v_cursor%FOUND,
        'El funcionario sigue existiendo.'
    );

    comprobar(
        v_nombre = 'Funcionario modificado'
        AND v_telefono = '8888-2222',
        'Se actualizan nombre y telefono.'
    );

    CLOSE v_cursor;

    v_cursor := validarUsuario(v_id, v_clave_nueva);

    FETCH v_cursor INTO
        v_id_leido, v_nombre, v_rol, v_primer_ingreso;

    comprobar(
        v_cursor%FOUND,
        'Modificar datos personales conserva la clave.'
    );

    comprobar(
        v_primer_ingreso = 0,
        'Se conserva el primer ingreso completado.'
    );

    CLOSE v_cursor;

    -- Administrador temporal para verificar filtros.
    INSERT INTO TUSUARIOS (
        ID, NOMBRE, CLAVE, ROL, PRIMER_INGRESO
    )
    VALUES (
        v_id_admin,
        'Administrador temporal',
        v_id_admin,
        'ADMINISTRADOR',
        1
    );

    v_cantidad := 0;
    v_cursor := listarFuncionarios;

    LOOP
        FETCH v_cursor INTO v_id_leido, v_nombre, v_telefono;
        EXIT WHEN v_cursor%NOTFOUND;

        comprobar(
            v_id_leido <> v_id_admin,
            'El listado excluye al administrador temporal.'
        );

        IF v_id_leido = v_id THEN
            v_cantidad := v_cantidad + 1;
        END IF;
    END LOOP;

    CLOSE v_cursor;

    comprobar(
        v_cantidad = 1,
        'El listado incluye al funcionario una sola vez.'
    );

    v_cursor := buscarFuncionario(v_id_admin);

    FETCH v_cursor INTO v_id_leido, v_nombre, v_telefono;

    comprobar(
        v_cursor%NOTFOUND,
        'Buscar funcionario no devuelve un administrador.'
    );

    CLOSE v_cursor;

    -- El CRUD de funcionarios no altera administradores.
    v_rechazado := FALSE;

    BEGIN
        modificarFuncionario(
            v_id_admin,
            'No debe cambiar',
            '8888-9999'
        );
    EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -20001 THEN
                v_rechazado := TRUE;
            ELSE
                RAISE;
            END IF;
    END;

    comprobar(
        v_rechazado,
        'El CRUD de funcionarios no modifica administradores.'
    );

    v_rechazado := FALSE;

    BEGIN
        eliminarFuncionario(v_id_admin);
    EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -20001 THEN
                v_rechazado := TRUE;
            ELSE
                RAISE;
            END IF;
    END;

    comprobar(
        v_rechazado,
        'El CRUD de funcionarios no elimina administradores.'
    );

    -- Identificador duplicado.
    v_rechazado := FALSE;

    BEGIN
        insertarFuncionario(
            v_id,
            'Duplicado',
            '8888-3333'
        );
    EXCEPTION
        WHEN DUP_VAL_ON_INDEX THEN
            v_rechazado := TRUE;
    END;

    comprobar(
        v_rechazado,
        'Se rechaza un identificador duplicado.'
    );

    -- Reserva temporal para probar la relacion con el usuario.
    insertarCategoria(
        'Categoria de prueba de usuarios',
        v_categoria
    );

    insertarRecurso(
        v_codigo,
        v_categoria,
        'Recurso de prueba de usuarios'
    );

    v_reserva := SEQ_RESERVAS.NEXTVAL;

    INSERT INTO TRESERVAS (
        ID, ID_USUARIO, DESCRIPCION_ACTIVIDAD,
        INICIO, FIN, ESTADO
    )
    VALUES (
        v_reserva,
        v_id,
        'Actividad de prueba',
        CAST(TRUNC(SYSDATE) + 1 + 9/24 AS TIMESTAMP),
        CAST(TRUNC(SYSDATE) + 1 + 10/24 AS TIMESTAMP),
        'ACTIVA'
    );

    INSERT INTO TRESERVA_RECURSO (
        ID_RESERVA, CODIGO_RECURSO
    )
    VALUES (
        v_reserva, v_codigo
    );

    v_rechazado := FALSE;

    BEGIN
        eliminarFuncionario(v_id);
    EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -2292 THEN
                v_rechazado := TRUE;
            ELSE
                RAISE;
            END IF;
    END;

    comprobar(
        v_rechazado,
        'Se protege el usuario con reservas asociadas.'
    );

    -- Retirar exclusivamente la reserva temporal.
    DELETE FROM TRESERVA_RECURSO
    WHERE ID_RESERVA = v_reserva;

    DELETE FROM TRESERVAS
    WHERE ID = v_reserva;

    eliminarRecurso(v_codigo);
    eliminarCategoria(v_categoria);

    -- Eliminar funcionario.
    eliminarFuncionario(v_id);
    v_cursor := buscarFuncionario(v_id);

    FETCH v_cursor INTO v_id_leido, v_nombre, v_telefono;

    comprobar(
        v_cursor%NOTFOUND,
        'El funcionario eliminado ya no aparece.'
    );

    CLOSE v_cursor;

    v_cursor := validarUsuario(v_id, v_clave_nueva);

    FETCH v_cursor INTO
        v_id_leido, v_nombre, v_rol, v_primer_ingreso;

    comprobar(
        v_cursor%NOTFOUND,
        'El usuario eliminado ya no puede iniciar sesion.'
    );

    CLOSE v_cursor;

    -- Telefono obligatorio.
    v_rechazado := FALSE;

    BEGIN
        insertarFuncionario(
            v_id,
            'Sin telefono',
            '   '
        );
    EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -20002 THEN
                v_rechazado := TRUE;
            ELSE
                RAISE;
            END IF;
    END;

    comprobar(
        v_rechazado,
        'El telefono es obligatorio.'
    );

    -- Cambio de clave de usuario inexistente.
    v_rechazado := FALSE;

    BEGIN
        cambiarClaveUsuario(v_id, v_clave_nueva);
    EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -20001 THEN
                v_rechazado := TRUE;
            ELSE
                RAISE;
            END IF;
    END;

    comprobar(
        v_rechazado,
        'Cambiar clave informa si el usuario no existe.'
    );

    ROLLBACK TO prueba_usuarios;

    SELECT COUNT(*)
    INTO v_cantidad
    FROM TUSUARIOS
    WHERE ID IN (v_id, v_id_admin);

    comprobar(
        v_cantidad = 0,
        'La prueba no deja usuarios temporales.'
    );

    SELECT COUNT(*)
    INTO v_cantidad
    FROM TRESERVAS
    WHERE ID = v_reserva;

    comprobar(
        v_cantidad = 0,
        'La prueba no deja su reserva.'
    );

    SELECT COUNT(*)
    INTO v_cantidad
    FROM TRECURSOS
    WHERE CODIGO = v_codigo;

    comprobar(
        v_cantidad = 0,
        'La prueba no deja su recurso.'
    );

    SELECT COUNT(*)
    INTO v_cantidad
    FROM TCATEGORIAS
    WHERE ID = v_categoria;

    comprobar(
        v_cantidad = 0,
        'La prueba no deja su categoria.'
    );

    DBMS_OUTPUT.PUT_LINE(
        'PRUEBAS DE USUARIOS COMPLETADAS CORRECTAMENTE.'
    );

EXCEPTION
    WHEN OTHERS THEN
        IF v_cursor%ISOPEN THEN
            CLOSE v_cursor;
        END IF;

        ROLLBACK TO prueba_usuarios;
        DBMS_OUTPUT.PUT_LINE('[ERROR] ' || SQLERRM);
        RAISE;
END;
/