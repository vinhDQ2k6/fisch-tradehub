package com.fisch_tradehub.tradehub_core.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fisch_tradehub.tradehub_core.common.Constants;
import com.fisch_tradehub.tradehub_core.entity.Fish;
import com.fisch_tradehub.tradehub_core.exception.ResourceNotFoundException;
import com.fisch_tradehub.tradehub_core.repository.FishRepository;
import com.fisch_tradehub.tradehub_core.web.dto.FishDTO;
import com.fisch_tradehub.tradehub_core.web.dto.FishRequest;

/**
 * Service for managing fish (product) catalog.
 */
@Service
@Transactional(readOnly = true)
public class FishService {

    private final FishRepository fishRepository;

    public FishService(FishRepository fishRepository) {
        this.fishRepository = fishRepository;
    }

    /**
     * Get all fish sorted by name.
     * 
     * @return list of all fish
     */
    public List<FishDTO> findAll() {
        return fishRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Get a specific fish by ID.
     * 
     * @param id the fish ID
     * @return the fish details
     * @throws ResourceNotFoundException if fish not found
     */
    public FishDTO findById(Long id) {
        Fish fish = getByIdOrThrow(id);
        return toDto(fish);
    }

    /**
     * Create a new fish entry.
     * 
     * @param request the fish details
     * @return the created fish
     */
    @Transactional
    public FishDTO create(FishRequest request) {
        Fish fish = Fish.builder()
                .name(request.name())
                .rarity(request.rarity())
                .value(request.value())
                .weight(request.weight())
                .build();

        Fish saved = fishRepository.save(fish);
        return toDto(saved);
    }

    /**
     * Update an existing fish entry.
     * 
     * @param id the fish ID
     * @param request the updated details
     * @return the updated fish
     * @throws ResourceNotFoundException if fish not found
     */
    @Transactional
    public FishDTO update(Long id, FishRequest request) {
        Fish fish = getByIdOrThrow(id);
        fish.setName(request.name());
        fish.setRarity(request.rarity());
        fish.setValue(request.value());
        fish.setWeight(request.weight());

        Fish saved = fishRepository.save(fish);
        return toDto(saved);
    }

    /**
     * Delete a fish entry.
     * 
     * @param id the fish ID
     * @throws ResourceNotFoundException if fish not found
     */
    @Transactional
    public void delete(Long id) {
        Fish fish = getByIdOrThrow(id);
        fishRepository.delete(fish);
    }

    private Fish getByIdOrThrow(Long id) {
        return fishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.FISH_NOT_FOUND));
    }

    private FishDTO toDto(Fish fish) {
        return new FishDTO(
                fish.getId(),
                fish.getName(),
                fish.getRarity(),
                fish.getValue(),
                fish.getWeight()
        );
    }
}
