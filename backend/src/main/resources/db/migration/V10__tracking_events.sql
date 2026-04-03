CREATE TABLE tracking_events (
                                 id UUID PRIMARY KEY,
                                 shipment_id UUID NOT NULL,
                                 tracking_number VARCHAR(255) NOT NULL,
                                 status VARCHAR(50) NOT NULL,
                                 description VARCHAR(500),
                                 location VARCHAR(255),
                                 created_at TIMESTAMP(6) WITH TIME ZONE
);

CREATE INDEX idx_tracking_events_tracking_number ON tracking_events(tracking_number);
CREATE INDEX idx_tracking_events_shipment_id ON tracking_events(shipment_id);