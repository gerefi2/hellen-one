#include "ch.h"
#include "hal.h"
#include "chprintf.h"

#include "uart.h"
#include "persistence.h"

static const UARTConfig uartCfg =
{
    .txend1_cb = nullptr,
    .txend2_cb = nullptr,
    .rxend_cb = nullptr,
    .rxchar_cb = nullptr,
    .rxerr_cb = nullptr,
    .timeout_cb = nullptr,

#ifdef STM32F0XX
    .timeout = 0,
#endif

    .speed = 115200,
    .cr1 = 0,
    .cr2 = 0,
    .cr3 = 0,
    .rxhalf_cb = nullptr,
};

static char printBuffer[200];

extern TestConfiguration configuration;
extern mfs_error_t flashState;

static THD_WORKING_AREA(waUartThread, 256);
static void UartThread(void*)
{
    while (true) {
        size_t writeCount = chsnprintf(printBuffer, 200, "%d.%03d\twrites=%d\treboots=%d\r\n", 0, (int)flashState, configuration.updateCounter, configuration.rebootCounter);
        uartStartSend(&UARTD1, writeCount, printBuffer);

        pokeConfiguration();

        chThdSleepMilliseconds(200);
    }
}

void InitUart()
{
  // stm32 TX/UART1 - dongle RX often White
  palSetPadMode(GPIOA, 9, PAL_MODE_STM32_ALTERNATE_PUSHPULL );
  // stm32 RX/UART1 - dongle TX often Green
  palSetPadMode(GPIOA,10, PAL_MODE_INPUT_PULLUP );

    uartStart(&UARTD1, &uartCfg);

    chThdCreateStatic(waUartThread, sizeof(waUartThread), NORMALPRIO, UartThread, nullptr);
}
