package com.svalero.mijuego.entities;

import com.badlogic.gdx.math.Rectangle;

public class EnemyCheese {


    private float x, y;// Posición del queso en el mundo
    private final float triggerRange;// Radio de detección: si el jugador entra en esta distancia, se activa
    private final boolean oneTime; // Indica si el queso solo se activa una vez
    private boolean triggered = false; // Estado: true si ya se ha activado
    private final float w = 0.8f, h = 0.8f; // Tamaño del queso en unidades del mundo
    private final Rectangle bounds = new Rectangle(); // Rectángulo de colisión (puede usarse para dibujar o detectar choques)

    public EnemyCheese(float x, float y, float triggerRange, boolean oneTime) {
        this.x = x; //posición inicial en X
        this.y = y; //posición inicial en Y
        this.triggerRange = triggerRange; //radio de detección para activarse
        this.oneTime = oneTime; //true si solo explota una vez, false si puede explotar varias veces
        updateBounds(); // Inicializa el rectángulo de colisión
    }

    //Comprueba si el jugador está lo suficientemente cerca para activar el queso

    public boolean checkTrigger(float playerX, float playerY) {
        if (triggered && oneTime) return false; // Si ya explotó y es de un solo uso, no vuelve a activarse
        // Calcula la distancia al jugador (distancia² para evitar raíz cuadrada)
        float dx = playerX - x; //posición X del jugador
        float dy = playerY - y; //posición Y del jugador

        // Si la distancia² es menor o igual al rango², se activa
        if (dx * dx + dy * dy <= triggerRange * triggerRange) {
            triggered = true;
            return true; //true si explota ahora (activa el efecto), false si no
        }
        return false;
    }

    //Actualiza la posición y tamaño del rectángulo de colisión

    private void updateBounds() {
        bounds.set(x, y, w, h);
    }

    //Devuelve el rectángulo de colisión del queso

    public Rectangle getBounds() {
        return bounds;
    }
}
