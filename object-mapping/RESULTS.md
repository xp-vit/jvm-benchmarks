# object-mapping results

JMH 1.37, 5 forks, 5 warmup + 5 measurement iterations (1s each), 1 thread,
`AverageTime`, ns per mapping, with the `gc` profiler on. Raw JSON in
`results/headline.json`.

## Environment

- CPU: AMD Ryzen 9 7950X3D (16C/32T)
- RAM: 96 GB
- OS: Manjaro Linux, kernel 6.12.103
- JDK: Amazon Corretto 25.0.3 (25.0.3+9-LTS)
- MapStruct 1.6.3, ModelMapper 3.2.6, Orika 1.5.4, JMH 1.37

Workload: one `Person` (11 fields, a nested `Address`, a 3-element roles list)
mapped to `PersonDto`, once per invocation. Mappers built once in setup.

## Results

| Mapper | Approach | ns/op | Error (99.9%) | vs hand-written | Alloc B/op |
|---|---|---|---|---|---|
| hand-written | plain Java (the floor) | 12.94 | 0.30 | 1.00x | 176 |
| MapStruct | compile-time code generation | 13.50 | 0.17 | 1.04x | 176 |
| Orika | runtime bytecode generation | 174.78 | 3.40 | 13.5x | 744 |
| ModelMapper | runtime reflection | 4074.94 | 52.97 | 315x | 12560 |

### GC secondary metrics

| Mapper | alloc rate (MB/s) | alloc norm (B/op) | gc.count | gc.time (ms) |
|---|---|---|---|---|
| hand-written | 12983.7 | 176.0 | 252 | 127 |
| MapStruct | 12436.3 | 176.0 | 241 | 122 |
| Orika | 4061.4 | 744.0 | 117 | 58 |
| ModelMapper | 2939.8 | 12560.0 | 83 | 42 |

## Reading

- **MapStruct is indistinguishable from hand-written in practice.** 13.50 vs 12.94 ns
  is a 4% delta inside the noise, and allocation settles it: both allocate exactly
  176 B/op. The generated code allocates the DTO, the nested `AddressDto`, and the
  roles list, and nothing else. It generates plain Java at compile time, so there is
  no runtime machinery to pay for, in time or in garbage.
- **ModelMapper is ~300x slower than MapStruct** (315x hand-written), and it is
  allocation-bound, not CPU-bound: 12560 B/op, 71x the floor. Note the inversion in
  the GC table: its alloc rate is the *lowest* of the four because it is too slow to
  allocate fast. Fine for cold/rare mappings, expensive on a hot path.
- **Orika sits in the middle (13.5x, 744 B/op).** Runtime bytecode generation beats
  reflection, but `MapperFacade` dispatch plus a per-call context object cost. It is
  unmaintained (last release Feb 2019) and does not start on JDK 25 without
  `--add-opens=java.base/java.lang=ALL-UNNAMED`. Hard to recommend in 2026.

The `--add-opens` flag was applied to all four benchmarks equally, so it introduces
no bias into the comparison.

Fastest is not the only axis: ModelMapper and Orika buy convenience (no annotation
processor, looser coupling). But if mapping is on a hot path, the numbers say
MapStruct, and it costs almost nothing over doing it by hand.

## Writeup

Full analysis: https://patotski.com/blog/mapstruct-vs-modelmapper-vs-orika/
