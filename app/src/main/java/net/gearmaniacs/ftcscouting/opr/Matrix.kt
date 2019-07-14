package net.gearmaniacs.ftcscouting.opr

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.util.ArrayList
import kotlin.math.pow

object Matrix {

    fun multiply(matrix: Array<DoubleArray>, vector: DoubleArray): DoubleArray {
        val rows = matrix.size
        val columns = matrix[0].size

        val result = DoubleArray(rows)

        for (row in 0 until rows) {
            var sum = 0.0
            for (column in 0 until columns)
                sum += matrix[row][column] * vector[column]
            result[row] = sum
        }

        return result
    }

    suspend fun invert(matrix: Array<DoubleArray>): Array<DoubleArray> = coroutineScope {
        val inverse = Array(matrix.size) { DoubleArray(matrix.size) }

        // minors and cofactors
        val jobs = ArrayList<Job>(matrix.size)

        for (i in matrix.indices)
            jobs += launch(Dispatchers.Default) {
                val minorCacheResult = Array(matrix.size - 1) { DoubleArray(matrix.size - 1) }

                for (j in matrix[i].indices) {
                    val determinant = DeterminantCalculator(minor(matrix, i, j, minorCacheResult)).determinant()
                    inverse[i][j] = (-1.0).pow((i + j).toDouble()) * determinant
                }
            }

        jobs.joinAll()

        // adjugate and determinant
        val det = 1.0 / DeterminantCalculator(matrix).determinant()
        for (i in inverse.indices) {
            for (j in 0..i) {
                val temp = inverse[i][j]
                inverse[i][j] = inverse[j][i] * det
                inverse[j][i] = temp * det
            }
        }

        inverse
    }

    private fun minor(
        matrix: Array<DoubleArray>,
        row: Int,
        column: Int,
        result: Array<DoubleArray>
    ): Array<DoubleArray> {
        if (result.size != matrix.size - 1 || result[0].size != matrix.size - 1)
            return minor(matrix, row, column)

        for (i in matrix.indices) {
            if (i == row)
                continue

            for (j in matrix[i].indices) {
                if (j != column)
                    result[if (i < row) i else i - 1][if (j < column) j else j - 1] = matrix[i][j]
            }
        }

        return result
    }

    private fun minor(matrix: Array<DoubleArray>, row: Int, column: Int): Array<DoubleArray> =
        minor(matrix, row, column, Array(matrix.size - 1) { DoubleArray(matrix.size - 1) })
}