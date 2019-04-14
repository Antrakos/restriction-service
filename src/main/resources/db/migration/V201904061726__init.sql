CREATE TABLE "restriction"
(
    "id"          SERIAL PRIMARY KEY,
    "sensor_id"   VARCHAR,
    "duration"    BIGINT,
    "count_limit" INT,
    "lower_bound" INT,
    "upper_bound" INT
);
