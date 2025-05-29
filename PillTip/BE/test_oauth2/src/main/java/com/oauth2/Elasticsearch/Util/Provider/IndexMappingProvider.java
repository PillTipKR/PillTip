package com.oauth2.Elasticsearch.Util.Provider;

import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import org.springframework.stereotype.Component;

@Component
public interface IndexMappingProvider<T> {
    String getIndexName();
    Class<T> getDtoClass();
    TypeMapping getMapping();
    IndexSettings getSettings();
}

