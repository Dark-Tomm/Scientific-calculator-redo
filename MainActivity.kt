package com.example.scientificcalculator

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvDisplay: TextView
    private lateinit var tvExpression: TextView

    private var currentInput = StringBuilder()
    private var expression = StringBuilder()
    private var lastResult = ""
    private var isNewCalculation = false
    private var isDegreeMode = true  // true = DEG, false = RAD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvDisplay = findViewById(R.id.tvDisplay)
        tvExpression = findViewById(R.id.tvExpression)

        setupButtons()
        updateDisplay("0")
    }

    private fun setupButtons() {
        // Number buttons
        val numberIds = mapOf(
            R.id.btn0 to "0", R.id.btn1 to "1", R.id.btn2 to "2",
            R.id.btn3 to "3", R.id.btn4 to "4", R.id.btn5 to "5",
            R.id.btn6 to "6", R.id.btn7 to "7", R.id.btn8 to "8",
            R.id.btn9 to "9"
        )
        numberIds.forEach { (id, value) ->
            findViewById<Button>(id).setOnClickListener { onNumberClick(value) }
        }

        // Operator buttons
        findViewById<Button>(R.id.btnAdd).setOnClickListener { onOperatorClick("+") }
        findViewById<Button>(R.id.btnSubtract).setOnClickListener { onOperatorClick("-") }
        findViewById<Button>(R.id.btnMultiply).setOnClickListener { onOperatorClick("×") }
        findViewById<Button>(R.id.btnDivide).setOnClickListener { onOperatorClick("÷") }

        // Decimal
        findViewById<Button>(R.id.btnDecimal).setOnClickListener { onDecimalClick() }

        // Equals
        findViewById<Button>(R.id.btnEquals).setOnClickListener { onEqualsClick() }

        // Clear / Backspace
        findViewById<Button>(R.id.btnClear).setOnClickListener { onClearClick() }
        findViewById<Button>(R.id.btnBackspace).setOnClickListener { onBackspaceClick() }

        // Sign toggle
        findViewById<Button>(R.id.btnToggleSign).setOnClickListener { onToggleSign() }

        // Parentheses
        findViewById<Button>(R.id.btnOpenParen).setOnClickListener { onParenClick("(") }
        findViewById<Button>(R.id.btnCloseParen).setOnClickListener { onParenClick(")") }

        // Scientific functions
        findViewById<Button>(R.id.btnSin).setOnClickListener { onFunctionClick("sin") }
        findViewById<Button>(R.id.btnCos).setOnClickListener { onFunctionClick("cos") }
        findViewById<Button>(R.id.btnTan).setOnClickListener { onFunctionClick("tan") }
        findViewById<Button>(R.id.btnLog).setOnClickListener { onFunctionClick("log") }
        findViewById<Button>(R.id.btnLn).setOnClickListener { onFunctionClick("ln") }
        findViewById<Button>(R.id.btnSqrt).setOnClickListener { onFunctionClick("√") }
        findViewById<Button>(R.id.btnSquare).setOnClickListener { onSuffixFunctionClick("²") }
        findViewById<Button>(R.id.btnPower).setOnClickListener { onOperatorClick("^") }
        findViewById<Button>(R.id.btnFactorial).setOnClickListener { onSuffixFunctionClick("!") }
        findViewById<Button>(R.id.btnPercent).setOnClickListener { onSuffixFunctionClick("%") }
        findViewById<Button>(R.id.btnPi).setOnClickListener { onConstantClick("π") }
        findViewById<Button>(R.id.btnE).setOnClickListener { onConstantClick("e") }
        findViewById<Button>(R.id.btn1DivX).setOnClickListener { onFunctionClick("1/") }

        // DEG / RAD toggle
        findViewById<Button>(R.id.btnDegRad).setOnClickListener { toggleDegRad() }
    }

    // ─── Input handlers ────────────────────────────────────────────────────────

    private fun onNumberClick(value: String) {
        if (isNewCalculation) {
            currentInput.clear()
            expression.clear()
            isNewCalculation = false
        }
        if (currentInput.toString() == "0") currentInput.clear()
        currentInput.append(value)
        expression.append(value)
        updateDisplay(currentInput.toString())
        updateExpression(expression.toString())
    }

    private fun onOperatorClick(op: String) {
        isNewCalculation = false
        if (currentInput.isNotEmpty()) {
            expression.append(" $op ")
            currentInput.clear()
            updateExpression(expression.toString())
            updateDisplay("0")
        } else if (expression.isNotEmpty()) {
            // Replace last operator
            val trimmed = expression.trimEnd()
            val lastChar = trimmed.lastOrNull()
            if (lastChar in listOf('+', '-', '×', '÷', '^')) {
                expression.clear()
                expression.append(trimmed.dropLast(1).trimEnd())
                expression.append(" $op ")
                updateExpression(expression.toString())
            }
        }
    }

    private fun onDecimalClick() {
        if (isNewCalculation) { currentInput.clear(); expression.clear(); isNewCalculation = false }
        if (!currentInput.contains('.')) {
            if (currentInput.isEmpty()) { currentInput.append("0"); expression.append("0") }
            currentInput.append('.')
            expression.append('.')
            updateDisplay(currentInput.toString())
            updateExpression(expression.toString())
        }
    }

    private fun onFunctionClick(func: String) {
        isNewCalculation = false
        val token = "$func("
        expression.append(token)
        currentInput.clear()
        updateExpression(expression.toString())
        updateDisplay("0")
    }

    private fun onSuffixFunctionClick(suffix: String) {
        if (currentInput.isNotEmpty()) {
            expression.append(suffix)
            currentInput.append(suffix)
            updateDisplay(currentInput.toString())
            updateExpression(expression.toString())
        }
    }

    private fun onConstantClick(constant: String) {
        if (isNewCalculation) { currentInput.clear(); expression.clear(); isNewCalculation = false }
        val value = if (constant == "π") Math.PI.toString() else Math.E.toString()
        currentInput.clear()
        currentInput.append(constant)
        expression.append(constant)
        updateDisplay(constant)
        updateExpression(expression.toString())
    }

    private fun onParenClick(paren: String) {
        isNewCalculation = false
        expression.append(paren)
        if (paren == "(") currentInput.clear()
        updateExpression(expression.toString())
    }

    private fun onToggleSign() {
        if (currentInput.isNotEmpty() && currentInput.toString() != "0") {
            val current = currentInput.toString().trimEnd('²', '!', '%')
            val negated = if (current.startsWith("-")) current.drop(1) else "-$current"
            // Rebuild expression tail
            val exprStr = expression.toString()
            val idx = exprStr.lastIndexOf(current)
            if (idx >= 0) {
                expression.replace(idx, idx + current.length, negated)
            }
            currentInput.clear()
            currentInput.append(negated)
            updateDisplay(currentInput.toString())
            updateExpression(expression.toString())
        }
    }

    private fun onClearClick() {
        currentInput.clear()
        expression.clear()
        lastResult = ""
        isNewCalculation = false
        updateDisplay("0")
        updateExpression("")
    }

    private fun onBackspaceClick() {
        if (isNewCalculation) return
        if (currentInput.isNotEmpty()) {
            currentInput.deleteCharAt(currentInput.length - 1)
            if (expression.isNotEmpty()) expression.deleteCharAt(expression.length - 1)
        } else if (expression.isNotEmpty()) {
            // Handle cases where expression has content but currentInput is empty (like after a function or operator)
            val exprStr = expression.toString()
            if (exprStr.endsWith(" ")) {
                // Operator case: " + " -> remove 3 chars
                expression.delete(expression.length - 3, expression.length)
            } else {
                expression.deleteCharAt(expression.length - 1)
            }
        }
        updateDisplay(if (currentInput.isEmpty()) "0" else currentInput.toString())
        updateExpression(expression.toString())
    }

    private fun toggleDegRad() {
        isDegreeMode = !isDegreeMode
        val btn = findViewById<Button>(R.id.btnDegRad)
        btn.text = if (isDegreeMode) "DEG" else "RAD"
    }

    // ─── Evaluation ────────────────────────────────────────────────────────────

    private fun onEqualsClick() {
        val expr = expression.toString().trim()
        if (expr.isEmpty()) return
        try {
            val result = evaluate(expr)
            val formatted = formatResult(result)
            updateDisplay(formatted)
            updateExpression("$expr =")
            lastResult = formatted
            currentInput.clear()
            currentInput.append(formatted)
            expression.clear()
            expression.append(formatted)
            isNewCalculation = true
        } catch (e: Exception) {
            updateDisplay("Error")
            updateExpression(expr)
            isNewCalculation = true
        }
    }

    private fun formatResult(value: Double): String {
        return if (value == value.toLong().toDouble() && !value.isInfinite()) {
            value.toLong().toString()
        } else {
            "%.10f".format(value).trimEnd('0').trimEnd('.')
        }
    }

    // ─── Expression evaluator ──────────────────────────────────────────────────

    private fun evaluate(expr: String): Double {
        val cleaned = expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-") // Handle Unicode minus
        return Parser(cleaned, isDegreeMode).parse()
    }

    // ─── Recursive-descent parser ──────────────────────────────────────────────

    inner class Parser(private val input: String, private val degMode: Boolean) {
        private var pos = 0

        fun parse(): Double {
            val result = parseExpr()
            skipSpaces()
            if (pos < input.length) throw IllegalArgumentException("Unexpected char at $pos")
            return result
        }

        private fun parseExpr(): Double {
            var left = parseTerm()
            while (pos < input.length) {
                skipSpaces()
                when {
                    consume('+') -> left += parseTerm()
                    consume('-') -> left -= parseTerm()
                    else -> break
                }
            }
            return left
        }

        private fun parseTerm(): Double {
            var left = parsePower()
            while (pos < input.length) {
                skipSpaces()
                when {
                    consume('*') -> left *= parsePower()
                    consume('/') -> {
                        val right = parsePower()
                        if (right == 0.0) throw ArithmeticException("Division by zero")
                        left /= right
                    }
                    else -> break
                }
            }
            return left
        }

        private fun parsePower(): Double {
            var base = parseUnary()
            skipSpaces()
            if (consume('^')) {
                val exp = parseUnary()
                base = base.pow(exp)
            }
            return base
        }

        private fun parseUnary(): Double {
            skipSpaces()
            if (consume('-')) return -parsePostfix()
            consume('+')
            return parsePostfix()
        }

        private fun parsePostfix(): Double {
            var value = parsePrimary()
            while (pos < input.length) {
                skipSpaces()
                when {
                    consume('²') -> value = value.pow(2.0)
                    consume('!') -> value = factorial(value)
                    consume('%') -> value /= 100.0
                    else -> break
                }
            }
            return value
        }

        private fun parsePrimary(): Double {
            skipSpaces()
            // Parenthesized expression
            if (consume('(')) {
                val value = parseExpr()
                skipSpaces()
                consume(')')
                return value
            }
            // Named functions / constants
            val funcName = readIdentifier()
            if (funcName.isNotEmpty()) {
                skipSpaces()
                return when (funcName) {
                    "sin" -> { val arg = parseParen(); applyTrig(::sin, arg) }
                    "cos" -> { val arg = parseParen(); applyTrig(::cos, arg) }
                    "tan" -> { val arg = parseParen(); applyTrig(::tan, arg) }
                    "log" -> { val arg = parseParen(); log10(arg) }
                    "ln"  -> { val arg = parseParen(); ln(arg) }
                    "sqrt", "√" -> { val arg = parseParen(); sqrt(arg) }
                    "1/x" -> { val arg = parseParen(); 1.0 / arg }
                    "π" -> Math.PI
                    "e" -> Math.E
                    else  -> throw IllegalArgumentException("Unknown function: $funcName")
                }
            }
            // √ without parentheses (suffix style handled elsewhere)
            if (consume('√')) {
                skipSpaces()
                return sqrt(parsePrimary())
            }
            // Number literal
            return readNumber()
        }

        private fun parseParen(): Double {
            skipSpaces()
            if (!consume('(')) throw IllegalArgumentException("Expected '('")
            val value = parseExpr()
            skipSpaces()
            consume(')')
            return value
        }

        private fun applyTrig(fn: (Double) -> Double, arg: Double): Double {
            val radians = if (degMode) Math.toRadians(arg) else arg
            return fn(radians)
        }

        private fun readNumber(): Double {
            skipSpaces()
            val start = pos
            if (pos < input.length && input[pos] == '-') pos++
            while (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) pos++
            if (pos == start) throw IllegalArgumentException("Expected number at $pos")
            return input.substring(start, pos).toDouble()
        }

        private fun readIdentifier(): String {
            skipSpaces()
            val start = pos
            while (pos < input.length && (input[pos].isLetter() || input[pos] == '_' || input[pos] == '/' || input[pos] == 'π')) pos++
            return input.substring(start, pos)
        }

        private fun skipSpaces() { while (pos < input.length && input[pos] == ' ') pos++ }

        private fun consume(ch: Char): Boolean {
            if (pos < input.length && input[pos] == ch) { pos++; return true }
            return false
        }
    }

    private fun factorial(n: Double): Double {
        if (n < 0 || n != kotlin.math.floor(n)) throw ArithmeticException("Invalid factorial")
        var result = 1.0
        for (i in 2..n.toInt()) result *= i
        return result
    }

    // ─── Display helpers ───────────────────────────────────────────────────────

    private fun updateDisplay(text: String) { tvDisplay.text = text }
    private fun updateExpression(text: String) { tvExpression.text = text }
}
