WITH sel_app_user_cte AS
(
    SELECT
        au.id
    FROM wc01.app_user au
    WHERE user_uuid = ?::UUID
),
del_image_row_cte as (
    DELETE FROM wc01.profile_image_staging pis
    USING sel_app_user_cte sauc
    	WHERE pis.app_user_profile_id = sauc.id
    returning
	    pis.file_name,
	    pis.img_uuid
)
SELECT
    file_name,
    img_uuid
FROM del_image_row_cte;