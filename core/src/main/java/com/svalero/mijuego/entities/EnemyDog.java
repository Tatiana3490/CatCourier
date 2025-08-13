package com.svalero.mijuego.entities;

import com.badlogic.gdx.math.Rectangle;

public class EnemyDog {

    // Posición actual del perro en el mundo
    private float x, y;

    // Velocidad a la que se mueve (unidades por segundo)
    private final float speed;

    // Rango máximo a partir del cual el perro detecta al jugador
    private final float detectionRange;

    // Tamaño del perro (ancho y alto en unidades del mundo)
    private final float w = 1.0f, h = 0.8f;

    // Rectángulo de colisión del perro (sirve para detectar choques con el jugador)
    private final Rectangle bounds = new Rectangle();

    /**
     * Constructor del enemigo perro
     * @param x posición inicial en el eje X
     * @param y posición inicial en el eje Y
     * @param speed velocidad de movimiento
     * @param detectionRange rango de detección del jugador
     */
    public EnemyDog(float x, float y, float speed, float detectionRange) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.detectionRange = detectionRange;
        updateBounds(); // Actualizamos el rectángulo de colisión
    }

    /**
     * Lógica de movimiento del perro.
     * Persigue al jugador solo si está dentro del rango de detección.
     * @param delta tiempo transcurrido desde el último frame (para movimiento fluido)
     * @param playerX posición actual del jugador en el eje X
     */
    public void update(float delta, float playerX) {
        // Calculamos la distancia horizontal hasta el jugador
        float dist = playerX - x;

        // Si el jugador está dentro del rango de detección, me muevo hacia él
        if (Math.abs(dist) <= detectionRange) {
            // Math.signum(dist) devuelve -1 si está a la izquierda, +1 si está a la derecha
            x += Math.signum(dist) * speed * delta;
        }

        // Actualizamos el rectángulo de colisión tras movernos
        updateBounds();
    }

    /**
     * Actualiza la posición y tamaño del rectángulo de colisión
     */
    private void updateBounds() {
        bounds.set(x, y, w, h);
    }

    /**
     * Devuelve el rectángulo de colisión del perro
     * @return Rectangle con la posición y tamaño actuales del enemigo
     */
    public Rectangle getBounds() {
        return bounds;
    }
}
