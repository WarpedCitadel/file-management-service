WITH del_staging_row_cte AS (
    DELETE FROM wc01.game_file_staging gfs
    WHERE gfs.file_uuid IN (
		SELECT
			unnest( ?::UUID[])
	)
    returning
    	gfs.game_profile_id,
    	gfs.file_uuid,
    	gfs.file_name,
    	gfs.file_size,
    	gfs.platform_id,
    	gfs.status_type_id,
    	gfs.created_dtm
)
INSERT INTO wc01.game_file (
    game_profile_id,
    file_uuid,
   	file_name,
   	file_size,
   	platform_id,
   	status_type_id,
   	created_dtm
)
SELECT
    game_profile_id,
    file_uuid,
   	file_name,
   	file_size,
   	platform_id,
   	5 AS status_type_id,
   	created_dtm
FROM del_staging_row_cte
RETURNING file_name;