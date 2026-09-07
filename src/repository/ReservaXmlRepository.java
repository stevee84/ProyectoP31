package repository;

import model.Empleado;
import model.EstadoReservacion;
import model.ModeloReservaciones;
import model.Recurso;
import model.Reservacion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservaXmlRepository {

    private final File archivo;

    public ReservaXmlRepository(String dataDir) {
        this.archivo = new File(dataDir, "reservas.xml");
    }

    public void guardar(ModeloReservaciones modelo) {
        Document doc = XmlUtil.crearDocumento();
        Element root = doc.createElement("reservas");
        root.setAttribute("siguienteId", String.valueOf(modelo.getSiguienteIdReservacion()));
        doc.appendChild(root);

        for (Reservacion reservacion : modelo.getReservaciones()) {
            Element elem = doc.createElement("reservacion");
            elem.setAttribute("id", String.valueOf(reservacion.getId()));
            elem.setAttribute("empleadoId", reservacion.getEmpleado().getId());
            elem.setAttribute("descripcionActividad", reservacion.getDescripcionActividad());
            elem.setAttribute("inicio", reservacion.getInicio().toString());
            elem.setAttribute("fin", reservacion.getFin().toString());
            elem.setAttribute("estado", reservacion.getEstado().name());

            StringBuilder codigos = new StringBuilder();
            for (Recurso recurso : reservacion.getRecursos()) {
                if (codigos.length() > 0) {
                    codigos.append(",");
                }
                codigos.append(recurso.getCodigo());
            }
            elem.setAttribute("recursoCodigos", codigos.toString());

            root.appendChild(elem);
        }

        XmlUtil.guardarDocumento(doc, archivo);
    }

    public void cargar(ModeloReservaciones modelo) {
        Document doc = XmlUtil.cargarDocumento(archivo);
        if (doc == null) {
            return;
        }

        Element root = doc.getDocumentElement();
        String siguienteId = root.getAttribute("siguienteId");
        if (!siguienteId.isEmpty()) {
            try {
                modelo.setSiguienteIdReservacion(Integer.parseInt(siguienteId));
            } catch (NumberFormatException e) {
                System.err.println("Advertencia: siguienteId de reservaciones malformado: " + siguienteId);
            }
        }

        NodeList nodos = doc.getElementsByTagName("reservacion");
        for (int i = 0; i < nodos.getLength(); i++) {
            try {
                Element elem = (Element) nodos.item(i);

                int id = Integer.parseInt(elem.getAttribute("id"));
                String empleadoId = elem.getAttribute("empleadoId");
                String descripcion = elem.getAttribute("descripcionActividad");
                LocalDateTime inicio = LocalDateTime.parse(elem.getAttribute("inicio"));
                LocalDateTime fin = LocalDateTime.parse(elem.getAttribute("fin"));
                String estadoStr = elem.getAttribute("estado");
                String codigosStr = elem.getAttribute("recursoCodigos");

                Empleado empleado = modelo.buscarEmpleado(empleadoId);
                if (empleado == null) {
                    continue;
                }

                List<Recurso> recursos = new ArrayList<>();
                for (String codigo : codigosStr.split(",")) {
                    Recurso recurso = modelo.buscarRecurso(codigo.trim());
                    if (recurso != null) {
                        recursos.add(recurso);
                    }
                }

                if (recursos.isEmpty()) {
                    continue;
                }

                Reservacion reservacion = new Reservacion(id, empleado, recursos, descripcion, inicio, fin);
                reservacion.setEstado(EstadoReservacion.valueOf(estadoStr));
                modelo.registrarReservacionCargada(reservacion);
            } catch (Exception e) {
                System.err.println("Advertencia: se omitió reservación malformada (índice " + i + "): " + e.getMessage());
            }
        }
    }
}
