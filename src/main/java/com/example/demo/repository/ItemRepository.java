package com.example.demo.repository;

import com.example.demo.entity.Item;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
	List<Item> findBySeqBetween(long fromSeq, long toSeq, Sort sort);

    @Query("select coalesce(max(i.seq), 0) from Item i")
    long maxSeq();
}
