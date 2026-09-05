package com.patotski.mapping;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.modelmapper.ModelMapper;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * One Person -> PersonDto mapping (nested Address + a roles list) per invocation,
 * four ways: MapStruct (compile-time), ModelMapper (reflection), Orika (bytecode),
 * and hand-written (the floor). Mappers are built once in setup; the benchmark
 * measures steady-state mapping cost, not configuration.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
// Orika 1.5.4 reflects into java.lang (CloneableConverter), which the JDK module
// system blocks by default since JDK 16. An unmaintained library needs this flag
// just to start on JDK 25. The maintained libraries do not.
@Fork(value = 5, jvmArgsAppend = "--add-opens=java.base/java.lang=ALL-UNNAMED")
public class MappingBenchmark {

    private Person source;
    private ModelMapper modelMapper;
    private MapperFacade orika;

    @Setup
    public void setup() {
        source = samplePerson();

        modelMapper = new ModelMapper();
        // Prime ModelMapper's type map so we measure mapping, not first-call analysis.
        modelMapper.map(source, PersonDto.class);

        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        factory.classMap(Person.class, PersonDto.class).byDefault().register();
        factory.classMap(Address.class, AddressDto.class).byDefault().register();
        orika = factory.getMapperFacade();
    }

    @Benchmark
    public PersonDto mapStruct() {
        return PersonMapper.INSTANCE.toDto(source);
    }

    @Benchmark
    public PersonDto modelMapper() {
        return modelMapper.map(source, PersonDto.class);
    }

    @Benchmark
    public PersonDto orika() {
        return orika.map(source, PersonDto.class);
    }

    @Benchmark
    public PersonDto handWritten() {
        return HandWrittenMapper.toDto(source);
    }

    private static Person samplePerson() {
        Address a = new Address();
        a.setStreet("Leopoldstrasse 12");
        a.setCity("Munich");
        a.setZip("80802");
        a.setCountry("Germany");

        Person p = new Person();
        p.setId(42L);
        p.setFirstName("Viktar");
        p.setLastName("Patotski");
        p.setEmail("viktar@example.com");
        p.setPhone("+49 89 123456");
        p.setBirthDate(LocalDate.of(1988, 5, 14));
        p.setActive(true);
        p.setLoginCount(1234);
        p.setAddress(a);
        p.setRoles(List.of("ADMIN", "USER", "AUDITOR"));
        p.setCreatedAt(Instant.parse("2020-01-15T09:30:00Z"));
        return p;
    }
}
