package net.gearmaniacs.tournament.spreadsheet

internal object SpreadsheetFields {
    const val TEAMS_SHEET = "Teams"
    const val MATCHES_SHEET = "Matches"
    const val OPR_SHEET = "OPR"

    val TEAM_COLUMNS = arrayOf(
        "Number",
        "Name",
        "Preferred Zone",
        "Notes",
        "Delivered Stones",
        "Placed Stones",
        "Auto - Repositioned",
        "Auto - Navigated",
        "Auto - Delivered Skystones",
        "Auto - Delivered Stones",
        "Auto - Placed Stones",
        "End - Foundation Moved",
        "End - Parked",
        "End - Cap Level",
        "Predicted Score"
    )

    val MATCHES_COLUMNS =
        arrayOf("Number", "Red 1", "Red 2", "Red Score", "Blue 1", "Blue 2", "Blue Score")

    val OPR_COLUMNS = arrayOf("Number", "Name", "Points")
}
