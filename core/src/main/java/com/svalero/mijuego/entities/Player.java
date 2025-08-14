package com.svalero.mijuego.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.svalero.mijuego.util.Constants;
import lombok.Getter;

import java.util.List;

public class Player {

    // --- Posición, velocidad y estado de suelo ---
    public final Vector2 pos = new Vector2();   // posición del jugador en “unidades de mundo”
    public final Vector2 vel = new Vector2();   // velocidad actual
    public boolean onGround = false;            // true si estoy apoyado en alguna plataforma

    // --- HUD / feedback ---
    public int energy = 100; // la uso como “barra de vida”: 100 -> 50 -> 0 (dos golpes)
    public int score  = 0;   // puntos por recoger cajas y aliados bonus

    // --- AABB del jugador para colisiones “rectangulares” sencillas ---
    private final Rectangle bounds = new Rectangle(0, 0, Constants.PLAYER_W, Constants.PLAYER_H);

    // --- Sistema de 2 golpes (al 2º muero) ---
    private int hits = 0;            // golpes recibidos (0, 1, 2)
    private static final int MAX_HITS = 2;

    //¿El jugador ya ha muerto (dos golpes)?
    @Getter
    private boolean dead = false;    // flag de “muerto” cuando llego al 2º golpe

    // --- Invulnerabilidad tras recibir un golpe (para no contar varios en el mismo frame) ---
    private float invulnTime = 0f;                 // tiempo restante invulnerable
    private static final float INVULN_AFTER_HIT = 0.5f; // medio segundo está bien

    // --- Escudo temporal (lo activan aliados). Mientras dura, ignoro golpes. ---
    private float shieldTime = 0f;

    // --- Evento de “acabo de saltar” (sirve para reproducir el SFX justo una vez) ---
    private boolean justJumped = false; // lo pongo a true sólo el frame en el que arranco el salto

    public Player(float x, float y) {
        pos.set(x, y);
    }

    //Rectángulo actual del jugador
    public Rectangle getBounds() {
        bounds.setPosition(pos.x, pos.y);
        return bounds;
    }

    /**
     * Lógica básica de movimiento + colisiones con el mapa.
     * OJO: aquí NO le resto tiempo al escudo; eso lo hago en tickShield(dt), que ya llamo desde GameScreen.
     */
    public void update(float dt, List<Rectangle> solids) {
        // --- Input horizontal ---
        float dir = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)  || Gdx.input.isKeyPressed(Input.Keys.A)) dir -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) dir += 1f;
        vel.x = dir * Constants.MOVE_SPEED;

        // --- Salto (sólo si estoy en el suelo) ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && onGround) {
            vel.y = Constants.JUMP_SPEED;
            onGround = false;
            justJumped = true; // marco el evento para que suene el sfx en GameScreen
        }

        // --- Gravedad (siempre hacia abajo) ---
        vel.y += Constants.GRAVITY * dt;

        // --- Integración + colisión eje X ---
        pos.x += vel.x * dt;
        collideX(solids);

        // --- Integración + colisión eje Y ---
        pos.y += vel.y * dt;
        onGround = false;   // en principio asumo que NO estoy en el suelo…
        collideY(solids);   // …hasta que choque por abajo con algo

        // --- Timers varios ---
        if (invulnTime > 0f) invulnTime -= dt;
        // IMPORTANTE: el escudo NO lo descuento aquí para no duplicar;
        // lo descuento en tickShield(dt), que ya se llama desde GameScreen.
    }

    // Resuelvo colisiones horizontales (izq/der) contra todas las “solids”
    private void collideX(List<Rectangle> solids) {
        Rectangle b = getBounds();
        for (Rectangle s : solids) {
            if (b.overlaps(s)) {
                if (vel.x > 0)       pos.x = s.x - b.width;   // venía desde la izquierda → me pego al borde izq del sólido
                else if (vel.x < 0)  pos.x = s.x + s.width;   // venía desde la derecha → me pego al borde dcho del sólido
                vel.x = 0;
                b.setPosition(pos.x, pos.y);                  // actualizo el AABB tras ajustar
            }
        }
    }

    // Resuelvo colisiones verticales (arriba/abajo) y detecto “estar en suelo”
    private void collideY(List<Rectangle> solids) {
        Rectangle b = getBounds();
        for (Rectangle s : solids) {
            if (b.overlaps(s)) {
                if (vel.y > 0) {
                    // chocando con “el techo”: coloco justo debajo
                    pos.y = s.y - b.height;
                } else if (vel.y < 0) {
                    // chocando con “el suelo”: coloco justo encima y marco que estoy en suelo
                    pos.y = s.y + s.height;
                    onGround = true;
                }
                vel.y = 0;
                b.setPosition(pos.x, pos.y);
            }
        }
    }

    // ==========================
    //   SISTEMA DE GOLPES / VIDA
    // ==========================

    //Activo el escudo X segundos (mientras dure, ignoro golpes).
    public void activateShield(float seconds) {
        // si me vuelven a dar un escudo más largo, me quedo con el mayor
        shieldTime = Math.max(shieldTime, seconds);
    }

    // ¿Tengo el escudo activo ahora mismo?
    public boolean isShieldActive() {
        return shieldTime > 0f;
    }

    /**
     * Descuento el tiempo del escudo. Prefiero hacerlo fuera de update() para no
     * descontarlo dos veces (GameScreen ya llama a esto cada frame).
     */
    public void tickShield(float dt) {
        if (shieldTime > 0f) shieldTime -= dt;
    }

    /**
     * Registro un golpe del enemigo.
     * Devuelvo true si el golpe “ha contado” (así en GameScreen sé si debo reproducir el sfx).
     * Reglas:
     *  - Si estoy muerto, con escudo o invulnerable → ignoro.
     *  - Si cuenta, aumento ‘hits’, pongo invulnerabilidad cortita y actualizo ‘energy’.
     */
    public boolean onHit() {
        if (dead)              return false; // ya estoy muerto
        if (isShieldActive())  return false; // escudo activo: ignoro daño
        if (invulnTime > 0f)   return false; // aún invulnerable por golpe previo

        // Este golpe sí cuenta
        hits++;
        invulnTime = INVULN_AFTER_HIT;

        // Actualizo la “barra” para el HUD: 100 -> 50 -> 0
        energy = Math.max(0, 100 - hits * 50);

        if (hits >= MAX_HITS) {
            dead = true;
            energy = 0;
        }
        return true;
    }

    // ==========================
    //   EVENTO DE SALTO (SFX)
    // ==========================

    /**
     * Devuelve true sólo el frame en el que he iniciado un salto (lo consumo).
     * Así en GameScreen puedo hacer: if (player.consumeJustJumped()) reproducir sJump.
     */
    public boolean consumeJustJumped() {
        boolean j = justJumped;
        justJumped = false; // lo “gasto” para que no se repita
        return j;
    }
}
