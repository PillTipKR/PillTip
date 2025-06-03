package com.oauth2.DetailPage.Controller;

import com.oauth2.DetailPage.Dto.DrugDetail;
import com.oauth2.DetailPage.Service.DrugDetailService;
import com.oauth2.Search.Dto.SearchIndexDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/detailPage")
public class DetailPageController {

    private final DrugDetailService drugDetailService;

    public DetailPageController(DrugDetailService drugDetailService) {
        this.drugDetailService = drugDetailService;
    }


    @GetMapping
    public DrugDetail detailPage(@RequestParam long id) throws IOException {
        SearchIndexDTO searchIndexDTO = drugDetailService.getDetailFromElasticsearch(id);
        return drugDetailService.getDetail(searchIndexDTO);
    }
}
