package com.bvd.java_fundamentals;

import com.bvd.java_fundamentals.model.BookLoan;
import com.bvd.java_fundamentals.model.ParseResult;

import java.util.*;
import java.util.stream.Collectors;

public class LibraryUtil {

    private LibraryUtil() {
    }

    // load resource file from resources folder
    static List<String> loadResourceFile(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Invalid file name");
        }

        try (var is = LibraryUtil.class.getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) {
                throw new IllegalArgumentException("File not found: " + fileName);
            }

            try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(is))) {
                return reader.lines().toList();
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not load file", e);
        }
    }

    // retrieve loans from csv lines

    protected static Map<String, List<BookLoan>> parseCsvLines(final List<String> file) {
        var valid = new java.util.ArrayList<BookLoan>();
        var malformed = new java.util.ArrayList<BookLoan>();

        file.forEach(line -> {
            try {
                var elem = line.split(",");

                if (elem.length != 7) throw new IllegalArgumentException();

                var loanId = elem[0].trim();
                var memberId = elem[1].trim();
                var loanDate = java.time.LocalDate.parse(elem[2].trim());
                var bookTitle = elem[3].trim();
                var genre = elem[4].trim();
                var author = elem[5].trim();
                var daysLoaned = Integer.parseInt(elem[6].trim());

                if (loanId.isBlank() || memberId.isBlank() || bookTitle.isBlank() || genre.isBlank() || author.isBlank())
                    throw new IllegalArgumentException();


                valid.add(new BookLoan(loanId, memberId, loanDate, bookTitle, genre, author, daysLoaned));
            } catch (Exception e) {
                malformed.add(null);
            }

        });
        return Map.of("valid", valid, "malformed", malformed);
    }

    // count loans per genre
    // sorted alphabetically by genre
    protected static Map<String, Long> loansByGenre(final List<BookLoan> loans) {
        return loans.stream().collect(
                Collectors.groupingBy(
                        BookLoan::genre,
                        TreeMap::new,
                        Collectors.counting()
                ));

    }

    // get top "n" authors by number of loans
    protected static List<String> topAuthorsByLoans(final List<BookLoan> loans, final int n) {

        if (loans == null || loans.isEmpty() || n <= 0)
            return Collections.emptyList();

        var counts = loans.stream()
                .collect(Collectors.groupingBy(
                        BookLoan::author,
                        Collectors.counting()
                ));

        var comparator = Comparator
                .comparingLong((Map.Entry<String, Long> e) -> e.getValue())
                .reversed()
                .thenComparing(Map.Entry::getKey);

        return counts.entrySet().stream()
                .sorted(comparator)
                .limit(n)
                .map(Map.Entry::getKey)
                .toList();
    }

    // get members who borrowed books from at least K genres
    protected static List<String> membersWithGenreDiversity(final List<BookLoan> loans, final int k) {
        if (loans == null || loans.isEmpty() || k <= 0) {
            return java.util.Collections.emptyList();
        }

        var genresPerMember = loans.stream()
                .collect(Collectors.groupingBy(
                        BookLoan::memberId,
                        Collectors.mapping(
                                BookLoan::genre,
                                Collectors.toSet()
                        )
                ));

        return genresPerMember.entrySet().stream()
                .filter(e -> e.getValue().size() >= k)
                .map(java.util.Map.Entry::getKey)
                .sorted()
                .toList();
    }

    // find the first book title containing a substring (case-insensitive)
    protected static Optional<BookLoan> findFirstBookContaining(final List<BookLoan> loans, final String book) {
        if (loans == null || loans.isEmpty() || book == null || book.isBlank()) {
            return Optional.empty();
        }

        var needB = book.trim().toLowerCase();

        return loans.stream()
                .filter(l -> l.bookTitle() != null && l.bookTitle().toLowerCase().contains(needB))
                .findFirst();
    }

    // checks if the book is present in the loans (case-insensitive)
    protected static Boolean isBookPresent(final List<BookLoan> loans, final String book) {
        if (loans == null || loans.isEmpty() || book == null || book.isBlank()) {
            return false;
        }

        var needB = book.trim().toLowerCase();

        return loans.stream()
                .anyMatch(l ->
                        l.bookTitle() != null &&
                                l.bookTitle().toLowerCase().equals(needB)
                );
    }


    protected static ParseResult parseJsonLines(final List<String> file) {
        if (file == null || file.isEmpty()) {
            return new ParseResult(List.of(), List.of());
        }

        var json = String.join("\n", file);

        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

            var listType = mapper.getTypeFactory()
                    .constructCollectionType(List.class, String.class);

            List<String> jsonObjectsAsStrings = mapper.readValue(json, listType);

            var valid = new java.util.ArrayList<BookLoan>();
            var malformed = new java.util.ArrayList<String>();

            for (var rawJson : jsonObjectsAsStrings) {
                try {
                    var loan = mapper.readValue(rawJson, BookLoan.class);

                    if (loan.loanId() == null || loan.loanId().isBlank()
                            || loan.memberId() == null || loan.memberId().isBlank()
                            || loan.loanDate() == null
                            || loan.bookTitle() == null || loan.bookTitle().isBlank()
                            || loan.genre() == null || loan.genre().isBlank()
                            || loan.author() == null || loan.author().isBlank()
                            || loan.daysLoaned() <= 0) {
                        throw new IllegalArgumentException("Invalid fields");
                    }

                    valid.add(loan);
                } catch (Exception e) {
                    malformed.add(rawJson);
                }
            }

            return new ParseResult(valid, malformed);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON file content", e);
        }
    }



}
