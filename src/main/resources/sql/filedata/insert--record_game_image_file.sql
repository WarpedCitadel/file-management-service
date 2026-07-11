WITH sel_game_profile_cte AS (
    SELECT
        id
    FROM wc01.game_profile
    WHERE game_profile_uuid = ?::uuid
),
ins_game_image_cte AS (
    SELECT
        file_name,
        file_size,
        iscover
    FROM unnest(
        ?::text[],
        ?::text[],
         ?::bool[]
    ) AS gi(file_name, file_size, iscover)
)
INSERT INTO wc01.game_image (
    game_profile_id,
    file_name,
    file_size,
    iscover
)
SELECT
    sgpc.id,
    gi.file_name,
    gi.file_size,
    gi.iscover
FROM sel_game_profile_cte sgpc
CROSS JOIN ins_game_image_cte gi
RETURNING file_name;