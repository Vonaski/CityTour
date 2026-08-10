package com.iksanov.citytour.booking.mapper;

import com.iksanov.citytour.booking.dto.BookingResponse;
import com.iksanov.citytour.booking.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "tourId", source = "tour.id")
    BookingResponse toResponse(Booking booking);

    List<BookingResponse> toResponseList(List<Booking> bookings);
}