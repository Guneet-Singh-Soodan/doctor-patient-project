package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Doctor extends User{
    @OneToMany
    private List<Patient> patientsRequests;

    @ManyToMany
    private List<Patient>patientsAccepted;

    @ManyToMany
    private List<Patient>patientsDeclined;

}
