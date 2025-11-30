# ShareWay

ShareWay ist eine Mitfahr-App für die Schweiz, bei der Fahrer Fahrten anbieten und Mitfahrer verfügbare Fahrten suchen und buchen können.

## Hauptfunktionen

- **Fahrten anbieten**: Driver erstellen Fahrten mit Start, Ziel, Datum, Preis und Plätzen
- **Fahrten suchen**: Rider filtern nach Ort, Datum, Preis und verfügbaren Plätzen
- **Buchungssystem**: Rider buchen Plätze, Driver akzeptieren/lehnen Buchungen ab
- **Bewertungssystem**: Bidirektionale Bewertungen (1-5 Sterne) nach Fahrten
- **AI Content Moderation**: Spring AI moderiert automatisch Beschreibungen, Kommentare und Nachrichten

## Rollen

Es werden drei Benutzerrollen unterschieden:

- **👤 Rider (Mitfahrer)**: Kann Fahrten suchen, buchen und bewerten
- **🚗 Driver (Fahrer)**: Kann Fahrten anbieten, Buchungen verwalten und Fahrzeuge verwalten
- **⚙️ Admin (Administrator)**: Kann Benutzer moderieren und Statistiken einsehen

## Use Case Diagramm

Unsere ShareWay App bietet verschiedene Use Cases für 3 Akteurstypen:

![Use Case Diagram](doc/use-case-shareway.drawio.svg)

### Wichtige Use Cases:
- **Fahrt suchen/buchen**: Rider findet passende Fahrten nach Kriterien
- **Fahrt anbieten**: Driver erstellt neue Fahrt mit Fahrzeug
- **Buchung verwalten**: Driver akzeptiert/lehnt Buchungsanfragen ab
- **Bewertung abgeben**: Gegenseitige Bewertung nach abgeschlossener Fahrt
- **Inhalte moderieren (AI)**: Automatische Prüfung von User-Content auf unangemessene Inhalte

## ER Diagramm

Unser Datenmodell besteht aus 5 Entitäten mit Referenced-Beziehungen:

- **User**: Basis-Entität (RIDER, DRIVER, ADMIN)
- **Vehicle**: Fahrzeuge gehören zu Drivers
- **Ride**: Fahrten werden von Drivers mit Vehicles angeboten
- **Booking**: Riders buchen Plätze auf Rides
- **Review**: Bidirektionales Bewertungssystem nach Fahrten

![ER Diagram](doc/er-diagram-shareway.drawio.svg)

### Beziehungen:
- User 1:N Vehicle (ownerId)
- User 1:N Ride (driverId)
- Vehicle 1:N Ride (vehicleId)
- Ride 1:N Booking (rideId)
- User 1:N Booking (riderId)
- Ride 1:N Review (rideId)
- User 1:N Review (fromUserId, toUserId)

### Enumerations:
- **UserRole**: RIDER, DRIVER, ADMIN
- **RideStatus**: OPEN, FULL, IN_PROGRESS, COMPLETED, CANCELED
- **BookingStatus**: REQUESTED, APPROVED, REJECTED, CANCELED, COMPLETED

## Tech Stack

- **Backend**: Spring Boot 3.4.0, Java 21
- **Frontend**: SvelteKit (geplant)
- **Datenbank**: MongoDB Atlas
- **Authentication**: Auth0

## Projekt-Info

- **Modul**: Software Engineering 2 (ZHAW)
- **Abgabedatum**: 21. Dezember 2025