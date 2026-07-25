WITH input_parameters_cte AS (
    SELECT
        unnest(?::UUID[]) AS game_profile_uuid,
        unnest(?::UUID[]) AS file_uuid
),
sel_game_profile_cte AS (
    SELECT
        gp.id AS game_profile_id,
        ipc.game_profile_uuid,
        ipc.file_uuid
    FROM wc01.game_profile gp
    JOIN
        input_parameters_cte ipc
    ON
        gp.game_profile_uuid = ipc.game_profile_uuid
),
upt_game_file_cte AS (
    UPDATE wc01.game_file_staging gfs
    SET
        status_type_id = ?::SMALLINT
    FROM sel_game_profile_cte sgp
    WHERE gfs.file_uuid = sgp.file_uuid
      AND gfs.game_profile_id = sgp.game_profile_id
    RETURNING
        sgp.game_profile_uuid,
        gfs.platform_id,
        gfs.file_name,
        gfs.file_uuid,
        gfs.status_type_id
)
SELECT
    game_profile_uuid,
    platform_id,
	file_name,
   	file_uuid,
   	status_type_id
FROM upt_game_file_cte;