package com.example.backend.shipping.application;

import com.example.backend.shipping.domain.model.Shipment;

import java.io.InputStream;

public interface LabelService {
    String generateAndUploadLabel(Shipment shipment);
    InputStream getLabelStream(String filePath) throws Exception;
}