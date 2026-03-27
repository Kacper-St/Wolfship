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

                           created_at TIMESTAMP(6) WITH TIME ZONE,
                           updated_at TIMESTAMP(6) WITH TIME ZONE,

                           picked_up_at TIMESTAMP(6) WITH TIME ZONE,
                           delivered_at TIMESTAMP(6) WITH TIME ZONE,
                           cancelled_at TIMESTAMP(6) WITH TIME ZONE
);

CREATE INDEX idx_tracking ON shipments(tracking_number);
CREATE INDEX idx_sender ON shipments(sender_id);
CREATE INDEX idx_status ON shipments(status);