# List of all the board related files.
BOARDCPPSRC = $(BOARD_DIR)/board_configuration.cpp \
  $(BOARD_DIR)/board_storage.cpp

# board.c from this directory
BOARD_C = $(BOARD_DIR)/board.c
# Required include directories
BOARDINC += $(BOARD_DIR)/config/controllers/algo

#LED
DDEFS +=  -DLED_CRITICAL_ERROR_BRAIN_PIN=Gpio::G7
DDEFS += -DLED_PIN_MODE=OM_INVERTED

# We are running on Subaru EG33 hardware!
DDEFS += -DHW_SUBARU_EG33=1
DDEFS += -DFIRMWARE_ID=\"EG33\"
DDEFS += -DSTATIC_BOARD_ID=STATIC_BOARD_ID_SUBARU_EG33_F7

# Override DEFAULT_ENGINE_TYPE
DDEFS += -DDEFAULT_ENGINE_TYPE=engine_type_e::SUBARU_EG33

#Some options override
DDEFS += -DHAL_USE_UART=FALSE
DDEFS += -DUART_USE_WAIT=FALSE

#Mass Storage
DDEFS += -DEFI_EMBED_INI_MSD=TRUE

# Shared variables
ALLINC    += $(BOARDINC)

#Serial flash support
include $(PROJECT_DIR)/hw_layer/drivers/flash/sst26f_jedec.mk
