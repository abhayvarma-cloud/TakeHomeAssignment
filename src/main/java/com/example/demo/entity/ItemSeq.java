package com.example.demo.entity;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
//only one  row inserted via data.sql
@Entity                                  // must be present
@Table(name = "item_seq")
public class ItemSeq {
	@Id
    private Integer id = 1;                

    @Column(name = "last_seq", nullable = false)
    private Long lastSeq = 0L;

    protected ItemSeq() {}

    public Long getLastSeq() { return lastSeq; }
    public void setLastSeq(Long lastSeq) { this.lastSeq = lastSeq; }

}
