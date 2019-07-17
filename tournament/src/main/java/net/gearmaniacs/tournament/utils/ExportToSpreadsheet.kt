package net.gearmaniacs.tournament.utils

import net.gearmaniacs.core.model.Match
import net.gearmaniacs.core.model.PreferredLocation
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.model.TeamPower
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.File
import java.io.IOException

class ExportToSpreadsheet {

    private companion object {
        private val teamFields = listOf("Number", "Name", "Preferred Location", "Comments")

        private val matchesFields = listOf("Number", "Red 1", "Red 2", "Red Score", "Blue 1", "Blue 2", "Blue Score")

        private val oprFields = listOf("Number", "Name", "Points")
    }

    private val workBook = HSSFWorkbook()

    private fun exportTeams(teamList: List<Team>) {
        val sheet = workBook.createSheet("Teams")

        val headerRow = sheet.createRow(0)

        teamFields.forEachIndexed { index, field ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(field)
        }

        teamList.forEachIndexed { index, team ->
            val row = sheet.createRow(index + 1)

            with(team) {
                row.createCell(0).setCellValue(id.toDouble())
                row.createCell(1).setCellValue(name)

                val preferredLocation = when (preferredLocation) {
                    PreferredLocation.CRATER -> "Crater"
                    PreferredLocation.DEPOT -> "Depot"
                    else -> "None"
                }
                row.createCell(2).setCellValue(preferredLocation)
                row.createCell(3).setCellValue(comments.orEmpty())
            }
        }
    }

    private fun exportMatches(matchesList: List<Match>) {
        val sheet = workBook.createSheet("Matches")

        val headerRow = sheet.createRow(0)

        matchesFields.forEachIndexed { index, field ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(field)
        }

        matchesList.forEachIndexed { index, match ->
            val row = sheet.createRow(index + 1)

            with(match) {
                row.createCell(0).setCellValue(id.toDouble())

                row.createCell(1).setCellValue(redAlliance.firstTeam.toDouble())
                row.createCell(2).setCellValue(redAlliance.secondTeam.toDouble())
                row.createCell(3).setCellValue(redAlliance.score.toDouble())

                row.createCell(4).setCellValue(blueAlliance.firstTeam.toDouble())
                row.createCell(5).setCellValue(blueAlliance.secondTeam.toDouble())
                row.createCell(6).setCellValue(blueAlliance.score.toDouble())
            }
        }
    }

    private fun exportOpr(powerList: List<TeamPower>) {
        val sheet = workBook.createSheet("OPR")

        val headerRow = sheet.createRow(0)

        oprFields.forEachIndexed { index, field ->
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
