import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * The main class to run the Automatic Timetable Generator.
 * This class sets up the problem by defining the subjects, teachers, and constraints,
 * then invokes the solver and prints the result.
 */
public class TimeTableDP {

    public static void main(String[] args) {
        // Use try-with-resources to automatically close the scanner
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("--- Automatic Timetable Generator ---");

            // --- Fixed Configuration ---
            int numDays = 5; // A standard week (Monday-Friday)
            int numSlots = 7; // 6 lectures + 1 lunch per day
            System.out.println("Configuration: 5-day week, six one-hour lectures per day (+1 lunch break).");

            // --- Step 1: Get Available Subjects from User ---
            System.out.print("\nStep 1: How many subjects do you want to enter? ");
            int numSubjects;
            try {
                numSubjects = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.err.println("Invalid number. Exiting.");
                return;
            }

            List<String> subjectNames = new ArrayList<>();
            System.out.println("Please enter the names of the " + numSubjects + " subjects, one per line.");
            for (int i = 0; i < numSubjects; i++) {
                System.out.print("> ");
                String line = scanner.nextLine();
                if (!line.trim().isEmpty()) {
                    subjectNames.add(line.trim());
                } else {
                    System.out.println("Subject name cannot be empty. Please try again.");
                    i--; // Decrement to re-do this iteration
                }
            }

            if (subjectNames.isEmpty()) {
                System.err.println("No subjects entered. Exiting.");
                return;
            }

            // --- Step 2: Get Available Teachers/Faculty from User ---
            System.out.print("\nStep 2: How many faculty members do you want to enter? ");
            int numTeachers;
            try {
                numTeachers = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.err.println("Invalid number. Exiting.");
                return;
            }

            List<String> teacherNames = new ArrayList<>();
            System.out.println("Please enter the names of the " + numTeachers + " faculty members, one per line.");
            for (int i = 0; i < numTeachers; i++) {
                System.out.print("> ");
                String line = scanner.nextLine();
                if (!line.trim().isEmpty()) {
                    teacherNames.add(line.trim());
                } else {
                    System.out.println("Faculty name cannot be empty. Please try again.");
                    i--; // Decrement to re-do this iteration
                }
            }

            if (teacherNames.isEmpty()) {
                System.err.println("No faculty members entered. Exiting.");
                return;
            }

            // --- Step 3: Assign Faculty and Hours to each Subject ---
            List<Subject> subjects = new ArrayList<>();
            int totalHours = 0;
            System.out.println("\nStep 3: Assign a faculty member and weekly hours to each subject.");

            for (String subjectName : subjectNames) {
                System.out.println("\n----- Configuring Subject: '" + subjectName + "' -----");

                // Assign faculty
                System.out.println("Available Faculty:");
                for (int i = 0; i < teacherNames.size(); i++) {
                    System.out.println((i + 1) + ". " + teacherNames.get(i));
                }

                int teacherChoice = -1;
                while (teacherChoice < 1 || teacherChoice > teacherNames.size()) {
                    System.out.print("Choose a faculty member for " + subjectName + " (1-" + teacherNames.size() + "): ");
                    try {
                        teacherChoice = Integer.parseInt(scanner.nextLine());
                        if (teacherChoice < 1 || teacherChoice > teacherNames.size()) {
                            System.err.println("Invalid choice. Please enter a number between 1 and " + teacherNames.size() + ".");
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid input. Please enter a number.");
                    }
                }
                String assignedTeacher = teacherNames.get(teacherChoice - 1);

                // Assign hours
                int hours = 0;
                while (hours <= 0) {
                    System.out.print("Enter total weekly lectures for " + subjectName + ": ");
                    try {
                        hours = Integer.parseInt(scanner.nextLine());
                        if (hours <= 0) {
                            System.err.println("Hours must be a positive number.");
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid input. Please enter a number.");
                    }
                }
                
                subjects.add(new Subject(subjectName, assignedTeacher, hours));
                totalHours += hours;
            }

            System.out.println("\nAttempting to generate timetable with the provided data...");

            // --- Pre-computation Check ---
            int availableSlots = (numDays * numSlots) - (numDays); // Subtracting 1 lunch slot per day
            if (totalHours > availableSlots) {
                System.err.println("Error: Total required subject hours (" + totalHours +
                                   ") exceed available teaching slots (" + availableSlots + ") in the week.");
                return;
            }

            // --- Solving ---
            TimetableSolver solver = new TimetableSolver(subjects, numDays, numSlots);
            boolean success = solver.solve();

            // --- Output ---
            if (success) {
                System.out.println("✅ Timetable generated successfully!");
                solver.printTimetable();
            } else {
                System.err.println("❌ Could not generate a timetable with the given constraints.");
                System.err.println("Please check for conflicts or impossible demands (e.g., too many hours for one teacher).");
            }
        }
    }
}

/**
 * Represents a single subject to be scheduled.
 * This class holds information about the subject's name, its teacher,
 * the total weekly hours required, and how many hours have been scheduled so far.
 */
class Subject {
    String name;
    String teacher;
    int totalHours;
    int placedHours;

