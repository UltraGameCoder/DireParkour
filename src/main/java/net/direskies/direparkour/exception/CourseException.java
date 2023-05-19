package net.direskies.direparkour.exception;

public class CourseException extends RuntimeException {

    private final String course;

    public CourseException(String course, String reason) {
        super(reason);
        this.course = course;
    }

    public String getCourse() {
        return course;
    }
}
