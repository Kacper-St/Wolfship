package com.example.backend.shipping.application;

import org.locationtech.jts.geom.Point;

public interface GeocodingService {
    Point geocode(String street, String houseNumber,
                  String city, String zipCode, String country);
}