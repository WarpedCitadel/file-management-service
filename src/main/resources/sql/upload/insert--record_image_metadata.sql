WITH sel_app_user_cte AS
(
    SELECT
        au.id
    FROM wc01.app_user au
    WHERE user_uuid = ?::UUID
),
ins_image_staging_cte AS
(
	insert into wc01.profile_image_staging
	(app_user_profile_id,
	 file_name,
	 file_size
	)
	select
		id,
		?,
		?
	from sel_app_user_cte
	returning
		file_name,
		img_uuid
)
select
	file_name,
	img_uuid
FROM ins_image_staging_cte;