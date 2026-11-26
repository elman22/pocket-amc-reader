/*
    This file is part of Pocket AMC Reader.
    Copyright © 2010-2026 Elman <holdingscythe@zoznam.sk>

    Pocket AMC Reader is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Pocket AMC Reader is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Pocket AMC Reader.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.holdingscythe.pocketamcreader;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Every translation has to contain all texts of the default language, with the same placeholders, and the language
 * has to be listed where Android and the About dialog need it. Add a text to values/strings.xml and this test lists
 * the languages where it is missing.
 */
public class LocalizationTest {

    private static final Pattern PLACEHOLDER = Pattern.compile("%\\d\\$[sd]");
    private static final Pattern UNESCAPED_APOSTROPHE = Pattern.compile("(?<!\\\\)'");

    private static File res;
    private static Map<String, Element> defaults;
    private static List<String> languages;

    /**
     * @param text all texts of the resource (plurals and arrays have several)
     */
    private record Resource(String type, String text) {
    }

    @BeforeClass
    public static void load() throws Exception {
        res = new File("src/main/res");
        if (!res.isDirectory())
            res = new File("PocketAMCReader/src/main/res"); // started from the root of the project

        defaults = new LinkedHashMap<>();
        for (Map.Entry<String, Element> entry : parse(new File(res, "values/strings.xml")).entrySet()) {
            // Texts which are not translated (names, numbers...)
            if (!"false".equals(entry.getValue().getAttribute("translatable")))
                defaults.put(entry.getKey(), entry.getValue());
        }

        languages = new ArrayList<>();
        File[] dirs = res.listFiles((dir, name) -> name.matches("values-[a-z]{2}"));
        assertTrue("Resources not found in " + res.getAbsolutePath(), dirs != null && dirs.length > 0);
        for (File dir : dirs) {
            if (new File(dir, "strings.xml").isFile())
                languages.add(dir.getName().substring("values-".length()));
        }
        Collections.sort(languages);
    }

