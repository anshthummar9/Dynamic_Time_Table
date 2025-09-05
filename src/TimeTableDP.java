import java.util.*;

class Lecture {
    String subject;
    String faculty;
    String classroom;

    Lecture(String subject, String faculty, String classroom) {
        this.subject = subject;
        this.faculty = faculty;
        this.classroom = classroom;
    }

    @Override
    public String toString() {
        return subject + " (" + faculty + ", " + classroom + ")";
    }
}

public class TimeTableDP {
    static final int DAYS = 5;  // Monday to Friday
    static final int HOURS = 6; // 6 lectures per day
    static Lecture[][] timetable = new Lecture[DAYS][HOURS];
    static String[] dayNames = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int type = 2; // default College
        List<Lecture> lectures = new ArrayList<>();

        try {
            System.out.println("Select Type (1. School, 2. College): ");
            type = sc.nextInt();
            sc.nextLine();

            System.out.print("Enter number of subjects: ");
            int nSubjects = sc.nextInt();
            sc.nextLine();

            for (int i = 0; i < nSubjects; i++) {
                System.out.print("Enter Subject " + (i+1) + ": ");
                String subject = sc.nextLine();

                System.out.print("Enter Faculty for " + subject + ": ");
                String faculty = sc.nextLine();

                System.out.print("Enter Classroom for " + subject + ": ");
                String classroom = sc.nextLine();

                lectures.add(new Lecture(subject, faculty, classroom));
            }
        } catch (Exception e) {
            // fallback default subjects if no input given
            lectures.add(new Lecture("Math", "Prof. A", "R101"));
            lectures.add(new Lecture("Physics", "Prof. B", "R102"));
            lectures.add(new Lecture("CS", "Prof. C", "Lab1"));
            System.out.println("⚠ No input given, using default subjects.");
        }

        // Fill timetable dynamically
        int idx = 0;
        for (int d = 0; d < DAYS; d++) {
            for (int h = 0; h < HOURS; h++) {
                timetable[d][h] = lectures.get(idx % lectures.size());
                idx++;
            }
        }

        printTable(type);
        sc.close();
    }

    static void printTable(int type) {
        System.out.println("\n================= TIME TABLE (" + (type==1?"School":"College") + ") =================\n");
        System.out.print("Time      ");
        for (String day : dayNames) {
            System.out.printf("\t%-30s", day);
        }
        System.out.println();

        for (int h = 0; h < HOURS; h++) {
            System.out.printf("%02d:00-%02d:00\t ", (9+h), (10+h));
            for (int d = 0; d < DAYS; d++) {
                System.out.printf("%-30s", timetable[d][h]);
            }
            System.out.println();
        }
    }
}