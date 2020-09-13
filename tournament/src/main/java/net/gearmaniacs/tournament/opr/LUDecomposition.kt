package net.gearmaniacs.tournament.opr

import kotlin.math.abs

internal class LUDecomposition(matrix: Matrix) {

    private val mat = matrix.getArrayCopy()
    private val rows = matrix.rows
    private val columns = matrix.columns
    private val piv = IntArray(rows) { it }

    init {
        var pivsign = 1
        val array = DoubleArray(rows)

        for (i in 0 until columns) {
            for (j in 0 until rows)
                array[j] = mat[j][i]

            for (j in 0 until rows) {
                val row = mat[j]
                val min = j.coerceAtMost(i)
                var n = 0.0

                for (l in 0 until min)
                    n += row[l] * array[l]

                row[i] = n.let { array[j] -= it; array[j] }
            }

            var n = i
            for (j in i + 1 until rows) {
                if (abs(array[j]) > abs(array[n]))
                    n = j
            }

            if (n != i) {
                for (k in 0 until columns) {
                    val tmp = mat[n][k]
                    mat[n][k] = mat[i][k]
                    mat[i][k] = tmp
                }

                val tmp = piv[n]
                piv[n] = piv[i]
                piv[i] = tmp
                pivsign = -pivsign
            }

            if (i < rows && mat[i][i] != 0.0) {
                for (j in i + 1 until rows)
                    mat[j][i] /= mat[i][i]
            }
        }
    }

    private fun isNonSingular(): Boolean {
        for (i in 0 until columns)
            if (mat[i][i] == 0.0)
                return false
        return true
    }

    fun solve(matrix: Matrix): Matrix {
        require(matrix.rows == rows) { "Matrix row dimensions must agree." }
        require(isNonSingular())

        val columnDimension = matrix.columns
        val resultMatrix = matrix.getMatrix(piv, 0, columnDimension - 1)
        val array = resultMatrix.getArray()

        for (i in 0 until columns)
            for (j in i + 1 until columns)
                for (k in 0 until columnDimension)
                    array[j][k] -= array[i][k] * mat[j][i]

        for (i in columns - 1 downTo 0) {
            for (j in 0 until columnDimension)
                array[i][j] /= mat[i][i]

            for (j in 0 until i) {
                for (k in 0 until columnDimension)
                    array[j][k] -= array[i][k] * mat[j][i]
            }
        }

        return resultMatrix
    }
}