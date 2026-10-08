package com.example.demo.cache;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.example.demo.entity.Item;
import com.example.demo.repository.ItemRepository;

import java.util.List;

@Component
public class ItemPageFetcher {

    private final ItemRepository itemRepo;

    public ItemPageFetcher(ItemRepository itemRepo) {
        this.itemRepo = itemRepo;
    }

    @Cacheable(cacheNames = "itemPages", key = "#from + '-' + #to")
    public List<Item> fetchRange(long from, long to) {
        // immutable copy, so nobody can modify the cached list
        return List.copyOf(itemRepo.findBySeqBetween(from, to, Sort.by("seq").descending()));
    }
}