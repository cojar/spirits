package com.ll.spirits.review;

import com.ll.spirits.product.Product;
import com.ll.spirits.product.ProductService;
import com.ll.spirits.user.SiteUser;
import com.ll.spirits.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RequestMapping("/review")
@RequiredArgsConstructor // 변수를 포함하는 생성자를 자동으로 생성.
@Controller
public class ReviewController {
    private final ProductService productService;
    private final UserService userService;
    private final ReviewService reviewService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/{id}")
    public String createReview(@PathVariable("id") Integer id, @Valid ReviewForm reviewForm,
                               BindingResult bindingResult, Principal principal,
                               Model model) {
        Product product = this.productService.getProduct(id);
        // TODO: 리뷰를 저장한다.
        SiteUser siteUser = this.userService.getUser(principal.getName());
        if (bindingResult.hasErrors()) {
            List<Review> reviews = this.reviewService.getReviewsForProduct(product); // 기존 리뷰 리스트를 가져옴
            model.addAttribute("product", product);
            model.addAttribute("reviews", reviews); // 기존 리뷰 리스트도 함께 모델에 추가
            return "product_detail";
        }
        System.out.println(reviewForm);
        Review review = this.reviewService.create(product, reviewForm.getContent(), siteUser);
        return String.format("redirect:/product/detail/%s#review_%s", review.getProduct().getId(), review.getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String reviewModify(@PathVariable("id") Long id,
                               ReviewForm reviewForm,
                               Principal principal,
                               Model model) {
        Review review = this.reviewService.getReview(id);
        if (!review.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        reviewForm.setContent(review.getContent());

        // 리뷰 목록과 리뷰 폼을 다시 전달
//        model.addAttribute("reviews", reviewService.getList()); // 리뷰 목록 데이터
        model.addAttribute("reviewForm", new ReviewForm()); // 리뷰 폼 초기화

        return String.format("redirect:/product/detail/%s#review_%s", review.getProduct().getId(), review.getId());
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String reviewModify(@Valid ReviewForm reviewForm, @PathVariable("id") Long id,
                               BindingResult bindingResult, Principal principal,
                               Model model) {
        if (bindingResult.hasErrors()) {
            // 기존 리뷰 객체를 가져오기
            Review review = this.reviewService.getReview(id);

            // 리뷰 폼만 모델에 추가
            model.addAttribute("reviewForm", reviewForm);
            model.addAttribute("review", review); // 수정하려는 리뷰 객체도 추가

            // 리뷰 목록도 모델에 추가
            List<Review> reviews = this.reviewService.getReviewsForProduct(review.getProduct());
            model.addAttribute("reviews", reviews);

            return "product_detail";
        }
        Review review = this.reviewService.getReview(id);
        if (!review.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        this.reviewService.modify(review, reviewForm.getContent());
        return String.format("redirect:/product/detail/%s#review_%s", review.getProduct().getId(), review.getId());
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String reviewDelete(Principal principal, @PathVariable("id") Long id) {
        Review review = this.reviewService.getReview(id);
        if (!review.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }
        this.reviewService.delete(review);
        return String.format("redirect:/product/detail/%s", review.getProduct().getId());
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String reviewVote(@PathVariable("id") Long id,
                             Principal principal) {
        Review review = this.reviewService.getReview(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.reviewService.vote(review, siteUser);
        return String.format("redirect:/question/detail/%s#answer_%s", review.getProduct().getId(), review.getId());
    }
}
