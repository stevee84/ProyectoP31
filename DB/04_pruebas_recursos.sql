SET SERVEROUTPUT ON

DECLARE
    v_codigo TRECURSOS.CODIGO%TYPE :=
        'PRUEBA-' || RAWTOHEX(SYS_GUID());

    v_categoria_a TCATEGORIAS.ID%TYPE;
    v_categoria_b TCATEGORIAS.ID%TYPE;

    v_categoria_inexistente TCATEGORIAS.ID%TYPE :=
        SUBSTR(RAWTOHEX(SYS_GUID()), 1, 20);

    v_cursor types.ref_cursor;
    v_codigo_leido TRECURSOS.CODIGO%TYPE;
    v_categoria_leida TCATEGORIAS.ID%TYPE;
    v_descripcion_leida TRECURSOS.DESCRIPCION%TYPE;
    v_descripcion_categoria TCATEGORIAS.DESCRIPCION%TYPE;
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
    SAVEPOINT prueba_recursos;

    insertarCategoria('Categoria A de prueba', v_categoria_a);
    insertarCategoria('Categoria B de prueba', v_categoria_b);

    insertarRecurso(
        v_codigo,
        v_categoria_a,
        '  Recurso de prueba  '
    );

    -- Buscar.
    v_cursor := buscarRecurso(v_codigo);

    FETCH v_cursor INTO
        v_codigo_leido,
        v_categoria_leida,
        v_descripcion_leida,
        v_descripcion_categoria;

    comprobar(
        v_cursor%FOUND,
        'Buscar encuentra el recurso insertado.'
    );

    comprobar(
        v_codigo_leido = v_codigo
        AND v_categoria_leida = v_categoria_a,
        'El codigo y la categoria son correctos.'
    );

    comprobar(
        v_descripcion_leida = 'Recurso de prueba',
        'La descripcion se guarda sin espacios externos.'
    );

    comprobar(
        v_descripcion_categoria = 'Categoria A de prueba',
        'La consulta incluye la descripcion de la categoria.'
    );

    CLOSE v_cursor;

    -- Listado general.
    v_cantidad := 0;
    v_cursor := listarRecursos;

    LOOP
        FETCH v_cursor INTO
            v_codigo_leido,
            v_categoria_leida,
            v_descripcion_leida,
            v_descripcion_categoria;

        EXIT WHEN v_cursor%NOTFOUND;

        IF v_codigo_leido = v_codigo THEN
            v_cantidad := v_cantidad + 1;
        END IF;
    END LOOP;

    CLOSE v_cursor;

    comprobar(
        v_cantidad = 1,
        'El listado general incluye el recurso una sola vez.'
    );

    -- Filtro por categoria.
    v_cursor := listarRecursosPorCategoria(v_categoria_a);

    FETCH v_cursor INTO
        v_codigo_leido,
        v_categoria_leida,
        v_descripcion_leida,
        v_descripcion_categoria;

    comprobar(
        v_cursor%FOUND,
        'El filtro encuentra el recurso.'
    );

    comprobar(
        v_codigo_leido = v_codigo
        AND v_categoria_leida = v_categoria_a,
        'El filtro devuelve la categoria correcta.'
    );

    FETCH v_cursor INTO
        v_codigo_leido,
        v_categoria_leida,
        v_descripcion_leida,
        v_descripcion_categoria;

    comprobar(
        v_cursor%NOTFOUND,
        'La categoria temporal tiene solamente ese recurso.'
    );

    CLOSE v_cursor;

    -- Modificar.
    modificarRecurso(
        v_codigo,
        v_categoria_b,
        'Recurso modificado'
    );

    v_cursor := buscarRecurso(v_codigo);

    FETCH v_cursor INTO
        v_codigo_leido,
        v_categoria_leida,
        v_descripcion_leida,
        v_descripcion_categoria;

    comprobar(
        v_cursor%FOUND,
        'El recurso sigue existiendo.'
    );

    comprobar(
        v_categoria_leida = v_categoria_b
        AND v_descripcion_leida = 'Recurso modificado'
        AND v_descripcion_categoria = 'Categoria B de prueba',
        'La modificacion actualiza categoria y descripcion.'
    );

    CLOSE v_cursor;

    v_cursor := listarRecursosPorCategoria(v_categoria_a);

    FETCH v_cursor INTO
        v_codigo_leido,
        v_categoria_leida,
        v_descripcion_leida,
        v_descripcion_categoria;

    comprobar(
        v_cursor%NOTFOUND,
        'La categoria anterior queda sin recursos.'
    );

    CLOSE v_cursor;

    v_cursor := listarRecursosPorCategoria(v_categoria_b);

    FETCH v_cursor INTO
        v_codigo_leido,
        v_categoria_leida,
        v_descripcion_leida,
        v_descripcion_categoria;

    comprobar(
        v_cursor%FOUND,
        'El recurso aparece en la nueva categoria.'
    );

    comprobar(
        v_codigo_leido = v_codigo,
        'El filtro nuevo devuelve el codigo correcto.'
    );

    CLOSE v_cursor;

    -- Duplicados.
    v_rechazado := FALSE;

    BEGIN
        insertarRecurso(v_codigo, v_categoria_b, 'Duplicado');
    EXCEPTION
        WHEN DUP_VAL_ON_INDEX THEN
            v_rechazado := TRUE;
    END;

    comprobar(
        v_rechazado,
        'Se rechaza un codigo duplicado.'
    );

    -- Categoria inexistente.
    SELECT COUNT(*)
    INTO v_cantidad
    FROM TCATEGORIAS
    WHERE ID = v_categoria_inexistente;

    comprobar(
        v_cantidad = 0,
        'La categoria del siguiente ensayo no existe.'
    );

    v_rechazado := FALSE;

    BEGIN
        insertarRecurso(
            v_codigo || '-X',
            v_categoria_inexistente,
            'Sin categoria'
        );
    EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -2291 THEN
                v_rechazado := TRUE;
            ELSE
                RAISE;
            END IF;
    END;

    comprobar(
        v_rechazado,
        'Se rechaza un recurso cuya categoria no existe.'
    );

    -- Categoria en uso.
    v_rechazado := FALSE;

    BEGIN
        eliminarCategoria(v_categoria_b);
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
        'Se protege la categoria con recursos asociados.'
    );

    -- Eliminar.
    eliminarRecurso(v_codigo);
    v_cursor := buscarRecurso(v_codigo);

    FETCH v_cursor INTO
        v_codigo_leido,
        v_categoria_leida,
        v_descripcion_leida,
        v_descripcion_categoria;

    comprobar(
        v_cursor%NOTFOUND,
        'El recurso eliminado ya no aparece.'
    );

    CLOSE v_cursor;

    -- Modificar inexistente.
    v_rechazado := FALSE;

    BEGIN
        modificarRecurso(
            v_codigo,
            v_categoria_b,
            'No existe'
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
        'Modificar informa que el recurso no existe.'
    );

    -- Eliminar inexistente.
    v_rechazado := FALSE;

    BEGIN
        eliminarRecurso(v_codigo);
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
        'Eliminar informa que el recurso no existe.'
    );

    ROLLBACK TO prueba_recursos;

    SELECT COUNT(*)
    INTO v_cantidad
    FROM TCATEGORIAS
    WHERE ID IN (v_categoria_a, v_categoria_b);

    comprobar(
        v_cantidad = 0,
        'Las categorias temporales se deshicieron.'
    );

    SELECT COUNT(*)
    INTO v_cantidad
    FROM TRECURSOS
    WHERE CODIGO IN (v_codigo, v_codigo || '-X');

    comprobar(
        v_cantidad = 0,
        'Los recursos temporales se deshicieron.'
    );

    DBMS_OUTPUT.PUT_LINE(
        'PRUEBAS DE RECURSOS COMPLETADAS CORRECTAMENTE.'
    );

EXCEPTION
    WHEN OTHERS THEN
        IF v_cursor%ISOPEN THEN
            CLOSE v_cursor;
        END IF;

        ROLLBACK TO prueba_recursos;
        DBMS_OUTPUT.PUT_LINE('[ERROR] ' || SQLERRM);
        RAISE;
END;
/