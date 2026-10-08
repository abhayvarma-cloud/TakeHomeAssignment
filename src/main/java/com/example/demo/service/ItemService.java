package com.example.demo.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.hibernate.sql.ast.tree.expression.Star;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.demo.cache.ItemPageFetcher;
import com.example.demo.entity.Item;
import com.example.demo.entity.ItemSeq;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.ItemSeqRepository;
import com.example.demo.repository.OutboxRepository;

import jakarta.transaction.Transactional;
@Service
public class ItemService {
	
	 private static final int MAX_SIZE = 100;
	 private static final int WINDOW = 10;
	 private final ItemSeqRepository seqRepo;
	    private final ItemRepository itemRepo;
	    private final  OutboxService outbox;
	    private final ItemPageFetcher pageFetcher;
	    
	    public record PageResponse(
	            List<Item> items,             // newest first, for display only
	            int page,
	            int size,
	            long totalElements,
	            int totalPages,
	            List<Integer> pageNumbers,    // highest first, e.g. 105, 104, ... 96
	            boolean lastPage) {}

	    public ItemService(ItemSeqRepository seqRepo, ItemRepository itemRepo,OutboxService outbox,ItemPageFetcher pageFetcher) {
	        this.seqRepo = seqRepo;
	        this.itemRepo = itemRepo;
	        this.outbox = outbox;
	        this.pageFetcher = pageFetcher;
	        
	    }

	    @Transactional
	    public Item create(String name, String description) {
	        // Lock held until commit: numbers are handed out one at a time, with no gaps
	        ItemSeq counter = seqRepo.findById(1)
	                .orElseThrow(() -> new IllegalStateException("item_seq row missing, check data.sql"));
	        long next = counter.getLastSeq() + 1;
	        counter.setLastSeq(next);           // flushed at commit
	        
	        Item item = new Item(name, description);
	        item.setSeq(next);
	        item =itemRepo.save(item);
	        outbox.add("notification-events",
	        		"Item",
	                   String.valueOf(item.getId()),
	                   
	                   "Item_Created",
	                   Map.of("ItemId", item.getId(),
	                          "customer", "",
	                          "to", ""));
	        
	        
	        
	        return item;         // rolls back together with the counter if it fails
	    }
	    
	    public PageResponse getPage(Integer page, int size) {
	    	//max size not over 100
	        size = validSize(size);
//gets max records number from database
	        long total = itemRepo.maxSeq();  
	        //calculate total pages based on no. of records in table and page pref sent by user
	        int totalPages = (int) Math.max(1, (total + size - 1) / size);

	        int p = (page == null) ? totalPages : page;       // default = newest page with latest records
	        if (p < 1 || p > totalPages) {
	            throw new IllegalArgumentException("page must be between 1 and " + totalPages);
	        }
//start and  end of seq  for the page
	        long from = (long) (p - 1) * size + 1;
	        long to = Math.min((long) p * size, total);      
	        boolean fullPage = to == (long) p * size;
	        List<Item> items = fullPage
	                ? pageFetcher.fetchRange(from, to)                                   // cached
	                : itemRepo.findBySeqBetween(from, to, Sort.by("seq").descending());  // partial: always live
	        int upper = Math.min(totalPages, p + WINDOW / 2);
	        int lower = Math.max(1, upper - WINDOW + 1);
	        List<Integer> pageNumbers = IntStream.iterate(upper, i -> i - 1)
	                .limit(upper - lower + 1)
	                .boxed()
	                .toList();

	        return new PageResponse(items, p, size, total, totalPages, pageNumbers, p == totalPages);
	    }
	    

	    private int validSize(int size) {
	        if (size < 1 || size > MAX_SIZE) {
	            throw new IllegalArgumentException("size must be between 1 and " + MAX_SIZE);
	        }
	        return size;
	    }
	   

}
