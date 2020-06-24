# Kotlin Native with WiringPi on Raspberry Pi
## Introduction
Sample IntelliJ IDEA project using Kotlin Native with WiringPi library for Raspberry Pi.\
Covered functionalities:
- Gpio digitalWrite (Led blink)
- Gpio digitalRead (Led on/off by Button)
- Gpio pwmWrite (Led fading)
- Spi write/read (Echo)
- I2c write/read (Pcf8574 with Led blink)
- 1-wire read (Ds18b20 sensor read temperature)
- Serial write/read (Echo)    

Each of these functionalities could be tested separately.
The WiringPi library (version 2.50) was compiled on Raspberry Pi and copied to the project libs folder.

***I am not responsible for any damage caused to your software, equipment, or anything else. Proceed at your own risk.**

## Requirements
- Necessary interfaces should be enabled in the 'Raspberry Pi Configuration' (I2C, 1Wire, etc.)
- The latest version of WiringPi library should be installed in the system (version 2.50).
```
   //update and check version
   $ sudo apt-get update
   $ gpio -v

   //install if missing
   $ sudo apt-get install wiringpi
```

## Getting Started
1. Build Project from IDE or Terminal.
    ```
    gradlew build
    ```
2. Transfer kexe executable file to your Raspberry Pi home folder with changed file name.
    ```
    scp .\KotlinNativeExampleRPi.kexe pi@192.168.0.XXX:~/app.kexe
    ```
   _*This step can be automated. Description how to do it can be found in the "Grade SSH plugin" section._
3. Connect to your Raspberry Pi via SSH
    ```
    ssh pi@192.168.0.XXX
    ```
4. Change app.kexe file permission
    ```
    chmod 777 app.kexe
    ```
   _*Necessary only when executable file was copied for the first time._
6. Run 
    ```
    sudo ./app.kexe
    ```
   _*Necessary only for pwm functionality._
7. Select a test which you want to run from menu
    ```
    1. GPIO   Blink Test
    2. GPIO   Button Test
    3. GPIO   Pwm Test
    4. SPI    Echo Pwm Test
    5. I2C    Expander Test
    6. 1-WIRE Temp Sensor Test
    7. SERIAL Echo Test
    ```

## Circuit
The entire circuit doesn't have to be built.\
 he only required elements are those that are used in the specific test.

