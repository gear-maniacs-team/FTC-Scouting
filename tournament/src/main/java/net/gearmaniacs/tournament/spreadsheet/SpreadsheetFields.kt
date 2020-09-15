package net.gearmaniacs.tournament.spreadsheet

internal object SpreadsheetFields {
    const val TEAMS_SHEET_NAME = "Teams"
    const val MATCHES_SHEET_NAME = "Matches"
    const val LEADERBOARD_SHEET_NAME = "Leaderboard"

    val TEAM_COLUMNS = arrayOf(
        "Number",
        "Name",
        "Preferred Starting Zone",
        "Notes",
        "Auto - Wobble Delivery",
        "Auto - Low Goal",
        "Auto - Mid Goal",
        "Auto - High Goal",
        "Auto - Power Shot",
        "Auto - Navigation",
        "Controlled - Low Goal",
        "Controlled - Mid Goal",
        "Controlled - High Goal",
        "End - Power Shot",
        "End - Wobble Rings",
        "End - Wobble Delivery Zone",
        "Predicted Score"
    )

    val MATCHES_COLUMNS =
        arrayOf("Number", "Red 1", "Red 2", "Red Score", "Blue 1", "Blue 2", "Blue Score")

    val LEADERBOARD_COLUMNS = arrayOf("Number", "Name", "Score")
}
