package io.crnk.example.springboot.domain.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.Set;

@Entity
public class UserEntity {

    @Id
    private String loginId;

    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "creator")
    private Set<ScheduleEntity> createdSchedules;

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
