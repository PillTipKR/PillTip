package com.oauth2.Util.Elasticsearch.Initializer;

import com.oauth2.Util.Elasticsearch.Manager.IndexManager;
import com.oauth2.Util.Elasticsearch.Provider.IndexMappingProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(1)
public class IndexInitializer implements CommandLineRunner {

    @Value("${app.seed}")
    private boolean seed;

    private final IndexManager indexManager;
    private final List<IndexMappingProvider<?>> indexProviders;

    public IndexInitializer(IndexManager indexManager, List<IndexMappingProvider<?>> indexProviders) {
        this.indexManager = indexManager;
        this.indexProviders = indexProviders;
    }

    @Override
    public void run(String... args) throws Exception {
        if(seed) {
            for (IndexMappingProvider<?> provider : indexProviders) {
                indexManager.createIndex(provider);
                System.out.println("Creating index in ES:" + provider.getIndexName());
            }
        }
    }
}

