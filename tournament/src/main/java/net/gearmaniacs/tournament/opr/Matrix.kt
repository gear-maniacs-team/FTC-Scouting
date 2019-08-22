package net.gearmaniacs.tournament.opr

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.abs

internal object Matrix {

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

    private fun determinant(matrix: Array<DoubleArray>): Double {
        val size = matrix.size
        var determinant = 1.0

        for (i in matrix.indices) {
            var pivotElement = matrix[i][i]
            var pivotRow = i

            for (row in i + 1 until size) {
                if (abs(matrix[row][i]) > abs(pivotElement)) {
                    pivotElement = matrix[row][i]
                    pivotRow = row
                }
            }

            if (pivotElement == 0.0)
                return 0.0

            if (pivotRow != i) {
                val temp = matrix[i]
                matrix[i] = matrix[pivotRow]
                matrix[pivotRow] = temp

                determinant *= -1.0
            }
            determinant *= pivotElement

            for (row in i + 1 until size)
                for (col in i + 1 until size)
                    matrix[row][col] -= matrix[row][i] * matrix[i][col] / pivotElement
        }

        return determinant
    }

    suspend fun invert(matrix: Array<DoubleArray>): Array<DoubleArray> = coroutineScope {
        val size = matrix.size
        val inverse = Array(size) { DoubleArray(size) }

        val firstHalf = launch {
            for (i in 0 until size / 2)
                minorAndCofactors(matrix, i, inverse)
        }

        for (i in matrix.size / 2 until size)
            minorAndCofactors(matrix, i, inverse)

        firstHalf.join()

        // adjugate and determinant
        val det = 1.0 / determinant(matrix)
        for (i in inverse.indices) {
            for (j in 0..i) {
                val temp = inverse[i][j]
                inverse[i][j] = inverse[j][i] * det
                inverse[j][i] = temp * det
            }
        }

        inverse
    }

    private fun minorAndCofactors(matrix: Array<DoubleArray>, index: Int, inverse: Array<DoubleArray>) {
        val minorCacheResult = Array(matrix.size - 1) { DoubleArray(matrix.size - 1) }

        for (j in matrix[index].indices) {
            val determinant = determinant(minor(matrix, index, j, minorCacheResult))
            inverse[index][j] = (-1.0).pow((index + j).toDouble()) * determinant
        }
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