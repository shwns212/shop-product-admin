package com.jun.shop.event.util;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EventRepository extends ReactiveCrudRepository<Event, Long> {
	
	@Query("select * from event where aggregate_type = :aggregateType and aggregate_id = :aggregateId order by version desc limit 1")
	Mono<Event> findRecentlyEvent(String aggregateType, String aggregateId);
	
	@Query("select * from event where aggregate_type = :aggregateType and aggregate_id = :aggregateId and version > :version order by version")
	Flux<Event> findAfterSnapshotEvents(String aggregateType, String aggregateId, Long version);
}
