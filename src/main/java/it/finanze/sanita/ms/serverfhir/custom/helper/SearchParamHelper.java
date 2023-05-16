package it.finanze.sanita.ms.serverfhir.custom.helper;

import ca.uhn.fhir.context.RuntimeSearchParam;
import org.hl7.fhir.r4.model.StringType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchParamHelper {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SearchParamHelper.class);

    public static List<String> getAllPaths(HashMap<String, RuntimeSearchParam> searchParams, String resourceName) {
        return searchParams
                .values()
                .stream()
                .filter(p -> p.getBase().contains(resourceName))
                .map(RuntimeSearchParam::getPath)
                .collect(Collectors.toList());
    }

    public static List<String> splitPathsWithPipe(List<String> paths, String resourceName) {
        return paths
                .stream()
                .flatMap(value -> Arrays.stream(value.split("\\|")))
                .filter(value -> value.contains(resourceName))
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    public static List<String> getPurePaths(List<String> paths) {
        return paths
                .stream()
                .filter(SearchParamHelper::isPurePath)
                .collect(Collectors.toList());
    }

    public static List<String> getDirtyPaths(List<String> paths) {
        return paths
                .stream()
                .filter(path -> !isPurePath(path))
                .collect(Collectors.toList());
    }

    private static boolean isPurePath(String path) {
        return Stream
                .of("(", "|", ")")
                .noneMatch(path::contains);
    }

    public static List<String> cleanPaths(String resourceName, List<String> dirtyPaths) {
        if (dirtyPaths.isEmpty()) return new ArrayList<>();
        List<String> cleanedPaths = new ArrayList<>();
        cleanedPaths.addAll(managePathsWithAs(dirtyPaths));
        cleanedPaths.addAll(managePathsWithAsMethod(dirtyPaths));
        cleanedPaths.addAll(managePathsWithWhere(dirtyPaths));
        logRemainingPaths(resourceName, dirtyPaths);
        return cleanedPaths;
    }

    private static List<String> managePathsWithAs(List<String> dirtyPaths) {
        // (ActivityDefinition.useContext.value as Quantity)
        List<String> paths = consumePathsWithValue(dirtyPaths, " as ");
        if (paths.isEmpty()) return new ArrayList<>();
        return cleanPathsWithRegex(paths, "^.*\\((.*) as .*$");
    }

    private static List<String> managePathsWithAsMethod(List<String> dirtyPaths) {
        // (ActivityDefinition.useContext.value as Quantity)
        List<String> paths = consumePathsWithValue(dirtyPaths, ".as(");
        if (paths.isEmpty()) return new ArrayList<>();
        return cleanPathsWithSplit(paths, "\\.as\\(");
    }

    private static List<String> managePathsWithWhere(List<String> dirtyPaths) {
        // PractitionerRole.telecom.where(system='email')
        // Specimen.subject.where(resolve() is Patient)
        // ResearchElementDefinition.relatedArtifact.where(type='successor').resource // NOT MANAGED
        List<String> paths = consumePathsWithValue(dirtyPaths, ".where(");
        if (paths.isEmpty()) return new ArrayList<>();
        return cleanPathsWithSplit(paths, "\\.where\\(");
    }

    private static List<String> cleanPathsWithRegex(List<String> paths, String regex) {
        Pattern pattern = Pattern.compile(regex);
        return paths
                .stream()
                .map(path -> getValue(pattern, path))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static List<String> cleanPathsWithSplit(List<String> paths, String regex) {
        return paths
                .stream()
                .map(path -> path.split(regex))
                .filter(values -> values.length > 0)
                .map(values -> values[0])
                .collect(Collectors.toList());
    }

    private static String getValue(Pattern pattern, String path) {
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) return matcher.group(1);
        return null;
    }

    private static List<String> consumePathsWithValue(List<String> dirtyPaths, String value) {
        List<String> result = dirtyPaths
                .stream()
                .filter(path -> path.contains(value))
                .collect(Collectors.toList());
        dirtyPaths.removeAll(result);
        return result;
    }

    @SafeVarargs
    public static List<StringType> aggregate(List<String>... lists) {
        return Arrays
                .stream(lists)
                .flatMap(Collection::stream)
                .distinct()
                .map(StringType::new)
                .collect(Collectors.toList());
    }

    private static void logRemainingPaths(String resourceName, List<String> dirtyPaths) {
        if (dirtyPaths.isEmpty()) return;
        LOGGER.info("Unmanaged Paths for " + resourceName + ": " + dirtyPaths);
    }

        public static void main(String[] args) {
            String input1 = "Account.subject.where(resolve() is Patient)";
            String input2 = "Account.subject.where(resolve() is Patient).resource";

            Pattern pattern = Pattern.compile("^(.*?)\\.where\\(");

            Matcher matcher1 = pattern.matcher(input1);
            if (matcher1.find()) {
                String result1 = matcher1.group(1);
                System.out.println("Result 1: " + result1);
            }
            Matcher matcher2 = pattern.matcher(input2);
            if (matcher2.find()) {
                String result2 = matcher2.group(1) + "." + matcher2.group(2);
                System.out.println("Result 2: " + result2);
            }
        }

}
