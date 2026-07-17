select
	gf.file_uuid,
	gf.file_name,
	gf.file_version,
	gf.file_size,
	gf.status_type_id,
	gf.modified_dtm,
   	gf.created_dtm
from wc01.game_file gf;