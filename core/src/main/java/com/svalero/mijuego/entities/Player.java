package com.svalero.mijuego.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.svalero.mijuego.util.Constants;

import java.util.List;

/**
 * Jugador muy sencillo con:
 * - movimiento y salto
 * - colisiones AABB contra rectángulos del nivel
 * - energía/score para el HUD
 * - temporizador de escudo (power-up)
 */
public class Player {

    // Posición y velocidad en "unidades de mundo"
    public final Vector2 pos = new Vector2();
    public final Vector2 vel = new Vector2();

    // Estado básico
    public boolean onGround = false;
    public int energy = 100;
    public int score  = 0;

    // AABB del jugador (mismo tamaño que en Constants)
    private final Rectangle bounds =
        new Rectangle(0, 0, Constants.PLAYER_W, Constants.PLAYER_H);

    // --- Escudo temporal (power-up) ---
    private float shieldTime = 0f; // segundos restantes de escudo

    public Player(float x, float y) {
        pos.set(x, y);
    }

    /** Devuelve el AABB del jugador en su posición actual. */
    public Rectangle getBounds() {
        bounds.setPosition(pos.x, pos.y);
        return bounds;
    }

    /**
     * Lógica principal de movimiento/colisión.
     * Recibo delta y la lista de rectángulos sólidos del nivel.
     */
    public void update(float dt, List<Rectangle> solids) {
        // 1) Entrada horizontal (teclas A/D o cursores)
        float dir = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)  || Gdx.input.isKeyPressed(Input.Keys.A)) dir -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) dir += 1f;
        vel.x = dir * Constants.MOVE_SPEED;

        // 2) Salto: solo si estoy en el suelo
        if (onGround && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            vel.y = Constants.JUMP_SPEED;
            onGround = false;
        }

        // 3) Gravedad
        vel.y += Constants.GRAVITY * dt;

        // 4) Integración + colisiones separando ejes (X primero, luego Y)
        // Eje X
        pos.x += vel.x * dt;
        collideX(solids);

        // Eje Y
        pos.y += vel.y * dt;
        onGround = false;           // se recalcula en collideY
        collideY(solids);
    }

    /** Resuelve colisiones en X con las plataformas. */
    private void collideX(List<Rectangle> solids) {
        Rectangle b = getBounds();
        for (Rectangle s : solids) {
            if (b.overlaps(s)) {
                if (vel.x > 0) pos.x = s.x - b.width;           // venía desde la izquierda
                else if (vel.x < 0) pos.x = s.x + s.width;      // venía desde la derecha
                vel.x = 0;
                b.setPosition(pos.x, pos.y);
            }
        }
    }

    /** Resuelve colisiones en Y y marca onGround cuando apoyo. */
    private void collideY(List<Rectangle> solids) {
        Rectangle b = getBounds();
        for (Rectangle s : solids) {
            if (b.overlaps(s)) {
                if (vel.y > 0) {
                    // venía subiendo: techo
                    pos.y = s.y - b.height;
                } else if (vel.y < 0) {
                    // venía cayendo: suelo
                    pos.y = s.y + s.height;
                    onGround = true;
                }
                vel.y = 0;
                b.setPosition(pos.x, pos.y);
            }
        }
    }

    // ========== ESCUDO (power-up) ==========

    /** Activo el escudo durante 'seconds'. Si ya tengo, me quedo con el mayor. */
    public void activateShield(float seconds) {
        shieldTime = Math.max(shieldTime, seconds);
    }

    /** ¿Sigo con escudo activo? */
    public boolean isShieldActive() {
        return shieldTime > 0f;
    }

    /** Llamar cada frame desde GameScreen para que el escudo vaya bajando. */
    public void tickShield(float dt) {
        if (shieldTime > 0f) shieldTime -= dt;
    }
}
