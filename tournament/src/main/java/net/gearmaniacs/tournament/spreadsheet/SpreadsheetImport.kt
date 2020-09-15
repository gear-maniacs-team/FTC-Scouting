package net.gearmaniacs.tournament.spreadsheet

import net.gearmaniacs.core.extensions.justTry
import net.gearmaniacs.core.model.Alliance
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.core.model.enums.ColorMarker
import net.gearmaniacs.core.model.enums.PreferredZone
import net.gearmaniacs.core.model.enums.WobbleDeliveryZone
import net.gearmaniacs.core.model.team.AutonomousPeriod
import net.gearmaniacs.core.model.team.ControlledPeriod
import net.gearmaniacs.core.model.team.EndGamePeriod
import net.gearmaniacs.core.model.team.Team
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import java.io.InputStream
import java.util.Locale

internal class SpreadsheetImport(inputStream: InputStream) {

    private val workbook = HSSFWorkbook(inputStream)

    private fun extractTeams(sheet: Sheet): List<Team> {
        val result = ArrayList<Team>(50)

        sheet.forEach { row ->
            justTry {
                val team = processTeam(row)
                result.add(team)
            }
        }

        return result
    }

    private fun processTeam(row: Row): Team {
        var number = -1
        var name: String? = null
        var preferredStartZoneString: String? = null
        var notes: String? = null

        var autoWobbleDelivery = false
        var autoLowGoal = 0
        var autoMidGoal = 0
        var autoHighGoal = 0
        var autoPowerShot = 0
        var autoNavigation = false

        var controlledLowGoal = 0
        var controlledMidGoal = 0
        var controlledHighGoal = 0

        var endPowerShot = 0
        var endWobbleRings = 0
        var endWobbleDeliveryZoneString: String? = null

        row.forEachIndexed { cellIndex, cell ->
            when (cellIndex) {
                0 -> number = cell.numericCellValue.toInt()
                1 -> name = cell.stringCellValue
                2 -> preferredStartZoneString = cell.stringCellValue
                3 -> notes = cell.stringCellValue.takeIf { it.isNotEmpty() }
                4 -> autoWobbleDelivery = cell.booleanCellValue
                5 -> autoLowGoal = cell.numericCellValue.toInt()
                6 -> autoMidGoal = cell.numericCellValue.toInt()
                7 -> autoHighGoal = cell.numericCellValue.toInt()
                8 -> autoPowerShot = cell.numericCellValue.toInt()
                9 -> autoNavigation = cell.booleanCellValue
                10 -> controlledLowGoal = cell.numericCellValue.toInt()
                11 -> controlledMidGoal = cell.numericCellValue.toInt()
                12 -> controlledHighGoal = cell.numericCellValue.toInt()
                13 -> endPowerShot = cell.numericCellValue.toInt()
                14 -> endWobbleRings = cell.numericCellValue.toInt()
                15 -> endWobbleDeliveryZoneString = cell.stringCellValue
            }
        }

        check(number > 0) { "Invalid Team number on row ${row.rowNum}" }

        val endWobbleDeliveryZone = when (endWobbleDeliveryZoneString?.toLowerCase(Locale.ROOT)) {
            "dead zone" -> WobbleDeliveryZone.DEAD_ZONE
            "start line" -> WobbleDeliveryZone.START_LINE
            else -> WobbleDeliveryZone.NONE
        }

        val autonomousPeriod = AutonomousPeriod(
            autoWobbleDelivery,
            autoLowGoal,
            autoMidGoal,
            autoHighGoal,
            autoPowerShot,
            autoNavigation,
        )
        val controlledPeriod =
            ControlledPeriod(controlledLowGoal, controlledMidGoal, controlledHighGoal)
        val endGamePeriod = EndGamePeriod(endPowerShot, endWobbleRings, endWobbleDeliveryZone)

        val preferredZone = when (preferredStartZoneString?.toLowerCase(Locale.ROOT)) {
            "right" -> PreferredZone.RIGHT
            "left" -> PreferredZone.LEFT
            else -> PreferredZone.NONE
        }

        return Team(
            "",
            "",
            number,
            name,
            autonomousPeriod.takeIf { it.isNotEmpty() },
            controlledPeriod.takeIf { it.isNotEmpty() },
            endGamePeriod.takeIf { it.isNotEmpty() },
            ColorMarker.DEFAULT,
            preferredZone,
            notes
        )
    }

    private fun extractMatches(sheet: Sheet): List<Match> {
        val result = ArrayList<Match>(50)

        sheet.forEach { row ->
            justTry {
                val match = processMatch(row)
                result.add(match)
            }
        }

        return result
    }

    private fun processMatch(row: Row): Match {
        var number = -1

        var firstRed = 0
        var secondRed = 0
        var redScore = 0

        var firstBlue = 0
        var secondBlue = 0
        var blueScore = 0

        row.forEachIndexed { cellIndex, cell ->
            when (cellIndex) {
                0 -> number = cell.numericCellValue.toInt()
                1 -> firstRed = cell.numericCellValue.toInt()
                2 -> secondRed = cell.numericCellValue.toInt()
                3 -> redScore = cell.numericCellValue.toInt()
                4 -> firstBlue = cell.numericCellValue.toInt()
                5 -> secondBlue = cell.numericCellValue.toInt()
                6 -> blueScore = cell.numericCellValue.toInt()
            }
        }

        check(number > 0) { "Invalid Match number on row ${row.rowNum}" }

        return Match(
            "",
            "",
            number,
            Alliance(firstRed, secondRed, redScore),
            Alliance(firstBlue, secondBlue, blueScore)
        )
    }

    fun getTeams(): List<Team> {
        workbook.getSheet(SpreadsheetFields.TEAMS_SHEET_NAME)?.let {
            return extractTeams(it)
        }

        return emptyList()
    }

    fun getMatches(): List<Match> {
        workbook.getSheet(SpreadsheetFields.MATCHES_SHEET_NAME)?.let {
            return extractMatches(it)
        }

        return emptyList()
    }
}
