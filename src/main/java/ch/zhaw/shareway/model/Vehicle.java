package ch.zhaw.shareway.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Document("vehicles")
@NoArgsConstructor
@RequiredArgsConstructor
@Getter
public class Vehicle {
    @Id
    private String id;
    
    @NonNull
    private String ownerId; // Referenz zu User (muss DRIVER sein)
    
    @NonNull
    private String make; // Marke (z.B. "Tesla")
    
    @NonNull
    private String model; // Modell (z.B. "Model 3")
    
    @NonNull
    private Integer seats; // Anzahl Sitzplätze
    
    @NonNull
    private String plateHash; // Kennzeichen (gehashed für Privacy)
    
    private String color; // Farbe (optional)
    private Integer year; // Baujahr (optional)
}