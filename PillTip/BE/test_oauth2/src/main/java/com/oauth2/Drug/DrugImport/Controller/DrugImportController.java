package com.oauth2.Drug.DrugImport.Controller;

import com.oauth2.Drug.DrugImport.Service.DrugImportService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
public class DrugImportController {
    private final DrugImportService drugImportService;

    public DrugImportController(DrugImportService drugImportService) {
        this.drugImportService = drugImportService;
    }

    @PostMapping("/api/import")
    public String importDrugs(@RequestParam String url) {
        // URL 디코딩
        String decodedPath = URLDecoder.decode(url, StandardCharsets.UTF_8);
        drugImportService.importFromFile(decodedPath);
        return "import success";
    }
} 