package pogo.parsers;

import pogo.commands.*;
import pogo.common.Messages;
import pogo.tasks.exceptions.PogoInvalidTaskException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Parser class is responsible for parsing user input.
 * Based on AddressBook2
 */
public class Parser {
    /**
     * The pattern used to parse commands.
     * A command consists of a command word e.g. "list" and an optional arguments string.
     */
    public static final Pattern COMMAND_PATTERN = Pattern.compile("(?<command>\\S+)(?<arguments>.*)");

    public static Command parseCommand(String input) {
        final Matcher matcher = COMMAND_PATTERN.matcher(input.trim());
        if (!matcher.matches()) {
            return new InvalidCommand(Messages.MESSAGE_INVALID_COMMAND);
        }

        final String commandWord = matcher.group("command");
        final String arguments = matcher.group("arguments").trim();

        try {
            switch (commandWord) {
            case ListTasksCommand.COMMAND_WORD:
                final Pattern LIST_PATTERN = Pattern.compile("/from (?<from>.*) /to (?<to>.*)");
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
            case AddDeadlineCommand.COMMAND_WORD:
                return TaskParser.parseDeadlineCommand(arguments);
            case AddToDoCommand.COMMAND_WORD:
                return TaskParser.parseToDoCommand(arguments);
            case AddEventCommand.COMMAND_WORD:
                return TaskParser.parseEventCommand(arguments);
            case MarkTaskCommand.COMMAND_WORD:
                return TaskParser.parseMarkCommand(arguments, true);
            case UnmarkTaskCommand.COMMAND_WORD:
                return TaskParser.parseMarkCommand(arguments, false);
            case DeleteTaskCommand.COMMAND_WORD:
                return TaskParser.parseDeleteCommand(arguments);
            case ExitCommand.COMMAND_WORD:
                return new ExitCommand();
            }
        } catch (PogoInvalidTaskException e) {
            return new InvalidCommand(Messages.INVALID_TASK);
        }

        return new InvalidCommand(Messages.MESSAGE_INVALID_COMMAND);
    }
}
