package com.oauth2.AutoComplete.Dto;


/**
 * @param id            약의 상세 페이지로 이동할 수 있는 ID
 * @param drugName      추천 텍스트
 * @param manufacturer  약의 제조사
 * @param imageUrl      약 이미지 URL
 */

public record AutocompleteResult(Long id, String drugName, String manufacturer, String imageUrl) {
}
