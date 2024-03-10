Here we have some dev utilities - average gerefi user would not need to use any of these!

```
./gradlew :config_definition:shadowJar
```

``configuration_definition`` is the most valuable subproject: that code reads


gcc_map_reader reads the .map file produced by GCC and prints some details on what is RAM used for

ts2c reads a piece of TunerStudio map and produces a matching C language piece of code

version2header creates a C language header containting current SVN version

enum2string

KiCad tools have moved to https://github.com/gerefi/KiCad-utils
