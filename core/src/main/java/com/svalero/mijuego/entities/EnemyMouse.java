package com.svalero.mijuego.entities;

import com.badlogic.gdx.math.Rectangle;

public class EnemyMouse {

    // Posición actual del ratón en el mundo
    private float x, y;

    // Amplitud del movimiento en zigzag (cuánto se desplaza a los lados)
    private final float amplitude;

    // Frecuencia del zigzag (qué tan rápido se mueve de lado a lado)
    private final float frequency;

    // Tiempo acumulado (sirve para calcular la posición en la onda seno)
    private float time = 0f;

    // Tamaño del ratón en unidades del mundo
    private final float w = 0.5f, h = 0.5f;

    // Rectángulo de colisión para detectar choques con el jugador
    private final Rectangle bounds = new Rectangle();

    /**
     * Constructor del enemigo ratón
     * @param x posición inicial en el eje X
     * @param y posición inicial en el eje Y
     * @param amplitude amplitud del zigzag (desplazamiento lateral)
     * @param frequency frecuencia del zigzag (velocidad lateral)
     */
    public EnemyMouse(float x, float y, float amplitude, float frequency) {
        this.x = x;
        this.y = y;
        this.amplitude = amplitude;
        this.frequency = frequency;
        updateBounds(); // Inicializamos el rectángulo de colisión
    }

    /**
     * Actualiza el movimiento del ratón en zigzag.
     * Se basa en una función seno para crear el patrón ondulado.
     * @param delta tiempo transcurrido desde el último frame
     */
    public void update(float delta) {
        // Avanzamos el tiempo en función de la frecuencia
        time += delta * frequency;

        // Movemos en X siguiendo una onda seno para lograr el zigzag
        x += (float) Math.sin(time) * amplitude * delta;

        // Actualizamos el rectángulo de colisión tras el movimiento
        updateBounds();
    }

    /**
     * Actualiza la posición y tamaño del rectángulo de colisión
     */
    private void updateBounds() {
        bounds.set(x, y, w, h);
    }

    /**
     * Devuelve el rectángulo de colisión del ratón
     * @return Rectangle con posición y tamaño actuales
     */
    public Rectangle getBounds() {
        return bounds;
    }
}
