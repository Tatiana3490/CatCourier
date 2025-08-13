package com.svalero.mijuego.entities;

import com.badlogic.gdx.math.Rectangle;

/** Enemigo que “salta” en el sitio con un movimiento vertical periódico. */
public class EnemyJumper {
    public float x, y;           // posición actual
    private final float baseY;   // altura base (centro del salto)
    private float t = 0f;        // tiempo acumulado
    private final float amp;     // amplitud del salto (unidades de mundo)
    private final float speed;   // velocidad angular del “salto”
    private final float w = 0.9f, h = 1.2f; // tamaño del enemigo
    private final Rectangle bounds = new Rectangle();

    public EnemyJumper(float x, float baseY){
        this(x, baseY, 0.9f, 2.4f);
    }

    public EnemyJumper(float x, float baseY, float amplitude, float angularSpeed){
        this.x = x;
        this.baseY = baseY;
        this.amp = amplitude;
        this.speed = angularSpeed;
        this.y = baseY;
        updateBounds();
    }

    public void update(float delta){
        t += delta * speed;
        // Movimiento vertical senoidal (0..1..0) usando |sin|
        float dy = (float)Math.abs(Math.sin(t)) * amp;
        y = baseY + dy;
        updateBounds();
    }

    private void updateBounds(){
        bounds.set(x, y, w, h);
    }

    public Rectangle getBounds(){
        return bounds;
    }
}
