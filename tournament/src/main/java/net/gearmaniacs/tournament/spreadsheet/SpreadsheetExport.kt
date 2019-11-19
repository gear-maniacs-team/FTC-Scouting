package net.gearmaniacs.tournament.spreadsheet

import net.gearmaniacs.core.model.AutonomousData
import net.gearmaniacs.core.model.EndGameData
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.core.model.PreferredZone
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.model.TeamPower
import net.gearmaniacs.core.model.TeleOpData
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.File
import java.io.IOException
import java.io.OutputStream

internal class SpreadsheetExport {

    private val workBook = HSSFWorkbook()

    private fun exportTeams(teamList: List<Team>) {
        val sheet = workBook.createSheet(SpreadsheetFields.TEAMS_SHEET)

        val headerRow = sheet.createRow(0)

        // Create the titles for each column
        SpreadsheetFields.TEAM_COLUMNS.forEachIndexed { index, field ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(field)
        }

        // Write the data for each team
        teamList.forEachIndexed { index, team ->
            val row = sheet.createRow(index + 1)
            var column = 0

            row.createCell(column++).setCellValue(team.id.toDouble())
            row.createCell(column++).setCellValue(team.name)

            val preferredZone = when (team.preferredZone) {
                PreferredZone.LOADING -> "Loading"
                PreferredZone.BUILDING -> "Building"
                else -> "None"
            }
            row.createCell(column++).setCellValue(preferredZone)
            row.createCell(column++).setCellValue(team.notes.orEmpty())

            // TeleOp
            val teleOp = team.teleOpData ?: TeleOpData()
            row.createCell(column++).setCellValue(teleOp.deliveredStones.toDouble())
            row.createCell(column++).setCellValue(teleOp.placedStones.toDouble())

            // Autonomous
            val autonomous = team.autonomousData ?: AutonomousData()
            row.createCell(column++).setCellValue(autonomous.repositionFoundation)
            row.createCell(column++).setCellValue(autonomous.navigated)
            row.createCell(column++).setCellValue(autonomous.deliveredSkystones.toDouble())
            row.createCell(column++).setCellValue(autonomous.deliveredStones.toDouble())
            row.createCell(column++).setCellValue(autonomous.placedStones.toDouble())

            // End Game
            val endGame = team.endGameData ?: EndGameData()
            row.createCell(column++).setCellValue(endGame.moveFoundation)
            row.createCell(column++).setCellValue(endGame.parked)
            row.createCell(column++).setCellValue(endGame.capLevel.toDouble())

            // Predicted Score
            row.createCell(column).setCellValue(team.score.toDouble())
        }
    }

    private fun exportMatches(matchesList: List<Match>) {
        val sheet = workBook.createSheet(SpreadsheetFields.MATCHES_SHEET)

        val headerRow = sheet.createRow(0)

        SpreadsheetFields.MATCHES_COLUMNS.forEachIndexed { index, field ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(field)
        }

        matchesList.forEachIndexed { index, match ->
            val row = sheet.createRow(index + 1)
            var column = 0

            row.createCell(column++).setCellValue(match.id.toDouble())

            row.createCell(column++).setCellValue(match.redAlliance.firstTeam.toDouble())
            row.createCell(column++).setCellValue(match.redAlliance.secondTeam.toDouble())
            row.createCell(column++).setCellValue(match.redAlliance.score.toDouble())

            row.createCell(column++).setCellValue(match.blueAlliance.firstTeam.toDouble())
            row.createCell(column++).setCellValue(match.blueAlliance.secondTeam.toDouble())
            row.createCell(column).setCellValue(match.blueAlliance.score.toDouble())
        }
    }

    private fun exportOpr(powerList: List<TeamPower>) {
        val sheet = workBook.createSheet(SpreadsheetFields.OPR_SHEET)

        val headerRow = sheet.createRow(0)

        SpreadsheetFields.OPR_COLUMNS.forEachIndexed { index, field ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(field)
        }

        powerList.forEachIndexed { index, teamPower ->
            val row = sheet.createRow(index + 1)

            row.createCell(0).setCellValue(teamPower.id.toDouble())
            row.createCell(1).setCellValue(teamPower.name)
            row.createCell(2).setCellValue(teamPower.power.toDouble())
        }
    }

    @Throws(IOException::class)
    fun writeToStream(outputStream: OutputStream) {
        outputStream.use {
            workBook.write(it)
        }
    }

    @Throws(IOException::class)
    fun writeToFile(file: File) {
        file.parentFile?.mkdirs()

        file.outputStream().use {
            workBook.write(it)
        }
    }

    fun export(teamList: List<Team>, matchList: List<Match>, powerList: List<TeamPower>) {
        exportTeams(teamList)
        exportMatches(matchList)
        exportOpr(powerList)
    }
}
