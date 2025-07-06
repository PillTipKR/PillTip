package com.oauth2.Drug.Review.Controller;

import com.oauth2.Drug.Review.Dto.*;
import com.oauth2.Drug.Review.Service.ReviewLikeService;
import com.oauth2.Drug.Review.Service.ReviewService;
import com.oauth2.User.Auth.Dto.ApiResponse;
import com.oauth2.User.Auth.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewLikeService reviewLikeService;

    /**
     * 리뷰 작성
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Long>> createReview(
            @AuthenticationPrincipal User user,
            @RequestBody ReviewCreateRequest request
    ) {
        Long reviewId = reviewService.createReview(user, request);
        return ResponseEntity.ok(ApiResponse.success("리뷰가 등록되었습니다", reviewId));
    }

    /**
     * 리뷰 삭제
     */
    @DeleteMapping("/delete/{reviewId}")
    public ResponseEntity<ApiResponse<String>> deleteReview(
            @AuthenticationPrincipal User user,
            @PathVariable Long reviewId
    ) {
        reviewService.deleteReview(user.getId(), reviewId);
        return ResponseEntity.ok(ApiResponse.success("리뷰가 삭제되었습니다", null));
    }

    /**
     * 특정 유저의 리뷰 조회
     */
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<UserReviewResponse>>> getUserReviews(
            @AuthenticationPrincipal User user) {
        List<UserReviewResponse> response = reviewService.getReviewsByUser(user.getId());
        return ResponseEntity.ok(ApiResponse.success("조회 성공", response));
    }

    /**
     * 특정 약의 리뷰 조회
     */
    @GetMapping("/drug")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviews(
            @AuthenticationPrincipal User user,
            @RequestParam Long drugId,
            @RequestParam(defaultValue = "latest") String sortKey,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Sort sort = reviewService.getSort(sortKey, direction);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ReviewResponse> reviews = reviewService.getPagedReviews(drugId, user.getId(), pageable);

        return ResponseEntity.ok(ApiResponse.success("조회 성공", reviews));
    }

    @GetMapping("/drug/{drugId}/stats")
    public ResponseEntity<ApiResponse<ReviewStats>> getDrugReviews(
            @AuthenticationPrincipal User user, @PathVariable Long drugId) {
        ReviewStats reviewStats = reviewService.getReviewStats(drugId);
        return ResponseEntity.ok(ApiResponse.success("조회 성공", reviewStats));
    }

    @PostMapping("/{reviewId}/like")
    public ResponseEntity<ApiResponse<String>> toggleLike(
            @AuthenticationPrincipal User user,
            @PathVariable Long reviewId
    ) {
        boolean liked = reviewLikeService.toggleLike(user.getId(), reviewId);
        String message = liked ? "좋아요 추가됨" : "좋아요 취소됨";
        return ResponseEntity.ok(ApiResponse.success(message));
    }
}
