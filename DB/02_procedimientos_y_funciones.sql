-- =========================================================
-- TIPO DE CURSOR
-- =========================================================

CREATE OR REPLACE PACKAGE types AS
    TYPE ref_cursor IS REF CURSOR;
END types;
/

SHOW ERRORS PACKAGE types


-- =========================================================
-- CATEGORIAS
-- =========================================================

CREATE OR REPLACE PROCEDURE insertarCategoria (
    p_descripcion IN TCATEGORIAS.DESCRIPCION%TYPE,
    p_id OUT TCATEGORIAS.ID%TYPE
)
AS
    v_numero VARCHAR2(30);
BEGIN
    v_numero := TO_CHAR(SEQ_CATEGORIAS.NEXTVAL);

    IF LENGTH(v_numero) < 6 THEN
        v_numero := LPAD(v_numero, 6, '0');
    END IF;

    p_id := 'CAT-' || v_numero;

    INSERT INTO TCATEGORIAS (ID, DESCRIPCION)
    VALUES (p_id, TRIM(p_descripcion));
END insertarCategoria;
/

SHOW ERRORS PROCEDURE insertarCategoria


CREATE OR REPLACE FUNCTION buscarCategoria (
    p_id IN TCATEGORIAS.ID%TYPE
)
RETURN types.ref_cursor
AS
    v_cursor types.ref_cursor;
BEGIN
    OPEN v_cursor FOR
        SELECT ID, DESCRIPCION
        FROM TCATEGORIAS
        WHERE ID = p_id;

    RETURN v_cursor;
END buscarCategoria;
/

SHOW ERRORS FUNCTION buscarCategoria


CREATE OR REPLACE FUNCTION listarCategorias
RETURN types.ref_cursor
AS
    v_cursor types.ref_cursor;
BEGIN
    OPEN v_cursor FOR
        SELECT ID, DESCRIPCION
        FROM TCATEGORIAS
        ORDER BY DESCRIPCION;

    RETURN v_cursor;
END listarCategorias;
/

SHOW ERRORS FUNCTION listarCategorias


CREATE OR REPLACE PROCEDURE modificarCategoria (
    p_id IN TCATEGORIAS.ID%TYPE,
    p_descripcion IN TCATEGORIAS.DESCRIPCION%TYPE
)
AS
BEGIN
    UPDATE TCATEGORIAS
    SET DESCRIPCION = TRIM(p_descripcion)
    WHERE ID = p_id;

    IF SQL%ROWCOUNT = 0 THEN
        RAISE_APPLICATION_ERROR(
            -20001,
            'No existe una categoria con ese identificador.'
        );
    END IF;
END modificarCategoria;
/

SHOW ERRORS PROCEDURE modificarCategoria


CREATE OR REPLACE PROCEDURE eliminarCategoria (
    p_id IN TCATEGORIAS.ID%TYPE
)
AS
BEGIN
    DELETE FROM TCATEGORIAS
    WHERE ID = p_id;

    IF SQL%ROWCOUNT = 0 THEN
        RAISE_APPLICATION_ERROR(
            -20001,
            'No existe una categoria con ese identificador.'
        );
    END IF;
END eliminarCategoria;
/

SHOW ERRORS PROCEDURE eliminarCategoria


-- =========================================================
-- RECURSOS
-- Las consultas devuelven las mismas cuatro columnas.
-- =========================================================

CREATE OR REPLACE PROCEDURE insertarRecurso (
    p_codigo IN TRECURSOS.CODIGO%TYPE,
    p_id_categoria IN TRECURSOS.ID_CATEGORIA%TYPE,
    p_descripcion IN TRECURSOS.DESCRIPCION%TYPE
)
AS
BEGIN
    INSERT INTO TRECURSOS (
        CODIGO,
        ID_CATEGORIA,
        DESCRIPCION
    )
    VALUES (
        TRIM(p_codigo),
        TRIM(p_id_categoria),
        TRIM(p_descripcion)
    );
END insertarRecurso;
/

SHOW ERRORS PROCEDURE insertarRecurso


CREATE OR REPLACE FUNCTION buscarRecurso (
    p_codigo IN TRECURSOS.CODIGO%TYPE
)
RETURN types.ref_cursor
AS
    v_cursor types.ref_cursor;
