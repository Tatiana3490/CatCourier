package com.svalero.mijuego.entities;

import com.badlogic.gdx.math.Rectangle;
import lombok.Getter;

public class EnemyDog {

    private float x, y;                     // Posición actual del perro en el mundo
    private final float speed;              // Velocidad a la que se mueve (unidades por segundo)
    private final float detectionRange;     // Rango máximo a partir del cual el perro detecta al jugador
    private final float w = 1.0f, h = 0.8f; // Tamaño del perro (ancho y alto en unidades del mundo)

    //Devuelve el rectángulo de colisión del perro
    // Rectángulo de colisión del perro (sirve para detectar choques con el jugador)
    @Getter
    private final Rectangle bounds = new Rectangle();

    //Constructor del enemigo perro
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
        float dist = playerX - x; // Calculamos la distancia horizontal hasta el jugador
        // Si el jugador está dentro del rango de detección, me muevo hacia él
        if (Math.abs(dist) <= detectionRange) {
            // Math.signum(dist) devuelve -1 si está a la izquierda, +1 si está a la derecha
            x += Math.signum(dist) * speed * delta;
        }

        updateBounds(); // Actualizamos el rectángulo de colisión tras movernos
    }

    //Actualiza la posición y tamaño del rectángulo de colisión
    private void updateBounds() {
        bounds.set(x, y, w, h);
    }

}
