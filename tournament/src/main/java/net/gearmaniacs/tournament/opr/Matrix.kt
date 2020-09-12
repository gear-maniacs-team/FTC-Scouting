package net.gearmaniacs.tournament.opr

class Matrix(val rows: Int, val columns: Int) {

    private val mat = Array(rows) { DoubleArray(columns) }

    fun getArrayCopy() = mat.clone()

    fun getArray() = mat

    operator fun get(row: Int, column: Int): Double {
        require(row in 0 until rows) { "Invalid row index: $row (Rows Count: $rows)" }
        require(column in 0 until columns) { "Invalid column index: $column (Rows Count: $columns)" }
        return mat[row][column]
    }

    operator fun set(row: Int, column: Int, value: Double) {
        require(row in 0 until rows) { "Invalid row index: $row (Rows Count: $rows)" }
        require(column in 0 until columns) { "Invalid column index: $column (Rows Count: $columns)" }
        mat[row][column] = value
    }

    operator fun plus(matrix: Matrix): Matrix {
        require(rows == matrix.rows)
        require(columns == matrix.columns)
        val resultMatrix = Matrix(rows, columns)

        for (row in 0 until rows)
            for (column in 0 until columns)
                resultMatrix.mat[row][column] = mat[row][column] + matrix.mat[row][column]

        return resultMatrix
    }

    operator fun times(vector: DoubleArray): DoubleArray {
        val result = DoubleArray(rows)

        for (row in 0 until rows) {
            var sum = 0.0

            for (column in 0 until columns)
                sum += mat[row][column] * vector[column]

            result[row] = sum
        }

        return result
    }

    operator fun times(matrix: Matrix): Matrix {
        require(matrix.rows == columns) { "matrix.rows (${matrix.rows}) != this.columns ($columns)" }

        val resultMatrix = Matrix(rows, matrix.columns)
        val columnsArray = DoubleArray(columns)

        for (i in 0 until matrix.columns) {
            for (j in 0 until columns)
                columnsArray[j] = matrix.mat[j][i]

            for (k in 0 until rows) {
                var rowsSum = 0.0
                for (l in 0 until columns)
                    rowsSum += mat[k][l] * columnsArray[l]

                resultMatrix.mat[k][i] = rowsSum
            }
        }

        return resultMatrix
    }

    operator fun times(n: Double): Matrix {
        val matrix = Matrix(rows, columns)

        for (row in 0 until rows)
            for (column in 0 until columns)
                matrix.mat[row][column] = n * mat[row][column]

        return matrix
    }

    fun getMatrix(array: IntArray, n: Int, n2: Int): Matrix {
        val matrix = Matrix(array.size, n2 - n + 1)

        for (i in array.indices)
            for (j in n..n2)
                matrix[i, j - n] = this[array[i], j]

        return matrix
    }

    fun setMatrix(rowStart: Int, rowEnd: Int, columnStart: Int, columnEnd: Int, matrix: Matrix) {
        for (i in rowStart..rowEnd) {
            for (j in columnStart..columnEnd) {
                this[i, j] = matrix[i - rowStart, j - columnStart]
            }
        }
    }

    fun setRows(rowStart: Int, rowEnd: Int, column: Int, array: DoubleArray) {
        for (i in rowStart..rowEnd)
            this[i, column] = array[i - rowStart]
    }

    fun transpose(): Matrix {
        val matrix = Matrix(columns, rows)

        for (i in 0 until rows)
            for (j in 0 until columns)
                matrix.mat[j][i] = mat[i][j]

        return matrix
    }

    fun inverse(): Matrix {
        require(rows == columns)
        return LUDecomposition(this).solve(identity(rows))
    }

    companion object {
        fun identity(size: Int): Matrix {
            val matrix = Matrix(size, size)

            for (i in 0 until size)
                matrix[i, i] = 1.0

            return matrix
        }
    }
}
