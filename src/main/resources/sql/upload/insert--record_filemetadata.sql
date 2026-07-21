WITH sel_game_profile_cte AS
(
    SELECT
        id
    FROM wc01.game_profile
    WHERE game_profile_uuid = ?::UUID
),
ins_game_file_cte AS
(
    INSERT INTO wc01.game_file
	(game_profile_id,
	 file_name,
 	 platform_id,
 	 file_size)
    SELECT
        id,
        ?,
        ?,
        ?
    FROM sel_game_profile_cte
    RETURNING file_uuid
)
SELECT file_uuid
FROM ins_game_file_cte;