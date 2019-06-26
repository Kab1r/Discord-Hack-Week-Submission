package hackweek.group.filterbot;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class CommandManager {
    private Database database;

    public CommandManager(Database database) {
        this.database = database;
    }

    public void handle(MessageReceivedEvent event) {
        // Assume Message is a command
        Command command = getCommand(event.getMessage());
        switch (command) {
            case HELP:
                help(event.getChannel());
                break;
            case ADD:
                add(event.getMessage());
                break;
            case REMOVE:
                remove(event.getMessage());
                break;
            case TEST:
                test(event.getMessage());
                break;
            case LIST:
                list(event.getMessage());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + command);
        }
    }

    private Command getCommand(Message message) {
        String commandPrefix = database.getCommandPrefix(message.getGuild().getId());
        String messageWithoutPrefix = message.getContentStripped().toLowerCase().substring(commandPrefix.length()).trim();
        if (messageWithoutPrefix.startsWith(Command.HELP.toString())) return Command.HELP;
        if (messageWithoutPrefix.startsWith(Command.ADD.toString())) return Command.ADD;
        if (messageWithoutPrefix.startsWith(Command.REMOVE.toString())) return Command.REMOVE;
        if (messageWithoutPrefix.startsWith(Command.TEST.toString())) return Command.TEST;
        if (messageWithoutPrefix.startsWith(Command.LIST.toString())) return Command.LIST;

        throw new IllegalStateException("Message: " + messageWithoutPrefix);

    }

    private void help(MessageChannel channel) {
        channel.sendMessage(
                new EmbedBuilder()
                        .setAuthor("FilterBot")
                        .setTitle("Help")
                        .setDescription("FilterBot filters specified words, images, and videos")
                        .addField("Help", "Lists commands and description", false)
                        .addField("Add", "Adds filter for current server", false)
                        .addField("Remove", "Removes filter for current server", false)
                        .addField("Test", "Provides list of possible filters given an Image or Video", false)
                        .addField("List", "Returns the list of filters added to the current server", false)
                        .setFooter("Help Command", "https://i.imgur.com/HXQSvGu.jpeg")
                        .build()
        ).queue();
    }

    private void add(Message message) {
        String commandPrefix = database.getCommandPrefix(message.getGuild().getId());
        String messageWithoutPrefix = message.getContentStripped().toLowerCase().substring(commandPrefix.length()).trim();
        String filter = messageWithoutPrefix.substring("add".length()).trim();
        if (filter.isEmpty()) {
            message.getChannel().sendMessage("No filter was provided").queue();
            return;
        }
        database.addFilters(message.getGuild().getId(), Collections.singletonList(filter));
        message.getChannel().sendMessage("Filter term: \"" + messageWithoutPrefix + "\" added").queue();
    }

    private void remove(Message message) {
        boolean removedObj = false; //set to true if an object is removed from the database
        String commandPrefix = database.getCommandPrefix(message.getGuild().getId());
        String messageWithoutPrefix = message.getContentStripped().toLowerCase().substring(commandPrefix.length()).trim();
        String filter = messageWithoutPrefix.substring("remove".length()).trim();

        if (filter.isEmpty()) {
            message.getChannel().sendMessage("No filter was provided").queue();
            return;
        }

        List<String> filters = database.getFilters(message.getGuild().getId());

        for (int i = 0; i < filters.size(); i++) {
            if (filter.equalsIgnoreCase(filters.get(i))) {
                filter = filters.remove(i);
                removedObj = true;
                break;
            }
        }


        if (removedObj) {
            database.setFilters(message.getGuild().getId(), filters);
            message.getChannel().sendMessage("Successfully removed filter \"" + filter + "\"").queue();
        } else
            message.getChannel().sendMessage("Unable to find filter \"" + filter + "\"").queue();
    }

    private void test(Message message) {
        // TODO
    }

    private void list(Message message) {
        if (database.getFilters(message.getGuild().getId()).isEmpty()) {
            message.getChannel().sendMessage("There are no filters added in this server, use f!add [term] to add a filter.").queue();
            return;
        }    //case: if no filters are added for the given server

        String filters = "";
        for (String tag : database.getFilters(message.getGuild().getId()))      //loop: building a string from the list of filters in the given server
            filters += tag + ", ";

        filters = filters.substring(0, filters.lastIndexOf(","));           //removing the last "," in the "filter" string
        message.getChannel().sendMessage("Filters in " + message.getGuild().getName() + ": " + filters).queue(); //sends message
    }

    private enum Command {
        HELP,   // Informational Help Command
        ADD,    // Add new Filter
        REMOVE, // Remove existing Filter
        TEST,   // Test image or video for possible filters
        LIST,   //Lists filters for the given server
    }
}
