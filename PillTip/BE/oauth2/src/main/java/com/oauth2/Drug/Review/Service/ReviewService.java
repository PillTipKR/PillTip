package com.oauth2.Drug.Review.Service;

import com.oauth2.Drug.DrugInfo.Domain.Drug;
import com.oauth2.Drug.DrugInfo.Repository.DrugRepository;
import com.oauth2.Drug.Review.Domain.*;
import com.oauth2.Drug.Review.Dto.*;
import com.oauth2.Drug.Review.Repository.*;
import com.oauth2.User.Auth.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final DrugRepository drugRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final TagRepository tagRepository;
    private final ReviewTagRepository reviewTagRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    /**
     * 리뷰 생성
     */
    public Long createReview(User user, ReviewCreateRequest request) {
        Drug drug = drugRepository.findById(request.getDrugId())
                .orElseThrow(() -> new RuntimeException("약을 찾을 수 없습니다"));

        Review review = new Review();
        review.setUser(user);
        review.setDrug(drug);
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        reviewRepository.save(review);

        saveReviewImages(review, request.getImageUrls());
        saveReviewTags(review, request.getTags());

        return review.getId();
    }

    private void saveReviewImages(Review review, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        IntStream.range(0, imageUrls.size())
                .mapToObj(i -> {
                    ReviewImage image = new ReviewImage();
                    image.setReview(review);
                    image.setImageUrl(imageUrls.get(i));
                    image.setSortOrder(i);
                    return image;
                })
                .forEach(reviewImageRepository::save);
    }

    private void saveReviewTags(Review review, Map<String, List<String>> tags) {
        if (tags == null || tags.isEmpty()) {
            return;
        }
        tags.forEach((typeStr, tagNames) -> {
            TagType type = TagType.valueOf(typeStr.toUpperCase());
            tagNames.forEach(tagName -> {
                Tag tag = tagRepository.findByNameAndType(tagName, type)
                        .orElseGet(() -> tagRepository.save(new Tag(tagName, type)));

                ReviewTag reviewTag = new ReviewTag();
                reviewTag.setReview(review);
                reviewTag.setTag(tag);
                reviewTagRepository.save(reviewTag);
            });
        });
    }

    /**
     * 리뷰 삭제
     */
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다"));

        if (!review.getUser().getId().equals(userId)) {
            throw new RuntimeException("삭제 권한이 없습니다");
        }

        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public List<UserReviewResponse> getReviewsByUser(Long userId) {
        List<Review> reviews = reviewRepository.findByUserId(userId);
        Set<Long> likedIdSet = getLikedReviewIds(userId, reviews);

        return reviews.stream()
                .map(review -> new UserReviewResponse(
                        review.getDrug().getId(),
                        review.getDrug().getName(),
                        ReviewResponse.from(review, userId, likedIdSet.contains(review.getId()))
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getPagedReviews(Long drugId, Long userId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByDrugId(drugId, pageable);
        List<Review> reviews = reviewPage.getContent();
        Set<Long> likedIdSet = getLikedReviewIds(userId, reviews);

        List<ReviewResponse> reviewResponses = reviews.stream()
                .map(review -> ReviewResponse.from(review, userId, likedIdSet.contains(review.getId())))
                .toList();

        return new PageImpl<>(reviewResponses, pageable, reviewPage.getTotalElements());
    }

    private Set<Long> getLikedReviewIds(Long userId, List<Review> reviews) {
        if (userId == null || reviews.isEmpty()) {
            return Collections.emptySet();
        }
        List<Long> reviewIds = reviews.stream().map(Review::getId).toList();
        return new HashSet<>(reviewLikeRepository.findLikedReviewIds(userId, reviewIds));
    }

    public Sort getSort(String key, String direction) {
        Sort.Direction dir = Sort.Direction.DESC; // 기본값
        try {
            dir = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException e) {
            // "asc" 또는 "desc"가 아닌 경우 기본값 사용, 로깅 등을 추가할 수 있음
        }

        return switch (key.toLowerCase()) {
            case "rating" -> Sort.by(dir, "rating");
            case "likes" -> Sort.by(dir, "likeCount");
            case "latest", "createdat" -> Sort.by(dir, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    @Transactional(readOnly = true)
    public ReviewStats getReviewStats(Long drugId) {
        List<Review> reviews = reviewRepository.findByDrugIdWithReviewTags(drugId);

        RatingStatsResponse ratingStats = computeRatingStats(reviews);
        Map<TagType, TagStatsDto> tagStats = computeTagStats(reviews);

        return new ReviewStats(
                (long) reviews.size(),
                ratingStats,
                tagStats
        );
    }

    private RatingStatsResponse computeRatingStats(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return new RatingStatsResponse(0.0, Collections.emptyMap());
        }

        double averageRating = reviews.stream()
                .mapToDouble(Review::getRating)
                .average().orElse(0.0);

        Map<Integer, Long> ratingDistribution = reviews.stream()
                .collect(Collectors.groupingBy(
                        review -> (int) Math.floor(review.getRating()),
                        Collectors.counting()
                ));

        // 1~5점까지 모든 점수에 대한 기본값 0을 보장
        IntStream.rangeClosed(1, 5).forEach(rating -> ratingDistribution.putIfAbsent(rating, 0L));

        return new RatingStatsResponse(averageRating, ratingDistribution);
    }

    private Map<TagType, TagStatsDto> computeTagStats(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<TagType, Map<String, Long>> tagCountsByType = new EnumMap<>(TagType.class);
        for (Review review : reviews) {
            for (ReviewTag reviewTag : review.getReviewTags()) {
                Tag tag = reviewTag.getTag();
                tagCountsByType
                        .computeIfAbsent(tag.getType(), k -> new HashMap<>())
                        .merge(tag.getName(), 1L, Long::sum);
            }
        }

        Map<TagType, TagStatsDto> tagStatsMap = new EnumMap<>(TagType.class);
        for (TagType type : TagType.values()) {
            Map<String, Long> tagCounts = tagCountsByType.getOrDefault(type, Collections.emptyMap());

            long total = tagCounts.values().stream().mapToLong(Long::longValue).sum();
            Optional<Map.Entry<String, Long>> mostUsedEntry = tagCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue());

            String mostUsedTag = mostUsedEntry.map(Map.Entry::getKey).orElse(null);
            long maxCount = mostUsedEntry.map(Map.Entry::getValue).orElse(0L);

            tagStatsMap.put(type, new TagStatsDto(mostUsedTag, maxCount, total));
        }

        return tagStatsMap;
    }
}

