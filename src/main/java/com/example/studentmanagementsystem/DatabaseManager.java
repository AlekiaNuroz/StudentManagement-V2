package com.example.studentmanagementsystem;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.*;

@SuppressWarnings("unused")
public class DatabaseManager implements AutoCloseable {
    private static final HikariConfig config = new HikariConfig();
    private static final HikariDataSource dataSource;
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);


    static {
        config.setJdbcUrl(System.getenv("PGSQL_DATABASE"));
        config.setUsername(System.getenv("PGSQL_USERNAME"));
        config.setPassword(System.getenv("PGSQL_PASSWORD"));
        config.setMaximumPoolSize(10); // Adjust based on expected load
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000); // 30 seconds
        config.setMaxLifetime(1800000); // 30 minutes
        config.setConnectionTimeout(10000); // 10 seconds
        dataSource = new HikariDataSource(config);
    }

    public DatabaseManager() {
        ensureTablesExist();
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            logger.error("Failed to get database connection", e);
            return null;  // Return null instead of throwing an exception
        }
    }


    private void ensureTablesExist() {
        String[] createTableSQLs = {
                "CREATE TABLE IF NOT EXISTS students (" +
                        "id TEXT PRIMARY KEY, " +
                        "name TEXT NOT NULL, " +
                        "is_deleted BOOLEAN DEFAULT FALSE)",

                "CREATE TABLE IF NOT EXISTS courses (" +
                        "course_code TEXT PRIMARY KEY, " +
                        "name TEXT NOT NULL, " +
                        "max_capacity INTEGER NOT NULL, " +
                        "enrolled INTEGER DEFAULT 0, " +
                        "is_deleted BOOLEAN DEFAULT FALSE)",

                "CREATE TABLE IF NOT EXISTS enrollments (" +
                        "student_id TEXT, " +
                        "course_code TEXT, " +
                        "grade REAL, " +
                        "PRIMARY KEY (student_id, course_code), " +
                        "FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY(course_code) REFERENCES courses(course_code) ON DELETE CASCADE)"
        };

        try (Connection connection = getConnection()) {
            if (connection == null) {
                logger.error("Database connection unavailable. Tables cannot be created.");
                return;
            }
            try (Statement statement = connection.createStatement()) {
                for (String sql : createTableSQLs) {
                    statement.addBatch(sql);
                }
                statement.executeBatch(); // Execute all queries in a batch for efficiency
            }
        } catch (SQLException e) {
            logger.error("Failed to create tables", e);
        }
    }

    public boolean insertCourse(Course course) {
        String insertSQL = "INSERT INTO courses (course_code, name, max_capacity) VALUES (?, ?, ?)";

        try (Connection connection = getConnection()) {
            if (connection == null) {
                logger.error("Database connection unavailable. Course cannot be created.");
                return false;
            }
            try (PreparedStatement statement = connection.prepareStatement(insertSQL)) {
                statement.setString(1, course.getId().toLowerCase());
                statement.setString(2, course.getName());
                statement.setInt(3, course.getMaxCapacity());

                int rowsAffected = statement.executeUpdate();
                logger.info("Inserted course: {} (Rows affected: {})", course.getId(), rowsAffected);
                return rowsAffected > 0;
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            logger.warn("Course {} already exists in the database.", course.getId());
        } catch (SQLException e) {
            logger.error("Error inserting course: {}", course.getId(), e);
        }
        return false;
    }

    public void deleteRestoreCourse(Course course, boolean delete) {
        String updateSQL = "UPDATE courses SET is_deleted = ? WHERE course_code = ?";

        try (Connection connection = getConnection()) {
            if (connection == null) {
                logger.error("Database connection unavailable. Course cannot be deleted or restored.");
                return;
            }
            try (PreparedStatement statement = connection.prepareStatement(updateSQL)) {

                statement.setBoolean(1, delete);
                statement.setString(2, course.getId());

                int rowsAffected = statement.executeUpdate();
                if (rowsAffected == 0) {
                    logger.info("Course {} does not exist in the database.", course.getId());
                }
            }
        } catch (SQLException e) {
            logger.error("Error deleting or restoring course: {}", course.getId(), e);
        }
    }

    public void deleteRestoreStudent(Student student, boolean delete) {
        String updateSQL = "UPDATE students SET is_deleted = ? WHERE id = ?";

        try (Connection connection = getConnection()) {
            if (connection == null) {
                logger.error("Database connection unavailable. Student cannot be deleted or restored.");
                return;
            }
            try (PreparedStatement statement = connection.prepareStatement(updateSQL)) {

                statement.setBoolean(1, delete);
                statement.setString(2, student.getId());

                int rowsAffected = statement.executeUpdate();
                if (rowsAffected == 0) {
                    logger.info("Student {} does not exist in the database.", student.getId());
                }
            }
        } catch (SQLException e) {
            logger.error("Error deleting or restoring student: {}", student.getId(), e);
        }
    }

    public List<Course> getCourses(boolean deleted) {
        String selectSQL = "SELECT * FROM courses WHERE is_deleted=? ORDER BY course_code";
        List<Course> courses = new ArrayList<>();

        try (Connection conn = getConnection()) {
            if (conn == null) {
                logger.error("Database connection unavailable. Courses cannot be retrieved.");
                return null;
            }
            try (PreparedStatement statement = conn.prepareStatement(selectSQL)) {
                statement.setBoolean(1, deleted);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        courses.add(getCourseFromResultSet(resultSet));
                    }
                }
                return courses;
            }
        } catch (SQLException e) {
            logger.error("Error getting courses", e);
            return new ArrayList<>();
        }
    }

    public Course getCourse(String id) {
        String sql = "SELECT * FROM courses WHERE course_code = ?";

        try (Connection conn = getConnection()) {
            if (conn == null) {
                logger.error("Database connection unavailable. Course cannot be retrieved.");
                return null;
            }
            try (PreparedStatement statement = conn.prepareStatement(sql)) {

                statement.setString(1, id.toLowerCase());

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return getCourseFromResultSet(resultSet);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting course: {}", id, e);
        }
        return null;
    }

    public List<Student> getStudents(boolean deleted) {
        String selectSQL = "SELECT * FROM students WHERE is_deleted=? ORDER BY id";
        List<Student> students = new ArrayList<>();

        try (Connection conn = getConnection()) {
            if (conn == null) {
                logger.error("Database connection unavailable. Students cannot be retrieved.");
                return null;
            }
            try (PreparedStatement statement = conn.prepareStatement(selectSQL)) {

                statement.setBoolean(1, deleted);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        students.add(getStudentFromResultSet(resultSet));
                    }
                }
                return students;
            }
        } catch (SQLException e) {
            logger.error("Error getting students", e);
            return new ArrayList<>();
        }
    }

    public Student getStudent(String id) {
        String sql = "SELECT * FROM students WHERE id = ?";

        try (Connection conn = getConnection()) {
            if (conn == null) {
                logger.error("Database connection unavailable. Student cannot be retrieved.");
                return null;
            }
            try (PreparedStatement statement = conn.prepareStatement(sql)) {

                statement.setString(1, id.toLowerCase());

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return getStudentFromResultSet(resultSet);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting student: {}", id, e);
        }
        return null;
    }

    private Course getCourseFromResultSet(ResultSet resultSet) {
        try {
            String id = resultSet.getString("course_code");
            String name = resultSet.getString("name");
            int capacity = resultSet.getInt("max_capacity");
            int enrolled = resultSet.getInt("enrolled");
            return new Course(id, name, capacity, enrolled);
        } catch (SQLException e) {
            logger.error("Error getting course data from resultSet: {}", resultSet, e);
            return null; // Return null if there's an issue retrieving data
        }
    }

    private Student getStudentFromResultSet(ResultSet resultSet) {
        try {
            String studentId = resultSet.getString("id");
            String name = resultSet.getString("name");
            Student student = new Student(studentId, name);
            populateEnrollments(student);
            return student;
        } catch (SQLException e) {
            logger.error("Error getting student data from resultSet: {}", resultSet, e);
            return null;
        }
    }

    public int countUniqueStudentsEnrolled() {
        int count = 0;
        String sql = "SELECT COUNT(DISTINCT student_id) FROM enrollments;"; // Assuming 'enrollments' table

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = Objects.requireNonNull(connection).prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error counting unique students: " + e.getMessage());
        }

        return count;
    }

    public static Map<Student, Double> getStudentsEnrolledInCourse(Course course) {
        String sql = "SELECT s.id, s.name, e.grade FROM students s " +
                "JOIN enrollments e ON s.id = e.student_id " +
                "WHERE e.course_code = ?";

        Map<Student, Double> enrolledStudents = new HashMap<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, course.getId().toLowerCase());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String studentId = rs.getString("id");
                    String studentName = rs.getString("name");
                    Double grade = rs.getDouble("grade");
                    if (rs.wasNull()) {
                        grade = null;
                    }
                    Student student = new Student(studentId, studentName);
                    enrolledStudents.put(student, grade);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting students enrolled in course: {}", course.getId(), e);
        }
        return enrolledStudents;
    }

    public boolean insertStudent(Student student) {
        String insertSQL = "INSERT INTO students (id, name) VALUES (?, ?)";

        try (Connection connection = getConnection()) {
            if (connection == null) {
                logger.error("Database connection unavailable. Student cannot be inserted.");
                return false;
            }
            try (PreparedStatement statement = connection.prepareStatement(insertSQL)) {

                statement.setString(1, student.getId().toLowerCase());
                statement.setString(2, student.getName());

                int rowsAffected = statement.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            logger.error("Integrity constraint violation while inserting student with ID '{}': {}",
                    student.getId(), e.getMessage(), e);
        } catch (SQLException e) {
            logger.error("SQL error while inserting student with ID '{}': {}",
                    student.getId(), e.getMessage(), e);
        }
        return false;
    }

    public boolean enrollStudentInCourse(String studentId, String courseCode) {
        String insertEnrollment = "INSERT INTO enrollments (student_id, course_code, grade) VALUES (?, ?, NULL)";
        String updateCapacity = "UPDATE courses SET enrolled = enrolled + 1 " +
                "WHERE course_code = ? AND enrolled < max_capacity";

        try (Connection conn = getConnection()) {
            if (conn == null) {
                logger.error("Database connection unavailable. Enrollment cannot be retrieved.");
                return false;
            }
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement updateStmt = conn.prepareStatement(updateCapacity)) {
                updateStmt.setString(1, courseCode.toLowerCase());
                int updatedRows = updateStmt.executeUpdate();
                if (updatedRows == 0) {
                    conn.rollback();
                    return false; // Enrollment failed due to capacity limit
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertEnrollment)) {
                insertStmt.setString(1, studentId.toLowerCase());
                insertStmt.setString(2, courseCode.toLowerCase());
                insertStmt.executeUpdate();
            }

            conn.commit(); // Commit transaction if both queries succeed
            return true;
        } catch (SQLException e) {
            logger.error("Error enrolling student with ID '{}'", studentId, e);
        }
        return false;
    }

    public boolean unenrollStudentFromCourse(String studentId, String courseCode) {
        String deleteEnrollment = "DELETE FROM enrollments WHERE student_id = ? AND course_code = ?";
        String updateCapacity = "UPDATE courses SET enrolled = enrolled - 1 WHERE course_code = ?";

        try (Connection conn = getConnection()) {
            if (conn == null) {
                logger.error("Database connection unavailable. Unenrollment cannot be processed.");
                return false;
            }
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteEnrollment)) {
                deleteStmt.setString(1, studentId.toLowerCase());
                deleteStmt.setString(2, courseCode.toLowerCase());
                int deletedRows = deleteStmt.executeUpdate();
                if (deletedRows == 0) {
                    conn.rollback();
                    return false; // Unenrollment failed, enrollment record not found
                }
            }

            try (PreparedStatement updateStmt = conn.prepareStatement(updateCapacity)) {
                updateStmt.setString(1, courseCode.toLowerCase());
                updateStmt.executeUpdate();
            }

            conn.commit(); // Commit transaction if both queries succeed
            return true;
        } catch (SQLException e) {
            logger.error("Error unenrolling student with ID '{}'", studentId, e);
        }
        return false;
    }

    private void populateEnrollments(Student student) {
        String sql = "SELECT course_code, grade FROM enrollments WHERE student_id = ?";
        try (Connection conn = getConnection()) {
            if (conn == null) {
                logger.error("Database connection unavailable. Enrollments cannot be populated.");
                return;
            }
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, student.getId());

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String courseCode = rs.getString("course_code");
                        Double grade = rs.getDouble("grade");
                        if (rs.wasNull()) {
                            grade = null;
                        }
                        Course course = getCourse(courseCode);
                        if (course != null) {
                            student.getEnrolledCourses().put(course, grade);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error enrolling student with ID '{}'", student.getId(), e);
        }
    }

    public boolean assignGrade(String studentId, String courseCode, Double grade) {
        String updateSql = "UPDATE enrollments SET grade = ? WHERE student_id = ? AND course_code = ?";

        try (Connection conn = getConnection()) {
            if (conn == null) {
                logger.error("Database connection unavailable. Grade cannot be assigned.");
                return false;
            }
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                if (grade == null) {
                    stmt.setNull(1, Types.DOUBLE);
                } else {
                    stmt.setDouble(1, grade);
                }
                stmt.setString(2, studentId);
                stmt.setString(3, courseCode);

                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            logger.error("Error assigning grade for student with ID '{}'", studentId, e);
            return false;
        }
    }

    public boolean updateStudentName(String studentId, String newName) {
        String updateSql = "UPDATE students SET name = ? WHERE id = ?";

        try (Connection conn = getConnection()) {
            if (conn == null) {
                logger.error("Database connection unavailable. Student name cannot be updated.");
                return false;
            }
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {

                stmt.setString(1, newName.trim());
                stmt.setString(2, studentId);

                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            logger.error("Error updating student with ID '{}'", studentId, e);
            return false;
        }
    }

    public boolean updateCourse(Course course) {
        String updateSql = "UPDATE courses SET name = ?, max_capacity = ? WHERE course_code = ?";

        try (Connection conn = getConnection()) {
            if (conn == null) {
                logger.error("Database connection unavailable. Max capacity cannot be updated.");
                return false;
            }
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {

                stmt.setString(1, course.getName());
                stmt.setInt(2, course.getMaxCapacity());
                stmt.setString(3, course.getId());

                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            logger.error("Error updating course with ID '{}'", course.getId(), e);
            return false;
        }
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}