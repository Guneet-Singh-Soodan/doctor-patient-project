package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;


@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Patient extends User{
    @ManyToOne
    private Doctor doctorRequested;
    @Enumerated
    private RequestStatus requestStatus=RequestStatus.NO_REQUEST;
}
