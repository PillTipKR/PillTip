package com.oauth2.AutoComplete.Controller;

import com.oauth2.AutoComplete.Dto.AutocompleteDTO;
import com.oauth2.AutoComplete.Service.AutocompleteService;
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

    private final int pageSize = 5;

    public AutocompleteController(AutocompleteService autocompleteService) {
        this.autocompleteService = autocompleteService;
    }

    @GetMapping("/drugs")
    public List<AutocompleteDTO> getDrugSearch(
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page) throws IOException {
        return autocompleteService.getAutocompleteFromElasticsearch(input, "drug", pageSize, page);
    }

    @GetMapping("/manufacturers")
    public List<AutocompleteDTO> getManufacturerSearch(
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page) throws IOException {
        return autocompleteService.getAutocompleteFromElasticsearch(input, "manufacturer", pageSize, page);
    }

    @GetMapping("/ingredients")
    public List<AutocompleteDTO> getIngredientSearch(
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page) throws IOException {
        return autocompleteService.getAutocompleteFromElasticsearch(input, "ingredient", pageSize, page);
    }
}
