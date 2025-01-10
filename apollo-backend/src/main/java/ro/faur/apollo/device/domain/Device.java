package ro.faur.apollo.device.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import ro.faur.apollo.libs.persistence.BaseEntity;

@Entity
public class Device extends BaseEntity {

    @Column
    private String name;

    public Device(String name) {
        this.name = name;
    }

    public Device() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
