package repository;

import model.Administrador;
import model.Empleado;
import model.Funcionario;
import model.ModeloReservaciones;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;

public class UsuarioXmlRepository {

    private final File archivo;

    public UsuarioXmlRepository(String dataDir) {
        this.archivo = new File(dataDir, "usuarios.xml");
    }

    public void guardar(ModeloReservaciones modelo) {
        Document doc = XmlUtil.crearDocumento();
        Element root = doc.createElement("usuarios");
        doc.appendChild(root);

        for (Empleado empleado : modelo.getEmpleados().values()) {
            Element elem = doc.createElement("empleado");
            elem.setAttribute("id", empleado.getId());
            elem.setAttribute("nombre", empleado.getName());
            elem.setAttribute("clave", empleado.getPass());
            elem.setAttribute("firstLog", String.valueOf(empleado.isFirstLog()));

            if (empleado instanceof Administrador) {
                elem.setAttribute("rol", "admin");
            } else if (empleado instanceof Funcionario f) {
                elem.setAttribute("rol", "funcionario");
                elem.setAttribute("telefono", f.getTelefono());
            }

            root.appendChild(elem);
        }

        XmlUtil.guardarDocumento(doc, archivo);
    }

    public void cargar(ModeloReservaciones modelo) {
        Document doc = XmlUtil.cargarDocumento(archivo);
        if (doc == null) {
            return;
        }

        NodeList nodos = doc.getElementsByTagName("empleado");
        for (int i = 0; i < nodos.getLength(); i++) {
            Element elem = (Element) nodos.item(i);
            String id = elem.getAttribute("id");
            String nombre = elem.getAttribute("nombre");
            String clave = elem.getAttribute("clave");
            boolean firstLog = Boolean.parseBoolean(elem.getAttribute("firstLog"));
            String rol = elem.getAttribute("rol");

            Empleado empleado;
            if ("admin".equals(rol)) {
                empleado = new Administrador(nombre, id);
            } else {
                String telefono = elem.getAttribute("telefono");
                empleado = new Funcionario(nombre, id, telefono);
            }

            empleado.setPass(clave);
            empleado.setFirstLog(firstLog);
            modelo.registrarEmpleadoCargado(empleado);
        }
    }
}
