@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")

package sample

import kotlinx.cinterop.*
import platform.posix.posix_errno
import platform.posix.system
import wiringpi.core.*
import wiringpi.ds18b20.ds18b20Setup
import wiringpi.i2c.wiringPiI2CSetup
import wiringpi.i2c.wiringPiI2CWrite
import wiringpi.serial.*
import wiringpi.spi.wiringPiSPIDataRW
import wiringpi.spi.wiringPiSPISetup
import kotlin.random.Random
import kotlin.random.nextUBytes

const val DS18B20_ADDRESS = "000000bf4fff"
const val PCF8574_ADDRESS = 0x38
const val SERIAL = "/dev/ttyS0"

const val SPI_CHANNEL = 0
const val SPI_MAX_SPEED_MHZ = 512
const val SPI_DATA_SIZE = 512

const val LED_PIN = 26 //GPIO_12
const val BTN_PIN = 11 //GPIO_07
const val VIRTUAL_PIN = 77

@ExperimentalUnsignedTypes
fun main() {
    println("Kotlin Native with WiringPi")
    wiringPiSetup()
    menu()
}

@ExperimentalUnsignedTypes
private fun menu() {
    system("clear")
    println()
    println("---------------------------")
    println("1. GPIO   Blink Test")
    println("2. GPIO   Button Test")
    println("3. GPIO   Pwm Test")
    println("4. SPI    Echo Pwm Test")
    println("5. I2C    Expander Test")
    println("6. 1-WIRE Temp Sensor Test")
    println("7. SERIAL Echo Test")
    println("---------------------------")
    println("Type test number (1-7) followed by ENTER")
    println()

    var input: Int? = null
    try {
        input = readLine()?.toInt()
    } catch (e: Exception) {
        menu()
    }
    println()
    when (input) {
        1 -> gpioBlinkTest()
        2 -> gpioButttonTest()
        3 -> gpioPwmTest()
        4 -> spiLoopbackSpeedTest()
        5 -> i2cTest()
        6 -> oneWireTest()
        7 -> serialLoopbackTest()
        else -> {
            menu()
        }
    }
    println()
    println("Test finished press ENTER for Menu")
    readLine()
    menu()
}

fun gpioBlinkTest() {
    println("----==== GPIO Blink Test ====----")
    pinMode(LED_PIN, OUTPUT)

    for (i in 1..10) {
        digitalWrite(LED_PIN, HIGH)
        println("LED ON")
        delay(500u)
        digitalWrite(LED_PIN, LOW)
        println("LED OFF")
        delay(500u)
    }
}

@ExperimentalUnsignedTypes
fun gpioButttonTest() {
    println("----==== GPIO Button Test ====----")
    pinMode(LED_PIN, OUTPUT)
    pinMode(BTN_PIN, INPUT)
    pullUpDnControl(BTN_PIN, PUD_DOWN)

    var lastState = 0
    for (c in 1..100) {
        val currentState = digitalRead(BTN_PIN)
        digitalWrite(LED_PIN, currentState)
        if (currentState != lastState) {
            println(if (currentState == 0) "BUTTON RELEASED" else "BUTTON PRESSED")
            lastState = currentState
        }
        delay(100u)
    }
}

fun gpioPwmTest() {
    println("----==== GPIO Pwm Test ====----")
    pinMode(LED_PIN, PWM_OUTPUT)
    for (i in 1..5) {
        for (bright in 0..1023) {
            pwmWrite(LED_PIN, bright)
            delay(1u)
        }
        println("LED FADE IN")
        for (bright in 1023 downTo 0) {
            pwmWrite(LED_PIN, bright)
            delay(1u)
        }
        println("LED FADE OUT")
    }
}

@ExperimentalUnsignedTypes
fun spiLoopbackSpeedTest() {
    println("----==== SPI Echo Speed Test ====----")
    var speed = 1
    val data = Random(1).nextBytes(SPI_DATA_SIZE)

    while (speed <= SPI_MAX_SPEED_MHZ) {
        if (wiringPiSPISetup(SPI_CHANNEL, speed * 1000000) < 0) {
            println("Unable to open SPI device! Error code: " + posix_errno())
        }
        memScoped {
            val buff: CArrayPointer<UByteVar> = allocArrayOf(data).reinterpret()
            if (wiringPiSPIDataRW(SPI_CHANNEL, buff, data.size) < 0) {
                println("Unable to read/write SPI! Error code: " + posix_errno())
            }

            var passed = true
            data.forEachIndexed { index, element ->
                if (buff[index].toByte() != element) {
                    passed = false
                }
            }
            println("SPEED: ${speed}MHz\t SIZE: ${data.size} B\t ${if (passed) "PASSED" else "FAILED"}")
        }
        speed *= 2
    }
}

fun i2cTest() {
    println("----==== I2C Expander Test ====----")
    val i2c = wiringPiI2CSetup(PCF8574_ADDRESS)
    if (i2c < 0) {
        println("Unable to open I2C device!")
        return
    }

    for (i in 1..10) {
        wiringPiI2CWrite(i2c, 0xFF)
        println("PORTS HIGH")
        delay(500u)
        wiringPiI2CWrite(i2c, 0x00)
        println("PORTS LOW")
        delay(500u)
    }
}

fun oneWireTest() {
    println("----==== 1-WIRE Temperature Sensor Test ====----")
    if (ds18b20Setup(VIRTUAL_PIN, DS18B20_ADDRESS) < 0) {
        println("Unable to open 1-WIRE device!")
        return
    }
    for (i in 1..10) {
        val value = analogRead(VIRTUAL_PIN).toString()
        val temp = value.substring(0, value.length - 1) + "." + value.substring(value.length - 1)
        println("TEMPERATURE: ${temp}C")
        delay(500u)
    }
}

@ExperimentalUnsignedTypes
fun serialLoopbackTest() {
    println("----==== SERIAL Echo Speed Test ====----")
    val bauds = arrayOf(9600, 19200, 38400, 57600, 115200, 230400, 460800, 500000, 576000, 921600, 1000000, 1152000, 1500000, 2000000, 2500000, 3000000, 3500000, 4000000)
    val data = Random(1).nextUBytes(100)

    for (baud in bauds) {
        val serialId = serialOpen(SERIAL, baud)
        if (serialId < 0) {
            println("Unable to open SERIAL device!")
            return
        }

        var passed = true
        run loop@{
            data.forEach {
                serialPutchar(serialId, it)
                val received = serialGetchar(serialId).toUByte()
                if (received != it) {
                    passed = false
                    return@loop
                }
            }
        }

        println("BAUD: $baud\t SIZE: ${data.size} B\t ${if (passed) "PASSED" else "FAILED"}")
        serialClose(serialId)
    }
}
