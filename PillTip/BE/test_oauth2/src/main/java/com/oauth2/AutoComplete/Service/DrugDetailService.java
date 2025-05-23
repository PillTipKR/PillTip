package com.oauth2.AutoComplete.Service;

import com.oauth2.DetailPage.Dto.DrugDetail;
import com.oauth2.DetailPage.Mapper.DrugDetailRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DrugDetailService {

    private final JdbcTemplate jdbcTemplate;

    public DrugDetailService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DrugDetail getDrugDetailById(int drugId) {
        String query = "SELECT * FROM drug WHERE id = ?";
        return jdbcTemplate.queryForObject(query, new Object[]{drugId}, new DrugDetailRowMapper());
    }
}

