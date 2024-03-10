# Combine the related files for a specific platform and MCU.

# Target ECU board design
BOARDCPPSRC = $(BOARD_DIR)/board_configuration.cpp

BOARDINC  = $(BOARD_DIR)

# see also openblt/board.mk STATUS_LED
DDEFS += -DLED_CRITICAL_ERROR_BRAIN_PIN=Gpio::E3

# *TEMPORARY* breaking TTL thus breaking Bluetooth for microgerefi in order to enable SPI3 for SD card
# *TODO* need to give people the horrible choice between Bluetooth via TTL or SD card via SPI :( horrible choice
# PB10/PB11 uses UART3 peripheral and J12/J13 on MRE
# we also have PC10/PC11 exposed on J4 but that's same UART3
DDEFS += -DEFI_CONSOLE_TX_BRAIN_PIN=Gpio::B10 -DEFI_CONSOLE_RX_BRAIN_PIN=Gpio::B11

# on MRE 0.6.0 we have SD card on SPI2 which shared channel 3 with USART3
# todo: enable serial which would not DMA thus not conflict?
DDEFS += -DSTM32_UART_USE_USART3=FALSE -DHAL_USE_UART=FALSE
DDEFS += -DEFI_USE_UART_DMA=FALSE

# maybe a way to disable SPI2 privately
#DDEFS += -DSTM32_SPI_USE_SPI2=FALSE

DDEFS += -DBOARD_TLE8888_COUNT=1
DDEFS += -DEFI_MC33816=TRUE

DDEFS += -DFIRMWARE_ID=\"microgerefi\"
DDEFS += -DEFI_SOFTWARE_KNOCK=TRUE -DSTM32_ADC_USE_ADC3=TRUE
DDEFS += $(VAR_DEF_ENGINE_TYPE)

# F7 we are running out of flash?! only package goodies into F4
ifeq ($(PROJECT_CPU),ARCH_STM32F4)
    # This board can capture SENT
    DDEFS += -DEFI_SENT_SUPPORT=TRUE

    # This board has LIN/K-line interface
    DDEFS += -DEFI_KLINE=TRUE
endif

DDEFS += -DKLINE_SERIAL_DEVICE_RX=Gpio::D9 -DKLINE_SERIAL_DEVICE_TX=Gpio::D8
DDEFS += -DKLINE_SERIAL_DEVICE=SD3
DDEFS += -DSTM32_SERIAL_USE_USART3=TRUE

# We are running on microgerefi hardware!
DDEFS += -DHW_MICRO_gerefi=1

ifeq ($(PROJECT_CPU),ARCH_STM32F7)
    USE_OPT += -Wl,--defsym=FLASH_SIZE=768k
    DDEFS += -DSTATIC_BOARD_ID=STATIC_BOARD_ID_MRE_F7
else ifeq ($(PROJECT_CPU),ARCH_STM32F4)
    DDEFS += -DSTATIC_BOARD_ID=STATIC_BOARD_ID_MRE_F4
	DDEFS += -DRAM_UNUSED_SIZE=4000
else
$(error Unsupported PROJECT_CPU [$(PROJECT_CPU)])
endif
