package net.gearmaniacs.tournament.spreadsheet

import net.gearmaniacs.core.model.AutonomousData
import net.gearmaniacs.core.model.EndGame
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.core.model.PreferredLocation
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.model.TeamPower
import net.gearmaniacs.core.model.TeleOpData
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.File
import java.io.IOException

class ExportToSpreadsheet {

    private val workBook = HSSFWorkbook()

    private fun exportTeams(teamList: List<Team>) {
        val sheet = workBook.createSheet(SpreadsheetFields.TEAMS_SHEET)

        val headerRow = sheet.createRow(0)

        SpreadsheetFields.TEAM_COLUMNS.forEachIndexed { index, field ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(field)
        }

        teamList.forEachIndexed { index, team ->
            val row = sheet.createRow(index + 1)
            var column = 0

            row.createCell(column++).setCellValue(team.id.toDouble())
            row.createCell(column++).setCellValue(team.name)

            val teleOp = team.teleOpData ?: TeleOpData()
            row.createCell(column++).setCellValue(teleOp.depotMinerals.toDouble())
            row.createCell(column++).setCellValue(teleOp.landerMinerals.toDouble())

            val endGameString = when (team.endGame) {
                EndGame.ROBOT_LATCHED -> "Robot Latched"
                EndGame.PARTIALLY_PARKED -> "Partially Parked"
                EndGame.COMPLETELY_PARKED -> "Completely Parked"
                else -> "None"
            }
            row.createCell(column++).setCellValue(endGameString)

            val preferredLocation = when (team.preferredLocation) {
                PreferredLocation.CRATER -> "Crater"
                PreferredLocation.DEPOT -> "Depot"
                else -> "None"
            }
            row.createCell(column++).setCellValue(preferredLocation)
            row.createCell(column++).setCellValue(team.comments.orEmpty())

            // Autonomous
            val autonomous = team.autonomousData ?: AutonomousData()
            row.createCell(column++).setCellValue(autonomous.latching)
            row.createCell(column++).setCellValue(autonomous.sampling)
            row.createCell(column++).setCellValue(autonomous.marker)
            row.createCell(column++).setCellValue(autonomous.parking)
            row.createCell(column).setCellValue(autonomous.minerals.toDouble())
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
    fun saveToFile(file: File) {
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
