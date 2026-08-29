package com.example.demo.controller;

import com.example.demo.entity.Item;
import com.example.demo.kafka.ItemEventProducer;
import com.example.demo.repository.ItemRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemRepository itemRepository;
    private final ItemEventProducer eventProducer;

    public ItemController(ItemRepository itemRepository, ItemEventProducer eventProducer) {
        this.itemRepository = itemRepository;
        this.eventProducer = eventProducer;
    }

    @GetMapping
    public List<Item> getAll() {
        return itemRepository.findAll();
    }

    @PostMapping
    public Item create(@RequestBody Item item) {
        Item saved = itemRepository.save(item);
        eventProducer.publishCreated(saved.getId(), saved.getName());
        return saved;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getById(@PathVariable Long id) {
        return itemRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!itemRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        itemRepository.deleteById(id);
        eventProducer.publishDeleted(id);
        return ResponseEntity.noContent().build();
    }
}
