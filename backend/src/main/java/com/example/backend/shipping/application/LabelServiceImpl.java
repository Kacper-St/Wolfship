package com.example.backend.shipping.application;

import com.example.backend.shipping.domain.model.Shipment;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class LabelServiceImpl implements LabelService {

    private final MinioClient minioClient;

    @Value("${app.minio.bucket-name}")
    private String bucketName;

    @Override
    public String generateAndUploadLabel(Shipment shipment) {
        log.info("Generating label for shipment: {}", shipment.getTrackingNumber());

        try {
            byte[] qrBytes = generateQrCode(shipment.getTrackingNumber());

            byte[] pdfBytes = generatePdf(shipment, qrBytes);

            String fileName = "labels/" + shipment.getTrackingNumber() + ".pdf";
            uploadToMinio(pdfBytes, fileName);

            String url = bucketName + "/" + fileName;
            log.info("Label generated and uploaded: {}", url);
            return url;

        } catch (Exception e) {
            log.error("Failed to generate label for shipment: {}",
                    shipment.getTrackingNumber(), e);
            throw new RuntimeException("Label generation failed", e);
        }
    }

    private byte[] generateQrCode(String trackingNumber)
            throws WriterException, IOException {

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(
                trackingNumber,
                BarcodeFormat.QR_CODE,
                300,
                300
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
        return out.toByteArray();
    }

    private byte[] generatePdf(Shipment shipment, byte[] qrBytes)
            throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();

        document.add(new Paragraph("WOLFSHIP — ETYKIETA PRZESYLKI"));
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Nr przesylki: "
                + shipment.getTrackingNumber()));
        document.add(new Paragraph(" "));

        document.add(new Paragraph("NADAWCA:"));
        document.add(new Paragraph(
                shipment.getSenderAddress().getFullName()));
        document.add(new Paragraph(
                shipment.getSenderAddress().getStreet() + " "
                        + shipment.getSenderAddress().getHouseNumber()));
        document.add(new Paragraph(
                shipment.getSenderAddress().getZipCode() + " "
                        + shipment.getSenderAddress().getCity()));
        document.add(new Paragraph(" "));

        document.add(new Paragraph("ODBIORCA:"));
        document.add(new Paragraph(
                shipment.getReceiverAddress().getFullName()));
        document.add(new Paragraph(
                shipment.getReceiverAddress().getStreet() + " "
                        + shipment.getReceiverAddress().getHouseNumber()));
        document.add(new Paragraph(
                shipment.getReceiverAddress().getZipCode() + " "
                        + shipment.getReceiverAddress().getCity()));
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Rozmiar: "
                + shipment.getSize().name()));
        document.add(new Paragraph(" "));

        Image qrImage = Image.getInstance(qrBytes);
        qrImage.scaleToFit(200, 200);
        qrImage.setAlignment(Image.ALIGN_CENTER);
        document.add(qrImage);

        document.close();
        return out.toByteArray();
    }

    private void uploadToMinio(byte[] pdfBytes, String fileName)
            throws Exception {

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(new ByteArrayInputStream(pdfBytes),
                                pdfBytes.length, -1)
                        .contentType("application/pdf")
                        .build()
        );
    }
}