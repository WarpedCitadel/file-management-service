WITH sel_game_profile_cte AS (
	SELECT
		gp.id
	FROM wc01.game_profile gp
	WHERE gp.game_profile_uuid = ?::UUID
),
sel_game_file_cte AS (
	SELECT
		gf.file_name,
		gf.file_version,
		gf.file_size,
		gf.isbrowser,
		gf.status_type_id,
		gf.modified_dtm,
		gf.created_dtm
	FROM wc01.game_file gf
	JOIN sel_game_profile_cte sgp
	ON gf.game_profile_id = sgp.id
)
SELECT
		sgp.file_name,
		sgp.file_version,
		sgp.file_size,
		sgp.isbrowser,
		sgp.status_type_id,
		sgp.modified_dtm,
		sgp.created_dtm
FROM sel_game_file_cte sgp;