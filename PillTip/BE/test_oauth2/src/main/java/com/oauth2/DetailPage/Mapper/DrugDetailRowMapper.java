package com.oauth2.DetailPage.Mapper;

import com.oauth2.DetailPage.Dto.DrugDetail;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class DrugDetailRowMapper implements RowMapper<DrugDetail> {

    public DrugDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
        DrugDetail drugDetail = new DrugDetail();
        drugDetail.setId(rs.getLong("id"));
        drugDetail.setName(rs.getString("drug_name"));
        drugDetail.setManufacturer(rs.getString("manufacturer"));

        // 성분을 리스트로 변환
        String ingredientsString = rs.getString("ingredients");
        if (ingredientsString != null && !ingredientsString.isEmpty()) {
            List<String> ingredients = Arrays.asList(ingredientsString.split(","));
            drugDetail.setIngredients(ingredients);
        }

        return drugDetail;
    }
}