![schem](https://github.com/michal-kurasik/assets-test/blob/master/img/schem/ALL.png?raw=true)

Please note that pin numbering in WiringPi library is different than in the OS.\
WiringPi pins numbering guide: http://wiringpi.com/pins/ 

## Test Modules Description

#### 1. GPIO Blink Test
LED blinks for ~10 seconds.

![#black](https://placeholder.pics/svg/35x10/404040-404040/404040-000000/) RPi GND pin -> LED cathode(-)\
![#orange](https://placeholder.pics/svg/35x10/EF6100-EF6100/EF6100-AD6A38/) RPi GPIO_12 pin -> 100Ω resistor -> LED anode(+)

#### 2. GPIO Button Test
LED turns on when BUTTON is pressed.

![#black](https://placeholder.pics/svg/35x10/404040-404040/404040-000000/) RPi GND pin -> LED cathode(-)\
![#orange](https://placeholder.pics/svg/35x10/EF6100-EF6100/EF6100-AD6A38/) RPi GPIO_12 pin -> 100Ω resistor -> LED anode(+)\
![#red](https://placeholder.pics/svg/35x10/CC1414-CC1414/CC1414-8C0000/) RPi 3.3V pin -> BUTTON\
![#gray](https://placeholder.pics/svg/35x10/999999-999999/999999-666666/) RPi GPIO_07 pin -> BUTTON

#### 3. GPIO Pwm Test
LED fades in and out for ~10 seconds.

![#black](https://placeholder.pics/svg/35x10/404040-404040/404040-000000/) RPi GND pin -> LED cathode(-)\
![#orange](https://placeholder.pics/svg/35x10/EF6100-EF6100/EF6100-AD6A38/) RPi GPIO_12 pin -> 100Ω Resistor -> LED anode(+)

#### 4. SPI Echo Test
SPI Loopback speed test. Message sent by SPI_MOSI pin is received by SPI_MISO pin at different SPI speeds. 

![#white](https://placeholder.pics/svg/35x10/FFFFFF-FFFFFF/FFFFFF-999999/) RPi SPI0_MOSI pin -> SPI0_MISO pin

#### 5. I2C Expander Test
I2C controls PCF8574 expander to blink LED for ~10 seconds.

![#black](https://placeholder.pics/svg/35x10/404040-404040/404040-000000/) RPi GND pin -> PCF8574 GND, A0, A1, A2 pins\
![#black](https://placeholder.pics/svg/35x10/404040-404040/404040-000000/) RPi GND pin -> LED cathode(-)\
![#green](https://placeholder.pics/svg/35x10/25CC35-25CC35/25CC35-00A527/) PCF8574 P4 pin -> 100Ω Resistor -> LED anode(+)\
![#red](https://placeholder.pics/svg/35x10/CC1414-CC1414/CC1414-8C0000/) RPi 3.3V pin -> PCF8574 VCC pin\
![#pink](https://placeholder.pics/svg/35x10/FA50E6-FA50E6/FA50E6-DC00DC/) RPi I2C1_SCL pin -> PCF8574 SCL pin\
![#cyan](https://placeholder.pics/svg/35x10/33FFC5-33FFC5/33FFC5-00A0C6/) RPi I2C1_SDA pin -> PCF8574 SDA pin

Expander address is 0x38 when A1,A2,A3 pins are grounded.
To check this address and connection run command on RPi:
```
i2cdetect -y 1
```

#### 6. 1-WIRE Temp Sensor Test
1-WIRE is reading temperature from DS18B20 sensor and print it to the console for ~10 seconds.

![#black](https://placeholder.pics/svg/35x10/404040-404040/404040-000000/) RPi GND pin -> DS18B20 GND pin\
![#yellow](https://placeholder.pics/svg/35x10/FFF800-FFF800/FFF800-D6D63A/) RPi GPIO_04(1_WIRE) pin -> DS18B20 DATA pin\
![#red](https://placeholder.pics/svg/35x10/CC1414-CC1414/CC1414-8C0000/) RPi 3.3V pin -> DS18B20 VCC pin\
![#red](https://placeholder.pics/svg/35x10/CC1414-CC1414/CC1414-8C0000/) RPi 3.3V -> Resistor 4.7kΩ Resistor -> DS18B20 DATA pin

Each DS18B20 sensor has its own unique address which must be properly set in the code.\
To check this address run command on RPi:
```
ls /sys/bus/w1/devices/
```

#### 7. SERIAL Echo Test
Serial loopback speed test. Message sent by UART0_TXD pin is received by UART0_RXD pin at different baud rates.

![#white](https://placeholder.pics/svg/35x10/FFFFFF-FFFFFF/FFFFFF-999999/) RPi UART0_TXD pin -> UART0_RXD pin\
 
 
## Grade SSH plugin
Transfering executable file to Raspberry Pi can be automated by dedicated gradle task.\
In order to use it provide SSH details in build.gradle file: host, username, password and uncomment this line:
  ```
  build.dependsOn upload
  ```   

## Enviroment Configurations
- Raspberry Pi 3B+ Rev 1.2, Raspberry Pi Zero-W Rev 1.1
- Debian Buster with Raspberry Pi Desktop (release: '2020-02-12', kernel: '4.19')
- Wiring Pi library version '2.50' (http://wiringpi.com/, https://github.com/WiringPi/WiringPi)
- Development machine:
  * Windows 10 Pro
  * IntelliJ IDEA 2020.1.1 (Community Edition)
   
   
## References 
WiringPi library: 
http://wiringpi.com/

Kotlin/Native interoperability:
https://kotlinlang.org/docs/reference/native/c_interop.html

Gradle SSH plugin:
https://gradle-ssh-plugin.github.io/

## Future Plans
- GPIO Speed test for different libraries
- Wrapper for wiringPi