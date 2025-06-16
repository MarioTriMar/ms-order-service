package com.tfm.ms_order_service.repository;

import com.tfm.ms_order_service.model.RecurrentOrder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecurrentOrderRepository extends MongoRepository<RecurrentOrder, String> {
}
