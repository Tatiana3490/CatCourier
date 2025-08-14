package com.svalero.mijuego.scores;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Array;

import java.text.SimpleDateFormat;
import java.util.*;

/** Lee/guarda scores en un JSON local y ofrece utilidades de ranking. */
public class ScoreManager {

    private static final String FILE_NAME = "scores.json"; // se guarda en Gdx.files.local()
    private static final Json json = new Json();

    //Devuelve TODAS las puntuaciones guardadas (puede estar vacío).
    public static Array<ScoreEntry> loadAll() {
        FileHandle fh = Gdx.files.local(FILE_NAME);
        if (!fh.exists()) return new Array<>();
        try {
            String txt = fh.readString();
            if (txt == null || txt.trim().isEmpty()) return new Array<>();
            return json.fromJson(Array.class, ScoreEntry.class, txt);
        } catch (Exception e) {
            Gdx.app.error("ScoreManager", "Error leyendo scores.json: " + e.getMessage(), e);
            // opcional: renombrar el archivo corrupto para no romper más
            try { fh.moveTo(Gdx.files.local(FILE_NAME + ".bak")); } catch (Exception ignore) {}
            return new Array<>();
        }
    }

    private static void saveAll(Array<ScoreEntry> list) {
        try {
            String data = json.prettyPrint(list);
            Gdx.files.local(FILE_NAME).writeString(data, false, "UTF-8");
        } catch (Exception e) {
            Gdx.app.error("ScoreManager", "Error guardando scores.json: " + e.getMessage(), e);
        }
    }


    //Añade una puntuación con fecha legible (String) y guarda.
    public static void addScore(String name, int score) {
        if (name == null || name.trim().isEmpty()) name = "Jugador";
        name = name.trim();

        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()); // Fecha legible (String), formato estable y corto

        Array<ScoreEntry> all = loadAll();
        all.add(new ScoreEntry(name, score, date));

        // Ordenar por puntos DESC
        all.sort(new Comparator<ScoreEntry>() {
            @Override public int compare(ScoreEntry a, ScoreEntry b) {
                return Integer.compare(b.score, a.score);
            }
        });

        // recortar a 100 como máximo para no crecer infinito
        if (all.size > 100) {
            Array<ScoreEntry> trimmed = new Array<>();
            for (int i = 0; i < 100; i++) trimmed.add(all.get(i));
            all = trimmed;
        }

        saveAll(all);
    }

    //Devuelve el Top-N (por defecto se muestra N=10).
    public static List<ScoreEntry> getTop(int n) {
        Array<ScoreEntry> all = loadAll();
        all.sort(new Comparator<ScoreEntry>() {
            @Override public int compare(ScoreEntry a, ScoreEntry b) {
                return Integer.compare(b.score, a.score);
            }
        });
        List<ScoreEntry> top = new ArrayList<>();
        for (int i = 0; i < Math.min(n, all.size); i++) top.add(all.get(i));
        return top;
    }

    //Borra todas las puntuaciones
    public static void clearAll() {
        Gdx.files.local(FILE_NAME).delete();
    }
}