BEGIN
    OPEN v_cursor FOR
        SELECT r.CODIGO,
               r.ID_CATEGORIA,
               r.DESCRIPCION,
               c.DESCRIPCION AS DESCRIPCION_CATEGORIA
        FROM TRECURSOS r
        JOIN TCATEGORIAS c ON c.ID = r.ID_CATEGORIA
        WHERE r.CODIGO = TRIM(p_codigo);

    RETURN v_cursor;
END buscarRecurso;
/

SHOW ERRORS FUNCTION buscarRecurso


CREATE OR REPLACE FUNCTION listarRecursos
RETURN types.ref_cursor
AS
    v_cursor types.ref_cursor;
BEGIN
    OPEN v_cursor FOR
        SELECT r.CODIGO,
               r.ID_CATEGORIA,
               r.DESCRIPCION,
               c.DESCRIPCION AS DESCRIPCION_CATEGORIA
        FROM TRECURSOS r
        JOIN TCATEGORIAS c ON c.ID = r.ID_CATEGORIA
        ORDER BY r.DESCRIPCION, r.CODIGO;

    RETURN v_cursor;
END listarRecursos;
/

SHOW ERRORS FUNCTION listarRecursos


CREATE OR REPLACE FUNCTION listarRecursosPorCategoria (
    p_id_categoria IN TCATEGORIAS.ID%TYPE
)
RETURN types.ref_cursor
AS
    v_cursor types.ref_cursor;
BEGIN
    OPEN v_cursor FOR
        SELECT r.CODIGO,
               r.ID_CATEGORIA,
               r.DESCRIPCION,
               c.DESCRIPCION AS DESCRIPCION_CATEGORIA
        FROM TRECURSOS r
        JOIN TCATEGORIAS c ON c.ID = r.ID_CATEGORIA
        WHERE r.ID_CATEGORIA = TRIM(p_id_categoria)
        ORDER BY r.DESCRIPCION, r.CODIGO;

    RETURN v_cursor;
END listarRecursosPorCategoria;
/

SHOW ERRORS FUNCTION listarRecursosPorCategoria


CREATE OR REPLACE PROCEDURE modificarRecurso (
    p_codigo IN TRECURSOS.CODIGO%TYPE,
    p_id_categoria IN TRECURSOS.ID_CATEGORIA%TYPE,
    p_descripcion IN TRECURSOS.DESCRIPCION%TYPE
)
AS
BEGIN
    UPDATE TRECURSOS
    SET ID_CATEGORIA = TRIM(p_id_categoria),
        DESCRIPCION = TRIM(p_descripcion)
    WHERE CODIGO = TRIM(p_codigo);

    IF SQL%ROWCOUNT = 0 THEN
        RAISE_APPLICATION_ERROR(
            -20001,
            'No existe un recurso con ese codigo.'
        );
    END IF;
END modificarRecurso;
/

SHOW ERRORS PROCEDURE modificarRecurso


CREATE OR REPLACE PROCEDURE eliminarRecurso (
    p_codigo IN TRECURSOS.CODIGO%TYPE
)
AS
BEGIN
    DELETE FROM TRECURSOS
    WHERE CODIGO = TRIM(p_codigo);

    IF SQL%ROWCOUNT = 0 THEN
        RAISE_APPLICATION_ERROR(
            -20001,
            'No existe un recurso con ese codigo.'
        );
    END IF;
END eliminarRecurso;
/

SHOW ERRORS PROCEDURE eliminarRecurso


-- =========================================================
-- FUNCIONARIOS Y ACCESO
-- =========================================================

-- p_clave_hash: hash PBKDF2 de la clave inicial (= ID), calculado en Java.
-- La base nunca recibe ni guarda claves en texto plano.
CREATE OR REPLACE PROCEDURE insertarFuncionario (
    p_id IN TUSUARIOS.ID%TYPE,
    p_nombre IN TUSUARIOS.NOMBRE%TYPE,
    p_telefono IN TUSUARIOS.TELEFONO%TYPE,
    p_clave_hash IN TUSUARIOS.CLAVE%TYPE
)
AS
BEGIN
    IF TRIM(p_id) IS NULL
       OR TRIM(p_nombre) IS NULL
       OR TRIM(p_telefono) IS NULL THEN
        RAISE_APPLICATION_ERROR(
            -20002,
            'El identificador, nombre y telefono son obligatorios.'
        );
    END IF;

    IF TRIM(p_clave_hash) IS NULL THEN
        RAISE_APPLICATION_ERROR(
            -20002,
            'La clave inicial es obligatoria.'
        );
    END IF;

    INSERT INTO TUSUARIOS (
        ID, NOMBRE, CLAVE, ROL, TELEFONO, PRIMER_INGRESO
    )
    VALUES (
        TRIM(p_id),
        TRIM(p_nombre),
        p_clave_hash,
        'FUNCIONARIO',
        TRIM(p_telefono),
        1
    );
