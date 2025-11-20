package com.fisch_tradehub.tradehub_core.web.api;

import java.net.URI;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fisch_tradehub.tradehub_core.service.FishService;
import com.fisch_tradehub.tradehub_core.web.dto.FishDTO;
import com.fisch_tradehub.tradehub_core.web.dto.FishRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/fish")
@RequiredArgsConstructor
public class FishController {

    private final FishService fishService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<FishDTO>> getAllFishes() {
        List<FishDTO> fishes = fishService.findAll();
        return ResponseEntity.ok(fishes);
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FishDTO> getFish(@PathVariable Long id) {
        FishDTO fish = fishService.findById(id);
        return ResponseEntity.ok(fish);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FishDTO> createFish(@Valid @RequestBody FishRequest request) {
        FishDTO created = fishService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FishDTO> updateFish(@PathVariable Long id, @Valid @RequestBody FishRequest request) {
        FishDTO updated = fishService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteFish(@PathVariable Long id) {
        fishService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
