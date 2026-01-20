package com.bvd.java_fundamentals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LibraryUtilTest {

    private static List<String> loans;

    @BeforeAll
    static void setUp() {
        loans = LibraryAnalytics.loadedFile;
    }

    @Test
    void testLoadResourceFile_returnsFile() {
        assertNotNull(loans);
        assertFalse(loans.isEmpty());
        assertEquals(36, loans.size());
    }

    @Test
    void testLoanByGenre() {
        var parsed = LibraryUtil.parseCsvLines(loans);
        var valid = parsed.get("valid");

        var result = LibraryUtil.loansByGenre(valid);

        assertEquals(5, result.size());
        assertEquals(9L, result.get("Classic"));
        assertEquals(5L, result.get("Dystopian"));
        assertEquals(7L, result.get("Fantasy"));
        assertEquals(4L, result.get("Horror"));
        assertEquals(7L, result.get("Science Fiction"));
    }

    @Test
    void testTopAuthorsByLoans_top2() {
        var parsed = LibraryUtil.parseCsvLines(loans);
        var valid = parsed.get("valid");

        var result = LibraryUtil.topAuthorsByLoans(valid, 2).stream()
                .map(String::trim)
                .toList();

        assertEquals(List.of("J.R.R. Tolkien", "Frank Herbert"), result);
    }

    @Test
    void testTopAuthorsByLoans_nZero_returnsEmpty() {
        var parsed = LibraryUtil.parseCsvLines(loans);
        var valid = parsed.get("valid");

        assertEquals(List.of(), LibraryUtil.topAuthorsByLoans(valid, 0));
    }

    @Test
    void testMembersWithGenreDiversity_k3() {
        var parsed = LibraryUtil.parseCsvLines(loans);
        var valid = parsed.get("valid");
        var result = LibraryUtil.membersWithGenreDiversity(valid, 3);

        assertEquals(List.of("M-002", "M-003", "M-008", "M-009", "M-010"), result);
    }

    @Test
    void testMembersWithGenreDiversity_k0_returnsEmpty() {
        var parsed = LibraryUtil.parseCsvLines(loans);
        var valid = parsed.get("valid");

        assertEquals(List.of(), LibraryUtil.membersWithGenreDiversity(valid, 0));
    }


    @Test
    void testFindFirstBookContaining_dune() {
        var parsed = LibraryUtil.parseCsvLines(loans);
        var valid = parsed.get("valid");
        var result = LibraryUtil.findFirstBookContaining(valid, "Dune");

        assertNotNull(result);
        assertEquals(true, result.isPresent());
        assertEquals("L-1006", result.get().loanId());
    }

    @Test
    void testFindFirstBookContaining_blank_returnsEmpty() {
        var parsed = LibraryUtil.parseCsvLines(loans);
        var valid = parsed.get("valid");

        assertEquals(Optional.empty(), LibraryUtil.findFirstBookContaining(valid, "   "));
    }


    @Test
    void testIsBookPresent_false() {
        var parsed = LibraryUtil.parseCsvLines(loans);
        var valid = parsed.get("valid");

        assertFalse(LibraryUtil.isBookPresent(valid, "Harry Potter"));
    }


    @Test
    void testIsBookPresent_true() {
        var parsed = LibraryUtil.parseCsvLines(loans);
        var valid = parsed.get("valid");

        assertEquals(true, LibraryUtil.isBookPresent(valid, "Dune"));
    }


    @Test
    void testParseCsvLines_splitsValidAndMalformed() {
        var parsed = LibraryUtil.parseCsvLines(loans);

        assertNotNull(parsed);
        assertNotNull(parsed.get("valid"));
        assertNotNull(parsed.get("malformed"));

        assertEquals(32, parsed.get("valid").size());
        assertEquals(4, parsed.get("malformed").size());
        assertEquals(36, parsed.get("valid").size() + parsed.get("malformed").size());
    }

    @Test
    void testParseJsonLines_validAndMalformed() {
        var jsonLines = LibraryUtil.loadResourceFile("loans/libraryLoans.json");
        var result = LibraryUtil.parseJsonLines(jsonLines);

        assertNotNull(result);
        assertFalse(result.validLoans().isEmpty());
        assertFalse(result.malformedLines().isEmpty());

        assertEquals(
                result.validLoans().size() + result.malformedLines().size(),
                35
        );
    }

}