END insertarFuncionario;
/

SHOW ERRORS PROCEDURE insertarFuncionario


CREATE OR REPLACE PROCEDURE modificarFuncionario (
    p_id IN TUSUARIOS.ID%TYPE,
    p_nombre IN TUSUARIOS.NOMBRE%TYPE,
    p_telefono IN TUSUARIOS.TELEFONO%TYPE
)
AS
BEGIN
    IF TRIM(p_id) IS NULL
       OR TRIM(p_nombre) IS NULL
       OR TRIM(p_telefono) IS NULL THEN
        RAISE_APPLICATION_ERROR(
            -20002,
            'El identificador, nombre y telefono son obligatorios.'
        );
    END IF;

    UPDATE TUSUARIOS
    SET NOMBRE = TRIM(p_nombre),
        TELEFONO = TRIM(p_telefono)
    WHERE ID = TRIM(p_id)
      AND ROL = 'FUNCIONARIO';

    IF SQL%ROWCOUNT = 0 THEN
        RAISE_APPLICATION_ERROR(
            -20001,
            'No existe un funcionario con ese identificador.'
        );
    END IF;
END modificarFuncionario;
/

SHOW ERRORS PROCEDURE modificarFuncionario


CREATE OR REPLACE PROCEDURE eliminarFuncionario (
    p_id IN TUSUARIOS.ID%TYPE
)
AS
BEGIN
    DELETE FROM TUSUARIOS
    WHERE ID = TRIM(p_id)
      AND ROL = 'FUNCIONARIO';

    IF SQL%ROWCOUNT = 0 THEN
        RAISE_APPLICATION_ERROR(
            -20001,
            'No existe un funcionario con ese identificador.'
        );
    END IF;
END eliminarFuncionario;
/

SHOW ERRORS PROCEDURE eliminarFuncionario


CREATE OR REPLACE FUNCTION buscarFuncionario (
    p_id IN TUSUARIOS.ID%TYPE
)
RETURN types.ref_cursor
AS
    v_cursor types.ref_cursor;
BEGIN
    OPEN v_cursor FOR
        SELECT ID, NOMBRE, TELEFONO
        FROM TUSUARIOS
        WHERE ID = TRIM(p_id)
          AND ROL = 'FUNCIONARIO';

    RETURN v_cursor;
END buscarFuncionario;
/

SHOW ERRORS FUNCTION buscarFuncionario


CREATE OR REPLACE FUNCTION listarFuncionarios
RETURN types.ref_cursor
AS
    v_cursor types.ref_cursor;
BEGIN
    OPEN v_cursor FOR
        SELECT ID, NOMBRE, TELEFONO
        FROM TUSUARIOS
        WHERE ROL = 'FUNCIONARIO'
        ORDER BY NOMBRE, ID;

    RETURN v_cursor;
END listarFuncionarios;
/

SHOW ERRORS FUNCTION listarFuncionarios


-- validarUsuario comparaba claves en texto plano: se retira si quedo de una
-- version anterior del script (ORA-04043 = no existe, se ignora).
BEGIN
    EXECUTE IMMEDIATE 'DROP FUNCTION validarUsuario';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE <> -4043 THEN
            RAISE;
        END IF;
END;
/


-- Devuelve el hash guardado (nunca una clave en texto plano) para que
-- Java lo verifique con PBKDF2. Si el usuario no existe, el cursor va vacio
-- y Java lo traduce a CodigoError.SESION.
CREATE OR REPLACE FUNCTION obtenerCredencialesUsuario (
    p_id IN TUSUARIOS.ID%TYPE
)
RETURN types.ref_cursor
AS
    v_cursor types.ref_cursor;
