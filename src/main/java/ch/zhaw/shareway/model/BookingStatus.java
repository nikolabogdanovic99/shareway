package ch.zhaw.shareway.model;

public enum BookingStatus {
    REQUESTED,      // Anfrage gestellt, wartet auf Bestätigung
    APPROVED,       // Vom Driver bestätigt
    REJECTED,       // Vom Driver abgelehnt
    CANCELED,       // Vom Rider storniert
    COMPLETED       // Fahrt abgeschlossen
}