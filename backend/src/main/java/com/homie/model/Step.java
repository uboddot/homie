package com.homie.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "step")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Step {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   // @ManyToOne
   // @JoinColumn(name = "recipe_id", nullable = false)
   // private Recipe recipe;

    private int stepOrder;
    private String instruction;

    public Step(int stepOrder, String instruction) {
        this.stepOrder = stepOrder;
        this.instruction = instruction;
    }
}
