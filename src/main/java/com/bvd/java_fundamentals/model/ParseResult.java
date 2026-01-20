package com.bvd.java_fundamentals.model;

import java.util.List;

public record ParseResult(
        List<BookLoan> validLoans,
        List<String> malformedLines
) {}
