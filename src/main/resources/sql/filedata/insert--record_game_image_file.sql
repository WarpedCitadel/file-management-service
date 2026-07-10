WITH sel_game_profile_cte AS (
    SELECT
        id
    FROM wc01.game_profile
    WHERE game_profile_uuid = ?::uuid
),
ins_game_image_cte AS (
    SELECT
        file_name,
        file_size
    FROM unnest(
        ?::text[],
        ?::text[]
    ) AS gi(file_name, file_size)
)
INSERT INTO wc01.game_image (
    game_profile_id,
    file_name,
    file_size
)
SELECT
    sgpc.id,
    gi.file_name,
    gi.file_size
FROM sel_game_profile_cte sgpc
CROSS JOIN ins_game_image_cte gi
RETURNING img_uuid;