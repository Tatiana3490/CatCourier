package com.svalero.mijuego.scores;

/** Entrada de ranking: nombre, puntos y fecha (como String). */
public class ScoreEntry {
    public String name;
    public int score;
    public String date;  // ← fecha legible, p.ej. "2025-08-12 12:36"

    public ScoreEntry() { } // Necesario para (de)serialización JSON de LibGDX

    public ScoreEntry(String name, int score, String date) {
        this.name = name;
        this.score = score;
        this.date = date;
    }
}
