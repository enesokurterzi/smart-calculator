import java.math.BigInteger

const val IDENTIFIER = "([a-zA-Z]+)"
const val OPERAND = "($IDENTIFIER|(\\d+))"
const val VALID_OPERAND = "((^([+-]?$OPERAND))|$OPERAND)"
const val OPERATOR = "(([+-]+)|([*/^]))"

class Calculator {
    private val variables: MutableMap<String, BigInteger> = mutableMapOf()

    fun run() {
        var terminate = false
        readln().trim().let {
            try {
                if (it.trim().isEmpty()) {}
                else if (it.isCommand()) terminate = it.command()
                else if (it.isAssignment()) it.assignment()
                else it.expression()
            } catch (e: Exception) {
                println(e.message)
            }
        }

        if (!terminate) run()
    }

    private fun String.command(): Boolean {
        when (this) {
            "/help" -> println("The program adds and subtracts numbers")
            "/exit" -> println("Bye!").run { return true }
            else -> throw Exception("Unknown command")
        }

        return false
    }

    private fun String.assignment() {
        val terms = split("\\s*=\\s*".toRegex())
        if (!terms[0].isIdentifier()) throw Exception("Invalid identifier")
        else if (terms.size != 2 || !terms[1].isOperand()) throw Exception("Invalid assignment")
        else variables[terms[0]] = terms[1].getValue()
    }

    private fun String.expression() {
        if (isValidExpression()) {
            val postfix = buildExpression()
            val stack = ArrayDeque<BigInteger>()
            for (element in postfix) {
                if (element.isOperand()) stack.push(element.getValue())
                else stack.push(operation(element, stack.pop(), stack.pop()))
            }

            println(stack.pop())
        } else throw Exception("Invalid expression")
    }

    private fun String.buildExpression(): List<String> {
        val result = mutableListOf<String>()
        val stack = ArrayDeque<String>()
        val terms = Regex("$VALID_OPERAND|$OPERATOR|([()])")
            .findAll(this.replace("\\s+".toRegex(), ""))
            .map { it.value
                .replace("--", "+")
                .replace("[+]+".toRegex(), "+")
                .replace("+-", "-") }
        for (term in terms) {
            if (term.isOperand()) result.add(term)
            else if (term == "(") stack.push(term)
            else if (term == ")") {
                while (!(stack.isEmpty() || stack.peek() == "(")) {
                    result.add(stack.pop())
                }

                try {
                    stack.pop()
                } catch (e: Exception) {
                    throw Exception("Invalid expression")
                }
            } else {
                while (!(stack.isEmpty() || stack.peek() == "(" || term.hasHigherPrecedence(stack.peek()))) {
                    result.add(stack.pop())
                }

                stack.push(term)
            }
        }

        while (!stack.isEmpty()) {
            stack.pop().let {
                if (it == "(") throw Exception("Invalid expression")
                else result.add(it)
            }
        }

        return result
    }

    private fun String.hasHigherPrecedence(other: String): Boolean {
        val precedence = mapOf("(" to 1, "^" to 2, "*" to 3, "/" to 3, "+" to 4, "-" to 4)
        return precedence[this]!! < precedence[other]!!
    }

    private fun operation(op: String, x: BigInteger, y: BigInteger): BigInteger = when (op) {
        "+" -> y + x
        "-" -> y - x
        "*" -> y * x
        "/" -> y / x
        "^" -> y.pow(x.toInt())
        else -> throw Exception("Invalid expression")
    }

    private fun String.getValue(): BigInteger =
        variables.getOrElse(this) { this.toBigIntegerOrNull() ?: throw Exception("Unknown variable") }

    private fun String.isIdentifier(): Boolean = IDENTIFIER.toRegex().matches(this)
    private fun String.isOperand(): Boolean = VALID_OPERAND.toRegex().matches(this)
    private fun String.isAssignment(): Boolean = contains("=")
    private fun String.isCommand(): Boolean = startsWith("/")
    private fun String.isValidExpression(): Boolean = "$VALID_OPERAND(\\s*$OPERATOR\\s*$VALID_OPERAND)*".toRegex()
        .matches(this.replace("[()]".toRegex(), ""))

    private fun <T> ArrayDeque<T>.push(item: T) = this.addLast(item)
    private fun <T> ArrayDeque<T>.pop(): T = this.removeLast()
    private fun <T> ArrayDeque<T>.peek(): T = this[this.lastIndex]
}