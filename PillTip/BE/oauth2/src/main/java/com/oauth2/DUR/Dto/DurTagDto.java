package com.oauth2.DUR.Dto;

import java.util.List;

public record DurTagDto(
   String title,
   List<DurDto> durDtos,
   boolean isTrue
) {}
