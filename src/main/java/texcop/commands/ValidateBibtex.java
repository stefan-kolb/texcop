package texcop.commands;

import org.jbibtex.*;
import texcop.FileTask;
import texcop.cop.Location;
import texcop.cop.Offense;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ValidateBibtex implements FileTask {

    @Override
    public String getName() {
        return "validate-bibtex";
    }

    @Override
    public String getDescription() {
        return "validates all .bib files for the existence of certain fields";
    }

    @Override
    public List<Offense> execute(Path file) {
        List<Offense> offenses = new ArrayList<>();
        // TODO accept? or filter in runner?
        if (!file.getFileName().toString().endsWith(".bib")) {
            return offenses;
        }

        try {
            BibTeXDatabase database = parseBibtexFile(file);
            offenses.addAll(validate(database, file));
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
        return offenses;
    }

    @Override
    public String getFileExtension() {
        return ".bib";
    }

    private static Map<String, List<String>> getRequiredFieldsDatabase() {
        Map<String, List<String>> result = new HashMap<>();

        result.put("article", Arrays.asList("author", "title", "year", "month", "volume", "number", "pages"));
        result.put("techreport", Arrays.asList("author", "title", "year", "month", "institution", "number"));
        result.put("manual", Arrays.asList("author", "title", "year", "month"));
        result.put("inproceedings", Arrays.asList("author", "title", "booktitle", "year", "pages"));

        result.put("book", Arrays.asList("author", "title", "publisher", "year"));
        result.put("phdthesis", Arrays.asList("author", "title", "school", "year"));
        result.put("misc", Arrays.asList("author", "title", "howpublished", "year"));

        result.put("incollection", Arrays.asList("author", "title", "booktitle", "year", "pages", "publisher"));

        return result;
    }

    private List<Offense> validate(BibTeXDatabase database, Path bibtexFile) {
        List<Offense> offenses = new ArrayList<>();

        for (BibTeXEntry entry : database.getEntries().values()) {
            String type = entry.getType().toString().toLowerCase();

            //detectRequiredAndMissingFields(bibtexFile, entry, type).ifPresent(offenses::add);
            //detectProceedingsWithPages(bibtexFile, entry, type).ifPresent(offenses::add);
            //detectAbbreviations(bibtexFile, entry, type).ifPresent(offenses::add);
            validateInProceedingsBooktitle(bibtexFile, entry, type).ifPresent(offenses::add);
        }
        return offenses;
    }

    private Optional<Offense> detectRequiredAndMissingFields(Path bibtexFile, BibTeXEntry entry, String type) {
        List<String> requiredFields = getRequiredFieldsDatabase().get(type);
        if (requiredFields == null) {
            return Optional.of(createOffense(bibtexFile, entry, "","no required fields available for this type"));
        }

        for (String key : requiredFields) {
            Optional<Offense> o = ensureKeyExistence(bibtexFile, entry, new Key(key));

            if (o.isPresent()) {
                return o;
            }
        }
        return Optional.empty();
    }

    private Optional<Offense> validateInProceedingsBooktitle(Path bibtexFile, BibTeXEntry entry, String type) {
        final String pattern = "Proceedings of the ";
        if ("inproceedings".equals(type)) {
            /* booktitle general structure
            if (entry.getField(BibTeXEntry.KEY_BOOKTITLE) == null || !entry.getField(BibTeXEntry.KEY_BOOKTITLE).toUserString().startsWith(pattern)) {
                return Optional.of(createOffense(bibtexFile, entry, "InProceedings booktitle should start with: Proceedings of the ..."));
            }*/
            // prohibited words
            String word = "international";
            String title = entry.getField(BibTeXEntry.KEY_BOOKTITLE).toUserString();
            if (entry.getField(BibTeXEntry.KEY_BOOKTITLE) == null || title.toLowerCase().contains(word)) {
                if (title.toLowerCase().contains("hawaii international")) {
                    return Optional.empty();
                }
                return Optional.of(createOffense(bibtexFile, entry, title,"InProceedings booktitle should not include obsolete word: " + word));
            }
        }
        return Optional.empty();
    }

    private Optional<Offense> detectProceedingsWithPages(Path bibtexFile, BibTeXEntry entry, String type) {
        if ("proceedings".equals(type) && entry.getField(BibTeXEntry.KEY_PAGES) != null) {
            return Optional.of(createOffense(bibtexFile, entry, "", "proceedings with pages, maybe should be inproceedings?"));
        }
        return Optional.empty();
    }

    private Optional<Offense> detectAbbreviations(Path bibtexFile, BibTeXEntry entry, String type) {
        if ("article".equals(type) && entry.getField(BibTeXEntry.KEY_JOURNAL) != null && entry.getField(BibTeXEntry.KEY_JOURNAL).toUserString().contains(".")) {
            return Optional.of(createOffense(bibtexFile, entry, "", "journal is abbreviated"));
        }
        return Optional.empty();
    }

    private Optional<Offense> ensureKeyExistence(Path bibtexFile, BibTeXEntry entry, Key key) {
        if (!entry.getFields().containsKey(key) || entry.getFields().get(key).toString().trim().isEmpty()) {
            String message = key + " is missing";
            return Optional.of(createOffense(bibtexFile, entry, "", message));
        }
        return Optional.empty();
    }

    private Offense createOffense(Path bibtexFile, BibTeXEntry entry, String line, String message) {
        return new Offense(new Location(line, 0, 0 , 0), String.format("%s\t%s\t%s\t%s%n", bibtexFile, entry.getKey(), entry.getType().toString().toUpperCase(), message));
    }

    private BibTeXDatabase parseBibtexFile(Path bibtexFile) {
        try (BufferedReader reader = Files.newBufferedReader(bibtexFile, StandardCharsets.UTF_8)) {
            return new BibTeXParser().parse(reader);
        } catch (IOException e) {
            throw new IllegalStateException("could not read file " + bibtexFile, e);
        } catch (ParseException e) {
            throw new IllegalStateException("could not parse bibtex file " + bibtexFile + "\n" + e.getMessage(), e);
        } catch (TokenMgrException e) {
            throw new IllegalStateException("bib tex file " + bibtexFile + " is not well formed. Reason: " + e.getMessage());
        }
    }
}
