package com.example.coursework6sem.application.events;

import com.example.coursework6sem.infrastructure.persistence.jpa.entity.EstateEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.MarketSnapshotEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.EstateRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.MarketSnapshotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

@Component
public class EstateCreatedObserver {
    private static final Logger log = LoggerFactory.getLogger(EstateCreatedObserver.class);

    private final EstateRepository estates;
    private final MarketSnapshotRepository snapshots;

    public EstateCreatedObserver(EstateRepository estates, MarketSnapshotRepository snapshots) {
        this.estates = estates;
        this.snapshots = snapshots;
    }

    @EventListener
    @Transactional
    public void on(EstateCreatedEvent event) {
        estates.findById(event.estateId()).ifPresent(estate -> {
            var district = estate.getDistrict();
            if (district.getAvgPrice() == null) return;

            LocalDate month = LocalDate.now().withDayOfMonth(1);
            MarketSnapshotEntity snapshot = new MarketSnapshotEntity(
                    district,
                    month,
                    district.getAvgPrice(),
                    district.getDemandLevel(),
                    estate.getPropertyType(),
                    Instant.now()
            );

            try {
                snapshots.save(snapshot);
                log.info("MarketSnapshot обновлён для района {} на тип {}", district.getName(), estate.getPropertyType());
            } catch (Exception e) {
                // Если уникальность по (district, month, type) нарушена — просто игнорируем (создаём один снапшот).
                log.debug("MarketSnapshot уже существовал, игнорируем: {}", e.getMessage());
            }
        });
    }
}

