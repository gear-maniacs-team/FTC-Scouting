package net.gearmaniacs.tournament.opr

import kotlin.math.abs

internal class DeterminantCalculator(private val matrix: Array<DoubleArray>) {

    private var sign = 1

    private fun isUpperTriangular(): Boolean {
        if (matrix.size < 2)
            return false

        for (i in matrix.indices)
            for (j in 0 until i)
                if (matrix[i][j] != 0.0)
                    return false

        return true
    }

    private fun isLowerTriangular(): Boolean {
        if (matrix.size < 2)
            return false

        for (j in matrix.indices) {
            var i = 0
            while (j > i) {
                if (matrix[i][j] != 0.0)
                    return false
                i++
            }
        }
        return true
    }

    fun determinant(): Double {
        if (!isUpperTriangular() && !isLowerTriangular())
            makeTriangular()

        return multiplyDiameter() * sign.toDouble()
    }

    /* Receives a matrix and make it triangular using allowed operations
     * on columns and rows
     * Returns {sign}
     */
    private fun makeTriangular() {
        for (j in matrix.indices) {
            sortColumn(j)

            for (i in matrix.size - 1 downTo j + 1) {
                if (matrix[i][j] == 0.0)
                    continue

                val x = matrix[i][j]
                val y = matrix[i - 1][j]
                multiplyRow(i, -y / x)
                addRow(i, i - 1)
                multiplyRow(i, -x / y)
            }
        }
    }

    private fun multiplyDiameter(): Double {
        var result = 1.0

        for (i in matrix.indices)
            for (j in matrix.indices)
                if (i == j)
                    result *= matrix[i][j]

        return result
    }

    // Add row1 to row2 and stores int row1
    private fun addRow(row1: Int, row2: Int) {
        for (j in matrix.indices)
            matrix[row1][j] += matrix[row2][j]
    }

    // Multiply the whole row by num
    private fun multiplyRow(row: Int, num: Double) {
        if (num < 0)
            sign *= -1

        for (j in matrix.indices)
            matrix[row][j] *= num
    }

    // Sort the cols from the biggest to the lowest value
    private fun sortColumn(col: Int) {
        for (i in matrix.size - 1 downTo col) {
            for (k in matrix.size - 1 downTo col) {
                val tmp1 = matrix[i][col]
                val tmp2 = matrix[k][col]

                if (abs(tmp1) < abs(tmp2))
                    replaceRow(i, k)
            }
        }
    }

    // Replace row1 with row2
    private fun replaceRow(row1: Int, row2: Int) {
        if (row1 != row2)
            sign *= -1

        val temp = matrix[row1]
        matrix[row1] = matrix[row2]
        matrix[row2] = temp
    }
}
