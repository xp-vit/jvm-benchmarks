package com.patotski.mapping;

import java.util.ArrayList;

/** Plain-Java mapping by hand. The control: the floor every library is measured against. */
public final class HandWrittenMapper {

    private HandWrittenMapper() {}

    public static PersonDto toDto(Person p) {
        PersonDto dto = new PersonDto();
        dto.setId(p.getId());
        dto.setFirstName(p.getFirstName());
        dto.setLastName(p.getLastName());
        dto.setEmail(p.getEmail());
        dto.setPhone(p.getPhone());
        dto.setBirthDate(p.getBirthDate());
        dto.setActive(p.isActive());
        dto.setLoginCount(p.getLoginCount());
        dto.setCreatedAt(p.getCreatedAt());
        Address a = p.getAddress();
        if (a != null) {
            AddressDto ad = new AddressDto();
            ad.setStreet(a.getStreet());
            ad.setCity(a.getCity());
            ad.setZip(a.getZip());
            ad.setCountry(a.getCountry());
            dto.setAddress(ad);
        }
        if (p.getRoles() != null) {
            dto.setRoles(new ArrayList<>(p.getRoles()));
        }
        return dto;
    }
}
