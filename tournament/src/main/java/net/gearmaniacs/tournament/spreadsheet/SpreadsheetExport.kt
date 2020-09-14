package net.gearmaniacs.tournament.spreadsheet

import net.gearmaniacs.core.model.Match
import net.gearmaniacs.core.model.enums.PreferredZone
import net.gearmaniacs.core.model.team.AutonomousPeriod
import net.gearmaniacs.core.model.team.ControlledPeriod
import net.gearmaniacs.core.model.team.EndGamePeriod
import net.gearmaniacs.core.model.team.RankedTeam
import net.gearmaniacs.core.model.team.Team
import org.apache.poi.hssf.usermodel.HSSFWorkbook
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

            row.createCell(column++).setCellValue(team.number.toDouble())
            row.createCell(column++).setCellValue(team.name)

            val preferredZone = when (team.preferredZone) {
                PreferredZone.LOADING -> "Loading"
                PreferredZone.BUILDING -> "Building"
                else -> "None"
            }
            row.createCell(column++).setCellValue(preferredZone)
            row.createCell(column++).setCellValue(team.notes.orEmpty())

            // Driver Controlled
            val controlled = team.controlledPeriod ?: ControlledPeriod()
            row.createCell(column++).setCellValue(controlled.lowGoal.toDouble())
            row.createCell(column++).setCellValue(controlled.midGoal.toDouble())
            row.createCell(column++).setCellValue(controlled.highGoal.toDouble())

            // Autonomous
            val autonomous = team.autonomousPeriod ?: AutonomousPeriod()
            row.createCell(column++).setCellValue(autonomous.wobbleDelivery)
            row.createCell(column++).setCellValue(autonomous.lowGoal.toDouble())
            row.createCell(column++).setCellValue(autonomous.midGoal.toDouble())
            row.createCell(column++).setCellValue(autonomous.highGoal.toDouble())
            row.createCell(column++).setCellValue(autonomous.parked)

            // End Game
            val endGame = team.endGamePeriod ?: EndGamePeriod()
            row.createCell(column++).setCellValue(endGame.powerShot.toDouble())
            row.createCell(column++).setCellValue(endGame.wobbleRings.toDouble())
            // TODO: Wobble Zone

            // Predicted Score
            row.createCell(column).setCellValue(team.score().toDouble())
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

    private fun exportOpr(powerList: List<RankedTeam>) {
        val sheet = workBook.createSheet(SpreadsheetFields.OPR_SHEET)

        val headerRow = sheet.createRow(0)

        SpreadsheetFields.OPR_COLUMNS.forEachIndexed { index, field ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(field)
        }

        powerList.forEachIndexed { index, teamPower ->
            val row = sheet.createRow(index + 1)

            row.createCell(0).setCellValue(teamPower.number.toDouble())
            row.createCell(1).setCellValue(teamPower.name)
            row.createCell(2).setCellValue(teamPower.score)
        }
    }

    @Throws(IOException::class)
    fun writeToStream(outputStream: OutputStream) {
        outputStream.use {
            workBook.write(it)
        }
    }

    fun export(teamList: List<Team>, matchList: List<Match>, powerList: List<RankedTeam>) {
        exportTeams(teamList)
        exportMatches(matchList)
        exportOpr(powerList)
    }
}
