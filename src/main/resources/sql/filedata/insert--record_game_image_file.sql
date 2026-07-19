WITH sel_game_profile_cte AS (
    SELECT id
    FROM wc01.game_profile
    WHERE game_profile_uuid = ?::UUID
),
del_staging_row_cte AS (
    DELETE FROM wc01.game_image_staging gis
    USING sel_game_profile_cte sgpc
    WHERE gis.game_profile_id = sgpc.id
    RETURNING
    	gis.game_profile_id,
    	gis.img_uuid,
    	gis.iscover,
    	gis.file_name,
    	gis.file_size
)
INSERT INTO wc01.game_image (
    game_profile_id,
    img_uuid,
   	iscover,
   	file_name,
   	file_size
)
SELECT
    game_profile_id,
    img_uuid,
    iscover,
    file_name,
    file_size
FROM del_staging_row_cte
RETURNING file_name;