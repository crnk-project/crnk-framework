package io.crnk.example.springboot.domain.model;

import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.SerializeType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.Set;

@JsonApiResource(type = "schedule")
@Entity
public class ScheduleEntity {

    @Id
    private Long id;

    private String name;

    @JsonApiRelationId()
    @Column(name = "creator_id")
    private String creatorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({@JoinColumn(name = "creator_id", insertable = false, updatable = false)})
    @JsonApiRelation(serialize = SerializeType.ONLY_ID)
    private UserEntity creator;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<UserEntity> verifiers;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
        this.creator = null;
    }

    public UserEntity getCreator() {
        return creator;
    }

    public void setCreator(UserEntity creator) {
        this.creator = creator;
        this.creatorId = creator != null ? creator.getLoginId() : null;
    }

    public Set<UserEntity> getVerifiers() {
        return verifiers;
    }

    public void setVerifiers(Set<UserEntity> verifiers) {
        this.verifiers = verifiers;
    }
}