BEGIN
    OPEN v_cursor FOR
        SELECT ID, NOMBRE, ROL, PRIMER_INGRESO, CLAVE
        FROM TUSUARIOS
        WHERE ID = TRIM(p_id);

    RETURN v_cursor;
END obtenerCredencialesUsuario;
/

SHOW ERRORS FUNCTION obtenerCredencialesUsuario


-- Java obtiene el ID del contexto de la conexion y envia el hash de la nueva clave.
CREATE OR REPLACE PROCEDURE cambiarClaveUsuario (
    p_id IN TUSUARIOS.ID%TYPE,
    p_nueva_clave IN TUSUARIOS.CLAVE%TYPE
)
AS
BEGIN
    IF TRIM(p_nueva_clave) IS NULL THEN
        RAISE_APPLICATION_ERROR(
            -20002,
            'La nueva clave es obligatoria.'
        );
    END IF;

    UPDATE TUSUARIOS
    SET CLAVE = p_nueva_clave,
        PRIMER_INGRESO = 0
    WHERE ID = TRIM(p_id);

    IF SQL%ROWCOUNT = 0 THEN
        RAISE_APPLICATION_ERROR(
            -20001,
            'No existe un usuario con ese identificador.'
        );
    END IF;
END cambiarClaveUsuario;
/

SHOW ERRORS PROCEDURE cambiarClaveUsuario


-- =========================================================
-- DISPONIBILIDAD Y RESERVAS
-- =========================================================

CREATE OR REPLACE FUNCTION listarRecursosDisponibles (
    p_id_categoria IN TCATEGORIAS.ID%TYPE,
    p_inicio IN TRESERVAS.INICIO%TYPE,
    p_fin IN TRESERVAS.FIN%TYPE
)
RETURN types.ref_cursor
AS
    v_cursor types.ref_cursor;
BEGIN
    IF p_inicio IS NULL
       OR p_fin IS NULL
       OR p_fin <= p_inicio THEN
        RAISE_APPLICATION_ERROR(
            -20002,
            'El intervalo de consulta no es valido.'
        );
    END IF;

    OPEN v_cursor FOR
        SELECT r.CODIGO,
               r.ID_CATEGORIA,
               r.DESCRIPCION,
               c.DESCRIPCION AS DESCRIPCION_CATEGORIA
        FROM TRECURSOS r
        JOIN TCATEGORIAS c ON c.ID = r.ID_CATEGORIA
        WHERE r.ID_CATEGORIA = TRIM(p_id_categoria)
          AND NOT EXISTS (
              SELECT 1
              FROM TRESERVA_RECURSO rr
              JOIN TRESERVAS reserva
                ON reserva.ID = rr.ID_RESERVA
              WHERE rr.CODIGO_RECURSO = r.CODIGO
                AND reserva.ESTADO = 'ACTIVA'
                AND reserva.INICIO < p_fin
                AND p_inicio < reserva.FIN
          )
        ORDER BY UPPER(r.DESCRIPCION), r.CODIGO;

    RETURN v_cursor;
END listarRecursosDisponibles;
/

SHOW ERRORS FUNCTION listarRecursosDisponibles


-- Solo crea la cabecera.
-- Java confirma cabecera y recursos en una misma transaccion.
CREATE OR REPLACE PROCEDURE insertarReserva (
    p_id_usuario IN TUSUARIOS.ID%TYPE,
    p_descripcion IN TRESERVAS.DESCRIPCION_ACTIVIDAD%TYPE,
    p_inicio IN TRESERVAS.INICIO%TYPE,
    p_fin IN TRESERVAS.FIN%TYPE,
    p_id OUT TRESERVAS.ID%TYPE
)
AS
    v_cantidad NUMBER;
    v_dia TIMESTAMP;
