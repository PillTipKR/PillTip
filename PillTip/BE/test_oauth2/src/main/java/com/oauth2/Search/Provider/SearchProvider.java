package com.oauth2.Search.Provider;

import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import com.oauth2.Search.Dto.SearchIndexDTO;
import com.oauth2.Elasticsearch.Util.Provider.CommonSettingsProvider;
import com.oauth2.Elasticsearch.Util.Provider.IndexMappingProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SearchProvider implements IndexMappingProvider<SearchIndexDTO> {

    @Value("${elastic.search.index}")
    private String index;
    @Value("${elastic.nGramAnalyzer}")
    private String autoNGramAnalyzer;
    @Value("${elastic.edgeNGramAnalyzer}")
    private String autoEdgeNGramAnalyzer;
    @Value("${elastic.drug.id}")
    private String id;
    @Value("${elastic.drug.drug}")
    private String drugName;
    @Value("${elastic.drug.manufacturer}")
    private String manufacturer;

    private final CommonSettingsProvider settingsProvider;

    public SearchProvider(CommonSettingsProvider settingsProvider) {
        this.settingsProvider = settingsProvider;
    }

    @Override
    public String getIndexName() {
        return index;
    }

    @Override
    public Class<SearchIndexDTO> getDtoClass() {
        return SearchIndexDTO.class;
    }

    @Override
    public TypeMapping getMapping() {
        return new TypeMapping.Builder()
                .properties(id, p -> p
                        .keyword(k -> k.index(false))
                )
                .properties(drugName, p -> p
                        .text(t -> t.fields("edge", f->f.text(edge -> edge.analyzer(autoEdgeNGramAnalyzer)))
                                .fields("gram", f->f.text(gram->gram.analyzer(autoNGramAnalyzer))))
                )
                .properties(manufacturer, p -> p
                        .text(t -> t.fields("edge", f->f.text(edge -> edge.analyzer(autoEdgeNGramAnalyzer)))
                                .fields("gram", f->f.text(gram->gram.analyzer(autoNGramAnalyzer))))
                )
                .build();
    }
    @Override
    public IndexSettings getSettings() {
        return settingsProvider.getDefaultSettings();
    }
}
