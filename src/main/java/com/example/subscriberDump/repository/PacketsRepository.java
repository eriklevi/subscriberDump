package com.example.subscriberDump.repository;

import com.example.subscriberDump.entity.Packet;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PacketsRepository extends MongoRepository<Packet, String> {

}
