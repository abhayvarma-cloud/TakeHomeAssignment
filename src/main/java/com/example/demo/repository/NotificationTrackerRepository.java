package com.example.demo.repository;


import com.example.demo.entity.NotificationTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface NotificationTrackerRepository extends JpaRepository<NotificationTracker, Long> {

    Optional<NotificationTracker> findByObjectIdAndType(String objectId, String type);
}
