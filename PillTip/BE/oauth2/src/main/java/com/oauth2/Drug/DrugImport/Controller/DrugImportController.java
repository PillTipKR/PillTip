package com.oauth2.Drug.DrugImport.Controller;

import com.oauth2.Drug.Domain.DrugCaution;
import com.oauth2.Drug.DrugImport.Service.DrugImportService;
import com.oauth2.Drug.DrugImport.Service.IngredientImportService;
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

    @PostMapping("/caution")
    public ResponseEntity<?> importCaution(@RequestParam MultipartFile age, @RequestParam MultipartFile lactation,
                                           @RequestParam MultipartFile elder, @RequestParam MultipartFile pregnancy,
                                           @RequestParam MultipartFile overdose, @RequestParam MultipartFile period) {

        ingredientImportService.importCautionsFromCsv(age, DrugCaution.ConditionType.AGE);
        ingredientImportService.importCautionsFromCsv(lactation, DrugCaution.ConditionType.LACTATION);
        ingredientImportService.importCautionsFromCsv(elder, DrugCaution.ConditionType.ELDER);
        ingredientImportService.importCautionsFromCsv(pregnancy, DrugCaution.ConditionType.PREGNANCY);
        ingredientImportService.importCautionsFromCsv(overdose, DrugCaution.ConditionType.OVERDOSE);
        ingredientImportService.importCautionsFromCsv(period, DrugCaution.ConditionType.PERIOD);

        return ResponseEntity.ok("주의사항 데이터 임포트 완료");
    }

    @PostMapping("/therapeuticDup")
    public ResponseEntity<?> uploadTherpeutic(@RequestParam MultipartFile file) {
        ingredientImportService.importTherapeuticsFromCsv(file);
        return ResponseEntity.ok("성분 데이터 임포트 완료");
    }



} 