package red;

import contrato.IServicioAplicacion;
import dto.CategoriaDTO;
import dto.FuncionarioDTO;
import dto.ResultadoReservaDTO;
import dto.ResultadoSesionDTO;
import protocolo.CodigoError;
import protocolo.EventoReservaCreada;
import protocolo.Operacion;
import protocolo.Peticion;
import protocolo.Respuesta;
import protocolo.ServicioException;
import servicio.ContextoSesion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Traduce cada petición a una llamada del servicio de su conexión y el resultado
 * a una respuesta. No resuelve reglas de negocio: solo exige sesión iniciada,
 * mantiene la identidad de la conexión y publica las reservas confirmadas.
 */
public class Despachador {

    private final IServicioAplicacion servicio;
    private final ContextoSesion contexto;
    private final PublicadorEventos publicador;

    public Despachador(IServicioAplicacion servicio, ContextoSesion contexto, PublicadorEventos publicador) {
        this.servicio = servicio;
        this.contexto = contexto;
        this.publicador = publicador;
    }

    public Respuesta atender(Peticion peticion) {
        long id = peticion.idPeticion();
        if (peticion.operacion() == null) {
            return Respuesta.error(id, CodigoError.VALIDACION, "Petición mal formada.");
        }
        Object[] datos = peticion.datos() == null ? new Object[0] : peticion.datos();
        try {
            if (peticion.operacion() != Operacion.INICIAR_SESION && !contexto.estaAutenticado()) {
                return Respuesta.error(id, CodigoError.SESION, "Debe iniciar sesión.");
            }
            return Respuesta.exito(id, ejecutar(peticion.operacion(), datos));
        } catch (ServicioException e) {
            return Respuesta.error(id, e.getCodigo(), e.getMessage());
        } catch (ClassCastException | IndexOutOfBoundsException e) {
            return Respuesta.error(id, CodigoError.VALIDACION, "Petición mal formada.");
        } catch (RuntimeException e) {
            // El detalle técnico queda en el servidor; al usuario solo un mensaje claro.
            e.printStackTrace();
            return Respuesta.error(id, CodigoError.INTERNO, "Ocurrió un error interno en el servidor.");
        }
    }

    @SuppressWarnings("unchecked")
    private Object ejecutar(Operacion operacion, Object[] d) {
        return switch (operacion) {
            case INICIAR_SESION -> iniciarSesion((String) d[0], (String) d[1]);
            case CERRAR_SESION -> {
                servicio.cerrarSesion();
                contexto.cerrar();
                yield null;
            }
            case CAMBIAR_CLAVE -> {
                servicio.cambiarClave((String) d[0]);
                yield null;
            }

            case REGISTRAR_FUNCIONARIO -> {
                servicio.registrarFuncionario((FuncionarioDTO) d[0]);
                yield null;
            }
            case ACTUALIZAR_FUNCIONARIO -> {
                servicio.actualizarFuncionario((FuncionarioDTO) d[0]);
                yield null;
            }
            case ELIMINAR_FUNCIONARIO -> {
                servicio.eliminarFuncionario((String) d[0]);
                yield null;
            }
            case BUSCAR_FUNCIONARIO -> servicio.buscarFuncionario((String) d[0]);
            case LISTAR_FUNCIONARIOS -> servicio.listarFuncionarios();

            case REGISTRAR_CATEGORIA -> servicio.registrarCategoria((String) d[0]);
            case ACTUALIZAR_CATEGORIA -> {
                servicio.actualizarCategoria((CategoriaDTO) d[0]);
                yield null;
            }
            case ELIMINAR_CATEGORIA -> {
                servicio.eliminarCategoria((String) d[0]);
                yield null;
            }
            case BUSCAR_CATEGORIA -> servicio.buscarCategoria((String) d[0]);
            case LISTAR_CATEGORIAS -> servicio.listarCategorias();

            case REGISTRAR_RECURSO -> {
                servicio.registrarRecurso((String) d[0], (String) d[1], (String) d[2]);
                yield null;
            }
            case ACTUALIZAR_RECURSO -> {
                servicio.actualizarRecurso((String) d[0], (String) d[1], (String) d[2]);
                yield null;
            }
            case ELIMINAR_RECURSO -> {
                servicio.eliminarRecurso((String) d[0]);
                yield null;
            }
            case BUSCAR_RECURSO -> servicio.buscarRecurso((String) d[0]);
            case LISTAR_RECURSOS -> servicio.listarRecursos();
            case LISTAR_RECURSOS_POR_CATEGORIA -> servicio.listarRecursosPorCategoria((String) d[0]);

            case CREAR_RESERVACION -> crearReservacion((List<String>) d[0], (String) d[1],
                    (LocalDateTime) d[2], (LocalDateTime) d[3]);
            case CANCELAR_RESERVACION -> {
                servicio.cancelarReservacion((Integer) d[0]);
                yield null;
            }
            case LISTAR_RESERVACIONES_SESION -> servicio.listarReservacionesSesion();
            case EXTRAER_DATOS_IA -> servicio.extraerDatosDesdeFrase((String) d[0]);

            case OBTENER_CALENDARIZACION -> servicio.obtenerCalendarizacion((LocalDate) d[0], (String) d[1]);
            case OBTENER_AGENDA_SEMANA -> servicio.obtenerAgendaSemana((LocalDate) d[0]);
            case CONTAR_POR_SEMANA -> servicio.contarPorSemana((LocalDate) d[0], (LocalDate) d[1]);
            case CONTAR_RESERVAS_CATEGORIA -> servicio.contarReservasCategoria((String) d[0],
                    (LocalDate) d[1], (LocalDate) d[2]);
        };
    }

    private ResultadoSesionDTO iniciarSesion(String id, String clave) {
        ResultadoSesionDTO resultado = servicio.iniciarSesion(id, clave);
        contexto.iniciar(resultado.usuario());
        return resultado;
    }

    private ResultadoReservaDTO crearReservacion(List<String> idsCategorias, String descripcion,
                                                 LocalDateTime inicio, LocalDateTime fin) {
        ResultadoReservaDTO resultado = servicio.crearReservacion(idsCategorias, descripcion, inicio, fin);
        if (resultado.exito()) {
            // La reserva ya quedó confirmada: un fallo al avisar no puede convertirse en error,
            // porque el cliente reintentaría y la duplicaría.
            try {
                publicador.notificar(new EventoReservaCreada(resultado.reserva(), LocalDateTime.now()),
                        resultado.reserva().autor().id());
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return resultado;
    }
}
