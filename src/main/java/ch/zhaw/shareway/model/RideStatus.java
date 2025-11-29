package ch.zhaw.shareway.model;

public enum RideStatus {
    OPEN,           // Fahrt offen, Buchungen möglich
    FULL,           // Alle Plätze gebucht
    IN_PROGRESS,    // Fahrt läuft gerade
    COMPLETED,      // Fahrt abgeschlossen
    CANCELED        // Fahrt abgesagt
}