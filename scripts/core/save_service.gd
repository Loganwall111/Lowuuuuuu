extends Node
## SaveService — JSON slot persistence for SessionState (AUTOLOAD: "SaveService").
##
## JSON (not binary) is deliberate during production: saves stay diffable and a
## designer can hand-edit a slot to reproduce a bug. Swap the two `_write`/`_read`
## helpers for FileAccess.open_encrypted_with_pass() before shipping.
##
## DEPENDENCIES: SessionState.

signal save_completed(slot: int)
signal load_completed(slot: int, state: SessionState)

const SAVE_DIR := "user://saves"
const AUTOSAVE_SLOT := 0
const AUTOSAVE_MIN_INTERVAL := 60.0

var _last_autosave_time: float = -999.0


func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	DirAccess.make_dir_recursive_absolute(SAVE_DIR)


func slot_path(slot: int) -> String:
	return "%s/slot_%d.json" % [SAVE_DIR, slot]


func has_slot(slot: int) -> bool:
	return FileAccess.file_exists(slot_path(slot))


func save_slot(slot: int, state: SessionState) -> bool:
	if state == null:
		return false
	var payload := state.to_dictionary()
	payload["saved_at"] = Time.get_datetime_string_from_system(true)
	var file := FileAccess.open(slot_path(slot), FileAccess.WRITE)
	if file == null:
		push_error("SaveService: cannot write slot %d (%s)" % [slot, error_string(FileAccess.get_open_error())])
		return false
	file.store_string(JSON.stringify(payload, "\t"))
	file.close()
	save_completed.emit(slot)
	return true


func load_slot(slot: int) -> SessionState:
	var path := slot_path(slot)
	if not FileAccess.file_exists(path):
		return null
	var file := FileAccess.open(path, FileAccess.READ)
	if file == null:
		return null
	var text := file.get_as_text()
	file.close()
	var parsed: Variant = JSON.parse_string(text)
	if typeof(parsed) != TYPE_DICTIONARY:
		push_error("SaveService: corrupt save in slot %d" % slot)
		return null
	var state := SessionState.from_dictionary(parsed)
	load_completed.emit(slot, state)
	return state


## Throttled autosave called by GameDirector; safe to call every frame.
func autosave(state: SessionState) -> void:
	var now := Time.get_ticks_msec() / 1000.0
	if now - _last_autosave_time < AUTOSAVE_MIN_INTERVAL:
		return
	_last_autosave_time = now
	save_slot(AUTOSAVE_SLOT, state)


func describe_slot(slot: int) -> Dictionary:
	var path := slot_path(slot)
	if not FileAccess.file_exists(path):
		return {}
	var file := FileAccess.open(path, FileAccess.READ)
	if file == null:
		return {}
	var parsed: Variant = JSON.parse_string(file.get_as_text())
	file.close()
	return parsed if typeof(parsed) == TYPE_DICTIONARY else {}
