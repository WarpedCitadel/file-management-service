WITH sel_app_user_cte AS
(
    SELECT
        au.id AS app_user_profile_id
    FROM wc01.app_user au
    WHERE user_uuid = ?::UUID
),
sel_old_profile_image_cte AS
(
    SELECT
        pi.app_user_profile_id,
        pi.file_name AS old_file_name,
        pi.img_uuid AS old_img_uuid
    FROM wc01.profile_image pi
    WHERE pi.app_user_profile_id = (SELECT app_user_profile_id FROM sel_app_user_cte)
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
	RETURNING
        pi.app_user_profile_id,
        pi.file_name
            AS new_file_name,
        pi.img_uuid
            AS new_img_uuid
)
SELECT
    opi.old_file_name,
    upi.new_file_name,
    opi.old_img_uuid,
    upi.new_img_uuid
FROM upt_profile_image_cte upi
JOIN sel_old_profile_image_cte opi
  ON upi.app_user_profile_id = opi.app_user_profile_id;