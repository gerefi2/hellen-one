# Combine the related files for a specific platform and MCU.

# Target ECU board design
BOARDCPPSRC = $(BOARD_DIR)/board_configuration.cpp
DDEFS += -DEFI_MAIN_RELAY_CONTROL=TRUE

# Add them all together
DDEFS += -DEFI_SOFTWARE_KNOCK=TRUE -DSTM32_ADC_USE_ADC3=TRUE

DDEFS += -DADC_MUX_PIN=Gpio::F2

# 37K would not fit
DDEFS += -DLUA_USER_HEAP=32000

# pretty temporary
DDEFS += -DDISABLE_PIN_STATE_VALIDATION=TRUE

DDEFS += -DEFI_LOGIC_ANALYZER=FALSE
DDEFS += -DEFI_MALFUNCTION_INDICATOR=FALSE

ONBOARD_MEMS_TYPE=LIS2DH12

include $(BOARDS_DIR)/hellen/hellen-common144.mk

# This board has trigger scope hardware!
DDEFS += -DTRIGGER_SCOPE

ifeq ($(PROJECT_CPU),ARCH_STM32F7)
	DDEFS += -DSTATIC_BOARD_ID=STATIC_BOARD_ID_ALPHAX_4CHAN_F7
	# TODO: why do I struggle to fit into flash? compare with Proteus
	#Linker options, flash size
    USE_OPT += -Wl,--defsym=FLASH_SIZE=768k
    # TODO do we only support serial on F7 but not UART?
    DDEFS += -DEFI_CONSOLE_TX_BRAIN_PIN=Gpio::D6 -DEFI_CONSOLE_RX_BRAIN_PIN=Gpio::D5
    DDEFS += -DTS_PRIMARY_UxART_PORT=SD2 -DEFI_TS_PRIMARY_IS_SERIAL=TRUE -DSTM32_SERIAL_USE_USART2=TRUE -DSTM32_UART_USE_USART2=FALSE
else ifeq ($(PROJECT_CPU),ARCH_STM32F4)
	DDEFS += -DSTATIC_BOARD_ID=STATIC_BOARD_ID_ALPHAX_4CHAN
	DDEFS += $(PRIMARY_COMMUNICATION_PORT_USART2)
else
$(error Unsupported PROJECT_CPU [$(PROJECT_CPU)])
endif

DDEFS += -DHW_HELLEN_4CHAN=1
