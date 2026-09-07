package repository;

import model.CategoriaRecurso;
import model.ModeloReservaciones;
import model.Recurso;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;

public class RecursoXmlRepository {

    private final File archivo;

    public RecursoXmlRepository(String dataDir) {
        this.archivo = new File(dataDir, "recursos.xml");
    }

    public void guardar(ModeloReservaciones modelo) {
        Document doc = XmlUtil.crearDocumento();
        Element root = doc.createElement("recursos");
        doc.appendChild(root);

        for (Recurso recurso : modelo.getRecursos().values()) {
            Element elem = doc.createElement("recurso");
            elem.setAttribute("codigo", recurso.getCodigo());
            elem.setAttribute("categoriaId", recurso.getCategoria().getId());
            elem.setAttribute("descripcion", recurso.getDescripcion());
            root.appendChild(elem);
        }

        XmlUtil.guardarDocumento(doc, archivo);
    }

    public void cargar(ModeloReservaciones modelo) {
        Document doc = XmlUtil.cargarDocumento(archivo);
        if (doc == null) {
            return;
        }

        NodeList nodos = doc.getElementsByTagName("recurso");
        for (int i = 0; i < nodos.getLength(); i++) {
            try {
                Element elem = (Element) nodos.item(i);
                String codigo = elem.getAttribute("codigo");
                String categoriaId = elem.getAttribute("categoriaId");
                String descripcion = elem.getAttribute("descripcion");

                CategoriaRecurso categoria = modelo.buscarCategoria(categoriaId);
                if (categoria != null) {
                    modelo.registrarRecursoCargado(new Recurso(codigo, categoria, descripcion));
                }
            } catch (Exception e) {
                System.err.println("Advertencia: se omitió recurso malformado (índice " + i + "): " + e.getMessage());
            }
        }
    }
}