BEGIN
    IF TRIM(p_id_usuario) IS NULL
       OR TRIM(p_descripcion) IS NULL THEN
        RAISE_APPLICATION_ERROR(
            -20002,
            'El funcionario y la actividad son obligatorios.'
        );
    END IF;

    IF p_inicio IS NULL
       OR p_fin IS NULL
       OR p_fin <= p_inicio THEN
        RAISE_APPLICATION_ERROR(
            -20002,
            'El horario de la reserva no es valido.'
        );
    END IF;

    IF p_inicio < LOCALTIMESTAMP THEN
        RAISE_APPLICATION_ERROR(
            -20002,
            'No se pueden crear reservas en el pasado.'
        );
    END IF;

    v_dia := CAST(
        TRUNC(CAST(p_inicio AS DATE))
        AS TIMESTAMP
    );

    IF TRUNC(CAST(p_fin AS DATE))
           <> TRUNC(CAST(p_inicio AS DATE))
       OR p_inicio < v_dia + INTERVAL '7' HOUR
       OR p_fin > v_dia + INTERVAL '18' HOUR THEN
        RAISE_APPLICATION_ERROR(
            -20002,
            'La reserva debe ser de un mismo dia, entre las 7 y las 18.'
        );
    END IF;

    SELECT COUNT(*)
    INTO v_cantidad
    FROM TUSUARIOS
    WHERE ID = TRIM(p_id_usuario)
      AND ROL = 'FUNCIONARIO';

    IF v_cantidad = 0 THEN
        RAISE_APPLICATION_ERROR(
            -20003,
            'La reserva debe pertenecer a un funcionario existente.'
        );
    END IF;

    p_id := SEQ_RESERVAS.NEXTVAL;

    INSERT INTO TRESERVAS (
        ID,
        ID_USUARIO,
        DESCRIPCION_ACTIVIDAD,
        INICIO,
        FIN,
        ESTADO
    )
    VALUES (
        p_id,
        TRIM(p_id_usuario),
        TRIM(p_descripcion),
        p_inicio,
        p_fin,
        'ACTIVA'
    );
END insertarReserva;
/

SHOW ERRORS PROCEDURE insertarReserva


-- Bloquea el recurso hasta finalizar la transaccion.
-- Java mantiene ademas un bloqueo compartido durante
-- disponibilidad, seleccion, insercion y confirmacion.
CREATE OR REPLACE PROCEDURE asignarRecursoReserva (
    p_id_reserva IN TRESERVAS.ID%TYPE,
    p_codigo IN TRECURSOS.CODIGO%TYPE
)
AS
    v_codigo TRECURSOS.CODIGO%TYPE;
    v_inicio TRESERVAS.INICIO%TYPE;
    v_fin TRESERVAS.FIN%TYPE;
    v_estado TRESERVAS.ESTADO%TYPE;
    v_cantidad NUMBER;
BEGIN
    SELECT CODIGO
    INTO v_codigo
    FROM TRECURSOS
    WHERE CODIGO = TRIM(p_codigo)
    FOR UPDATE;

    SELECT INICIO, FIN, ESTADO
    INTO v_inicio, v_fin, v_estado
    FROM TRESERVAS
    WHERE ID = p_id_reserva
    FOR UPDATE;

    IF v_estado <> 'ACTIVA' THEN
        RAISE_APPLICATION_ERROR(
            -20002,
            'La reserva no esta activa.'
        );
    END IF;

    SELECT COUNT(*)
    INTO v_cantidad
    FROM TRESERVA_RECURSO rr
    JOIN TRESERVAS r ON r.ID = rr.ID_RESERVA
    WHERE rr.CODIGO_RECURSO = v_codigo
      AND r.ESTADO = 'ACTIVA'
      AND r.ID <> p_id_reserva
      AND r.INICIO < v_fin
      AND v_inicio < r.FIN;

    IF v_cantidad > 0 THEN
        RAISE_APPLICATION_ERROR(
            -20004,
            'El recurso ya esta reservado en ese horario.'
        );
    END IF;

    INSERT INTO TRESERVA_RECURSO (
        ID_RESERVA,
        CODIGO_RECURSO
    )
    VALUES (
        p_id_reserva,
        v_codigo
    );

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(
            -20001,
            'No existe la reserva o el recurso.'
        );
END asignarRecursoReserva;
/

SHOW ERRORS PROCEDURE asignarRecursoReserva


CREATE OR REPLACE PROCEDURE cancelarReserva (
    p_id IN TRESERVAS.ID%TYPE,
    p_id_usuario IN TUSUARIOS.ID%TYPE
)
AS
    v_autor TUSUARIOS.ID%TYPE;
    v_inicio TRESERVAS.INICIO%TYPE;
    v_estado TRESERVAS.ESTADO%TYPE;
