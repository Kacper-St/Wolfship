package com.example.backend.notifications.application;

import com.example.backend.notifications.domain.model.NotificationStatus;
import com.example.backend.notifications.infrastructure.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final EmailService emailService;

    @Override
    public void notifyShipmentCreated(String to, String trackingNumber, String labelUrl) {
        log.info("Sending shipment created notification to: {}", to);

        String subject = "Wolfship — paczka przyjęta: " + trackingNumber;
        String body = """
                <h2>Paczka przyjęta w systemie</h2>
                <p>Numer śledzenia: <strong>%s</strong></p>
                <p>Pobierz etykietę:
                <a href="http://localhost:8080/api/v1/shipments/%s/label">
                Pobierz PDF</a></p>
                <p>Śledź paczkę:
                <a href="http://localhost:8080/api/v1/tracking/%s/history">
                Historia przesyłki</a></p>
                """.formatted(trackingNumber, trackingNumber, trackingNumber);

        emailService.sendEmail(to, subject, body);
    }

    @Override
    public void notifyStatusChanged(String to, String trackingNumber, NotificationStatus status, String description) {
        log.info("Sending status update notification to: {} status: {}", to, status);

        String subject = "Wolfship — aktualizacja paczki: " + trackingNumber;
        String body = """
                <h2>Status paczki zaktualizowany</h2>
                <p>Numer śledzenia: <strong>%s</strong></p>
                <p>Aktualny status: <strong>%s</strong></p>
                <p>%s</p>
                <p>Śledź paczkę:
                <a href="http://localhost:8080/api/v1/tracking/%s/history">
                Historia przesyłki</a></p>
                """.formatted(trackingNumber, status.name(),
                description, trackingNumber);

        emailService.sendEmail(to, subject, body);
    }

    @Override
    public void notifyShipmentCancelled(String to, String trackingNumber) {
        log.info("Sending cancellation notification to: {}", to);

        String subject = "Wolfship — paczka anulowana: " + trackingNumber;
        String body = """
                <h2>Paczka anulowana</h2>
                <p>Numer śledzenia: <strong>%s</strong></p>
                <p>Twoja paczka została anulowana.</p>
                <p>W razie pytań skontaktuj się z nami.</p>
                """.formatted(trackingNumber);

        emailService.sendEmail(to, subject, body);
    }

    @Override
    public void notifyAccountCreated(String to, String tempPassword) {
        log.info("Sending account creation notification to: {}", to);

        String subject = "Wolfship — Twoje konto zostało utworzone";
        String body = """
            <h2>Witaj w Wolfship!</h2>
            <p>Twoje konto zostało utworzone przez administratora.</p>
            <p>Dane do logowania:</p>
            <p>Email: <strong>%s</strong></p>
            <p>Hasło tymczasowe: <strong>%s</strong></p>
            """.formatted(to, tempPassword);

        emailService.sendEmail(to, subject, body);
    }
}