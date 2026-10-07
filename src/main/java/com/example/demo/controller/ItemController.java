package com.example.demo.controller;

import com.example.demo.entity.Item;
import com.example.demo.kafka.ItemEventProducer;
import com.example.demo.repository.ItemRepository;
import com.example.demo.service.ItemService;
import com.example.demo.service.ItemService.PageResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {
	public record CreateItemRequest(String name, String description) {}
    private final ItemRepository itemRepository;
    private final ItemEventProducer eventProducer;
    private final ItemService itemService ;

    public ItemController(ItemRepository itemRepository, ItemEventProducer eventProducer,ItemService itemService ) {
        this.itemRepository = itemRepository;
        this.eventProducer = eventProducer;
        this.itemService = itemService;
    }

   /* @GetMapping
    public List<Item> getAll() {
        return itemRepository.findAll();
    }*/
    
    @GetMapping
    public PageResponse list(@RequestParam(required = false) Integer page,
                             @RequestParam(defaultValue = "50") int size) {
        return itemService.getPage(page, size);
    }

   /* @PostMapping
    public Item create(@RequestBody Item item) {
        Item saved = itemRepository.save(item);
        eventProducer.publishCreated(saved.getId(), saved.getName());
        return saved;
    }*/
    
    @PostMapping
    public Item create(@RequestBody CreateItemRequest req) {
        return itemService.create(req.name(), req.description());
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
