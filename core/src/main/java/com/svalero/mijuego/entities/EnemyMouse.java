package com.svalero.mijuego.entities;

import com.badlogic.gdx.math.Rectangle;
import lombok.Getter;

public class EnemyMouse {

    private float x, y; // Posición actual del ratón en el mundo
    private final float amplitude; // Amplitud del movimiento en zigzag (cuánto se desplaza a los lados)
    private final float frequency;// Frecuencia del zigzag (qué tan rápido se mueve de lado a lado)
    private float time = 0f;// Tiempo acumulado (sirve para calcular la posición en la onda seno)
    private final float w = 0.5f, h = 0.5f; // Tamaño del ratón en unidades del mundo

    //Devuelve el rectángulo de colisión del ratón
    @Getter
    private final Rectangle bounds = new Rectangle(); // Rectángulo de colisión para detectar choques con el jugador

    /**
     * Constructor del enemigo ratón
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
        time += delta * frequency;  // Avanzamos el tiempo en función de la frecuencia
        x += (float) Math.sin(time) * amplitude * delta;  // Movemos en X siguiendo una onda seno para lograr el zigzag
        updateBounds();// Actualizamos el rectángulo de colisión tras el movimiento
    }

    //Actualiza la posición y tamaño del rectángulo de colisión

    private void updateBounds() {
        bounds.set(x, y, w, h);
    }

}
