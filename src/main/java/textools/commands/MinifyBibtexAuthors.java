package textools.commands;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXFormatter;
import org.jbibtex.BibTeXParser;
import org.jbibtex.Key;
import org.jbibtex.ParseException;
import org.jbibtex.StringValue;
import org.jbibtex.Value;
import textools.Command;
import textools.cop.Offense;
import textools.tasks.FileSystemTasks;

public class MinifyBibtexAuthors implements Command {

    @Override
    public String getName() {
        return "minify-bibtex-authors";
    }

    @Override
    public String getDescription() {
        return "replace three or more authors with et al. in bibtex entries";
    }

    @Override
    public void execute() {
        List<Path> bibtexFiles = new FileSystemTasks().getFilesByExtension(".bib");

        for (Path bibtexFile : bibtexFiles) {
            try {
                BibTeXDatabase database = new BibTeXParser().parse(Files.newBufferedReader(bibtexFile, StandardCharsets.UTF_8));
                minifyDatabase(database);
                BibTeXFormatter formatter = new BibTeXFormatter();
                formatter.setIndent("  ");
                formatter.format(database, Files.newBufferedWriter(bibtexFile, StandardCharsets.UTF_8));
            } catch (IOException | ParseException e) {
                System.out.println("\tError during minification of " + bibtexFile + ". Reason: " + e.getMessage());
            }
        }
    }

    @Override
    public List<Offense> run(Path file) {
        return null;
    }

    public void minifyDatabase(BibTeXDatabase database) {
        for (BibTeXEntry entry : database.getEntries().values()) {

            Value authorValue = entry.getField(new Key("author"));

            if (authorValue == null) {
                System.out.println("\tSkipping " + entry.getKey() + ": missing author field");
                continue;
            }

            String author = authorValue.toUserString();
            String abbreviatedAuthor = abbreviateAuthor(author);

            if (!author.equals(abbreviatedAuthor)) {
                //entry.removeField(new Key("author"));
                System.out.println("\tMinifying " + entry.getKey() + ": abbreviating author to " + abbreviatedAuthor);
                entry.addField(new Key("author"), new StringValue(abbreviatedAuthor, StringValue.Style.BRACED));
            }
        }
    }

    public String abbreviateAuthor(String author) {
        // single author
        String authorSeparator = " and ";

        if (!author.contains(authorSeparator)) {
            return author;
        }

        String[] authors = author.split(authorSeparator);

        // trim authors (remove or let it in? is some magic...)
        for (int i = 0; i < authors.length; i++) {
            authors[i] = authors[i].trim();
        }

        // already abbreviated
        if ("others".equals(authors[authors.length - 1]) && authors.length == 2) {
            return author;
        }

        // abbreviate
        if (authors.length < 3) {
            return author;
        }

        return authors[0] + authorSeparator + "others";
    }
}
