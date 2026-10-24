package protocolo;

import dto.AgendaSemanaDTO;
import dto.CategoriaDTO;
import dto.EstadoReserva;
import dto.InfoReservaDTO;
import dto.MatrizCalendarioDTO;
import dto.RecursoDTO;
import dto.ReservaDTO;
import dto.ResultadoReservaDTO;
import dto.Rol;
import dto.UsuarioDTO;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProtocoloTest {

    private static final CategoriaDTO SALA = new CategoriaDTO("CAT-000001", "Sala");
    private static final RecursoDTO SALA_01 = new RecursoDTO("SALA-01", SALA, "Sala A");
    private static final UsuarioDTO ANA = new UsuarioDTO("F001", "Ana Mora", Rol.FUNCIONARIO);
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 2, 8, 0);

    @SuppressWarnings("unchecked")
    private static <T> T idaYVuelta(T objeto) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream salida = new ObjectOutputStream(bytes)) {
            salida.writeObject(objeto);
        }
        try (ObjectInputStream entrada = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (T) entrada.readObject();
        }
    }

    private static ReservaDTO reserva() {
        return new ReservaDTO(1, ANA, List.of(SALA_01), "Clase", INICIO, INICIO.plusHours(2), EstadoReserva.ACTIVA);
    }

    @Test
    void peticionConservaOperacionYDatos() throws Exception {
        Peticion original = new Peticion(7, Operacion.CREAR_RESERVACION,
                new Object[]{List.of("CAT-000001"), "Clase", INICIO, INICIO.plusHours(1)});

        Peticion copia = idaYVuelta(original);

        assertEquals(7, copia.idPeticion());
        assertEquals(Operacion.CREAR_RESERVACION, copia.operacion());
        assertEquals(List.of("CAT-000001"), copia.datos()[0]);
        assertEquals(INICIO, copia.datos()[2]);
    }

    @Test
    void respuestaExitosaConResultadoReserva() throws Exception {
        Respuesta copia = idaYVuelta(Respuesta.exito(3, ResultadoReservaDTO.exito(reserva())));

        assertTrue(copia.exito());
        assertEquals(reserva(), ((ResultadoReservaDTO) copia.resultado()).reserva());
    }

    @Test
    void respuestaDeErrorConservaCodigoYMensaje() throws Exception {
        Respuesta copia = idaYVuelta(Respuesta.error(4, CodigoError.PERMISO, "Sin permiso"));

        assertFalse(copia.exito());
        assertEquals(CodigoError.PERMISO, copia.codigoError());
        assertEquals("Sin permiso", copia.mensaje());
    }

    @Test
    void eventoConservaLaReserva() throws Exception {
        EventoReservaCreada evento = new EventoReservaCreada(reserva(), INICIO.minusDays(1));
        assertEquals(evento, idaYVuelta(evento));
    }

    @Test
    void consultasSonSerializables() throws Exception {
        MatrizCalendarioDTO matriz = new MatrizCalendarioDTO(INICIO.toLocalDate(), List.of(SALA_01),
                List.of(new ArrayList<>(List.of("Disponible"))));
        InfoReservaDTO info = new InfoReservaDTO(INICIO, INICIO.plusHours(1), "Clase", "Ana Mora", List.of("Sala A"));
        AgendaSemanaDTO agenda = new AgendaSemanaDTO(LocalDate.of(2026, 11, 2),
                Map.of(DayOfWeek.MONDAY, Map.of(8, new ArrayList<>(List.of(info)))));

        assertEquals(matriz, idaYVuelta(matriz));
        assertEquals(agenda, idaYVuelta(agenda));
    }

    @Test
    void dtosCopianSusListas() {
        List<RecursoDTO> recursos = new ArrayList<>(List.of(SALA_01));
        ReservaDTO reserva = new ReservaDTO(1, ANA, recursos, "Clase", INICIO, INICIO.plusHours(1), EstadoReserva.ACTIVA);

        recursos.clear();

        assertEquals(1, reserva.recursos().size());
    }
}
