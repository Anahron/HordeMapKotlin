package ru.newlevel.hordemap.presentation.map.utils

import ru.newlevel.hordemap.R

enum class GpxMarkersItem(val resourceId: Int, val markerType: String, val color: String) {
    DEFAULT(R.drawable.marker_point0, "DEFAULT", ""),
    FLAG_GREEN(R.drawable.marker_flag_green, "Flag", "Green"),
    FLAG_RED(R.drawable.marker_flag_red, "Flag", "Red"),
    FLAG_BLUE(R.drawable.marker_flag_blue, "Flag", "Blue"),
    FLAG_YELLOW(R.drawable.marker_flag_yellow, "Flag", "Yellow"),
    NAVAID_RED(R.drawable.marker_navaid_red, "Navaid", "Red"),
    NAVAID_YELLOW(R.drawable.marker_navaid_yellow, "Navaid", "Yellow"),
    NAVAID_GREEN(R.drawable.marker_navaid_green, "Navaid", "Green"),
    NAVAID_BLUE(R.drawable.marker_navaid_blue, "Navaid", "Blue"),
    NAVAID_WHITE(R.drawable.marker_navaid_white, "Navaid", "White"),
    NAVAID_ORANGE(R.drawable.marker_navaid_orange, "Navaid", "Orange"),
    NAVAID_RED_WHITE(R.drawable.marker_navaid_red_white, "Navaid", "Red/White"),
    NAVAID_RED_GREEN(R.drawable.marker_navaid_red_green, "Navaid", "Red/Green"),
    NAVAID_GREEN_RED(R.drawable.marker_navaid_green_red, "Navaid", "Green/Red"),
    NAVAID_GREEN_WHITE(R.drawable.marker_navaid_green_white, "Navaid", "Green/White"),
    NAVAID_WHITE_GREEN(R.drawable.marker_navaid_white_green, "Navaid", "White/Green"),
    NAVAID_WHITE_RED(R.drawable.marker_navaid_white_red, "Navaid", "White/Red"),
    NAVAID_AMBER(R.drawable.marker_navaid_amber, "Navaid", "Amber"),
    NAVAID_BLACK(R.drawable.marker_navaid_black, "Navaid", "Black"),
    NAVAID_VIOLET(R.drawable.marker_navaid_violet, "Navaid", "Violet"),
    BLOCK_RED(R.drawable.marker_block_red, "Block", "Red"),
    BLOCK_YELLOW(R.drawable.marker_block_yellow, "Block", "Yellow"),
    BLOCK_GREEN(R.drawable.marker_block_green, "Block", "Green"),
    BLOCK_BLUE(R.drawable.marker_block_blue, "Block", "Blue"),
    CAMPGROUND(R.drawable.marker_campground, "Campground", ""),
    HELIPORT(R.drawable.marker_heliport, "Heliport", ""),
    PARKING_AREA(R.drawable.marker_parking, "Parking Area", ""),
    MEDICAL_FACILLITY(R.drawable.marker_medical_facillity, "Medical Facility", ""),
    CEMETERY(R.drawable.marker_cemetery, "Cemetery", ""),
    RESIDENCE(R.drawable.marker_residence, "Residence", ""),
    OIL_FIELD(R.drawable.marker_oil_field, "Oil Field", ""),
}
enum class StaticMarkersItem(val resourceId: Int, val id: Int) {
    DEFAULT(R.drawable.marker_point0, 0),
    SWORDS(R.drawable.img_marker_swords, 1),
    FLAG_RED(R.drawable.flag_red, 2),
    FLAG_YELLOW(R.drawable.flag_yellow, 3),
    FLAG_GREEN(R.drawable.flag_green, 4),
    FLAG_BLUE(R.drawable.flag_blue, 5),
    MARKER_1(R.drawable.marker_point1, 11),
    MARKER_2(R.drawable.marker_point2, 12),
    MARKER_3(R.drawable.marker_point3, 13),
    MARKER_4(R.drawable.marker_point4, 14),
    MARKER_5(R.drawable.marker_point5, 15),
    MARKER_6(R.drawable.marker_point6, 16),
    MARKER_7(R.drawable.marker_point7, 17),
    MARKER_8(R.drawable.marker_point8, 18),
    MARKER_9(R.drawable.marker_point9, 19)
}

enum class UsersMarkersItem(val resourceId: Int, val id: Int) {
    RED(R.drawable.img_marker_red, 0),
    YELLOW(R.drawable.img_marker_yellow, 1),
    GREEN(R.drawable.img_marker_green, 2),
    BLUE(R.drawable.img_marker_blue, 3),
    PURPLE(R.drawable.img_marker_purple, 4)
}