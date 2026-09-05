package view;

import javax.swing.*;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class GraficaRecursosPanel extends JPanel {

    private List<String> categorias;
    private List<Integer> cantidades;

    public GraficaRecursosPanel() {
        categorias = new ArrayList<>();
        cantidades = new ArrayList<>();

        setBackground(Color.WHITE);

        setBorder(
                BorderFactory.createTitledBorder(
                        "Gráfica de barras"
                )
        );
    }

    public void actualizarDatos(
            List<String> nuevasCategorias,
            List<Integer> nuevasCantidades
    ) {
        categorias =
                new ArrayList<>(nuevasCategorias);

        cantidades =
                new ArrayList<>(nuevasCantidades);

        repaint();
    }

    @Override
    protected void paintComponent(Graphics grafico) {
        super.paintComponent(grafico);

        if (categorias.isEmpty()) {
            grafico.setColor(Color.DARK_GRAY);

            grafico.drawString(
                    "No hay datos para mostrar.",
                    20,
                    40
            );

            return;
        }

        int margenIzquierdo = 50;
        int margenDerecho = 20;
        int margenSuperior = 50;
        int margenInferior = 60;

        int anchoDisponible =
                getWidth()
                        - margenIzquierdo
                        - margenDerecho;

        int altoDisponible =
                getHeight()
                        - margenSuperior
                        - margenInferior;

        int cantidadMayor = 1;

        for (int cantidad : cantidades) {
            if (cantidad > cantidadMayor) {
                cantidadMayor = cantidad;
            }
        }

        grafico.setColor(Color.BLACK);

        grafico.drawLine(
                margenIzquierdo,
                margenSuperior,
                margenIzquierdo,
                margenSuperior + altoDisponible
        );

        grafico.drawLine(
                margenIzquierdo,
                margenSuperior + altoDisponible,
                margenIzquierdo + anchoDisponible,
                margenSuperior + altoDisponible
        );

        int espacio =
                anchoDisponible / categorias.size();

        int anchoBarra =
                Math.min(80, espacio / 2);

        for (int i = 0;
             i < categorias.size();
             i++) {

            int cantidad = cantidades.get(i);

            int altoBarra =
                    cantidad
                            * altoDisponible
                            / cantidadMayor;

            int posicionX =
                    margenIzquierdo
                            + (i * espacio)
                            + ((espacio - anchoBarra) / 2);

            int posicionY =
                    margenSuperior
                            + altoDisponible
                            - altoBarra;

            grafico.setColor(
                    new Color(70, 130, 180)
            );

            grafico.fillRect(
                    posicionX,
                    posicionY,
                    anchoBarra,
                    altoBarra
            );

            grafico.setColor(Color.BLACK);

            grafico.drawRect(
                    posicionX,
                    posicionY,
                    anchoBarra,
                    altoBarra
            );

            grafico.drawString(
                    String.valueOf(cantidad),
                    posicionX + (anchoBarra / 2),
                    posicionY - 5
            );

            String nombre = categorias.get(i);

            if (nombre.length() > 15) {
                nombre =
                        nombre.substring(0, 12)
                                + "...";
            }

            grafico.drawString(
                    nombre,
                    posicionX,
                    margenSuperior
                            + altoDisponible
                            + 20
            );
        }
    }
}
