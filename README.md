# 📅 Dynamic Time Table

A console-based **Dynamic Time Table** built in **Java** using a **backtracking algorithm**.  
It takes subjects, faculty, and weekly lecture requirements as input and generates a **conflict-free timetable**.

---
## ✨ Features
- User inputs:
  - Number of subjects & their names
  - Number of faculty members & their names
  - Weekly hours required for each subject
- Automatic assignment of subjects to slots
- Constraints:
  - Fixed **lunch break** every day (`01:00 - 02:00`)
  - Each subject appears at most **once per day**
  - Teachers cannot exceed **5 lectures per day**
  - No back-to-back lectures for the same subject
- Prints timetable in a **well-formatted grid**
- Handles invalid inputs & impossible schedules


---
## 🧩 Future Improvements

- GUI-based timetable visualization
- Export timetable as Excel / PDF / CSV
- Smarter teacher-subject allocation
- Support for weekends & multiple lunch breaks

---
## 🚀 How to Run
1. **Clone this repository:**
```
git clone https://github.com/anshthummar9/Dynamic_Time_Table.git
cd Dynamic_Time_Table
cd src
```
2. **Compile the program:**
```
javac TimeTableDP.java
```
3. **Run it:**
```
java TimeTableDP
```
