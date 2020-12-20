package com.novyr.callfilter.db.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.novyr.callfilter.db.converter.RuleActionConverter;
import com.novyr.callfilter.db.converter.RuleTypeConverter;
import com.novyr.callfilter.db.entity.enums.RuleAction;
import com.novyr.callfilter.db.entity.enums.RuleType;
import com.novyr.callfilter.model.Rule;

@Entity(tableName = "rule_entity")
public class RuleEntity implements Rule {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @TypeConverters(RuleTypeConverter.class)
    private RuleType type;

    @NonNull
    @TypeConverters(RuleActionConverter.class)
    private RuleAction action;

    @Nullable
    private String value;

    private boolean enabled;

    private int order;

    public RuleEntity(
            @NonNull RuleType type,
            @NonNull RuleAction action,
            @Nullable String value,
            boolean enabled,
            int order
    ) {
        this.type = type;
        this.action = action;
        this.value = value;
        this.enabled = enabled;
        this.order = order;
    }

    public RuleEntity(RuleEntity entity) {
        this.id = entity.id;
        this.type = entity.type;
        this.action = entity.action;
        this.value = entity.value;
        this.enabled = entity.enabled;
        this.order = entity.order;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public RuleType getType() {
        return type;
    }

    public void setType(@NonNull RuleType type) {
        this.type = type;
    }

    @NonNull
    public RuleAction getAction() {
        return action;
    }

    public void setAction(@NonNull RuleAction action) {
        this.action = action;
    }

    @Nullable
    public String getValue() {
        return value;
    }

    public void setValue(@Nullable String value) {
        this.value = value;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
