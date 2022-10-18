package com.decagon.rewardyourteacherapi11bjavapodf2.model;

import com.decagon.rewardyourteacherapi11bjavapodf2.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "transactions")
public class Transaction extends BaseClass{
    private Long uuid;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    private String description;

    private BigDecimal amount;
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "user_id" , referencedColumnName = "id")
    private User user;

}

