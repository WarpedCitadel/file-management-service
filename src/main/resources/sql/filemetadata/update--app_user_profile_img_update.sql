UPDATE wc01.profile_image pi
SET file_name = ?,
    file_size = ?
    FROM wc01.app_user aup
INNER JOIN wc01.app_user au
ON aup.id = au.id
WHERE pi.app_user_profile_id = aup.id
  AND au.id = ?