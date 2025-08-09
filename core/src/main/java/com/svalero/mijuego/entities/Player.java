package com.svalero.mijuego.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.svalero.mijuego.util.Constants;

import java.util.List;

public class Player {
    public final Vector2 pos = new Vector2();
    public final Vector2 vel = new Vector2();
    public boolean onGround = false;
    public int energy = 100;      // para HUD
    public int score = 0;         // para HUD

    private final Rectangle bounds = new Rectangle(0,0, Constants.PLAYER_W, Constants.PLAYER_H);

    public Player(float x, float y){
        pos.set(x, y);
    }

    public Rectangle getBounds(){
        bounds.setPosition(pos.x, pos.y);
        return bounds;
    }

    public void update(float dt, List<Rectangle> solids){
        // Entrada horizontal
        float dir = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) dir -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) dir += 1f;
        vel.x = dir * Constants.MOVE_SPEED;

        // Salto
        if ((Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) && onGround){
            vel.y = Constants.JUMP_SPEED;
            onGround = false;
        }

        // Gravedad
        vel.y += Constants.GRAVITY * dt;

        // Integración + colisiones por ejes (AABB simple)
        // Eje X
        pos.x += vel.x * dt;
        collideX(solids);
        // Eje Y
        pos.y += vel.y * dt;
        onGround = false;
        collideY(solids);
    }

    private void collideX(List<Rectangle> solids){
        Rectangle b = getBounds();
        for (Rectangle s : solids){
            if (b.overlaps(s)){
                if (vel.x > 0) pos.x = s.x - b.width; else if (vel.x < 0) pos.x = s.x + s.width;
                vel.x = 0; b.setPosition(pos.x, pos.y);
            }
        }
    }

    private void collideY(List<Rectangle> solids){
        Rectangle b = getBounds();
        for (Rectangle s : solids){
            if (b.overlaps(s)){
                if (vel.y > 0) pos.y = s.y - b.height; else if (vel.y < 0){ pos.y = s.y + s.height; onGround = true; }
                vel.y = 0; b.setPosition(pos.x, pos.y);
            }
        }
    }
}
