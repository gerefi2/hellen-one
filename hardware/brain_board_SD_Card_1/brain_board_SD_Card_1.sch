EESchema Schematic File Version 2
LIBS:KICAD_Older_Version
LIBS:power
LIBS:device
LIBS:conn
LIBS:linear
LIBS:analog_switches
LIBS:stm32
LIBS:art-electro-conn
LIBS:art-electro-ic
LIBS:art-electro-conn_2
LIBS:logo_flipped
LIBS:crystal(mc306)
LIBS:brain_board_SD_Card_1-cache
EELAYER 25 0
EELAYER END
$Descr A 11000 8500
encoding utf-8
Sheet 1 1
Title "SD_Card_1"
Date "2016-12-31"
Rev "R0.1"
Comp "gerefi.com"
Comment1 ""
Comment2 ""
Comment3 ""
Comment4 ""
$EndDescr
Connection ~ -4675 3450
$Comp
L LOGO G1
U 1 1 52FE356F
P 6000 7775
F 0 "G1" H 6000 7672 60  0001 C CNN
F 1 "LOGO" H 6000 7878 60  0001 C CNN
F 2 "LOGO_F" H 6000 7775 60  0001 C CNN
F 3 "" H 6000 7775 60  0000 C CNN
	1    6000 7775
	1    0    0    -1  
$EndComp
Text Label 8425 5500 2    60   ~ 0
PB5
Text Label 8425 6025 2    60   ~ 0
PB3
Text Label 8425 5925 2    60   ~ 0
PD4
Text Label 8425 5600 2    60   ~ 0
VDD
Text Label 8425 5400 2    60   ~ 0
PB4
$Comp
L GND #PWR01
U 1 1 5861744D
P 8425 5825
F 0 "#PWR01" H 8425 5825 30  0001 C CNN
F 1 "GND" H 8425 5755 30  0001 C CNN
F 2 "" H 8425 5825 60  0000 C CNN
F 3 "" H 8425 5825 60  0000 C CNN
	1    8425 5825
	0    1    1    0   
$EndComp
Text Notes 8775 5725 0    60   ~ 0
SD Card
$Comp
L CONN_01X03 P5
U 1 1 58618F13
P 8625 5500
F 0 "P5" H 8625 5700 50  0000 C CNN
F 1 "CONN_01X03" V 8725 5500 50  0001 C CNN
F 2 "Pin_Headers:Pin_Header_Angled_1x03" H 8625 5500 50  0001 C CNN
F 3 "" H 8625 5500 50  0000 C CNN
	1    8625 5500
	1    0    0    -1  
$EndComp
$Comp
L CONN_01X03 P6
U 1 1 58619400
P 8625 5925
F 0 "P6" H 8625 6125 50  0000 C CNN
F 1 "CONN_01X03" V 8725 5925 50  0001 C CNN
F 2 "Pin_Headers:Pin_Header_Angled_1x03" H 8625 5925 50  0001 C CNN
F 3 "" H 8625 5925 50  0000 C CNN
	1    8625 5925
	1    0    0    -1  
$EndComp
$Comp
L MICRO-SDCARD-CONNECTOR-3300060P1 P352
U 1 1 58624F8A
P 6575 5700
F 0 "P352" H 6425 6200 60  0000 C CNN
F 1 "MICRO-SDCARD-CONNECTOR-3300060P1" H 6575 5200 60  0001 C CNN
F 2 "MICRO-SDCARD-CONNECTOR-3300060P1" H 6575 5700 60  0001 C CNN
F 3 "" H 6575 5700 60  0000 C CNN
F 4 "seeed,3300060P1" H 6575 5700 60  0001 C CNN "VEND2,VEND2#"
	1    6575 5700
	-1   0    0    -1  
