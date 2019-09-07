package net.gearmaniacs.tournament.spreadsheet

import net.gearmaniacs.core.extensions.justTry
import net.gearmaniacs.core.model.Alliance
import net.gearmaniacs.core.model.AutonomousData
import net.gearmaniacs.core.model.EndGame
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.core.model.PreferredLocation
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.model.TeleOpData
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import java.io.File
import java.util.Locale

class ImportFromSpreadsheet(spreadsheetFile: File) {

    private val workbook = readSpreadsheet(spreadsheetFile)

    private fun readSpreadsheet(spreadsheetFile: File): Workbook {
        return HSSFWorkbook(spreadsheetFile.inputStream())
    }

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
        var depotMinerals = 0
        var landerMinerals = 0
        var endGameString: String? = null
        var preferredLocationString: String? = null
        var notes: String? = null

        var autoLatching = false
        var autoSampling = false
        var autoMarker = false
        var autoParking = false
        var autoMinerals = 0

        row.forEachIndexed { cellIndex, cell ->
            when (cellIndex) {
                0 -> id = cell.numericCellValue.toInt()
                1 -> name = cell.stringCellValue
                2 -> depotMinerals = cell.numericCellValue.toInt()
                3 -> landerMinerals = cell.numericCellValue.toInt()
                4 -> endGameString = cell.stringCellValue
                5 -> preferredLocationString = cell.stringCellValue
                6 -> notes = cell.stringCellValue
                7 -> autoLatching = cell.booleanCellValue
                8 -> autoSampling = cell.booleanCellValue
                9 -> autoMarker = cell.booleanCellValue
                10 -> autoParking = cell.booleanCellValue
                11 -> autoMinerals = cell.numericCellValue.toInt()
            }
        }

        check(id == -1) { "Team id == -1" }

        val autonomousData =
            AutonomousData(autoLatching, autoSampling, autoMarker, autoParking, autoMinerals)
        val teleOpData = TeleOpData(depotMinerals, landerMinerals)

        val endGame = when (endGameString?.toLowerCase(Locale.ROOT)) {
            "robot latched" -> EndGame.ROBOT_LATCHED
            "partially parked" -> EndGame.PARTIALLY_PARKED
            "completely parked" -> EndGame.COMPLETELY_PARKED
            else -> 0
        }

        val preferredLocation = when (preferredLocationString?.toLowerCase(Locale.ROOT)) {
            "crater" -> PreferredLocation.CRATER
            "depot" -> PreferredLocation.DEPOT
            else -> PreferredLocation.NONE
        }

        return Team(
            id,
            name,
            autonomousData.takeIf { it.isNotEmpty },
            teleOpData.takeIf { it.isNotEmpty },
            endGame,
            preferredLocation,
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

        check(number == -1) { "Match number == -1" }

        return Match(
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
