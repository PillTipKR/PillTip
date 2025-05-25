package com.oauth2.Util.Seeder;

import com.oauth2.AutoComplete.Service.AutocompleteService;
import com.oauth2.Search.Service.DataSyncService;
import com.oauth2.Search.Service.SearchService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class AutocompleteSeeder implements CommandLineRunner {

    private final AutocompleteService autocompleteIndexService;
    private final SearchService searchService;

    private final DataSyncService dataSyncService;

    public AutocompleteSeeder(AutocompleteService autocompleteIndexService, SearchService searchService, DataSyncService dataSyncService) {
        this.autocompleteIndexService = autocompleteIndexService;
        this.searchService = searchService;
        this.dataSyncService = dataSyncService;
    }

    @Override
    public void run(String... args) throws Exception {
        autocompleteIndexService.syncTextToElasticsearch();
        searchService.syncDrugsToElasticsearch();
        dataSyncService.syncDataWithRedis();
        System.out.println("Index injection complete");
    }
}