    private static Map<String, Element> parse(File file) throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
        Map<String, Element> result = new LinkedHashMap<>();
        NodeList nodes = document.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i) instanceof Element element) {
                result.put(element.getAttribute("name"), element);
            }
        }
        return result;
    }

    private static Set<String> placeholders(String text) {
        Set<String> result = new TreeSet<>();
        Matcher matcher = PLACEHOLDER.matcher(text);
        while (matcher.find())
            result.add(matcher.group());
        return result;
    }

    private static Map<String, Element> translation(String language) throws Exception {
        return parse(new File(res, "values-" + language + "/strings.xml"));
    }

    @Test
    public void thereAreTranslations() {
        assertTrue(languages.contains("cs"));
        assertTrue(languages.size() >= 6);
    }

    @Test
    public void everyTranslationHasAllTexts() throws Exception {
        List<String> problems = new ArrayList<>();
        for (String language : languages) {
            Map<String, Element> translated = translation(language);
            for (String key : defaults.keySet()) {
                if (!translated.containsKey(key))
                    problems.add(language + ": missing " + key);
            }
            for (String key : translated.keySet()) {
                if (!defaults.containsKey(key))
                    problems.add(language + ": unknown (removed?) " + key);
            }
        }
        if (!problems.isEmpty())
            fail(String.join("\n", problems));
    }

    @Test
    public void translationsUseTheSameKindOfResourceAndPlaceholders() throws Exception {
        List<String> problems = new ArrayList<>();
        for (String language : languages) {
            Map<String, Element> translated = translation(language);
            for (Map.Entry<String, Element> entry : defaults.entrySet()) {
                Element other = translated.get(entry.getKey());
                if (other == null)
                    continue; // reported by everyTranslationHasAllTexts

                if (!entry.getValue().getTagName().equals(other.getTagName())) {
                    problems.add(language + ": " + entry.getKey() + " should be <" + entry.getValue().getTagName()
                            + ">");
                    continue;
                }

                Set<String> expected = placeholders(entry.getValue().getTextContent());
                Set<String> actual = placeholders(other.getTextContent());
                if (!expected.equals(actual))
                    problems.add(language + ": " + entry.getKey() + " has placeholders " + actual + " instead of "
                            + expected);

                if (entry.getValue().getTagName().equals("string-array")) {
                    int expectedItems = entry.getValue().getElementsByTagName("item").getLength();
                    int actualItems = other.getElementsByTagName("item").getLength();
                    if (expectedItems != actualItems)
                        problems.add(language + ": " + entry.getKey() + " has " + actualItems + " items instead of "
                                + expectedItems);
                }
            }
        }
        if (!problems.isEmpty())
            fail(String.join("\n", problems));
    }

    @Test
    public void translationsAreNotEmptyAndApostrophesAreEscaped() throws Exception {
        List<String> problems = new ArrayList<>();
        for (String language : languages) {
            for (Map.Entry<String, Element> entry : translation(language).entrySet()) {
                NodeList items = entry.getValue().getElementsByTagName("item");
                List<String> texts = new ArrayList<>();
                if (items.getLength() == 0) {
                    texts.add(entry.getValue().getTextContent());
                } else {
                    for (int i = 0; i < items.getLength(); i++)
                        texts.add(items.item(i).getTextContent());
                }
                for (String text : texts) {
                    String trimmed = text.trim();
                    if (trimmed.isEmpty())
                        problems.add(language + ": " + entry.getKey() + " is empty");
                    boolean quoted = trimmed.startsWith("\"") && trimmed.endsWith("\"");
                    if (!quoted && UNESCAPED_APOSTROPHE.matcher(text).find())
                        problems.add(language + ": " + entry.getKey() + " has an unescaped apostrophe");
                    if (trimmed.startsWith("@") || trimmed.startsWith("?"))
                        problems.add(language + ": " + entry.getKey() + " starts with @ or ? (has to be escaped)");
                }
            }
        }
        if (!problems.isEmpty())
            fail(String.join("\n", problems));
    }

    @Test
    public void languagesAreOfferedInAndroidLanguageSettings() throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new File(res, "xml/locales_config.xml"));
        NodeList nodes = document.getElementsByTagName("locale");
        Set<String> configured = new HashSet<>();
        for (int i = 0; i < nodes.getLength(); i++)
            configured.add(((Element) nodes.item(i)).getAttribute("android:name"));

        Set<String> expected = new HashSet<>(languages);
        expected.add("en");
        assertEquals(expected, configured);
    }

    @Test
    public void languagePickerOffersSystemAndAllLanguages() throws Exception {
        Map<String, Element> resources = parse(new File(res, "values/strings_all_locales.xml"));
        NodeList items = resources.get("pref_setting_language_values").getElementsByTagName("item");
        List<String> values = new ArrayList<>();
        for (int i = 0; i < items.getLength(); i++)
            values.add(items.item(i).getTextContent().trim());

        int names = resources.get("pref_setting_language_names").getElementsByTagName("item").getLength();
        assertEquals("language names and values differ in size", values.size(), names);
        assertEquals("system", values.get(0));

        Set<String> expected = new HashSet<>(languages);
        expected.add("en");
        assertEquals(expected, new HashSet<>(values.subList(1, values.size())));
    }

    @Test
    public void languagesAreListedInTheAboutDialog() throws Exception {
        Map<String, Element> names = parse(new File(res, "values/strings_all_locales.xml"));
        List<String> problems = new ArrayList<>();
        for (String language : languages) {
            for (String part : new String[]{"independent", "local", "translator"}) {
                String key = "locale_" + language + "_" + part;
                if (!names.containsKey(key))
                    problems.add("strings_all_locales.xml: missing " + key);
            }
        }
        if (!problems.isEmpty())
            fail(String.join("\n", problems));
    }

    /**
     * Plural categories of the languages (CLDR). Android lint reports a missing quantity (e.g. "many" in Polish,
     * Russian, Czech, Slovak and French) and a quantity which is not used by the language.
     */
    private static final Map<String, List<String>> QUANTITIES = new LinkedHashMap<>();

    static {
        QUANTITIES.put("en", Arrays.asList("one", "other"));
        QUANTITIES.put("de", Arrays.asList("one", "other"));
        QUANTITIES.put("fr", Arrays.asList("one", "many", "other"));
        for (String language : new String[]{"cs", "sk", "pl", "ru"})
            QUANTITIES.put(language, Arrays.asList("one", "few", "many", "other"));
    }

    private static void collectPluralProblems(String language, Map<String, Element> resources, List<String> problems) {
        List<String> expected = QUANTITIES.get(language);
        if (expected == null) {
            problems.add(language + ": add the plural quantities of the language to this test");
            return;
        }

        for (Element element : resources.values()) {
            if (!element.getTagName().equals("plurals"))
                continue;

            Set<String> actual = new TreeSet<>();
            NodeList items = element.getElementsByTagName("item");
            for (int i = 0; i < items.getLength(); i++)
                actual.add(((Element) items.item(i)).getAttribute("quantity"));

            if (!actual.equals(new TreeSet<>(expected)))
                problems.add(language + ": " + element.getAttribute("name") + " has quantities " + actual
                        + " instead of " + new TreeSet<>(expected));
        }
    }

    @Test
    public void pluralsHaveExactlyTheQuantitiesOfTheLanguage() throws Exception {
        List<String> problems = new ArrayList<>();

        collectPluralProblems("en", parse(new File(res, "values/strings.xml")), problems);
        for (String language : languages)
            collectPluralProblems(language, translation(language), problems);

        if (!problems.isEmpty())
            fail(String.join("\n", problems));
    }
}
