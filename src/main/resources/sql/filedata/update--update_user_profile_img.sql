WITH sel_app_user_cte AS
(
    SELECT
        au.id
        	AS app_user_profile_id
    FROM wc01.app_user au
    WHERE user_uuid = ?::UUID
),
del_staging_row_cte AS
(
    DELETE FROM wc01.profile_image_staging pis
    USING sel_app_user_cte sau
    	WHERE pis.app_user_profile_id = sau.app_user_profile_id
    RETURNING
    	pis.app_user_profile_id,
    	pis.file_name,
    	pis.file_size,
    	pis.img_uuid
),
upt_profile_image_cte AS
(
	UPDATE wc01.profile_image pi
	SET
		file_name = dsrc.file_name,
    	file_size = dsrc.file_size,
    	img_uuid = dsrc.img_uuid
    FROM del_staging_row_cte dsrc
	WHERE pi.app_user_profile_id = dsrc.app_user_profile_id
	returning
	    pi.file_name,
	    pi.img_uuid
)
SELECT
    file_name,
    img_uuid
FROM upt_profile_image_cte;