package com.project.teama_be.domain.location.converter;

import com.project.teama_be.domain.location.entity.Location;
import com.project.teama_be.domain.post.dto.request.PostReqDTO;

public class LocationConverter {

    // Location 생성
    public static Location toLocation(
            PostReqDTO.PostUpload dto
    ){
        return Location.builder()
                .latitude(dto.latitude())
                .longitude(dto.longitude())
                .placeName(dto.placeName())
                .addressName(dto.addressName())
                .roadAddressName(dto.roadAddressName())
                .build();
    }
}
