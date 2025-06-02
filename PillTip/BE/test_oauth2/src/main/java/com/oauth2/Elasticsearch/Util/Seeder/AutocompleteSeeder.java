package com.oauth2.Elasticsearch.Util.Seeder;

import com.oauth2.Search.Service.DataSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
public class AutocompleteSeeder implements CommandLineRunner {

    @Value("${app.seed}")
    private boolean seed;

    private final DataSyncService dataSyncService;

    @Override
    public void run(String... args) throws Exception {
        if(seed) {
            dataSyncService.loadAll();
            System.out.println("Index injection complete");
        }
    }
}
