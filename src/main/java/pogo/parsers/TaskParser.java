package pogo.parsers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pogo.commands.AddDeadlineCommand;
import pogo.commands.AddEventCommand;
import pogo.commands.AddToDoCommand;
import pogo.commands.Command;
import pogo.commands.DeleteTaskCommand;
import pogo.commands.InvalidCommand;
import pogo.commands.ListTasksCommand;
import pogo.commands.MarkTaskCommand;
import pogo.commands.UnmarkTaskCommand;
import pogo.common.Messages;
import pogo.tasks.exceptions.PogoInvalidTaskException;

/**
 * TaskParser parses user input into a Command object.
 */
public class TaskParser {
    private static final Pattern LIST_PATTERN = Pattern.compile("/from (?<from>.*) /to (?<to>.*)");
    private static final Pattern DEADLINE_PATTERN = Pattern.compile("(?<description>.*) /by (?<by>.*)");
    private static final Pattern TODO_PATTERN = Pattern.compile("(?<description>.*)");
    private static final Pattern EVENT_PATTERN = Pattern.compile("(?<description>.*) /from (?<from>.*) /to (?<to>.*)");
    private static final Pattern MARK_PATTERN = Pattern.compile("(?<index>\\d+)");


    /**
     * Parses a list command.
     * A valid list command has the format "list /from FROM /to TO".
     * Returns an InvalidCommand if the command is invalid.
     *
     * @param arguments The input string.
     * @return The ListTasksCommand if the command is valid.
     */
    public static Command parseListCommand(String arguments) {
        final Matcher listMatcher = LIST_PATTERN.matcher(arguments);
        // Set from and to encompass all dates
        LocalDateTime from = LocalDateTime.of(LocalDate.MIN, LocalTime.MIN);
        LocalDateTime to = LocalDateTime.of(LocalDate.MAX, LocalTime.MAX);

        try {
            if (listMatcher.matches()) {
                String fromString = listMatcher.group("from");
                String toString = listMatcher.group("to");
                from = DateTimeParser.parse(fromString);
                to = DateTimeParser.parse(toString);

                if (from.isAfter(to)) {
                    return new InvalidCommand(Messages.INVALID_DATE_RANGE);
                }
            }
        } catch (DateTimeParseException e) {
            return new InvalidCommand(e.getMessage());
        }

        return new ListTasksCommand(from, to);
    }

    private static boolean isEmptyDescription(String description) {
        return description.equals("");
    }

    /**
     * Parses a deadline command.
     * A valid deadline command has the format "deadline DESCRIPTION /by DEADLINE".
     * Returns an InvalidCommand if the command is invalid.
     *
     * @param input The input string.
     * @return The AddDeadlineCommand if the command is valid.
     */
    public static Command parseDeadlineCommand(String input) throws PogoInvalidTaskException {
        final Matcher matcher = DEADLINE_PATTERN.matcher(input);

        InvalidCommand ic = new InvalidCommand(Messages.INVALID_TASK
                + System.lineSeparator()
                + AddDeadlineCommand.MESSAGE_USAGE);

        if (!matcher.matches()) {
            return ic;
        }

        String description = matcher.group("description");
        if (isEmptyDescription(description)) {
            return ic;
        }

        String by = matcher.group("by");
        try {
            return new AddDeadlineCommand(description, DateTimeParser.parse(by));
        } catch (DateTimeParseException e) {
            return new InvalidCommand(e.getMessage());
        }
    }

    /**
     * Parses a todo command.
     * A valid todo command has the format "todo TASK".
     * Returns an InvalidCommand if the command is invalid.
     *
     * @param input The input string.
     * @return The AddToDoCommand if the command is valid.
     */
    public static Command parseToDoCommand(String input) throws PogoInvalidTaskException {
        final Matcher matcher = TODO_PATTERN.matcher(input);
        InvalidCommand ic =
                new InvalidCommand(Messages.INVALID_TASK
                        + System.lineSeparator()
                        + AddToDoCommand.MESSAGE_USAGE);

        if (!matcher.matches()) {
            return ic;
        }

        String description = matcher.group("description");
        if (isEmptyDescription(description)) {
            return ic;
        }
        return new AddToDoCommand(description);
    }

    /**
     * Parses an event command.
     * A valid event command has the format "event DESCRIPTION /from START /to END".
     * Returns an InvalidCommand if the command is invalid.
     *
     * @param input The input string.
     * @return The AddEventCommand if the command is valid.
     */
    public static Command parseEventCommand(String input) throws PogoInvalidTaskException {
        final Matcher matcher = EVENT_PATTERN.matcher(input);
        InvalidCommand ic =
                new InvalidCommand(Messages.INVALID_TASK
                        + System.lineSeparator()
                        + AddEventCommand.MESSAGE_USAGE);

        if (!matcher.matches()) {
            return ic;
        }

        String description = matcher.group("description");
        if (isEmptyDescription(description)) {
            return ic;
        }

        String from = matcher.group("from");
        String to = matcher.group("to");
        try {
            return new AddEventCommand(description, DateTimeParser.parse(from), DateTimeParser.parse(to));
        } catch (DateTimeParseException e) {
            return new InvalidCommand(e.getMessage());
        }
    }

    /**
     * Parses the index of a task, returning -1 for invalid indices.
     *
     * @param input The input string.
     * @return The index of the task, -1 if it does not exist.
     */
    private static int parseIndex(String input) {
        final Matcher matcher = MARK_PATTERN.matcher(input);
        if (!matcher.matches()) {
            return -1;
        }

        try {
            return Integer.parseInt(matcher.group("index")) - 1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Parses a "mark task as done" command.
     *
     * @param input  The input string.
     * @param isMark Whether the command is a mark command or an unmark command.
     */
    public static Command parseMarkCommand(String input, boolean isMark) {
        int index = parseIndex(input);
        if (index < 0) {
            return new InvalidCommand(Messages.INVALID_INDEX);
        }

        if (isMark) {
            return new MarkTaskCommand(index);
        }
        return new UnmarkTaskCommand(index);
    }

    /**
     * Parses a "delete task" command.
     *
     * @param input The input string.
     * @return The DeleteTaskCommand object.
     */
    public static Command parseDeleteCommand(String input) {
        int index = parseIndex(input);
        if (index < 0) {
            return new InvalidCommand(Messages.INVALID_INDEX);
        }

        return new DeleteTaskCommand(index);
    }
}