BEGIN
    SELECT ID_USUARIO, INICIO, ESTADO
    INTO v_autor, v_inicio, v_estado
    FROM TRESERVAS
    WHERE ID = p_id
    FOR UPDATE;

    IF TRIM(p_id_usuario) IS NULL
       OR v_autor <> TRIM(p_id_usuario) THEN
        RAISE_APPLICATION_ERROR(
            -20003,
            'Solo puede cancelar sus propias reservas.'
        );
    END IF;

    IF v_estado <> 'ACTIVA'
       OR v_inicio <= LOCALTIMESTAMP THEN
        RAISE_APPLICATION_ERROR(
            -20002,
            'Solo se cancelan reservas activas que no hayan iniciado.'
        );
    END IF;

    UPDATE TRESERVAS
    SET ESTADO = 'CANCELADA'
    WHERE ID = p_id;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(
            -20001,
            'No existe la reserva.'
        );
END cancelarReserva;
/

SHOW ERRORS PROCEDURE cancelarReserva


-- Las consultas siguientes devuelven ocho columnas.
CREATE OR REPLACE FUNCTION buscarReserva (
    p_id IN TRESERVAS.ID%TYPE
)
RETURN types.ref_cursor
AS
    v_cursor types.ref_cursor;
BEGIN
    OPEN v_cursor FOR
        SELECT r.ID,
               r.ID_USUARIO,
               u.NOMBRE AS NOMBRE_USUARIO,
               u.ROL,
               r.DESCRIPCION_ACTIVIDAD,
               r.INICIO,
               r.FIN,
               r.ESTADO
        FROM TRESERVAS r
        JOIN TUSUARIOS u ON u.ID = r.ID_USUARIO
        WHERE r.ID = p_id;

    RETURN v_cursor;
END buscarReserva;
/

SHOW ERRORS FUNCTION buscarReserva


CREATE OR REPLACE FUNCTION listarReservasUsuario (
    p_id_usuario IN TUSUARIOS.ID%TYPE
)
RETURN types.ref_cursor
AS
    v_cursor types.ref_cursor;
BEGIN
    OPEN v_cursor FOR
        SELECT r.ID,
               r.ID_USUARIO,
               u.NOMBRE AS NOMBRE_USUARIO,
               u.ROL,
               r.DESCRIPCION_ACTIVIDAD,
               r.INICIO,
               r.FIN,
               r.ESTADO
        FROM TRESERVAS r
        JOIN TUSUARIOS u ON u.ID = r.ID_USUARIO
        WHERE r.ID_USUARIO = TRIM(p_id_usuario)
        ORDER BY r.INICIO DESC, r.ID DESC;

    RETURN v_cursor;
END listarReservasUsuario;
/

SHOW ERRORS FUNCTION listarReservasUsuario


-- Intervalo [desde, hasta), util para calendario y agenda.
CREATE OR REPLACE FUNCTION listarReservasEnRango (
    p_desde IN TRESERVAS.INICIO%TYPE,
    p_hasta IN TRESERVAS.FIN%TYPE
)
RETURN types.ref_cursor
AS
    v_cursor types.ref_cursor;
BEGIN
    IF p_desde IS NULL
       OR p_hasta IS NULL
       OR p_hasta <= p_desde THEN
        RAISE_APPLICATION_ERROR(
            -20002,
            'El intervalo de consulta no es valido.'
        );
    END IF;

    OPEN v_cursor FOR
        SELECT r.ID,
               r.ID_USUARIO,
               u.NOMBRE AS NOMBRE_USUARIO,
               u.ROL,
               r.DESCRIPCION_ACTIVIDAD,
               r.INICIO,
               r.FIN,
               r.ESTADO
        FROM TRESERVAS r
        JOIN TUSUARIOS u ON u.ID = r.ID_USUARIO
        WHERE r.ESTADO = 'ACTIVA'
          AND r.INICIO < p_hasta
          AND p_desde < r.FIN
        ORDER BY r.INICIO, r.ID;

    RETURN v_cursor;
END listarReservasEnRango;
/

SHOW ERRORS FUNCTION listarReservasEnRango


