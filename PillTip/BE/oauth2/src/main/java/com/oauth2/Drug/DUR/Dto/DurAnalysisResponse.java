package com.oauth2.Drug.DUR.Dto;

public record DurAnalysisResponse(
   DurPerDrugDto durA,
   DurPerDrugDto durB,
   DurPerDrugDto interact,
   boolean userTaken
) {}
