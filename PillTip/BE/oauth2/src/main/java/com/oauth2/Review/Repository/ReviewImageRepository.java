package com.oauth2.Review.Repository;

import com.oauth2.Review.Domain.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

    List<ReviewImage> findByReviewIdOrderBySortOrderAsc(Long reviewId);
}

