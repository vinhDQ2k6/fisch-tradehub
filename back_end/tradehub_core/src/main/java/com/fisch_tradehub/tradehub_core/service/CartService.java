package com.fisch_tradehub.tradehub_core.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fisch_tradehub.tradehub_core.entity.Cart;
import com.fisch_tradehub.tradehub_core.entity.Fish;
import com.fisch_tradehub.tradehub_core.entity.User;
import com.fisch_tradehub.tradehub_core.repository.CartRepository;
import com.fisch_tradehub.tradehub_core.repository.FishRepository;
import com.fisch_tradehub.tradehub_core.repository.UserRepository;
import com.fisch_tradehub.tradehub_core.web.dto.AddToCartRequest;
import com.fisch_tradehub.tradehub_core.web.dto.CartItemDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final FishRepository fishRepository;

    public List<CartItemDTO> getMyCart(String username) {
        User user = getUser(username);
        return cartRepository.findByUser(user).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void addToCart(String username, AddToCartRequest request) {
        User user = getUser(username);
        Fish fish = fishRepository.findById(request.fishId())
                .orElseThrow(() -> new RuntimeException("Fish not found"));

        Cart cartItem = cartRepository.findByUserIdAndFishId(user.getId(), fish.getId())
                .orElse(Cart.builder()
                        .user(user)
                        .fish(fish)
                        .quantity(0)
                        .build());

        cartItem.setQuantity(cartItem.getQuantity() + request.quantity());
        cartRepository.save(cartItem);
    }

    @Transactional
    public void removeFromCart(String username, Long fishId) {
        User user = getUser(username);
        cartRepository.findByUserIdAndFishId(user.getId(), fishId)
                .ifPresent(cartRepository::delete);
    }

    @Transactional
    public void clearCart(String username) {
        User user = getUser(username);
        cartRepository.deleteByUserId(user.getId());
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private CartItemDTO toDto(Cart cart) {
        Fish fish = cart.getFish();
        return new CartItemDTO(
                fish.getId(),
                fish.getName(),
                fish.getRarity(),
                fish.getValue(),
                fish.getWeight(),
                cart.getQuantity()
        );
    }
}
