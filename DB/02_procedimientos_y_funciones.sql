-- Tipo de cursor compartido por las funciones de consulta.
CREATE OR REPLACE PACKAGE types AS
    TYPE ref_cursor IS REF CURSOR;
END types;
/

-- Mostrar posibles errores de compilación.
SHOW ERRORS PACKAGE types

CREATE OR REPLACE PROCEDURE insertarCategoria (
    p_descripcion IN TCATEGORIAS.DESCRIPCION%TYPE,
    p_id OUT TCATEGORIAS.ID%TYPE
)
AS
    v_numero VARCHAR2(30);
BEGIN
    -- Obtener el siguiente número de la secuencia.
    v_numero := TO_CHAR(SEQ_CATEGORIAS.NEXTVAL);

    -- Completar con ceros hasta tener al menos seis dígitos.
    IF LENGTH(v_numero) < 6 THEN
        v_numero := LPAD(v_numero, 6, '0');
    END IF;

    -- Construir un identificador como CAT-000001.
    p_id := 'CAT-' || v_numero;

    INSERT INTO TCATEGORIAS (ID, DESCRIPCION)
    VALUES (p_id, TRIM(p_descripcion));
END insertarCategoria;
/

SHOW ERRORS PROCEDURE insertarCategoria


-- Buscar una categoría por su identificador.
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


-- Listar todas las categorías.
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

-- Modificar la descripción de una categoría.
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
-- Eliminar una categoría.
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

-- Registrar un recurso dentro de una categoría existente.
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