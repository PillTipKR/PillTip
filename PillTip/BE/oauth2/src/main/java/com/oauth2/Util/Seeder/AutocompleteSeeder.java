package com.oauth2.Util.Seeder;

import com.oauth2.Util.Service.DataSyncService;
import com.oauth2.Util.Service.DurRedisLoader;
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
    private final DurRedisLoader durRedisLoader;

    @Override
    public void run(String... args) throws Exception {
        if(seed) {
            dataSyncService.loadAll();
            //durRedisLoader.loadAll();
            System.out.println("Index injection complete");
        }
    }
}
