package com.mango.products.pricing.domain.valueobject;

import com.mango.products.pricing.domain.exception.InvalidDateRangeException;

import java.time.LocalDate;
import java.util.Objects;

public final class DateRange {

    private final LocalDate initDate;
    private final LocalDate endDate;

    public DateRange(LocalDate initDate, LocalDate endDate) {
        if (initDate == null) {
            throw new InvalidDateRangeException("initDate cannot be null");
        }
        if (endDate != null && !initDate.isBefore(endDate)) {
            throw new InvalidDateRangeException("initDate must be before endDate");
        }
        this.initDate = initDate;
        this.endDate = endDate;
    }

    public LocalDate initDate() {
        return initDate;
    }

    public LocalDate endDate() {
        return endDate;
    }

    public boolean contains(LocalDate date) {
        Objects.requireNonNull(date, "date cannot be null");
        return !date.isBefore(initDate) && (endDate == null || !date.isAfter(endDate));
    }

    public boolean overlaps(DateRange other) {
        Objects.requireNonNull(other, "other cannot be null");
        LocalDate thisEnd = endDate == null ? LocalDate.MAX : endDate;
        LocalDate otherEnd = other.endDate == null ? LocalDate.MAX : other.endDate;
        return !thisEnd.isBefore(other.initDate) && !otherEnd.isBefore(this.initDate);
    }
}


