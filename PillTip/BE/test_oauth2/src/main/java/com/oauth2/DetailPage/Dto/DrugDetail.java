package com.oauth2.DetailPage.Dto;

import com.oauth2.Drug.Domain.Drug;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class DrugDetail {
    private Long id;
    private String name;
    private String manufacturer;

    private List<String> ingredients;

    private String form;
    private List<String> packaging;

    private String atcCode;

    private Drug.Tag tag;

    private Date approvalDate;

    private List<StorageDetail> storageDetails;
    private List<EffectDetail> effectDetails;

}
