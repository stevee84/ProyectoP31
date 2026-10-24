package falso;

import dto.CategoriaDTO;
import dto.FuncionarioDTO;
import dto.RecursoDTO;
import dto.ReservaDTO;
import dto.Rol;
import dto.UsuarioDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Datos en memoria compartidos por todas las conexiones del servidor de prueba.
 * Quien los use debe sincronizar sobre esta instancia.
 */
class DatosFalsos {

    final Map<String, UsuarioDTO> usuarios = new LinkedHashMap<>();
    final Map<String, String> claves = new LinkedHashMap<>();
    /** Usuarios que todavía deben cambiar la clave inicial. */
    final Set<String> primerIngreso = new HashSet<>();
    final Map<String, FuncionarioDTO> funcionarios = new LinkedHashMap<>();
    final Map<String, CategoriaDTO> categorias = new LinkedHashMap<>();
    final Map<String, RecursoDTO> recursos = new LinkedHashMap<>();
    final List<ReservaDTO> reservas = new ArrayList<>();
    int siguienteReserva = 1;
    int siguienteCategoria = 1;

    DatosFalsos() {
        agregarUsuario(new UsuarioDTO("ADMIN", "Administrador", Rol.ADMINISTRADOR), "ADMIN", null);
        agregarUsuario(new UsuarioDTO("F001", "Ana Mora", Rol.FUNCIONARIO), "F001", "8888-0001");
        agregarUsuario(new UsuarioDTO("F002", "Luis Rojas", Rol.FUNCIONARIO), "F002", "8888-0002");
        agregarUsuario(new UsuarioDTO("F003", "Carla Vargas", Rol.FUNCIONARIO), "F003", "8888-0003");

        CategoriaDTO sala = nuevaCategoria("Sala de reuniones");
        CategoriaDTO laptop = nuevaCategoria("Laptop Windows");
        recursos.put("SALA-01", new RecursoDTO("SALA-01", sala, "Sala A, segundo piso"));
        recursos.put("LAP-01", new RecursoDTO("LAP-01", laptop, "Laptop Dell 14\""));
    }

    private void agregarUsuario(UsuarioDTO usuario, String clave, String telefono) {
        usuarios.put(usuario.id(), usuario);
        claves.put(usuario.id(), clave);
        if (telefono != null) {
            funcionarios.put(usuario.id(), new FuncionarioDTO(usuario.id(), usuario.nombre(), telefono));
        }
    }

    CategoriaDTO nuevaCategoria(String descripcion) {
        CategoriaDTO categoria = new CategoriaDTO(String.format("CAT-%06d", siguienteCategoria++), descripcion);
        categorias.put(categoria.id(), categoria);
        return categoria;
    }
}
