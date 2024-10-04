package edu.tcu.hienminhdau.calculator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Stack

class MainActivity : AppCompatActivity() {
    // after operator flag
    private var afterOp: Boolean = false
    // zero entered flag
    private var zero: Boolean = false
    // dot flag
    private var hasDot:Boolean = false
    // input string
    private var input: String = ""
    // the text view instance
    private lateinit var result: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // binding all the buttons to their correct function
        result = findViewById(R.id.result_tv)
        findViewById<Button>(R.id.one_btn).setOnClickListener{ onDigit("1") }
        findViewById<Button>(R.id.two_btn).setOnClickListener{ onDigit("2") }
        findViewById<Button>(R.id.three_btn).setOnClickListener { onDigit("3") }
        findViewById<Button>(R.id.four_btn).setOnClickListener { onDigit("4") }
        findViewById<Button>(R.id.five_btn).setOnClickListener { onDigit("5") }
        findViewById<Button>(R.id.six_btn).setOnClickListener { onDigit("6") }
        findViewById<Button>(R.id.seven_btn).setOnClickListener { onDigit("7") }
        findViewById<Button>(R.id.eight_btn).setOnClickListener { onDigit("8") }
        findViewById<Button>(R.id.nine_btn).setOnClickListener { onDigit("9") }
        findViewById<Button>(R.id.zero_btn).setOnClickListener { onDigit("0") }
        findViewById<Button>(R.id.add_btn).setOnClickListener { onOperator("+") }
        findViewById<Button>(R.id.subtract_btn).setOnClickListener { onOperator("-") }
        findViewById<Button>(R.id.multiply_btn).setOnClickListener { onOperator("*") }
        findViewById<Button>(R.id.divide_btn).setOnClickListener { onOperator("/") }
        findViewById<Button>(R.id.dot_btn).setOnClickListener { onDot() }
        findViewById<Button>(R.id.equal_btn).setOnClickListener { onEqual() }
        findViewById<Button>(R.id.clear_btn).setOnClickListener { onClear() }
    }

    // when a digit is clicked
    private fun onDigit(digit: String){
        // if a digit is entered after an operator
        if(afterOp){
            zero = false
            hasDot = false
            afterOp = false
        }

        // if it is the first zero or leading zeros
        if(digit == "0" && (input.isEmpty() || zero)){
            return
        }else if(digit == "0"){ // if zero is entered
            zero = true
        }else if(zero && !hasDot){ // if not enter a zero, but already have a zero and it was not the start of a decimal number
            zero = false
            input = input.substring(0, input.length - 1)
        }

        // add the digit to the input string and render
        input += digit
        result.text = input
    }

    // if an operator is clicked
    private fun onOperator(op: String){
        // if not after an operator
        if(!afterOp){
            // if nothing has been entered
            input += if(input.isEmpty()){
                "0$op"
            }else{ // if something has already been entered
                op
            }
            // render the view
            result.text = input
            // set flags
            afterOp = true
            hasDot = false
        }
    }

    // if dot is clicked
    private fun onDot(){
        // if not already has a dot
        if(!hasDot){
            // set flags
            hasDot = true
            afterOp = false
            zero = false

            // if the input is either empty or the last entered was not a digit (a.k.a an operator)
            input += if(input.isEmpty() || !input.last().isDigit()){
                "0."
            }else{
                "."
            }

            // render
            result.text = input
        }
    }

    // if equal is clicked
    private fun onEqual(){
        // if just enter an operator
        if(afterOp){
            return
        }


        // two-stack expression evaluation
        val nums = Stack<BigDecimal>()
        val ops = Stack<String>()

        var i = 0
        while(i < input.length){
            val c = input[i]

            when{
                c.isDigit() -> {
                    val start = i
                    while(i < input.length && input[i] !in "+-*/"){
                        i++
                    }
                    nums.push(BigDecimal(input.substring(start, i)))
                    i--
                }

                c in "+-*/" -> {
                    try {
                        while (ops.isNotEmpty() && precedence(ops.peek()) >= precedence(c.toString())){
                            val a = nums.pop()
                            val b = nums.pop()
                            val op = ops.pop()
                            nums.push(doOp(op, a, b))
                        }

                        ops.push(c.toString())
                    }catch (e: Exception){
                        val mes = "Error!\nTap CLR to continue."
                        result.text = mes
                        return
                    }
                }
            }

            i++
        }

        while(ops.isNotEmpty()){
            try{
                val a = nums.pop()
                val b = nums.pop()
                val op = ops.pop()
                nums.push(doOp(op, a, b))
            }catch (e: Exception){
                val mes = "Error!\nTap CLR to continue."
                result.text = mes
                return
            }

        }

        // render
        result.text = nums.pop().stripTrailingZeros().toPlainString()
    }

    // helper function to tell the precedence of operator
    private fun precedence(op: String): Int{
        return when(op){
            "+", "-" -> 1
            "*", "/" -> 2
            else -> -1
        }
    }

    // helper function to do calculation
    private fun doOp(op: String, a: BigDecimal, b: BigDecimal): BigDecimal{
        return when(op){
            "+" -> a.add(b)
            "-" -> b.subtract(a)
            "*" -> a.multiply(b).setScale(8, RoundingMode.HALF_UP)
            "/" -> {
                if(a == BigDecimal.ZERO){
                    throw ArithmeticException()
                }
                b.divide(a, 8, RoundingMode.HALF_UP)
            }
            else -> throw IllegalArgumentException()
        }
    }

    // if the clear button is clicked
    private fun onClear(){
        // reset all flags
        afterOp = false
        zero = false
        hasDot = false
        input = ""
        result.text = "0"
    }
}