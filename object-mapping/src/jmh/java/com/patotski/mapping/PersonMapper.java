package com.patotski.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct mapper. The annotation processor generates a plain-Java implementation
 * at compile time (PersonMapperImpl), so there is no reflection at run time.
 */
@Mapper
public interface PersonMapper {
    PersonMapper INSTANCE = Mappers.getMapper(PersonMapper.class);

    PersonDto toDto(Person person);

    AddressDto toDto(Address address);
}
