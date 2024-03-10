Win32 or posix version of firmware allows to explore gerefi on a PC without any embedded hardware!

Simulator runs a subset of ECU on your pc, easier to debug some things, tighter dev loop.

* self-stimulation for trigger event capture
* mocked analog sensors
* mocked outputs
* SocketCAN integration on Linux

One of ways to mock analog sensors
```
// see SensorType.java for numeric ordinals
set_sensor_mock 4 90
```
