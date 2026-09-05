package model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ModeloReservacionesTest {

    private ModeloReservaciones modelo;

    @BeforeEach
    void setUp() {
        modelo = new ModeloReservaciones();
    }

    @Test
    void testLoginCorrecto() {
        // El admin viene registrado por defecto con id=ADMIN, pass=ADMIN
        Empleado admin = modelo.validarCredenciales("ADMIN", "ADMIN");
        assertNotNull(admin);
        assertEquals("ADMIN", admin.getId());
    }

    @Test
    void testLoginIncorrecto() {
        Empleado resultado = modelo.validarCredenciales("ADMIN", "wrongpass");
        assertNull(resultado);
    }

    @Test
    void testCambiarClave() {
        Empleado admin = modelo.validarCredenciales("ADMIN", "ADMIN");
        assertNotNull(admin);

        admin.cambiarContraseña("nuevaClave123");

        // La vieja contraseña ya no funciona
        assertNull(modelo.validarCredenciales("ADMIN", "ADMIN"));
        // La nueva sí
        assertNotNull(modelo.validarCredenciales("ADMIN", "nuevaClave123"));
    }

    @Test
    void testRegistrarFuncionarioDuplicado() {
        Funcionario f1 = new Funcionario("Juan", "F001", "8888-0000");
        assertTrue(modelo.registrarEmpleado(f1));

        Funcionario f2 = new Funcionario("Pedro", "F001", "9999-0000");
        assertFalse(modelo.registrarEmpleado(f2));
    }

    @Test
    void testBuscarFuncionarioPorNombre() {
        modelo.registrarEmpleado(new Funcionario("María López", "F010", "8888-1111"));
        modelo.registrarEmpleado(new Funcionario("Carlos Ruiz", "F011", "8888-2222"));

        List<Funcionario> resultados = modelo.buscarFuncionariosPorTexto("María");
        assertEquals(1, resultados.size());
        assertEquals("F010", resultados.get(0).getId());
    }

    @Test
    void testCategoriaAutoId() {
        // El modelo ya tiene 3 categorías iniciales (CAT-000001, CAT-000002, CAT-000003)
        CategoriaRecurso nueva = modelo.registrarCategoria("Proyector HD");
        assertNotNull(nueva.getId());
        assertTrue(nueva.getId().startsWith("CAT-"));
        assertEquals("Proyector HD", nueva.getDescripcion());
    }

    @Test
    void testActualizarCategoria() {
        CategoriaRecurso cat = modelo.registrarCategoria("Sala pequeña");
        boolean actualizada = modelo.actualizarCategoria(cat.getId(), "Sala mediana");
        assertTrue(actualizada);

        CategoriaRecurso encontrada = modelo.buscarCategoria(cat.getId());
        assertEquals("Sala mediana", encontrada.getDescripcion());
    }

    @Test
    void testEliminarCategoriaConRecursos() {
        CategoriaRecurso cat = modelo.listarCategorias().get(0);
        modelo.registrarRecurso("REC-X01", cat.getId(), "Recurso de prueba");

        assertThrows(IllegalStateException.class, () -> {
            modelo.eliminarCategoria(cat.getId());
        });
    }

    @Test
    void testEliminarCategoriaSinRecursos() {
        CategoriaRecurso cat = modelo.registrarCategoria("Categoría temporal");
        boolean eliminada = modelo.eliminarCategoria(cat.getId());
        assertTrue(eliminada);
        assertNull(modelo.buscarCategoria(cat.getId()));
    }

    @Test
    void testRegistrarRecurso() {
        CategoriaRecurso cat = modelo.listarCategorias().get(0);
        boolean registrado = modelo.registrarRecurso("REC-001", cat.getId(), "Sala A");
        assertTrue(registrado);

        Recurso recurso = modelo.buscarRecurso("REC-001");
        assertNotNull(recurso);
        assertEquals("Sala A", recurso.getDescripcion());
        assertEquals(cat, recurso.getCategoria());
    }

    @Test
    void testEliminarRecursoConReservaActiva() {
        CategoriaRecurso cat = modelo.listarCategorias().get(0);
        modelo.registrarRecurso("REC-DEL", cat.getId(), "Recurso a eliminar");

        Funcionario func = new Funcionario("Test User", "FTEST", "0000-0000");
        modelo.registrarEmpleado(func);

        LocalDateTime manana = LocalDate.now().plusDays(1).atTime(10, 0);
        modelo.reservarPorCategorias(func, List.of(cat.getId()), "Actividad",
                manana, manana.plusHours(1));

        assertThrows(IllegalStateException.class, () -> {
            modelo.eliminarRecurso("REC-DEL");
        });
    }

    @Test
    void testEliminarEmpleadoConReservaActiva() {
        CategoriaRecurso cat = modelo.listarCategorias().get(0);
        modelo.registrarRecurso("REC-EMP", cat.getId(), "Recurso empleado");

        Funcionario func = new Funcionario("Empleado Test", "FEMP", "1111-1111");
        modelo.registrarEmpleado(func);

        LocalDateTime manana = LocalDate.now().plusDays(1).atTime(10, 0);
        modelo.reservarPorCategorias(func, List.of(cat.getId()), "Actividad emp",
                manana, manana.plusHours(1));

        assertThrows(IllegalStateException.class, () -> {
            modelo.eliminarEmpleado("FEMP");
        });
    }

    @Test
    void testCrearReservacion() {
        CategoriaRecurso cat = modelo.listarCategorias().get(0);
        modelo.registrarRecurso("REC-R01", cat.getId(), "Recurso reserva");

        Funcionario func = new Funcionario("Reservador", "FRES", "2222-2222");
        modelo.registrarEmpleado(func);

        LocalDateTime manana = LocalDate.now().plusDays(1).atTime(8, 0);
        Reservacion reservacion = modelo.reservarPorCategorias(func, List.of(cat.getId()),
                "Reunión", manana, manana.plusHours(2));

        assertNotNull(reservacion);
        assertTrue(reservacion.esActiva());
        assertEquals(func, reservacion.getEmpleado());
    }

    @Test
    void testReservacionSolapada() {
        CategoriaRecurso cat = modelo.listarCategorias().get(0);
        modelo.registrarRecurso("REC-SOL", cat.getId(), "Recurso solapado");

        Funcionario func = new Funcionario("Solapado", "FSOL", "3333-3333");
        modelo.registrarEmpleado(func);

        LocalDateTime manana = LocalDate.now().plusDays(1).atTime(10, 0);

        // Primera reservación exitosa
        modelo.reservarPorCategorias(func, List.of(cat.getId()),
                "Primera", manana, manana.plusHours(2));

        // Segunda reservación en el mismo horario debe fallar (solo hay 1 recurso en esa categoría)
        assertThrows(IllegalArgumentException.class, () -> {
            modelo.reservarPorCategorias(func, List.of(cat.getId()),
                    "Segunda", manana, manana.plusHours(1));
        });
    }

    @Test
    void testCancelarReservacionFutura() {
        CategoriaRecurso cat = modelo.listarCategorias().get(0);
        modelo.registrarRecurso("REC-CAN", cat.getId(), "Recurso cancelar");

        Funcionario func = new Funcionario("Cancelador", "FCAN", "4444-4444");
        modelo.registrarEmpleado(func);

        LocalDateTime manana = LocalDate.now().plusDays(1).atTime(14, 0);
        Reservacion reservacion = modelo.reservarPorCategorias(func, List.of(cat.getId()),
                "Para cancelar", manana, manana.plusHours(1));

        assertDoesNotThrow(() -> modelo.cancelarReservacion(reservacion.getId()));
        assertFalse(reservacion.esActiva());
    }

    @Test
    void testCancelarReservacionPasada() {
        CategoriaRecurso cat = modelo.listarCategorias().get(0);
        modelo.registrarRecurso("REC-PAS", cat.getId(), "Recurso pasado");

        Funcionario func = new Funcionario("Pasado", "FPAS", "5555-5555");
        modelo.registrarEmpleado(func);

        // Crear una reservación en el pasado directamente con el constructor
        LocalDateTime pasado = LocalDate.now().minusDays(1).atTime(10, 0);
        Reservacion reservacion = new Reservacion(9999, func, List.of(modelo.buscarRecurso("REC-PAS")),
                "Actividad pasada", pasado, pasado.plusHours(1));

        assertThrows(IllegalStateException.class, reservacion::cancelar);
    }
}
