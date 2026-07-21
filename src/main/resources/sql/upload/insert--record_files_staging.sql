WITH sel_game_profile_cte AS (
    SELECT
        id
    FROM wc01.game_profile
    WHERE game_profile_uuid = ?::UUID
),
ins_game_file_staging_cte AS
(
    SELECT
        file_name,
        file_size,
        platform_id
    FROM unnest(
        ?::TEXT[],
        ?::TEXT[],
        ?::SMALLINT[]
    ) AS igpc(file_name, file_size, platform_id)
)
    INSERT INTO wc01.game_file_staging
	(
	    game_profile_id,
	    file_name,
	    file_size,
 	    platform_id
 	)
SELECT
    sgpc.id,
    igpc.file_name,
    igpc.file_size,
    igpc.platform_id
FROM sel_game_profile_cte sgpc
CROSS JOIN ins_game_file_staging_cte igpc
RETURNING file_name;