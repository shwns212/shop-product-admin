package com.jun.shop.event.util;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Mono;

public interface SnapshotRepository extends ReactiveCrudRepository<Snapshot, Long> {
	
	@Query("select * from snapshot where aggregate_type = :aggregateType and aggregate_id = :aggregateId order by version desc limit 1")
	Mono<Snapshot> findRecentlySnapshot(String aggregateType, String aggregateId);
}
