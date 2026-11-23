package com.example.template.item.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface ItemJpaRepository extends JpaRepository<ItemJpaEntity, Long> {}
