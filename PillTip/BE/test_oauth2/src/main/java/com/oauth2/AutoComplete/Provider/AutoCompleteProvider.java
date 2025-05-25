package com.oauth2.AutoComplete.Provider;

import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import com.oauth2.AutoComplete.Dto.AutocompleteDTO;
import com.oauth2.Util.Provider.CommonSettingsProvider;
import com.oauth2.Util.Provider.IndexMappingProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AutoCompleteProvider implements IndexMappingProvider<AutocompleteDTO> {

    private final CommonSettingsProvider settingsProvider;

    public AutoCompleteProvider(CommonSettingsProvider settingsProvider) {
        this.settingsProvider = settingsProvider;
    }

    @Value("${elastic.autocomplete}")
    private String index;

    @Override
    public String getIndexName() {
        return index;
    }

    @Override
    public Class<AutocompleteDTO> getDtoClass() {
        return AutocompleteDTO.class;
    }

    @Override
    public TypeMapping getMapping() {
        return new TypeMapping.Builder()
                .properties("type", p -> p.keyword(k -> k)) // drugName, ingredient 등
                .properties("value", p -> p
                        .text(t -> t
                                .fields("edge", f -> f.text(edge -> edge.analyzer(settingsProvider.getAutoEdgeNGramAnalyzer())))
                                .fields("gram", f -> f.text(gram -> gram.analyzer(settingsProvider.getAutoNGramAnalyzer())))
                        )
                ).build();
    }
    @Override
    public IndexSettings getSettings() {
        return settingsProvider.getDefaultSettings();
    }
}
