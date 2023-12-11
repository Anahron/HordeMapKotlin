package ru.newlevel.hordemap.app

const val TAG = "AAA"

//Keys
const val SHARE_PREFS_NAME = "sharedHordeMap"
const val KEY_NAME = "userName"
const val KEY_MARKER = "userMarker"
const val KEY_TIME_TO_SEND_DATA = "timeToSend"
const val KEY_STATIC_MARKER_SIZE = "staticMarkerSize"
const val KEY_USERS_MARKER_SIZE = "usersMarkerSize"
const val KEY_USER_ID = "keyUserId"
const val KEY_AUTH_NAME = "keyAuthName"
const val KEY_PROFILE_URL = "keyProfileUrl"
const val KEY_IS_AUTO_LOAD = "isAutoLoad"
const val KEY_NEW_MESSAGES_COUNT = "keyNewMessages_count"

const val DEFAULT_SIZE = 60
const val DEFAULT_TIME = 30
const val TIME_TO_DELETE_USER_MARKER = 30 // в минутах
const val SHADOW_QUALITY = 0.15f
const val KML_EXTENSION = ".kml"
const val KMZ_EXTENSION = ".kmz"
const val GPX_EXTENSION = ".gpx"
const val GARMIN_TAG = "Garmin_tag"

//db keys
const val MESSAGE_FILE_FOLDER = "MessengerFiles0"
const val PROFILE_PHOTO_FOLDER = "UsersPhotoStorage"
const val MAP_URL = "gs://horde-4112c.appspot.com/maps/map.kmz"  // карта полигона
const val GEO_USER_MARKERS_PATH = "geoData0"
const val GEO_STATIC_MARKERS_PATH = "geoMarkers0"
const val MESSAGE_PATH = "messages0"
const val USERS_PROFILES_PATH = "usersProfiles0"
const val TIMESTAMP_PATH = "timestamp"
const val LOCATION_DATABASE = "my_location_table"
const val BASE_LAST_MAP_FILENAME = "lastSavedMap"

// Permissions
const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1010
const val REQUEST_CODE_CAMERA_PERMISSION = 1011
const val REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE = 34
const val REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE = 56

const val ACTION_PROCESS_UPDATES = "ru.newlevel.hordemap.app.action.PROCESS_UPDATES"