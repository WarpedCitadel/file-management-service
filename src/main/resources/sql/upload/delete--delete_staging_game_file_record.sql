WITH sel_game_profile_cte AS
(
    SELECT
        gp.id
    FROM wc01.game_profile gp
    WHERE gp.game_profile_uuid = ?::UUID
),
del_image_row_cte AS
 (
    DELETE FROM wc01.game_file_staging gfs
    USING sel_game_profile_cte sgpc
    	WHERE gfs.game_profile_id = sgpc.id
    returning
	    gfs.file_name
)
SELECT
    file_name
FROM del_image_row_cte;