WITH sel_game_profile_cte AS (
	SELECT
		gp.id
	FROM wc01.game_profile gp
	WHERE gp.game_profile_uuid = ?::UUID
),
upt_game_file_cte AS (
    UPDATE wc01.game_file gf
    SET
        status_type_id = ?::INT
    FROM sel_game_profile_cte sgp
    WHERE gf.game_profile_id = sgp.id
    returning
    	status_type_id
)
SELECT status_type_id FROM upt_game_file_cte;