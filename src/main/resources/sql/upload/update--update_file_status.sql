WITH sel_game_profile_cte AS (
	SELECT
		gp.id
	FROM wc01.game_profile gp
	WHERE gp.game_profile_uuid = ?::UUID
),
upt_game_file_staging_cte AS (
    UPDATE wc01.game_file_staging gfs
    SET
        status_type_id = ?::SMALLINT
    FROM sel_game_profile_cte sgp
    WHERE gfs.game_profile_id = sgp.id
    returning
    	file_name
)
SELECT
	file_name
FROM upt_game_file_staging_cte;