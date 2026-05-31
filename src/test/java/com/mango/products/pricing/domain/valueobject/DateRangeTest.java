package com.mango.products.pricing.domain.valueobject;

import com.mango.products.pricing.domain.exception.InvalidDateRangeException;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DateRangeTest {

    @Test
    void shouldCreateClosedRange() {
        DateRange range = new DateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30));
        assertEquals(LocalDate.of(2024, 1, 1), range.initDate());
        assertEquals(LocalDate.of(2024, 6, 30), range.endDate());
    }

    @Test
    void shouldCreateOpenEndedRange() {
        DateRange range = new DateRange(LocalDate.of(2025, 1, 1), null);
        assertEquals(LocalDate.of(2025, 1, 1), range.initDate());
        assertNull(range.endDate());
    }

    @Test
    void shouldThrowWhenInitDateIsNull() {
        assertThrows(InvalidDateRangeException.class, () ->
                new DateRange(null, LocalDate.of(2024, 6, 30)));
    }

    @Test
    void shouldThrowWhenInitDateEqualsEndDate() {
        assertThrows(InvalidDateRangeException.class, () ->
                new DateRange(LocalDate.of(2024, 6, 30), LocalDate.of(2024, 6, 30)));
    }

    @Test
    void shouldThrowWhenInitDateIsAfterEndDate() {
        assertThrows(InvalidDateRangeException.class, () ->
                new DateRange(LocalDate.of(2024, 12, 31), LocalDate.of(2024, 1, 1)));
    }

    @Test
    void shouldContainDateInsideRange() {
        DateRange range = new DateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30));
        assertTrue(range.contains(LocalDate.of(2024, 4, 15)));
    }

    @Test
    void shouldContainInitDate() {
        DateRange range = new DateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30));
        assertTrue(range.contains(LocalDate.of(2024, 1, 1)));
    }

    @Test
    void shouldContainEndDate() {
        DateRange range = new DateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30));
        assertTrue(range.contains(LocalDate.of(2024, 6, 30)));
    }

    @Test
    void shouldNotContainDateBeforeRange() {
        DateRange range = new DateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30));
        assertFalse(range.contains(LocalDate.of(2023, 12, 31)));
    }

    @Test
    void shouldNotContainDateAfterClosedRange() {
        DateRange range = new DateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30));
        assertFalse(range.contains(LocalDate.of(2024, 7, 1)));
    }

    @Test
    void shouldContainDateInOpenEndedRange() {
        DateRange range = new DateRange(LocalDate.of(2025, 1, 1), null);
        assertTrue(range.contains(LocalDate.of(2030, 12, 31)));
    }

    @Test
    void shouldDetectOverlapWhenRangesIntersect() {
        DateRange a = new DateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30));
        DateRange b = new DateRange(LocalDate.of(2024, 6, 15), LocalDate.of(2024, 12, 31));
        assertTrue(a.overlaps(b));
    }

    @Test
    void shouldNotDetectOverlapForContiguousRanges() {
        DateRange a = new DateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30));
        DateRange b = new DateRange(LocalDate.of(2024, 7, 1), LocalDate.of(2024, 12, 31));
        assertFalse(a.overlaps(b));
    }

    @Test
    void shouldDetectOverlapWithTwoOpenEndedRanges() {
        DateRange a = new DateRange(LocalDate.of(2025, 1, 1), null);
        DateRange b = new DateRange(LocalDate.of(2025, 6, 1), null);
        assertTrue(a.overlaps(b));
    }

    @Test
    void shouldNotDetectOverlapWhenClosedRangeIsBeforeOpenEnded() {
        DateRange a = new DateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
        DateRange b = new DateRange(LocalDate.of(2025, 1, 1), null);
        assertFalse(a.overlaps(b));
    }
}


