CREATE TABLE hubs (
                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                      name VARCHAR(100) NOT NULL,
                      code VARCHAR(10) NOT NULL UNIQUE,
                      location geometry(Point, 4326) NOT NULL
);

CREATE TABLE zones (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       name VARCHAR(100) NOT NULL,
                       teryt_code VARCHAR(10) NOT NULL UNIQUE,
                       boundary geometry(MultiPolygon, 4326) NOT NULL,
                       hub_id UUID REFERENCES hubs(id)
);

CREATE TABLE hub_connections (
                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 source_hub_id UUID NOT NULL REFERENCES hubs(id),
                                 target_hub_id UUID NOT NULL REFERENCES hubs(id),
                                 distance_km DOUBLE PRECISION NOT NULL,
                                 travel_time_minutes INTEGER NOT NULL,
                                 CONSTRAINT uq_hub_connection UNIQUE (source_hub_id, target_hub_id)
);

CREATE TABLE shipment_routes (
                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 shipment_id UUID NOT NULL UNIQUE,
                                 source_zone_id UUID NOT NULL REFERENCES zones(id),
                                 target_zone_id UUID NOT NULL REFERENCES zones(id),
                                 status VARCHAR(50) NOT NULL DEFAULT 'PLANNED',
                                 created_at TIMESTAMP(6) WITH TIME ZONE,
                                 updated_at TIMESTAMP(6) WITH TIME ZONE
);

CREATE TABLE shipment_route_steps (
                                      shipment_route_id UUID NOT NULL REFERENCES shipment_routes(id),
                                      hub_id UUID NOT NULL,
                                      step_index INTEGER NOT NULL,
                                      PRIMARY KEY (shipment_route_id, step_index)
);

CREATE INDEX idx_hubs_location ON hubs USING GIST (location);
CREATE INDEX idx_zones_boundary ON zones USING GIST (boundary);
CREATE INDEX idx_zones_hub ON zones(hub_id);
CREATE INDEX idx_shipment_routes_shipment ON shipment_routes(shipment_id);
CREATE INDEX idx_shipment_route_steps ON shipment_route_steps(shipment_route_id);