package io.crnk.example.springboot.domain.model;

import io.crnk.core.resource.annotations.JsonApiResource;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.Set;

@JsonApiResource(type = "user")
@Entity
public class UserEntity {

    @Id
    private String loginId;

    private String name;

    private UserAddress address;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "creator")
    private Set<ScheduleEntity> createdSchedules;

    public UserAddress getAddress() {
        return address;
    }

    public void setAddress(UserAddress address) {
        this.address = address;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<ScheduleEntity> getCreatedSchedules() {
        return createdSchedules;
    }

    public void setCreatedSchedules(Set<ScheduleEntity> createdSchedules) {
        this.createdSchedules = createdSchedules;
    }
}
