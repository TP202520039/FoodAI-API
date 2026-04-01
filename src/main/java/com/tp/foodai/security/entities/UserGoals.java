package com.tp.foodai.security.entities;

import com.tp.foodai.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_goals")
public class UserGoals extends AuditableAbstractAggregateRoot<UserGoals> {

    public static final int DEFAULT_DAILY_CALORIES_GOAL = 2000;
    public static final int DEFAULT_DAILY_PROTEIN_GOAL = 100;
    public static final int DEFAULT_DAILY_FAT_GOAL = 65;
    public static final int DEFAULT_DAILY_CARBS_GOAL = 250;

    @Column(name = "firebase_uid", unique = true, nullable = false, length = 128)
    private String firebaseUid;

    @Column(name = "daily_calories_goal", nullable = false)
    @Builder.Default
    private Integer dailyCaloriesGoal = DEFAULT_DAILY_CALORIES_GOAL;

    @Column(name = "daily_protein_goal", nullable = false)
    @Builder.Default
    private Integer dailyProteinGoal = DEFAULT_DAILY_PROTEIN_GOAL;

    @Column(name = "daily_fat_goal", nullable = false)
    @Builder.Default
    private Integer dailyFatGoal = DEFAULT_DAILY_FAT_GOAL;

    @Column(name = "daily_carbs_goal", nullable = false)
    @Builder.Default
    private Integer dailyCarbsGoal = DEFAULT_DAILY_CARBS_GOAL;
}
