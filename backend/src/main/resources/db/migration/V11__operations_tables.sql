CREATE TABLE couriers (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          user_id UUID NOT NULL,
                          courier_type VARCHAR(50) NOT NULL,
                          zone_id UUID,
                          source_hub_id UUID,
                          target_hub_id UUID,
                          active BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
                          updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
                          CONSTRAINT chk_courier_assignment CHECK (
                              (courier_type = 'ZONE_COURIER' AND zone_id IS NOT NULL
                                  AND source_hub_id IS NULL AND target_hub_id IS NULL)
                                  OR
                              (courier_type = 'LINE_HAUL_COURIER' AND source_hub_id IS NOT NULL
                                  AND target_hub_id IS NOT NULL AND zone_id IS NULL)
                              )
);

CREATE TABLE tasks (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       shipment_id UUID NOT NULL,
                       tracking_number VARCHAR(255) NOT NULL,
                       courier_id UUID REFERENCES couriers(id),
                       task_type VARCHAR(50) NOT NULL,
                       task_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                       source_hub_id UUID,
                       target_hub_id UUID,
                       sequence_order INTEGER NOT NULL,
                       created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
                       updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_tasks_shipment ON tasks(shipment_id);
CREATE INDEX idx_tasks_courier ON tasks(courier_id);
CREATE INDEX idx_tasks_status ON tasks(task_status);
CREATE INDEX idx_couriers_zone ON couriers(zone_id);
CREATE INDEX idx_couriers_hubs ON couriers(source_hub_id, target_hub_id);