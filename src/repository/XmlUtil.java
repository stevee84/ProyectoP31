package repository;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class XmlUtil {

    public static Document crearDocumento() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.newDocument();
        } catch (Exception e) {
            throw new RuntimeException("Error al crear documento XML.", e);
        }
    }

    public static Document cargarDocumento(File archivo) {
        if (!archivo.exists()) {
            return null;
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(archivo);
        } catch (Exception e) {
            throw new RuntimeException("Error al cargar documento XML: " + archivo.getName(), e);
        }
    }

    public static void guardarDocumento(Document documento, File archivo) {
        try {
            archivo.getParentFile().mkdirs();
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(new DOMSource(documento), new StreamResult(archivo));
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar documento XML: " + archivo.getName(), e);
        }
    }
}
