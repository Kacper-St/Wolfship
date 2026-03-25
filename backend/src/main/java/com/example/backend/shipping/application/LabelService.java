package com.example.backend.shipping.application;

import com.example.backend.shipping.domain.model.Shipment;

public interface LabelService {
    String generateAndUploadLabel(Shipment shipment);
}