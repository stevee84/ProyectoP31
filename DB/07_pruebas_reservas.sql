SET SERVEROUTPUT ON

DECLARE
    v_autor TUSUARIOS.ID%TYPE :=
        'PRUEBA-F-' || RAWTOHEX(SYS_GUID());

    v_otro TUSUARIOS.ID%TYPE :=
        'PRUEBA-G-' || RAWTOHEX(SYS_GUID());

    v_a TCATEGORIAS.ID%TYPE;
    v_b TCATEGORIAS.ID%TYPE;
    v_c TCATEGORIAS.ID%TYPE;

    v_r1 TRECURSOS.CODIGO%TYPE :=
        'PRUEBA-R-' || RAWTOHEX(SYS_GUID());

    v_r2 TRECURSOS.CODIGO%TYPE :=
        'PRUEBA-S-' || RAWTOHEX(SYS_GUID());

    v_inexistente TRECURSOS.CODIGO%TYPE :=
        'PRUEBA-X-' || RAWTOHEX(SYS_GUID());

    v_id TRESERVAS.ID%TYPE;
    v_solape TRESERVAS.ID%TYPE;
    v_parcial TRESERVAS.ID%TYPE;
    v_invalida TRESERVAS.ID%TYPE;

    v_inicio TIMESTAMP :=
        CAST(TRUNC(SYSDATE) + 1 + 9/24 AS TIMESTAMP);

    v_fin TIMESTAMP;
    v_cursor types.ref_cursor;

    v_codigo TRECURSOS.CODIGO%TYPE;
    v_categoria TCATEGORIAS.ID%TYPE;
    v_descripcion TRECURSOS.DESCRIPCION%TYPE;
    v_desc_categoria TCATEGORIAS.DESCRIPCION%TYPE;

    v_id_leido TRESERVAS.ID%TYPE;
    v_usuario TUSUARIOS.ID%TYPE;
    v_nombre TUSUARIOS.NOMBRE%TYPE;
    v_rol TUSUARIOS.ROL%TYPE;
    v_actividad TRESERVAS.DESCRIPCION_ACTIVIDAD%TYPE;
    v_inicio_leido TIMESTAMP;
    v_fin_leido TIMESTAMP;
    v_estado TRESERVAS.ESTADO%TYPE;

    v_semana DATE;
    v_fin_semana DATE;
    v_cantidad NUMBER;
    v_coincidencias NUMBER;
    v_base_semana NUMBER;
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
    SAVEPOINT prueba_reservas;

    v_fin := v_inicio + INTERVAL '1' HOUR;

    -- Datos propios de la prueba.
    insertarFuncionario(
        v_autor, 'Autor temporal', '8888-1001',
        'pbkdf2$1$UFJVRUJB$SU5JQ0lBTA=='
    );

    insertarFuncionario(
        v_otro, 'Otro temporal', '8888-1002',
        'pbkdf2$1$UFJVRUJB$SU5JQ0lBTA=='
    );

    insertarCategoria('Categoria A temporal', v_a);
    insertarCategoria('Categoria B temporal', v_b);
    insertarCategoria('Categoria sin recursos', v_c);

    insertarRecurso(v_r1, v_a, 'Recurso A temporal');
    insertarRecurso(v_r2, v_b, 'Recurso B temporal');

    -- Conteo previo para poder probar sobre una base con datos.
    SELECT COUNT(*)
    INTO v_base_semana
    FROM TRESERVAS
    WHERE ESTADO = 'ACTIVA'
      AND INICIO >= CAST(
          TRUNC(CAST(v_inicio AS DATE), 'IW')
          AS TIMESTAMP
      )
      AND INICIO < CAST(
          TRUNC(CAST(v_inicio AS DATE), 'IW') + 7
          AS TIMESTAMP
      );

    -- Crear reserva con dos recursos.
    insertarReserva(
        v_autor,
        'Actividad temporal',
        v_inicio,
        v_fin,
        v_id
    );

    asignarRecursoReserva(v_id, v_r1);
    asignarRecursoReserva(v_id, v_r2);

    -- Buscar la reserva.
    v_cursor := buscarReserva(v_id);

    FETCH v_cursor INTO
        v_id_leido,
        v_usuario,
        v_nombre,
        v_rol,
        v_actividad,
        v_inicio_leido,
        v_fin_leido,
        v_estado;

    comprobar(
        v_cursor%FOUND,
        'Buscar encuentra la reserva creada.'
    );

    comprobar(
        v_id_leido = v_id
        AND v_usuario = v_autor
        AND v_estado = 'ACTIVA'
        AND v_inicio_leido = v_inicio
        AND v_fin_leido = v_fin,
        'La reserva conserva autor, horario y estado.'
    );

    CLOSE v_cursor;

    -- Listado del autor.
    v_cursor := listarReservasUsuario(v_autor);

    FETCH v_cursor INTO
        v_id_leido,
        v_usuario,
        v_nombre,
        v_rol,
        v_actividad,
        v_inicio_leido,
        v_fin_leido,
        v_estado;

    comprobar(
        v_cursor%FOUND,
        'El listado del autor incluye la reserva.'
    );

    comprobar(
        v_id_leido = v_id,
        'El listado devuelve la reserva correcta.'
    );

    CLOSE v_cursor;

    -- Recursos asignados.
    v_cantidad := 0;
    v_cursor := listarRecursosReserva(v_id);

    LOOP
        FETCH v_cursor INTO
            v_codigo,
            v_categoria,
            v_descripcion,
            v_desc_categoria;

        EXIT WHEN v_cursor%NOTFOUND;

        comprobar(
            v_codigo IN (v_r1, v_r2),
            'La reserva contiene un recurso de la prueba.'
        );

        v_cantidad := v_cantidad + 1;
    END LOOP;

    CLOSE v_cursor;

    comprobar(
        v_cantidad = 2,
        'La reserva tiene sus dos recursos.'
    );

    -- Consulta por rango.
    v_cantidad := 0;
    v_cursor := listarReservasEnRango(v_inicio, v_fin);

    LOOP
        FETCH v_cursor INTO
            v_id_leido,
            v_usuario,
            v_nombre,
            v_rol,
            v_actividad,
            v_inicio_leido,
            v_fin_leido,
            v_estado;

        EXIT WHEN v_cursor%NOTFOUND;

        IF v_id_leido = v_id THEN
            v_cantidad := v_cantidad + 1;
        END IF;
    END LOOP;

    CLOSE v_cursor;

    comprobar(
        v_cantidad = 1,
        'La consulta por rango incluye la reserva activa.'
    );

    -- Disponibilidad durante la reserva.
    v_cursor := listarRecursosDisponibles(
        v_a, v_inicio, v_fin
    );

    FETCH v_cursor INTO
        v_codigo,
        v_categoria,
        v_descripcion,
        v_desc_categoria;

    comprobar(
        v_cursor%NOTFOUND,
        'El recurso ocupado no aparece disponible.'
    );

    CLOSE v_cursor;

    -- Horario contiguo: terminar a las 10 permite iniciar a las 10.
    v_cursor := listarRecursosDisponibles(
        v_a,
        v_fin,
        v_fin + INTERVAL '1' HOUR
    );

    FETCH v_cursor INTO
        v_codigo,
        v_categoria,
        v_descripcion,
        v_desc_categoria;

    comprobar(
        v_cursor%FOUND,
        'Un horario contiguo tiene disponibilidad.'
    );

    comprobar(
        v_codigo = v_r1,
        'El recurso disponible es el esperado.'
    );

    CLOSE v_cursor;

    -- Categoria sin recursos.
    v_cursor := listarRecursosDisponibles(
        v_c, v_inicio, v_fin
    );

    FETCH v_cursor INTO
        v_codigo,
        v_categoria,
        v_descripcion,
        v_desc_categoria;

    comprobar(
        v_cursor%NOTFOUND,
        'Una categoria sin recursos devuelve un cursor vacio.'
    );

    CLOSE v_cursor;

    -- Estadisticas por categoria.
    v_coincidencias := 0;

    v_cursor := contarPorCategoria(
        CAST(v_inicio AS DATE),
        CAST(v_inicio AS DATE)
    );

    LOOP
        FETCH v_cursor INTO
            v_categoria,
            v_desc_categoria,
            v_cantidad;

        EXIT WHEN v_cursor%NOTFOUND;

        IF v_categoria IN (v_a, v_b) THEN
            comprobar(
                v_cantidad = 1,
                'Cada categoria utilizada cuenta una reserva.'
            );

            v_coincidencias := v_coincidencias + 1;

        ELSIF v_categoria = v_c THEN
            comprobar(
                v_cantidad = 0,
                'Se incluye la categoria con cero reservas.'
            );

            v_coincidencias := v_coincidencias + 1;
        END IF;
    END LOOP;

    CLOSE v_cursor;

    comprobar(
        v_coincidencias = 3,
        'El conteo devuelve las tres categorias temporales.'
    );

    -- Estadisticas semanales.
    v_cursor := contarPorSemana(
        CAST(v_inicio AS DATE),
        CAST(v_inicio AS DATE)
    );

    FETCH v_cursor INTO
        v_semana,
        v_fin_semana,
        v_cantidad;

    comprobar(
        v_cursor%FOUND,
        'El conteo semanal incluye la semana de la reserva.'
    );

    comprobar(
        v_cantidad = v_base_semana + 1,
        'La semana suma exactamente una reserva nueva.'
    );

    CLOSE v_cursor;

    -- Intentar asignar un recurso ya ocupado.
    SAVEPOINT intento_solape;

    insertarReserva(
        v_otro,
        'Intento solapado',
        v_inicio,
        v_fin,
        v_solape
    );

    v_rechazado := FALSE;

    BEGIN
        asignarRecursoReserva(v_solape, v_r1);
    EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -20004 THEN
                v_rechazado := TRUE;
            ELSE
                RAISE;
            END IF;
    END;

    comprobar(
        v_rechazado,
        'Se rechaza asignar un recurso ya ocupado.'
    );

    ROLLBACK TO intento_solape;

    SELECT COUNT(*)
    INTO v_cantidad
    FROM TRESERVAS
    WHERE ID = v_solape;

    comprobar(
        v_cantidad = 0,
        'El intento solapado no deja una cabecera.'
    );

    -- Fallo en segunda asignacion: deshacer todo.
    SAVEPOINT operacion_parcial;

    insertarReserva(
        v_autor,
        'Intento parcial',
        v_fin,
        v_fin + INTERVAL '1' HOUR,
        v_parcial
    );

    asignarRecursoReserva(v_parcial, v_r1);

    v_rechazado := FALSE;

    BEGIN
        asignarRecursoReserva(v_parcial, v_inexistente);
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
        'La segunda asignacion falla por recurso inexistente.'
    );

    ROLLBACK TO operacion_parcial;

    SELECT COUNT(*)
    INTO v_cantidad
    FROM TRESERVAS
    WHERE ID = v_parcial;

    comprobar(
        v_cantidad = 0,
        'El rollback elimina la cabecera parcial.'
    );

    SELECT COUNT(*)
    INTO v_cantidad
    FROM TRESERVA_RECURSO
    WHERE ID_RESERVA = v_parcial;

    comprobar(
        v_cantidad = 0,
        'El rollback elimina tambien la primera asignacion.'
    );

    -- Cancelacion ajena.
    v_rechazado := FALSE;

    BEGIN
        cancelarReserva(v_id, v_otro);
    EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -20003 THEN
                v_rechazado := TRUE;
            ELSE
                RAISE;
            END IF;
    END;

    comprobar(
        v_rechazado,
        'Otro usuario no puede cancelar la reserva.'
    );

    -- Cancelacion propia.
    cancelarReserva(v_id, v_autor);

    SELECT ESTADO
    INTO v_estado
    FROM TRESERVAS
    WHERE ID = v_id;

    comprobar(
        v_estado = 'CANCELADA',
        'El autor cancela su reserva futura.'
    );

    -- Disponibilidad tras cancelar.
    v_cursor := listarRecursosDisponibles(
        v_a, v_inicio, v_fin
    );

    FETCH v_cursor INTO
        v_codigo,
        v_categoria,
        v_descripcion,
        v_desc_categoria;

    comprobar(
        v_cursor%FOUND,
        'Cancelar libera la disponibilidad del recurso.'
    );

    CLOSE v_cursor;

    -- Las canceladas no se cuentan.
    v_cursor := contarPorCategoria(
        CAST(v_inicio AS DATE),
        CAST(v_inicio AS DATE)
    );

    v_coincidencias := 0;

    LOOP
        FETCH v_cursor INTO
            v_categoria,
            v_desc_categoria,
            v_cantidad;

        EXIT WHEN v_cursor%NOTFOUND;

        IF v_categoria IN (v_a, v_b, v_c) THEN
            comprobar(
                v_cantidad = 0,
                'Las canceladas no suman en las estadisticas.'
            );

            v_coincidencias := v_coincidencias + 1;
        END IF;
    END LOOP;

    CLOSE v_cursor;

    comprobar(
        v_coincidencias = 3,
        'Las categorias siguen apareciendo con cantidad cero.'
    );

    -- No cancelar dos veces.
    v_rechazado := FALSE;

    BEGIN
        cancelarReserva(v_id, v_autor);
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
        'No se permite cancelar otra vez la misma reserva.'
    );

    -- Horario no permitido.
    v_rechazado := FALSE;

    BEGIN
        insertarReserva(
            v_autor,
            'Horario incorrecto',
            CAST(
                TRUNC(CAST(v_inicio AS DATE))
                AS TIMESTAMP
            ) + INTERVAL '20' HOUR,
            CAST(
                TRUNC(CAST(v_inicio AS DATE))
                AS TIMESTAMP
            ) + INTERVAL '21' HOUR,
            v_invalida
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
        'Se rechaza una reserva fuera de las 7 a las 18.'
    );

    -- Deshacer todos los datos temporales.
    ROLLBACK TO prueba_reservas;

    SELECT COUNT(*)
    INTO v_cantidad
    FROM TRESERVAS
    WHERE ID = v_id;

    comprobar(
        v_cantidad = 0,
        'La prueba no deja su reserva en la base.'
    );

    SELECT COUNT(*)
    INTO v_cantidad
    FROM TUSUARIOS
    WHERE ID IN (v_autor, v_otro);

    comprobar(
        v_cantidad = 0,
        'La prueba no deja sus usuarios.'
    );

    SELECT COUNT(*)
    INTO v_cantidad
    FROM TRECURSOS
    WHERE CODIGO IN (v_r1, v_r2);

    comprobar(
        v_cantidad = 0,
        'La prueba no deja sus recursos.'
    );

    SELECT COUNT(*)
    INTO v_cantidad
    FROM TCATEGORIAS
    WHERE ID IN (v_a, v_b, v_c);

    comprobar(
        v_cantidad = 0,
        'La prueba no deja sus categorias.'
    );

    DBMS_OUTPUT.PUT_LINE(
        'PRUEBAS DE RESERVAS COMPLETADAS CORRECTAMENTE.'
    );

EXCEPTION
    WHEN OTHERS THEN
        IF v_cursor%ISOPEN THEN
            CLOSE v_cursor;
        END IF;

        ROLLBACK TO prueba_reservas;
        DBMS_OUTPUT.PUT_LINE('[ERROR] ' || SQLERRM);
        RAISE;
END;
/