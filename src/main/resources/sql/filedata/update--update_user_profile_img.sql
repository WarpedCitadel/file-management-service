WITH sel_app_user_cte AS
(
    SELECT
        au.id
    FROM wc01.app_user au
    WHERE user_uuid = ?::UUID
),
upt_profile_image_cte AS
(
	UPDATE wc01.profile_image pi
	SET
		file_name = ?,
    	file_size = ?
    FROM sel_app_user_cte sauc
		INNER JOIN wc01.app_user_profile aup
	ON aup.app_user_id = sauc.id
	WHERE pi.app_user_profile_id = aup.id
	returning img_uuid
)
SELECT img_uuid
FROM upt_profile_image_cte;