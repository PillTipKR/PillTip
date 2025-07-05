package com.oauth2.Drug.Prompt.Dto;

public record DurResponse(
    String drugA,
    String drugB,
    String durA,
    String durB,
    String interact,
    Boolean durTrueA,
    Boolean durTrueB,
    Boolean durTrueInter
) {}
