package com.svalero.mijuego.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.svalero.mijuego.util.Constants;
import lombok.var;

public class Hud {
    public final Stage stage;
    private final Label scoreL, energyL, levelL;

    public Hud(com.badlogic.gdx.graphics.g2d.Batch sharedBatch){
        stage = new Stage(new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT), sharedBatch);
        var skin = UiStyles.makeSkin();
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        scoreL = new Label("Score: 0", skin);
        energyL = new Label("Energía: 100", skin);
        levelL = new Label("Nivel: 1", skin);
        root.top().left();
        root.add(scoreL).pad(10);
        root.add(energyL).pad(10);
        root.add(levelL).expandX().left().pad(10);
    }
    public void setScore(int v){ scoreL.setText("Score: " + v); }
    public void setEnergy(int v){ energyL.setText("Energía: " + v); }
    public void setLevel(int v){ levelL.setText("Nivel: " + v); }
}
