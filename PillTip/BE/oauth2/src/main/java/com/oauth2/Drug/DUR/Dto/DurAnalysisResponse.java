package com.oauth2.Drug.DUR.Dto;

// DUR redis 결과 저장용
public record DurAnalysisResponse(
   DurPerDrugDto durA,
   DurPerDrugDto durB,
   DurPerDrugDto interact,
   boolean userTaken
) {}
