package com.svalero.mijuego.entities;

import com.badlogic.gdx.math.Rectangle;

public class EnemyCheese {

    // Posición del queso en el mundo
    private float x, y;

    // Radio de detección: si el jugador entra en esta distancia, se activa
    private final float triggerRange;

    // Indica si el queso solo se activa una vez
    private final boolean oneTime;

    // Estado: true si ya se ha activado
    private boolean triggered = false;

    // Tamaño del queso en unidades del mundo
    private final float w = 0.8f, h = 0.8f;

    // Rectángulo de colisión (puede usarse para dibujar o detectar choques)
    private final Rectangle bounds = new Rectangle();

    /**
     * Constructor del enemigo queso explosivo
     * @param x posición inicial en X
     * @param y posición inicial en Y
     * @param triggerRange radio de detección para activarse
     * @param oneTime true si solo explota una vez, false si puede explotar varias veces
     */
    public EnemyCheese(float x, float y, float triggerRange, boolean oneTime) {
        this.x = x;
        this.y = y;
        this.triggerRange = triggerRange;
        this.oneTime = oneTime;
        updateBounds(); // Inicializa el rectángulo de colisión
    }

    /**
     * Comprueba si el jugador está lo suficientemente cerca para activar el queso
     * @param playerX posición X del jugador
     * @param playerY posición Y del jugador
     * @return true si explota ahora (activa el efecto), false si no
     */
    public boolean checkTrigger(float playerX, float playerY) {
        // Si ya explotó y es de un solo uso, no vuelve a activarse
        if (triggered && oneTime) return false;

        // Calcula la distancia al jugador (distancia² para evitar raíz cuadrada)
        float dx = playerX - x;
        float dy = playerY - y;

        // Si la distancia² es menor o igual al rango², se activa
        if (dx * dx + dy * dy <= triggerRange * triggerRange) {
            triggered = true;
            return true;
        }
        return false;
    }

    /**
     * Actualiza la posición y tamaño del rectángulo de colisión
     */
    private void updateBounds() {
        bounds.set(x, y, w, h);
    }

    /**
     * Devuelve el rectángulo de colisión del queso
     * @return Rectangle con posición y tamaño
     */
    public Rectangle getBounds() {
        return bounds;
    }
}
