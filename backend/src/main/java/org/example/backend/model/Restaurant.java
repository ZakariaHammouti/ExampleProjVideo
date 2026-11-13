package org.example.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("restaurants")
public class Restaurant {
    @Id
    private String id;
    private String name;
    private String cuisine;
    private String address;
    private String city;
    private double rating;
    private int priceLevel;
    private double latitude;
    private double longitude;
    private boolean openNow;
}
