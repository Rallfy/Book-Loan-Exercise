package com.bvd.java_fundamentals.model;

import java.time.LocalDate;

public record BookLoan(
        String loanId,
        String memberId,
        LocalDate loanDate,
        String bookTitle,
        String genre,
        String author,
        int daysLoaned
) {}
