package com.oauth2.Drug.Controller;

import com.oauth2.Drug.Service.DrugImportService;
import com.oauth2.Drug.Service.IngredientImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/api/import")
@RestController
public class DrugImportController {
    private final DrugImportService drugImportService;
    private final IngredientImportService ingredientImportService;


    public DrugImportController(DrugImportService drugImportService, IngredientImportService ingredientImportService) {
        this.drugImportService = drugImportService;
        this.ingredientImportService = ingredientImportService;
    }

    @PostMapping("/drugs")
    public String importDrugs(@RequestParam String url1, @RequestParam String url2, @RequestParam String url3
                                ,@RequestParam MultipartFile file) {
        // URL 디코딩
        drugImportService.importFromFile(url1);
        drugImportService.importFromFile(url2);
        drugImportService.importFromFile(url3);
        ingredientImportService.importIngredientsFromCsv(file);
        return "import success";
    }

    @PostMapping("/ingredients")
    public ResponseEntity<?> uploadIngredients(@RequestParam MultipartFile file) {
        ingredientImportService.importIngredientsFromCsv(file);
        return ResponseEntity.ok("성분 데이터 임포트 완료");
    }

    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam MultipartFile file) {
        drugImportService.importImageFromCsv(file);
        return ResponseEntity.ok("이미지 세팅");
    }

} 