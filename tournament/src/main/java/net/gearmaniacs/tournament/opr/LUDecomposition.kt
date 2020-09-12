package net.gearmaniacs.tournament.opr

import kotlin.math.abs

class LUDecomposition(matrix: Matrix) {

    private val LU = matrix.getArrayCopy()
    private val rows = matrix.rows
    private val columns = matrix.columns
    private val piv = IntArray(rows) { it }
    private val isNonSingular: Boolean
        get() {
            for (i in 0 until columns) if (LU[i][i] == 0.0) return false
            return true
        }

    init {
        var pivsign = 1
        val array = DoubleArray(rows)

        for (i in 0 until columns) {
            for (j in 0 until rows)
                array[j] = LU[j][i]

            for (j in 0 until rows) {
                val array2 = LU[j]
                val min = j.coerceAtMost(i)
                var n = 0.0
                for (n2 in 0 until min) {
                    n += array2[n2] * array[n2]
                }
                array2[i] = n.let { array[j] -= it; array[j] }
            }

            var n = i
            for (j in i + 1 until rows) {
                if (abs(array[j]) > abs(array[n]))
                    n = j
            }

            if (n != i) {
                for (k in 0 until columns) {
                    val tmp = LU[n][k]
                    LU[n][k] = LU[i][k]
                    LU[i][k] = tmp
                }

                val tmp = piv[n]
                piv[n] = piv[i]
                piv[i] = tmp
                pivsign = -pivsign
            }

            if (i < rows and LU[i][i].toInt()) {
                for (j in i + 1 until rows)
                    LU[j][i] /= LU[i][i]
            }
        }
    }

    fun solve(matrix: Matrix): Matrix {
        require(matrix.rows == rows) { "Matrix row dimensions must agree." }
        require(isNonSingular)

        val columnDimension = matrix.columns
        val resultMatrix = matrix.getMatrix(piv, 0, columnDimension - 1)
        val array = resultMatrix.getArray()

        for (i in 0 until columns)
            for (j in i + 1 until columns)
                for (k in 0 until columnDimension)
                    array[j][k] -= array[i][k] * LU[j][i]

        for (i in columns - 1 downTo 0) {
            for (j in 0 until columnDimension)
                array[i][j] /= LU[i][i]

            for (j in 0 until i) {
                for (k in 0 until columnDimension)
                    array[j][k] -= array[i][k] * LU[j][i]
            }
        }

        return resultMatrix
    }
}