$EndComp
NoConn ~ 6975 5300
NoConn ~ 6975 6000
NoConn ~ 6975 6100
$Comp
L GND #PWR02
U 1 1 58624F8B
P 6975 5800
F 0 "#PWR02" H 6975 5800 30  0001 C CNN
F 1 "GND" H 6975 5730 30  0001 C CNN
F 2 "" H 6975 5800 60  0001 C CNN
F 3 "" H 6975 5800 60  0001 C CNN
	1    6975 5800
	1    0    0    -1  
$EndComp
Wire Wire Line
	7150 5700 6975 5700
Wire Wire Line
	6975 5900 7150 5900
Wire Wire Line
	7150 5600 6975 5600
Wire Wire Line
	7150 5500 6975 5500
Wire Wire Line
	7150 5400 6975 5400
$Comp
L GND #PWR03
U 1 1 58624F8C
P 5975 5900
F 0 "#PWR03" H 5975 5900 30  0001 C CNN
F 1 "GND" H 5975 5830 30  0001 C CNN
F 2 "" H 5975 5900 60  0001 C CNN
F 3 "" H 5975 5900 60  0001 C CNN
	1    5975 5900
	1    0    0    -1  
$EndComp
Wire Wire Line
	5975 5550 5975 5900
Connection ~ 5975 5650
Connection ~ 5975 5750
Connection ~ 5975 5850
Text Notes 5475 6950 0    160  ~ 0
SD card slot module for brain board
Text HLabel 7175 5800 2    60   Input ~ 0
GND
Text HLabel 7150 5400 2    60   Input ~ 0
CS_SD_MODULE
Text HLabel 7150 5500 2    60   Input ~ 0
SPI_MOSI
Text HLabel 7150 5600 2    60   Input ~ 0
3.3V
Text HLabel 7150 5700 2    60   Input ~ 0
SPI_SCK
Text HLabel 7150 5900 2    60   Input ~ 0
SPI_MISO
Wire Wire Line
	6975 5800 7175 5800
Text Label 6975 5400 0    60   ~ 0
PD4
Text Label 6975 5700 0    60   ~ 0
PB3
Text Label 6975 5900 0    60   ~ 0
PB4
Text Label 6975 5500 0    60   ~ 0
PB5
Text Label 6975 5600 0    60   ~ 0
VDD
$Comp
L C C2
U 1 1 5869211A
P 9975 5650
F 0 "C2" H 9800 5750 50  0000 L CNN
F 1 "0.1uF" H 9725 5550 50  0000 L CNN
F 2 "C_0805" V -3670 6625 60  0001 C CNN
F 3 "" H 9975 5650 60  0001 C CNN
F 4 "AVX,08055C104KAT2A" V -3670 6625 60  0001 C CNN "MFG,MFG#"
F 5 "DIGI,478-1395-1-ND" V -3670 6625 60  0001 C CNN "VEND1,VEND1#"
	1    9975 5650
	-1   0    0    1   
$EndComp
$Comp
L C C1
U 1 1 5869212A
P 9600 5650
F 0 "C1" H 9700 5750 50  0000 L CNN
F 1 "1000pF" H 9600 5550 50  0000 L CNN
F 2 "C_0805" V -1770 5925 60  0001 C CNN
F 3 "" H 9600 5650 60  0001 C CNN
F 4 "AVX,08052C102KAT2A " V -1770 5925 60  0001 C CNN "MFG,MFG#"
F 5 "DIGI,478-1339-1-ND" V -1770 5925 60  0001 C CNN "VEND1,VEND1#"
	1    9600 5650
	1    0    0    1   
$EndComp
$Comp
L GND #PWR04
U 1 1 58692613
P 9600 5850
F 0 "#PWR04" H 9600 5850 30  0001 C CNN
F 1 "GND" H 9600 5780 30  0001 C CNN
F 2 "" H 9600 5850 60  0000 C CNN
F 3 "" H 9600 5850 60  0000 C CNN
	1    9600 5850
	1    0    0    -1  
$EndComp
Wire Wire Line
	9600 5850 9975 5850
Wire Wire Line
	9975 5450 9600 5450
Text Label 9600 5450 0    60   ~ 0
VDD
$EndSCHEMATC
