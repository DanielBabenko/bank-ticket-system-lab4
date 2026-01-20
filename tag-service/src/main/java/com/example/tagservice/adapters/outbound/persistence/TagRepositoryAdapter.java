package com.example.tagservice.adapters.outbound.persistence;

import com.example.tagservice.adapters.outbound.persistence.entity.TagJpaEntity;
import com.example.tagservice.adapters.outbound.persistence.jpa.SpringTagJpaRepository;
import com.example.tagservice.domain.model.Tag;
import com.example.tagservice.domain.port.outbound.TagRepositoryPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class TagRepositoryAdapter implements TagRepositoryPort {

    private final SpringTagJpaRepository jpaRepository;

    public TagRepositoryAdapter(SpringTagJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Tag> findByName(String name) {
        return jpaRepository.findByName(name)
                .map(this::toDomain);
    }

    @Override
    public List<Tag> findByNames(List<String> names) {
        if (names == null || names.isEmpty()) return Collections.emptyList();
        return jpaRepository.findByNameIn(names).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Tag save(Tag tag) {
        TagJpaEntity entity = toEntity(tag);
        TagJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional
    public List<Tag> saveAll(List<Tag> tags) {
        List<TagJpaEntity> entities = tags.stream().map(this::toEntity).collect(Collectors.toList());
        List<TagJpaEntity> saved = jpaRepository.saveAll(entities);
        return saved.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Tag> findAll(int page, int size) {
        var pr = PageRequest.of(page, size);
        return jpaRepository.findAll(pr).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.findByName(name).isPresent();
    }

    public void deleteAll() {
        jpaRepository.deleteAll();
    }

    public long count() {
        return jpaRepository.count();
    }

    private Tag toDomain(TagJpaEntity e) {
        Tag tag = new Tag();
        tag.setId(e.getId());
        tag.setName(e.getName());
        if (e.getApplicationIds() != null) {
            tag.setApplicationIds(new HashSet<>(e.getApplicationIds()));
        }
        return tag;
    }

    private TagJpaEntity toEntity(Tag t) {
        TagJpaEntity e = new TagJpaEntity();
        e.setId(t.getId());
        e.setName(t.getName());
        if (t.getApplicationIds() != null) {
            e.setApplicationIds(new HashSet<>(t.getApplicationIds()));
        }
        return e;
    }

}
