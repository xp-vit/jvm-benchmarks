# object-mapping results

JMH 1.37, 2 forks, 3 warmup + 5 measurement iterations (1s each), `AverageTime`,
ns per mapping. Raw JSON in `results/headline.json`.

## Environment

- CPU: AMD Ryzen 9 7950X3D (16C/32T)
- RAM: 96 GB
- OS: Manjaro Linux, kernel 6.12.94
- JDK: Amazon Corretto 25.0.3
- MapStruct 1.6.3, ModelMapper 3.2.6, Orika 1.5.4, JMH 1.37

Workload: one `Person` (11 fields, a nested `Address`, a 3-element roles list)
mapped to `PersonDto`, once per invocation. Mappers built once in setup.

## Results

| Mapper | Approach | ns/op | Error (99.9%) | vs hand-written |
|---|---|---|---|---|
| hand-written | plain Java (the floor) | 13.0 | 0.2 | 1.0x |
| MapStruct | compile-time code generation | 13.6 | 0.4 | 1.05x |
| Orika | runtime bytecode generation | 192.8 | 8.2 | ~15x |
| ModelMapper | runtime reflection | 4148.4 | 111.8 | ~319x |

## Reading

- **MapStruct is within 5% of hand-written.** It generates plain Java at compile
  time, so there is no runtime reflection to pay for. For hot mapping paths this is
  the only option that does not cost you.
- **ModelMapper is ~305x slower than MapStruct** (~319x hand-written). Convenient,
  convention-based, no build step, but every call pays reflection. Fine for
  cold/rare mappings, expensive on a hot path.
- **Orika sits in the middle (~15x).** Runtime bytecode generation beats reflection
  but it is unmaintained (last release Feb 2019) and does not start on JDK 25
  without `--add-opens=java.base/java.lang=ALL-UNNAMED`. Hard to recommend in 2026.

Fastest is not the only axis: ModelMapper and Orika buy convenience (no annotation
processor, looser coupling). But if mapping is on a hot path, the numbers say
MapStruct, and it costs almost nothing over doing it by hand.
