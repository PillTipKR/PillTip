package com.oauth2.Util.Initializer;

import com.oauth2.Util.Manager.IndexManager;
import com.oauth2.Util.Provider.IndexMappingProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(1)
public class IndexInitializer implements CommandLineRunner {

    private final IndexManager indexManager;
    private final List<IndexMappingProvider<?>> indexProviders;

    public IndexInitializer(IndexManager indexManager, List<IndexMappingProvider<?>> indexProviders) {
        this.indexManager = indexManager;
        this.indexProviders = indexProviders;
    }

    @Override
    public void run(String... args) throws Exception {
        for (IndexMappingProvider<?> provider : indexProviders) {
            indexManager.createIndex(provider);
            System.out.println("Creating index in ES:" + provider.getIndexName());
        }
    }
}