    /**
     * Constructs a new Subject.
     * @param name The name of the subject (e.g., "Mathematics").
     * @param teacher The name of the teacher assigned to the subject (e.g., "Mr. Smith").
     * @param totalHours The total number of periods this subject requires per week.
     */
    public Subject(String name, String teacher, int totalHours) {
        this.name = name;
        this.teacher = teacher;
        this.totalHours = totalHours;
        this.placedHours = 0; // Initially, no hours have been placed on the timetable.
    }

    @Override
    public String toString() {
        // A helper method to easily print subject details, useful for debugging.
        return String.format("%s (%s)", this.name, this.teacher);
    }
}

/**
 * The core engine for generating the timetable.
 * This class uses a recursive backtracking algorithm to find a valid schedule
 * that satisfies all defined constraints.
 */
class TimetableSolver {

    private final List<Subject> subjects;
    private final Subject[][] timetable;
    private final int numDays;
    private final int numSlots;
    private static final int LUNCH_SLOT_INDEX = 3; // 4th period (01:00-02:00) is reserved for lunch.

    /**
     * Constructs the solver with the necessary configuration.
     * @param subjects A list of all subjects to be scheduled.
     * @param numDays The number of working days in a week.
     * @param numSlots The number of periods (slots) per day.
     */
    public TimetableSolver(List<Subject> subjects, int numDays, int numSlots) {
        this.subjects = subjects;
        this.numDays = numDays;
        this.numSlots = numSlots;
        // Initialize the timetable grid with nulls, representing empty slots.
        this.timetable = new Subject[numDays][numSlots];
    }

    /**
     * Public method to start the solving process.
     * @return true if a valid timetable was generated, false otherwise.
     */
    public boolean solve() {
        return solveRecursive(0, 0);
    }

    /**
     * The core recursive backtracking function. It tries to fill the timetable slot by slot.
     * @param day The current day index to fill.
     * @param slot The current slot index to fill.
     * @return true if a solution is found from this point, false otherwise.
     */
    private boolean solveRecursive(int day, int slot) {
        // Base case: If we have moved past the last day, the timetable is complete.
        if (day >= numDays) {
            // Final validation: Ensure every subject has its required hours fulfilled.
            return subjects.stream().allMatch(s -> s.placedHours == s.totalHours);
        }

        // Calculate the coordinates for the next recursive call.
        int nextSlot = (slot + 1) % numSlots;
        int nextDay = (nextSlot == 0) ? day + 1 : day;

        // Randomize subjects to get different valid timetables on each run.
        Collections.shuffle(subjects);

        // --- Attempt 1: Try to place a subject in the current slot ---
        for (Subject subject : subjects) {
            if (isValidPlacement(subject, day, slot)) {
                // Place the subject
                placeSubject(subject, day, slot);

                // Recurse to the next slot
                if (solveRecursive(nextDay, nextSlot)) {
                    return true; // Success! A solution was found down this path.
                }

                // Backtrack: If the recursive call failed, undo the placement.
                removeSubject(subject, day, slot);
            }
        }

        // --- Attempt 2: Try to leave the slot empty (as a free period) ---
        // This is crucial for timetables where total subject hours are less than total available slots.
        if (solveRecursive(nextDay, nextSlot)) {
            return true;
        }

        // If no subject could be placed and leaving it empty also failed, this path is a dead end.
        return false;
    }

