package com.svalero.mijuego.levels;

import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Define la geometría de cada nivel:
 *  - solids: rectángulos sólidos (suelo/plataformas) en unidades de mundo
 *  - exit:   rectángulo de salida (colisión para pasar de nivel)
 *  - widthUnits: ancho total del nivel (limita la cámara)
 *
 * Sistema de coordenadas:
 *  X → derecha, Y → arriba. Suelo base suele estar en y=0 con altura 1.
 */
public class Level {

    /** Sólidos del nivel (suelos + plataformas) */
    public final List<Rectangle> solids = new ArrayList<>();

    /** Zona de salida (colisión para cambiar de nivel) */
    public Rectangle exit;

    /** Ancho del nivel en unidades de mundo (para limitar cámara) */
    public float widthUnits = 28f;

    /**
     * Crea el nivel pedido (1..4). Si llega un número fuera de rango, devuelve el 1.
     */
    public static Level create(int number){
        Level L = new Level();

        switch (number) {
            case 1: {
                // ===== NIVEL 1 =====
                L.widthUnits = 28f;

                // Suelo continuo
                L.solids.add(new Rectangle(0, 0, L.widthUnits, 1f));

                // Plataformas
                L.solids.add(new Rectangle(4f,  2.5f, 4f,   0.6f));
                L.solids.add(new Rectangle(10f, 4.0f, 3f,   0.6f));
                L.solids.add(new Rectangle(15f, 5.5f, 3f,   0.6f));

                // Salida al final (apoyada en el suelo)
                L.exit = new Rectangle(L.widthUnits - 1.5f, 1f, 1.0f, 2f);
                break;
            }

            case 2: {
                // ===== NIVEL 2 =====
                L.widthUnits = 30f;

                // Suelo continuo
                L.solids.add(new Rectangle(0, 0, L.widthUnits, 1f));

                // Plataformas
                L.solids.add(new Rectangle(6f,  2.8f, 3.5f, 0.6f));
                L.solids.add(new Rectangle(11f, 4.2f, 3.0f, 0.6f));
                L.solids.add(new Rectangle(16f, 6.0f, 2.5f, 0.6f));
                L.solids.add(new Rectangle(21f, 3.6f, 4.0f, 0.6f));

                // Salida
                L.exit = new Rectangle(L.widthUnits - 1.5f, 1f, 1.0f, 2f);
                break;
            }

            case 3: {
                // ===== NIVEL 3 =====
                L.widthUnits = 32f;

                // Suelo en tramos (sin huecos reales entre 0..32 para que sea cómodo)
                L.solids.add(new Rectangle(0,   0,  8, 1));
                L.solids.add(new Rectangle(8,   0,  8, 1));
                L.solids.add(new Rectangle(16,  0,  8, 1));
                L.solids.add(new Rectangle(24,  0,  8, 1));

                // Plataformas “en escalera”
                L.solids.add(new Rectangle(6f,   2.0f, 2f, 0.6f));
                L.solids.add(new Rectangle(12f,  3.0f, 2f, 0.6f));
                L.solids.add(new Rectangle(18f,  4.0f, 2f, 0.6f));
                L.solids.add(new Rectangle(22f,  2.5f, 3f, 0.6f));
                L.solids.add(new Rectangle(27f,  3.5f, 2f, 0.6f));

                // Salida (ligeramente antes del final, apoyada en suelo)
                L.exit = new Rectangle(30.5f, 1f, 1.2f, 2.2f);
                break;
            }

            case 4: {
                // ===== NIVEL 4 =====
                L.widthUnits = 40f;

                // Suelo por tramos (con huecos)
                L.solids.add(new Rectangle(0,   0, 10, 1));
                L.solids.add(new Rectangle(12,  0,  6, 1));
                L.solids.add(new Rectangle(20,  0,  8, 1));
                L.solids.add(new Rectangle(30,  0, 10, 1));

                // Plataformas en altura
                L.solids.add(new Rectangle(5f,   2.2f, 3f, 0.6f));
                L.solids.add(new Rectangle(9f,   3.2f, 2f, 0.6f));
                L.solids.add(new Rectangle(15f,  2.8f, 3f, 0.6f));

                // ⚠️ Ajuste importante:
                //   Esta estaba en y=3.8. Si colocas una caja en (24.0, 3.8),
                //   se metía dentro del sólido y no podías cogerla.
                //   La bajamos a y=3.2 (top = 3.8) para que la caja apoye encima.
                L.solids.add(new Rectangle(23f,  3.2f, 3f, 0.6f)); // <— bajada 0.6

                L.solids.add(new Rectangle(34f,  2.8f, 3f, 0.6f));

                // Salida final
                L.exit = new Rectangle(37.5f, 1f, 1.2f, 2.2f);
                break;
            }

            default:
                // Si llega un número raro, volvemos al nivel 1
                return create(1);
        }

        return L;
    }
}
