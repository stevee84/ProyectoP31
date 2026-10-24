VARIABLE id_prueba VARCHAR2(20)
VARIABLE resultado REFCURSOR

SAVEPOINT prueba_categorias;

-- Insertar.
BEGIN
    insertarCategoria(
        'Categoria de prueba',
        :id_prueba
    );
END;
/

PRINT id_prueba

-- Buscar.
BEGIN
    :resultado := buscarCategoria(:id_prueba);
END;
/

PRINT resultado

-- Listar.
BEGIN
    :resultado := listarCategorias;
END;
/

PRINT resultado

-- Modificar.
BEGIN
    modificarCategoria(
        :id_prueba,
        'Categoria modificada'
    );
END;
/

-- Comprobar modificacion.
BEGIN
    :resultado := buscarCategoria(:id_prueba);
END;
/

PRINT resultado

-- Eliminar.
BEGIN
    eliminarCategoria(:id_prueba);
END;
/

-- Debe devolver cero filas.
BEGIN
    :resultado := buscarCategoria(:id_prueba);
END;
/

PRINT resultado

ROLLBACK TO prueba_categorias;