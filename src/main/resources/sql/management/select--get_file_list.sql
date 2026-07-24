SELECT
	f.app_user_id,
	f.game_profile_uuid,
	f.file_uuid,
	f.title,
	f.file_name,
	f.file_size,
	f.platform_id,
	f.status_type_id,
	f.created_dtm
FROM wc01.fnc_search_files_select (      	?::TEXT,
                                      	    ?::UUID,
                                   		    ?::SMALLINT,
                                          	?::SMALLINT
    ) f
LIMIT COALESCE( ?, 50) OFFSET COALESCE( ?, 0);