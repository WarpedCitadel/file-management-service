WITH sel_game_profile_cte AS (
    SELECT
        id
    FROM wc01.game_profile
    WHERE game_profile_uuid = ?::UUID
),
ins_game_image_staging_cte AS (
    SELECT
        file_name,
        file_size,
        iscover
    FROM unnest(
        ?::TEXT[],
        ?::TEXT[],
        ?::BOOL[]
    ) AS gis(file_name, file_size, iscover)
)
INSERT INTO wc01.game_image_staging (
    game_profile_id,
    file_name,
    file_size,
    iscover
)
SELECT
    sgpc.id,
    gis.file_name,
    gis.file_size,
    gis.iscover
FROM sel_game_profile_cte sgpc
CROSS JOIN ins_game_image_staging_cte gis
RETURNING file_name;