CREATE OR REPLACE FUNCTION listarRecursosReserva (
    p_id_reserva IN TRESERVAS.ID%TYPE
)
RETURN types.ref_cursor
AS
    v_cursor types.ref_cursor;
BEGIN
    OPEN v_cursor FOR
        SELECT r.CODIGO,
               r.ID_CATEGORIA,
               r.DESCRIPCION,
               c.DESCRIPCION AS DESCRIPCION_CATEGORIA
        FROM TRESERVA_RECURSO rr
        JOIN TRECURSOS r
          ON r.CODIGO = rr.CODIGO_RECURSO
        JOIN TCATEGORIAS c
          ON c.ID = r.ID_CATEGORIA
        WHERE rr.ID_RESERVA = p_id_reserva
        ORDER BY r.DESCRIPCION, r.CODIGO;

    RETURN v_cursor;
END listarRecursosReserva;
/

SHOW ERRORS FUNCTION listarRecursosReserva


-- =========================================================
-- ESTADISTICAS
-- =========================================================

-- Fechas inclusivas.
-- Incluye todas las categorias, tambien las que tienen cero.
CREATE OR REPLACE FUNCTION contarPorCategoria (
    p_desde IN DATE,
    p_hasta IN DATE
)
RETURN types.ref_cursor
AS
    v_cursor types.ref_cursor;
BEGIN
    IF p_desde IS NULL
       OR p_hasta IS NULL
       OR TRUNC(p_desde) > TRUNC(p_hasta) THEN
        RAISE_APPLICATION_ERROR(
            -20002,
            'El rango de fechas no es valido.'
        );
    END IF;

    OPEN v_cursor FOR
        SELECT c.ID AS ID_CATEGORIA,
               c.DESCRIPCION AS DESCRIPCION_CATEGORIA,
               COUNT(
                   DISTINCT CASE
                       WHEN reserva.ESTADO = 'ACTIVA'
                        AND reserva.INICIO >=
                            CAST(TRUNC(p_desde) AS TIMESTAMP)
                        AND reserva.INICIO <
                            CAST(TRUNC(p_hasta) + 1 AS TIMESTAMP)
                       THEN reserva.ID
                   END
               ) AS CANTIDAD
        FROM TCATEGORIAS c
        LEFT JOIN TRECURSOS recurso
          ON recurso.ID_CATEGORIA = c.ID
        LEFT JOIN TRESERVA_RECURSO rr
          ON rr.CODIGO_RECURSO = recurso.CODIGO
        LEFT JOIN TRESERVAS reserva
          ON reserva.ID = rr.ID_RESERVA
        GROUP BY c.ID, c.DESCRIPCION
        ORDER BY c.DESCRIPCION, c.ID;

    RETURN v_cursor;
END contarPorCategoria;
/

SHOW ERRORS FUNCTION contarPorCategoria


-- Semanas completas de lunes a domingo.
-- El servicio Java completa con cero las semanas sin registros.
CREATE OR REPLACE FUNCTION contarPorSemana (
    p_desde IN DATE,
    p_hasta IN DATE
)
RETURN types.ref_cursor
AS
    v_cursor types.ref_cursor;
BEGIN
    IF p_desde IS NULL
       OR p_hasta IS NULL
       OR TRUNC(p_desde) > TRUNC(p_hasta) THEN
        RAISE_APPLICATION_ERROR(
            -20002,
            'El rango de fechas no es valido.'
        );
    END IF;

    OPEN v_cursor FOR
        SELECT TRUNC(CAST(INICIO AS DATE), 'IW')
                   AS INICIO_SEMANA,
               TRUNC(CAST(INICIO AS DATE), 'IW') + 6
                   AS FIN_SEMANA,
               COUNT(*) AS CANTIDAD
        FROM TRESERVAS
        WHERE ESTADO = 'ACTIVA'
          AND INICIO >=
              CAST(TRUNC(p_desde, 'IW') AS TIMESTAMP)
          AND INICIO <
              CAST(TRUNC(p_hasta, 'IW') + 7 AS TIMESTAMP)
        GROUP BY TRUNC(CAST(INICIO AS DATE), 'IW')
        ORDER BY INICIO_SEMANA;

    RETURN v_cursor;
END contarPorSemana;
/

SHOW ERRORS FUNCTION contarPorSemana