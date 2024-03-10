# List of all the board related files.

# we have:
# 	PDL_DEVICE_TYPE=PDL_TYPE3
# 	PDL_MCU_INT_TYPE=PDL_FM4_INT_TYPE_B
PDL_DIR = $(CYPRESS_CONTRIB)/os/hal/ports/Cypress/PDL

BOARDSRC = $(BOARD_DIR)/board.c

BOARDCPPSRC = $(BOARD_DIR)/board_configuration.cpp

# Required include directories
BOARDINC += $(BOARD_DIR)/config/controllers/algo

BOARDINC += $(PDL_DIR)/driver $(PDL_DIR)/driver/gpio $(PDL_DIR)/driver/usb $(PDL_DIR)/driver/mfs $(PDL_DIR)/midware/usb/device

BOARDCPPSRC += $(CONTROLLERS_ALGO_AUTO_GENERATED_ENUMS)

# Define linker script file here
LDSCRIPT = $(STARTUPLD_CYPRESS)/cypress_S6E2CxAH.ld

DDEFS += -D__USE_CMSIS
DDEFS += -DEFI_ENABLE_ASSERTS=FALSE
DDEFS += -DCH_DBG_ENABLE_CHECKS=FALSE
DDEFS += -DCH_DBG_ENABLE_ASSERTS=FALSE
DDEFS += -DCH_DBG_ENABLE_STACK_CHECK=FALSE
DDEFS += -DCH_DBG_FILL_THREADS=FALSE
DDEFS += -DCH_DBG_THREADS_PROFILING=FALSE
