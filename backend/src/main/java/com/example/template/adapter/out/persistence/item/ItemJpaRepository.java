package com.example.template.adapter.out.persistence.item;

import org.springframework.data.jpa.repository.JpaRepository;

interface ItemJpaRepository extends JpaRepository<ItemJpaEntity, Long> {
}
