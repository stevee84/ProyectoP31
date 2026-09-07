package repository;

import model.CategoriaRecurso;
import model.ModeloReservaciones;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;

public class CategoriaXmlRepository {

    private final File archivo;

    public CategoriaXmlRepository(String dataDir) {
        this.archivo = new File(dataDir, "categorias.xml");
    }

    public void guardar(ModeloReservaciones modelo) {
        Document doc = XmlUtil.crearDocumento();
        Element root = doc.createElement("categorias");
        root.setAttribute("siguienteId", String.valueOf(modelo.getSiguienteIdCategoria()));
        doc.appendChild(root);

        for (CategoriaRecurso categoria : modelo.getCategorias().values()) {
            Element elem = doc.createElement("categoria");
            elem.setAttribute("id", categoria.getId());
            elem.setAttribute("descripcion", categoria.getDescripcion());
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
                modelo.setSiguienteIdCategoria(Integer.parseInt(siguienteId));
            } catch (NumberFormatException e) {
                System.err.println("Advertencia: siguienteId de categorías malformado: " + siguienteId);
            }
        }

        NodeList nodos = doc.getElementsByTagName("categoria");
        for (int i = 0; i < nodos.getLength(); i++) {
            try {
                Element elem = (Element) nodos.item(i);
                String id = elem.getAttribute("id");
                String descripcion = elem.getAttribute("descripcion");
                modelo.registrarCategoriaCargada(new CategoriaRecurso(id, descripcion));
            } catch (Exception e) {
                System.err.println("Advertencia: se omitió categoría malformada (índice " + i + "): " + e.getMessage());
            }
        }
    }
}
