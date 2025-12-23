void main() throws Exception {
    var lines = Files.readAllLines(Path.of("day01/src/main/resources/input.txt"));
    int current = 50;
    int timesPointingAtZero = 0;
    for (String line : lines) {
        int increment = line.startsWith("R") ? 1 : -1;
        int rotation = Integer.parseInt(line.substring(1));
        for (int i = 0; i < rotation; i++) {
            current += increment;
            if (current < 0) current = 99;
            if (current > 99) current = 0;
            if (current == 0) timesPointingAtZero++;
        }
    }
    IO.println("The dial points at zero a total of " + timesPointingAtZero + " times.");
}
