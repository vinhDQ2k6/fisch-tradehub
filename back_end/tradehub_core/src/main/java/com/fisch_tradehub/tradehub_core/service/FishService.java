package com.fisch_tradehub.tradehub_core.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fisch_tradehub.tradehub_core.entity.Fish;
import com.fisch_tradehub.tradehub_core.repository.FishRepository;
import com.fisch_tradehub.tradehub_core.web.dto.FishDTO;
import com.fisch_tradehub.tradehub_core.web.dto.FishRequest;

@Service
@Transactional(readOnly = true)
public class FishService {

    private final FishRepository fishRepository;

    public FishService(FishRepository fishRepository) {
        this.fishRepository = fishRepository;
    }

    public List<FishDTO> findAll() {
        return fishRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(this::toDto)
                .toList();
    }

    public FishDTO findById(Long id) {
        Fish fish = getByIdOrThrow(id);
        return toDto(fish);
    }

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

    @Transactional
    public void delete(Long id) {
        Fish fish = getByIdOrThrow(id);
        fishRepository.delete(fish);
    }

    private Fish getByIdOrThrow(Long id) {
        return fishRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fish not found"));
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