    /**
     * Checks if placing a subject in a given slot is valid based on all constraints.
     * @param subject The subject to place.
     * @param day The day index for the placement.
     * @param slot The slot index for the placement.
     * @return true if the placement is valid, false otherwise.
     */
    private boolean isValidPlacement(Subject subject, int day, int slot) {
        // Constraint: Enforce a mandatory lunch break.
        if (slot == LUNCH_SLOT_INDEX) {
            return false; // This slot is always reserved for lunch.
        }
        
        // Constraint: Subject's required hours must not be already fulfilled.
        if (subject.placedHours >= subject.totalHours) {
            return false;
        }

        // New Constraint: A subject can only appear once per day.
        for (int s = 0; s < numSlots; s++) {
            Subject entry = timetable[day][s];
            if (entry != null && entry.name.equals(subject.name)) {
                return false; // Subject already scheduled for this day.
            }
        }

        // Constraint: Avoid back-to-back lectures of the same subject (this is now redundant due to the above check but harmless).
        if (slot > 0 && timetable[day][slot - 1] != null && timetable[day][slot - 1].name.equals(subject.name)) {
            return false;
        }

        // Constraint: Limit teacher's daily load to a maximum of 5 teaching periods.
        int dailyLoad = 0;
        for (int s = 0; s < numSlots; s++) {
            Subject entry = timetable[day][s];
            if (entry != null && entry.teacher.equals(subject.teacher)) {
                dailyLoad++;
            }
        }
        return dailyLoad < 5; // Max 5 teaching slots + 1 lunch break = 6 total daily periods.
    }

    private void placeSubject(Subject subject, int day, int slot) {
        timetable[day][slot] = subject;
        subject.placedHours++;
    }

    private void removeSubject(Subject subject, int day, int slot) {
        timetable[day][slot] = null;
        subject.placedHours--;
    }

    /**
     * Prints the generated timetable to the console in a formatted grid.
     */
    public void printTimetable() {
        System.out.println("\nGenerated Timetable:");
        String[] dayNames = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        String[] timeSlots = {
            "10:00-11:00", // 0
            "11:00-12:00", // 1
            "12:00-01:00", // 2
            "01:00-02:00", // 3 (Lunch)
            "02:00-03:00", // 4
            "03:00-04:00", // 5
            "04:00-05:00"  // 6
        };

        // Print header
        System.out.printf("%-12s", "Time");
        for (int i = 0; i < numDays; i++) {
            System.out.printf("%-25s", dayNames[i]);
        }
        System.out.println("\n" + "=".repeat(12 + 25 * numDays));

        // Print timetable body
        for (int slot = 0; slot < numSlots; slot++) {
            System.out.printf("%-12s", timeSlots[slot]);
            for (int day = 0; day < numDays; day++) {
                if (slot == LUNCH_SLOT_INDEX) {
                    System.out.printf("%-25s", "--- Lunch Break ---");
                } else {
                    Subject subject = timetable[day][slot];
                    if (subject != null) {
                        System.out.printf("%-25s", subject);
                    } else {
                        System.out.printf("%-25s", "- Free -");
                    }
                }
            }
            System.out.println();
        }
    }
}
