package com.oauth2.AutoComplete.Controller;

import com.oauth2.AutoComplete.Dto.AutocompleteResult;
import com.oauth2.AutoComplete.Service.AutocompleteService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/autocomplete")
public class AutocompleteController {

    private final AutocompleteService autocompleteService;

    @Value("${elastic.drug.drug}")
    private String drug;

    @Value("${elastic.drug.ingredient}")
    private String ingredient;

    @Value("${elastic.drug.manufacturer}")
    private String manufacturer;

    private int pageSize = 20;

    public AutocompleteController(AutocompleteService autocompleteService) {
        this.autocompleteService = autocompleteService;
    }

    @GetMapping("/drugs")
    public List<AutocompleteResult> getDrugSearch(
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page) throws IOException {
        return autocompleteService.getDrugSearch(input, drug, pageSize, page);
    }

    @GetMapping("/manufacturer")
    public List<AutocompleteResult> getManufacturerSearch(
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page) throws IOException {
        return autocompleteService.getDrugSearch(input, manufacturer, pageSize, page);
    }

    @GetMapping("/ingredient")
    public List<AutocompleteResult> getIngredientSearch(
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page) throws IOException {
        return autocompleteService.getDrugSearch(input, ingredient, pageSize, page);
    }
}
