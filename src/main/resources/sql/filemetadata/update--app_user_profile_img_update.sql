UPDATE wc01.profile_image pi
SET file_name = ?,
    file_size = ?
    FROM wc01.app_user au
INNER JOIN wc01.app_user_profile aup
ON aup.app_user_id = au.id
WHERE pi.app_user_profile_id = aup.id
  AND au.id = ?;