-- Variables para recibir el ID generado y las consultas.
VARIABLE id_prueba VARCHAR2(20)
VARIABLE resultado REFCURSOR

-- Punto al que volveremos al terminar la prueba.
SAVEPOINT prueba_categorias;

-- 1. INSERTAR una categoría temporal.
BEGIN
    insertarCategoria(
        'Categoria de prueba',
        :id_prueba
    );
END;
/

PRINT id_prueba

-- 2. BUSCAR la categoría insertada.
BEGIN
    :resultado := buscarCategoria(:id_prueba);
END;
/

PRINT resultado

-- 3. LISTAR todas las categorías.
BEGIN
    :resultado := listarCategorias;
END;
/

PRINT resultado

-- 4. MODIFICAR la categoría.
BEGIN
    modificarCategoria(
        :id_prueba,
        'Categoria modificada'
    );
END;
/

-- 5. COMPROBAR la modificación.
BEGIN
    :resultado := buscarCategoria(:id_prueba);
END;
/

PRINT resultado

-- 6. ELIMINAR la categoría.
BEGIN
    eliminarCategoria(:id_prueba);
END;
/

-- 7. COMPROBAR la eliminación: debe devolver cero filas.
BEGIN
    :resultado := buscarCategoria(:id_prueba);
END;
/

PRINT resultado

-- Deshacer los cambios realizados desde el inicio de la prueba.
ROLLBACK TO prueba_categorias;