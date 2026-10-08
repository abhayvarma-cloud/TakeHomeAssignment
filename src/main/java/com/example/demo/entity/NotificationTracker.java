package com.example.demo.entity;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "notification_tracker",
uniqueConstraints = @UniqueConstraint(columnNames = {"object_id", "type"}))
public class NotificationTracker {

    public static final int PENDING = 0;
    public static final int SENT = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                       // its own auto-generated id

    @Column(name = "object_id", nullable = false)
    private String objectId;               // e.g. the order id

    @Column(nullable = false)
    private String type;                   // e.g. ORDER_CONFIRMED

    @Column(nullable = false)
    private Integer status = PENDING;      // 0 or 1

    protected NotificationTracker() {}     // required by JPA

    public NotificationTracker(String objectId, String type) {
        this.objectId = objectId;
        this.type = type;
    }

    public Long getId() { return id; }
    public String getObjectId() { return objectId; }
    public String getType() { return type; }
    public Integer getStatus() { return status; }

    public void markSent() { this.status = SENT; }
}