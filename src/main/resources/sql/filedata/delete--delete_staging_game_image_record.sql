WITH sel_game_profile_cte AS
(
    SELECT
        gp.id
    FROM wc01.game_profile gp
    WHERE gp.game_profile_uuid = ?::UUID
),
del_image_row_cte as (
    DELETE FROM wc01.game_image_staging gis
    USING sel_game_profile_cte sgpc
    	WHERE gis.game_profile_id = sgpc.id
    returning
	    gis.file_name
)
SELECT
    file_name
FROM del_image_row_cte;