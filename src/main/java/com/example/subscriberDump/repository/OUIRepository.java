package com.example.subscriberDump.repository;

import com.example.subscriberDump.entity.OUI;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OUIRepository extends MongoRepository<OUI, String> {
    Optional<OUI> findByOui(String oui);
}
