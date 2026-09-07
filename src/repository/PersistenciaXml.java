package repository;

import model.ModeloReservaciones;

import java.io.File;

public class PersistenciaXml {

    private final ModeloReservaciones modelo;
    private final UsuarioXmlRepository usuarioRepo;
    private final CategoriaXmlRepository categoriaRepo;
    private final RecursoXmlRepository recursoRepo;
    private final ReservaXmlRepository reservaRepo;

    public PersistenciaXml(ModeloReservaciones modelo, String dataDir) {
        this.modelo = modelo;
        new File(dataDir).mkdirs();
        this.usuarioRepo = new UsuarioXmlRepository(dataDir);
        this.categoriaRepo = new CategoriaXmlRepository(dataDir);
        this.recursoRepo = new RecursoXmlRepository(dataDir);
        this.reservaRepo = new ReservaXmlRepository(dataDir);
    }

    public PersistenciaXml(ModeloReservaciones modelo) {
        this(modelo, "data");
    }

    public synchronized void guardarTodo() {
        usuarioRepo.guardar(modelo);
        categoriaRepo.guardar(modelo);
        recursoRepo.guardar(modelo);
        reservaRepo.guardar(modelo);
    }

    public synchronized void cargarTodo() {
        usuarioRepo.cargar(modelo);
        categoriaRepo.cargar(modelo);
        recursoRepo.cargar(modelo);
        reservaRepo.cargar(modelo);
    }
}
