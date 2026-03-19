CREATE TABLE addresses (
                           id UUID PRIMARY KEY,
                           full_name VARCHAR(255) NOT NULL,
                           email VARCHAR(255) NOT NULL,
                           phone_number VARCHAR(20) NOT NULL,
                           street VARCHAR(255) NOT NULL,
                           house_number VARCHAR(20) NOT NULL,
                           apartment_number VARCHAR(20),
                           country VARCHAR(2) NOT NULL DEFAULT 'PL',
                           city VARCHAR(100) NOT NULL,
                           zip_code VARCHAR(10) NOT NULL
);

CREATE TABLE shipments (
                           id UUID PRIMARY KEY,
                           tracking_number VARCHAR(255) NOT NULL UNIQUE,
                           sender_id UUID NOT NULL,
                           courier_id UUID,
                           sender_address_id UUID NOT NULL REFERENCES addresses(id),
                           receiver_address_id UUID NOT NULL REFERENCES addresses(id),
                           status VARCHAR(50) NOT NULL,
                           size VARCHAR(10) NOT NULL,
                           price DECIMAL(19, 2) NOT NULL,
                           currency VARCHAR(3) NOT NULL DEFAULT 'PLN',

                           created_at TIMESTAMP NOT NULL,
                           updated_at TIMESTAMP NOT NULL,

                           picked_up_at TIMESTAMP,
                           delivered_at TIMESTAMP,
                           cancelled_at TIMESTAMP
);

CREATE TABLE shipment_status_history (
                                         id UUID PRIMARY KEY,
                                         shipment_id UUID NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
                                         status VARCHAR(50) NOT NULL,
                                         changed_at TIMESTAMP NOT NULL,
                                         comment VARCHAR(255),
                                         changed_by UUID
);

CREATE INDEX idx_tracking ON shipments(tracking_number);
CREATE INDEX idx_sender ON shipments(sender_id);
CREATE INDEX idx_status ON shipments(status);