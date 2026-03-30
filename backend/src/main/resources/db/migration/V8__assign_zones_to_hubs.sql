UPDATE zones z
SET hub_id = h.id
FROM hubs h
WHERE h.id = (
    SELECT id FROM hubs
    ORDER BY ST_Distance(
                     ST_Centroid(z.boundary),
                     location
             )
    LIMIT 1
);