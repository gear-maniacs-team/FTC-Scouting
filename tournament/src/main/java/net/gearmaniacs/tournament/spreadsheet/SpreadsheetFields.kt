package net.gearmaniacs.tournament.spreadsheet

internal object SpreadsheetFields {
    const val TEAMS_SHEET = "Teams"
    const val MATCHES_SHEET = "Matches"
    const val OPR_SHEET = "OPR"

    val TEAM_COLUMNS = listOf(
        "Number",
        "Name",
        "Depot Minerals",
        "Lander Minerals",
        "Endgame",
        "Preferred Location",
        "Notes",
        "Auto - Latching",
        "Auto - Sampling",
        "Auto - Marker",
        "Auto - Parking",
        "Auto - Minerals"
    )

    val MATCHES_COLUMNS =
        listOf("Number", "Red 1", "Red 2", "Red Score", "Blue 1", "Blue 2", "Blue Score")

    val OPR_COLUMNS = listOf("Number", "Name", "Points")
}
