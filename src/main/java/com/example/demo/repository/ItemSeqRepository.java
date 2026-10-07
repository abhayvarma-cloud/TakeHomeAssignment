package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.ItemSeq;

import jakarta.persistence.LockModeType;
@Repository
public interface ItemSeqRepository extends JpaRepository<ItemSeq, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ItemSeq> findById(Integer id);   // overrides the inherited method
}
