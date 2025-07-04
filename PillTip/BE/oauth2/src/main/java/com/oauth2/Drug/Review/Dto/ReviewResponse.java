package com.oauth2.Drug.Review.Dto;

import com.oauth2.Drug.Review.Domain.Review;
import com.oauth2.Drug.Review.Domain.ReviewImage;
import com.oauth2.Drug.Review.Domain.TagType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponse {

    private Long id;
    private String userNickname;
    private String gender;
    private Boolean isMine;
    private Boolean isLiked;
    private Float rating;
    private int likeCount;
    private String content;
    private List<String> imageUrls;
    private List<String> efficacyTags;
    private List<String> sideEffectTags;
    private List<String> otherTags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public static ReviewResponse from(Review review, Long userId, boolean isLiked) {
        String gender = review.getUser().getUserProfile().getGender().name().equals("MALE") ? "남성":"여성";
        return ReviewResponse.builder()
                .id(review.getId())
                .userNickname(review.getUser().getNickname())
                .gender(gender)
                .rating(review.getRating())
                .likeCount(review.getLikeCount())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .imageUrls(review.getImages().stream().map(ReviewImage::getImageUrl).toList())
                .efficacyTags(getTagsByType(review, TagType.EFFICACY))
                .sideEffectTags(getTagsByType(review, TagType.SIDE_EFFECT))
                .otherTags(getTagsByType(review, TagType.OTHER))
                .isMine(review.getUser().getId().equals(userId))
                .isLiked(isLiked)
                .build();
    }

    private static List<String> getTagsByType(Review review, TagType type) {
        if (review.getReviewTags() == null) return List.of();

        return review.getReviewTags().stream()
                .filter(rt -> rt.getTag().getType() == type)
                .map(rt -> rt.getTag().getName())
                .toList();
    }

}

