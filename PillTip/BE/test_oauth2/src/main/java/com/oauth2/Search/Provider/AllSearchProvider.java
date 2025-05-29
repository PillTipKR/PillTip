package com.oauth2.Search.Provider;

import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import com.oauth2.Elasticsearch.Util.Provider.CommonSettingsProvider;
import com.oauth2.Elasticsearch.Util.Provider.IndexMappingProvider;
import com.oauth2.Search.Dto.IngredientDetail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AllSearchProvider implements IndexMappingProvider<IngredientDetail> {

    @Value("${elastic.allSearch}")
    private String index;
    @Value("${elastic.nGramAnalyzer}")
    private String autoNGramAnalyzer;
    @Value("${elastic.edgeNGramAnalyzer}")
    private String autoEdgeNGramAnalyzer;
    @Value("${elastic.drug.id}")
    private String id;
    @Value("${elastic.drug.ingredient}")
    private String ingredient;
    @Value("${elastic.drug.drug}")
    private String drugName;
    @Value("${elastic.drug.manufacturer}")
    private String manufacturer;


    private final CommonSettingsProvider settingsProvider;

    public AllSearchProvider(CommonSettingsProvider settingsProvider) {
        this.settingsProvider = settingsProvider;
    }

    @Override
    public String getIndexName() {
        return index;
    }

    @Override
    public Class<IngredientDetail> getDtoClass() {
        return IngredientDetail.class;
    }

    @Override
    public TypeMapping getMapping() {
        return new TypeMapping.Builder()
                .properties(id, p -> p
                        .keyword(k -> k.index(true))
                )
                .properties(drugName, p -> p
                        .text(t -> t.fields("edge", f->f.text(edge -> edge.analyzer(autoEdgeNGramAnalyzer)))
                                .fields("gram", f->f.text(gram->gram.analyzer(autoNGramAnalyzer))))
                )
                .properties(ingredient, p -> p
                        .nested(n -> n
                                .properties("name", np -> np
                                        .text(t -> t
                                                .fields("edge", f -> f.text(edge -> edge.analyzer(autoEdgeNGramAnalyzer)))
                                                .fields("gram", f -> f.text(gram -> gram.analyzer(autoNGramAnalyzer)))
                                        )
                                )
                                .properties("dose", d -> d.text(t->t))
                                .properties("is_main", np -> np.boolean_(b -> b))
                                .properties("priority", np -> np.integer(i -> i))
                        )
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

