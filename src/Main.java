import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    private static void cls() {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (Exception ignored) {
            System.out.println("\n".repeat(200));
        }
    }

    private static String getSeriesNameFromInput() {
        while (true) {
            System.out.println("What is the series named?");
            String res = scanner.nextLine().strip();
            if (res.isBlank()) {
                cls();
                continue;
            }
            cls();
            return res;
        }
    }

    private static List<Integer> getSeasonsFromInput() {
        List<Integer> res = new ArrayList<>();
        while (true) {
            System.out.println(
                "Write the number of each season you have downloaded; separated " +
                "by spaces or , (e.g.: '2,3,9 7 11')"
            );
            String seasons = scanner.nextLine().strip();
            cls();
            if (!seasons.matches("([\\s,]*(\\s)*\\b\\d+\\b[\\s,]*)+")) {
                continue;
            }
            Arrays
                .asList(seasons.split("[\\D|\\s]+"))
                .forEach(season -> {
                    if (season.matches("\\d+")) {
                        int n = Integer.parseInt(season);
                        if (n >= 0 && !res.contains(n)) res.add(n);
                    }
                });
            res.sort(Comparator.comparingInt(x -> x));
            res.forEach(season -> System.out.print(season + " "));
            System.out.println("\n\nAre those all the downloaded seasons? (Y/N)");
            String invalidInput = "";
            while (true) {
                System.out.print(invalidInput);
                String confirmInput = scanner.nextLine().strip().toUpperCase();
                cls();
                invalidInput = "Invalid input. Try again (Y/N): ";
                if (confirmInput.matches("Y|YES") && !confirmInput.matches("N|NO")) {
                    return res;
                } else if (
                    confirmInput.matches("N|NO") && !confirmInput.matches("Y|YES")
                ) {
                    res.clear();
                    break;
                }
            }
        }
    }

    private static boolean isValidPath(String path) {
        try {
            Paths.get(path);
        } catch (InvalidPathException | NullPointerException ex) {
            return false;
        }
        return true;
    }

    private static Path getPathFromInput(String seriesName) {
        String examplePath = "Users/Your Name/" + seriesName;

        if (File.separator.equals("\\")) {
            examplePath = "C:\\Users\\Your Name\\" + seriesName;
        }
        System.out.printf(
            "Please put all episodes into one shared folder if they're" +
            " not already and enter the path to that folder (e.g.: '%s'):%n",
            examplePath
        );
        while (true) {
            String pathString = scanner.nextLine().strip();
            cls();
            if (!isValidPath(pathString)) {
                System.out.println("Invalid Path.\nTry again:");
                continue;
            }
            Path pathToDir = Paths.get(pathString);
            if (Files.exists(pathToDir) && Files.isDirectory(pathToDir)) {
                return pathToDir;
            } else if (!Files.exists(pathToDir)) {
                System.out.println("Path doesn't exist.");
            } else if (!Files.isDirectory(pathToDir)) {
                System.out.println("Path exists but is not a folder.");
            } else {
                System.out.println("That didn't work.");
            }
            System.out.println("Please enter the path again:");
        }
    }

    public static void printProgress(double percentage, int barLength) {
        if (barLength < 1) {
            System.out.printf("\r%.2f%%", percentage * 100);
            return;
        }
        percentage *= barLength;
        System.out.printf(
            "\r%s%s  %.2f%%",
            "█".repeat((int) percentage),
            "░".repeat(barLength - (int) percentage),
            percentage * 100 / barLength
        );
    }

    public static void main(String[] args) {
        String seriesName = getSeriesNameFromInput();
        List<Integer> seriesSeasons = getSeasonsFromInput();
        String pathToDir = getPathFromInput(seriesName).toString();

        List<Path> seasonPaths = new ArrayList<>();

        seriesSeasons.forEach(number -> {
            String season = String.format("Season %02d", number);
            Path seasonPath = Paths.get(pathToDir, season);
            seasonPaths.add(seasonPath);
            if (Files.exists(seasonPath) && Files.isDirectory(seasonPath)) {
                return;
            }
            seasonPath.toFile().mkdirs();
        });

        seasonPaths.sort(Comparator.comparing(Path::toString));

        Map<Path, List<Path>> episodesMappedToSeasons = new TreeMap<>();

        seasonPaths.forEach(seasonPath ->
            episodesMappedToSeasons.putIfAbsent(seasonPath, new ArrayList<>())
        );

        List<File> filesInDir = new ArrayList<>(
            Arrays.asList(Objects.requireNonNull(new File(pathToDir).listFiles()))
        );

        System.out.println(
            "--- Attempting to move files into separate folders for" +
            " each season ---\n"
        );

        seasonPaths.forEach(seasonPath -> {
            String seasonNum = seasonPath
                .getFileName()
                .toString()
                .replace("Season ", "");
            filesInDir.forEach(file -> {
                if (file.getName().matches(".*[Ss]" + seasonNum + ".*")) {
                    episodesMappedToSeasons
                        .get(seasonPath)
                        .add(Paths.get(file.getPath()));
                }
            });
        });

        episodesMappedToSeasons.forEach((seasonPath, episodes) -> {
            String seasonNum = seasonPath
                .getFileName()
                .toString()
                .replace("Season ", "");
            System.out.println(seasonPath.getFileName() + ":");
            for (int i = 0; i < episodes.size(); i++) {
                Path file = episodes.get(i);
                String filename = file.getFileName().toString();
                if (filename.matches(".*[Ss]" + seasonNum + ".*")) {
                    Path newPathForEpisode = Paths.get(
                        seasonPath.toString(),
                        filename
                    );
                    try {
                        Files.move(file, newPathForEpisode);
                        episodes.set(i, newPathForEpisode);
                    } catch (IOException e) {
                        System.out.println();
                        throw new RuntimeException(e);
                    }
                    printProgress((double) (i + 1) / episodes.size(), 40);
                }
            }
            System.out.println();
        });
        System.out.println("\nPress ENTER to continue");
        scanner.nextLine();
        cls();

        episodesMappedToSeasons.forEach((seasonPath, episodes) -> {
            List<File> mkvFiles = new ArrayList<>();
            episodes.forEach(episode ->
                Arrays
                    .stream(Objects.requireNonNull(episode.toFile().listFiles()))
                    .filter(file -> file.getName().matches(".*\\.mkv"))
                    .forEach(mkvFiles::add)
            );
            mkvFiles.forEach(file -> {
                try {
                    Files.move(
                        Paths.get(file.getPath()),
                        Paths.get(seasonPath.toString(), file.getName())
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });

        episodesMappedToSeasons.forEach((s, episodes) ->
            episodes.forEach(episode -> {
                try (Stream<Path> files = Files.walk(episode)) {
                    files
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
        );

        seasonPaths.forEach(seasonPath -> {
            String seasonNum = seasonPath
                .getFileName()
                .toString()
                .replace("Season ", "s");
            File[] filesInSeason = Objects.requireNonNull(
                seasonPath.toFile().listFiles()
            );
            for (int i = 0; i < filesInSeason.length; i++) {
                Path oldPath = Paths.get(filesInSeason[i].getPath());
                Path newPath = Paths.get(
                    String.format(
                        "%s %se%02d.mkv",
                        seasonPath.getParent().getFileName(),
                        seasonNum,
                        i + 1
                    )
                );
                newPath = Paths.get(seasonPath.toString(), newPath.toString());
                try {
                    Files.move(oldPath, newPath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        scanner.close();
    }
}
