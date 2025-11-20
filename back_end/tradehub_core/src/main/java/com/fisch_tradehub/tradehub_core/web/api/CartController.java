package com.fisch_tradehub.tradehub_core.web.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fisch_tradehub.tradehub_core.service.CartService;
import com.fisch_tradehub.tradehub_core.web.dto.AddToCartRequest;
import com.fisch_tradehub.tradehub_core.web.dto.CartItemDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<List<CartItemDTO>> getMyCart(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cartService.getMyCart(userDetails.getUsername()));
    }

    @PostMapping
    public ResponseEntity<Void> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddToCartRequest request) {
        cartService.addToCart(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{fishId}")
    public ResponseEntity<Void> removeFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long fishId) {
        cartService.removeFromCart(userDetails.getUsername(), fishId);
        return ResponseEntity.ok().build();
    }
}
