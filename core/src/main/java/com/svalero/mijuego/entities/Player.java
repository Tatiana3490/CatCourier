package com.svalero.mijuego.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.svalero.mijuego.util.Constants;

import java.util.List;

public class Player {
    // Posición y velocidad en unidades de mundo
    public final Vector2 pos = new Vector2();
    public final Vector2 vel = new Vector2();

    // Estado del jugador
    public boolean onGround = false;
    public int energy = 100;   // para HUD
    public int score  = 0;     // para HUD

    // AABB para colisiones
    private final Rectangle bounds = new Rectangle(0, 0, Constants.PLAYER_W, Constants.PLAYER_H);

    // ---- ESCUDO ----
    // Duración restante del escudo en segundos. Si > 0, el escudo está activo.
    private float shieldTime = 0f;

    public Player(float x, float y){
        pos.set(x, y);
    }

    /** Rectángulo actual del jugador (para colisiones) */
    public Rectangle getBounds(){
        bounds.setPosition(pos.x, pos.y);
        return bounds;
    }

    /**
     * Update principal del jugador:
     * - Consume el escudo si está activo
     * - Lee input (izq/der/salto)
     * - Aplica gravedad
     * - Integra y resuelve colisiones por ejes
     */
    public void update(float dt, List<Rectangle> solids){
        // 1) Consumir tiempo de escudo (si está activo)
        if (shieldTime > 0f) {
            shieldTime -= dt;
            if (shieldTime < 0f) shieldTime = 0f;
        }

        // 2) Entrada horizontal
        float dir = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)  || Gdx.input.isKeyPressed(Input.Keys.A)) dir -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) dir += 1f;
        vel.x = dir * Constants.MOVE_SPEED;

        // 3) Salto (solo si está en el suelo)
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && onGround){
            vel.y = Constants.JUMP_SPEED;
            onGround = false;
        }

        // 4) Gravedad
        vel.y += Constants.GRAVITY * dt;

        // 5) Integración + colisiones por ejes (AABB simple)
        // Eje X
        pos.x += vel.x * dt;
        collideX(solids);

        // Eje Y
        pos.y += vel.y * dt;
        onGround = false; // se recalcula en collideY si pisa algo
        collideY(solids);
    }

    /** Colisiones en X (empuja al borde del bloque y anula vel.x) */
    private void collideX(List<Rectangle> solids){
        Rectangle b = getBounds();
        for (Rectangle s : solids){
            if (b.overlaps(s)){
                if (vel.x > 0) {
                    pos.x = s.x - b.width;   // venía desde la izquierda
                } else if (vel.x < 0) {
                    pos.x = s.x + s.width;   // venía desde la derecha
                }
                vel.x = 0;
                b.setPosition(pos.x, pos.y);
            }
        }
    }

    /** Colisiones en Y (detecta suelo y techo, y marca onGround si corresponde) */
    private void collideY(List<Rectangle> solids){
        Rectangle b = getBounds();
        for (Rectangle s : solids){
            if (b.overlaps(s)){
                if (vel.y > 0) {
                    // chocó con el “techo”
                    pos.y = s.y - b.height;
                } else if (vel.y < 0){
                    // chocó con el “suelo”
                    pos.y = s.y + s.height;
                    onGround = true;
                }
                vel.y = 0;
                b.setPosition(pos.x, pos.y);
            }
        }
    }

    // ==== API del escudo ====

    /** Activa el escudo durante 'seconds' segundos. Si ya hay, lo extiende. */
    public void activateShield(float seconds) {
        shieldTime = Math.max(shieldTime, 0f) + Math.max(0f, seconds);
    }

    /** ¿Está activo el escudo ahora mismo? */
    public boolean isShieldActive() {
        return shieldTime > 0f;
    }

    /** Tiempo restante de escudo (>= 0). Útil para HUD. */
    public float getShieldTime() {
        return Math.max(0f, shieldTime);
    }

    // (Opcional) Centralizar daño aquí para respetar el escudo:
    // public void applyDamage(int dmg) {
    //     if (isShieldActive()) return; // ignora daño con escudo
    //     energy = Math.max(0, energy - dmg);
    // }
}
