package net.gearmaniacs.tournament.spreadsheet

import net.gearmaniacs.core.extensions.justTry
import net.gearmaniacs.core.model.Alliance
import net.gearmaniacs.core.model.AutonomousData
import net.gearmaniacs.core.model.EndGameData
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.core.model.PreferredZone
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.model.TeleOpData
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
        var id = -1
        var name: String? = null
        var preferredZoneString: String? = null
        var notes: String? = null

        var deliveredStones = 0
        var placedStones = 0
        var skyscraperHeight = 0

        var autoReposition = false
        var autoNavigated = false
        var autoDeliveredSkystones = 0
        var autoDeliveredStones = 0
        var autoPlacedStones = 0

        var endFoundationMoved = false
        var endParked = false
        var endCapLevel = 0

        row.forEachIndexed { cellIndex, cell ->
            when (cellIndex) {
                0 -> id = cell.numericCellValue.toInt()
                1 -> name = cell.stringCellValue
                2 -> preferredZoneString = cell.stringCellValue
                3 -> notes = cell.stringCellValue.takeIf { it.isNotEmpty() }
                4 -> deliveredStones = cell.numericCellValue.toInt()
                5 -> placedStones = cell.numericCellValue.toInt()
                6 -> skyscraperHeight = cell.numericCellValue.toInt()
                7 -> autoReposition = cell.booleanCellValue
                8 -> autoNavigated = cell.booleanCellValue
                9 -> autoDeliveredSkystones = cell.numericCellValue.toInt()
                10 -> autoDeliveredStones = cell.numericCellValue.toInt()
                11 -> autoPlacedStones = cell.numericCellValue.toInt()
                12 -> endFoundationMoved = cell.booleanCellValue
                13 -> endParked = cell.booleanCellValue
                14 -> endCapLevel = cell.numericCellValue.toInt()
            }
        }

        check(id > 0) { "Invalid Team id on row ${row.rowNum}" }

        val autonomousData = AutonomousData(
            autoReposition,
            autoNavigated,
            autoDeliveredSkystones,
            autoDeliveredStones,
            autoPlacedStones
        )
        val teleOpData = TeleOpData(deliveredStones, placedStones, skyscraperHeight)
        val endGameData = EndGameData(endFoundationMoved, endParked, endCapLevel)

        val preferredZone = when (preferredZoneString?.toLowerCase(Locale.ROOT)) {
            "crater" -> PreferredZone.LOADING
            "depot" -> PreferredZone.BUILDING
            else -> PreferredZone.NONE
        }

        return Team(
            "",
            "",
            id,
            name,
            autonomousData.takeIf { it.isNotEmpty },
            teleOpData.takeIf { it.isNotEmpty },
            endGameData.takeIf { it.isNotEmpty },
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
        workbook.getSheet(SpreadsheetFields.TEAMS_SHEET)?.let {
            return extractTeams(it)
        }

        return emptyList()
    }

    fun getMatches(): List<Match> {
        workbook.getSheet(SpreadsheetFields.MATCHES_SHEET)?.let {
            return extractMatches(it)
        }

        return emptyList()
    }
}
