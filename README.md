# jvm-benchmarks

Reproducible JMH benchmarks for common "Library X vs Y vs Z" questions on the
JVM. Every battle is one module, with the raw harness so you can run it on your
own hardware instead of trusting a number in a blog post.

Methodology is deliberately strict, because that is the whole point:

- [JMH](https://github.com/openjdk/jmh) with proper warmup, multiple forks, and a
  `Blackhole` / returned value so dead-code elimination cannot fake a result.
- A realistic workload, not a toy example. The fixture is documented per module.
- A hand-written / no-library baseline wherever it makes sense, so every number is
  read against the floor, not in the abstract.
- Full hardware + version disclosure. Reproducibility is the point.
- Honesty: where a "slower" library wins on ergonomics or features, the writeup
  says so. No strawman configs.

## Battles

| Module | Question | Writeup |
|---|---|---|
| [`object-mapping`](object-mapping/) | MapStruct vs ModelMapper vs Orika vs hand-written | (link when published) |

## Run a battle

```bash
./gradlew :object-mapping:jmh          # full run (2 forks, 3+5 iterations)
# fast smoke (proves it runs, numbers not publishable):
java -jar object-mapping/build/libs/object-mapping-1.0.0-jmh.jar -f 1 -wi 1 -i 2 -w 1s -r 1s
```

Results (JSON) land in `object-mapping/build/results/jmh/`.

## object-mapping

Maps one `Person` (nested `Address` + a roles list) to a `PersonDto`, four ways:
MapStruct (compile-time code generation), ModelMapper (runtime reflection), Orika
(runtime bytecode generation), and hand-written (the floor). `AverageTime`, ns per
mapping.

Note on Orika: it is unmaintained (last release 1.5.4, February 2019). On JDK 25 it
does not even start without `--add-opens=java.base/java.lang=ALL-UNNAMED`, because
it reflects into `java.lang`. The benchmark bakes that flag into its fork so Orika
can be measured; the maintained libraries need no such flag.

## Hardware disclosure

Every published number states the box. Primary: AMD Ryzen 9 7950X3D (16C/32T),
96 GB RAM, Manjaro Linux (kernel 6.12.94), Amazon Corretto JDK 25.0.3. CI (GitHub
Actions) is a smoke test only; shared runners are too noisy for publishable numbers.

## Versions

MapStruct 1.6.3 . ModelMapper 3.2.6 . Orika 1.5.4 . JMH 1.37 . Gradle 9.x . JDK 25.

## License

MIT. See `LICENSE`.
