package com.fisch_tradehub.tradehub_core.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fisch_tradehub.tradehub_core.common.Constants;
import com.fisch_tradehub.tradehub_core.entity.Cart;
import com.fisch_tradehub.tradehub_core.entity.Fish;
import com.fisch_tradehub.tradehub_core.entity.User;
import com.fisch_tradehub.tradehub_core.exception.ResourceNotFoundException;
import com.fisch_tradehub.tradehub_core.repository.CartRepository;
import com.fisch_tradehub.tradehub_core.repository.FishRepository;
import com.fisch_tradehub.tradehub_core.repository.UserRepository;
import com.fisch_tradehub.tradehub_core.web.dto.AddToCartRequest;
import com.fisch_tradehub.tradehub_core.web.dto.CartItemDTO;

import lombok.RequiredArgsConstructor;

/**
 * Service for managing shopping cart operations.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final FishRepository fishRepository;

    /**
     * Get all cart items for the current user.
     * 
     * @param username the username
     * @return list of cart items
     */
    public List<CartItemDTO> getMyCart(String username) {
        User user = getUser(username);
        return cartRepository.findByUser(user).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Add an item to the cart or update quantity if it already exists.
     * 
     * @param username the username
     * @param request the item to add
     * @throws ResourceNotFoundException if user or fish not found
     */
    @Transactional
    public void addToCart(String username, AddToCartRequest request) {
        User user = getUser(username);
        Fish fish = fishRepository.findById(request.fishId())
                .orElseThrow(() -> new ResourceNotFoundException(Constants.FISH_NOT_FOUND));

        Cart cartItem = cartRepository.findByUserIdAndFishId(user.getId(), fish.getId())
                .orElse(Cart.builder()
                        .user(user)
                        .fish(fish)
                        .quantity(0)
                        .build());

        cartItem.setQuantity(cartItem.getQuantity() + request.quantity());
        cartRepository.save(cartItem);
    }

    /**
     * Remove an item from the cart.
     * 
     * @param username the username
     * @param fishId the fish ID to remove
     */
    @Transactional
    public void removeFromCart(String username, Long fishId) {
        User user = getUser(username);
        cartRepository.findByUserIdAndFishId(user.getId(), fishId)
                .ifPresent(cartRepository::delete);
    }

    /**
     * Clear all items from the user's cart.
     * 
     * @param username the username
     */
    @Transactional
    public void clearCart(String username) {
        User user = getUser(username);
        cartRepository.deleteByUserId(user.getId());
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.USER_NOT_FOUND));
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
