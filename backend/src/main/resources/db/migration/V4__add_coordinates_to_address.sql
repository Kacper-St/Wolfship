CREATE EXTENSION IF NOT EXISTS postgis;

ALTER TABLE addresses ADD COLUMN IF NOT EXISTS coordinates geometry(Point, 4326);

ALTER TABLE shipments ADD COLUMN IF NOT EXISTS label_url VARCHAR(500);

CREATE INDEX IF NOT EXISTS idx_addresses_coordinates ON addresses USING GIST (coordinates);