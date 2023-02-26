package net.gearmaniacs.tournament.csv

internal object CsvFields {
    val TEAM_COLUMNS = listOf(
        "Number",
        "Name",
        "Autonomous Score",
        "TeleOp Score",
        "Color Marker",
        "Preferred Start Zone",
        "Notes",
    )

    val MATCH_COLUMNS =
        listOf("Number", "Red 1", "Red 2", "Red Score", "Blue 1", "Blue 2", "Blue Score")

    val LEADERBOARD_COLUMNS = listOf("Team Number", "Name", "Score")
}
