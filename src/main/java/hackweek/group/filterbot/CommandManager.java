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
            case SET_PREFIX:
                setPrefix(event.getMessage());
                break;
            case INVALID:
            default:
                invalid(event.getMessage());
        }
    }

    private void invalid(Message message) {
        String commandPrefix = database.getCommandPrefix(message.getGuild().getId());
        String messageWithoutPrefix = message.getContentStripped().toLowerCase().substring(commandPrefix.length()).trim();
        message.getChannel().sendMessage("\"" + messageWithoutPrefix + "\"" + " is not a valid command").queue();
    }

    private Command getCommand(Message message) {
        String commandPrefix = database.getCommandPrefix(message.getGuild().getId());
        String messageWithoutPrefix = message.getContentStripped().toLowerCase().substring(commandPrefix.length()).trim();
        if (messageWithoutPrefix.startsWith(Command.HELP.toString().toLowerCase())) return Command.HELP;
        if (messageWithoutPrefix.startsWith(Command.ADD.toString().toLowerCase())) return Command.ADD;
        if (messageWithoutPrefix.startsWith(Command.REMOVE.toString().toLowerCase())) return Command.REMOVE;
        if (messageWithoutPrefix.startsWith(Command.TEST.toString().toLowerCase())) return Command.TEST;
        if (messageWithoutPrefix.startsWith(Command.LIST.toString().toLowerCase())) return Command.LIST;
        if (messageWithoutPrefix.startsWith("setPrefix".toLowerCase())) return Command.SET_PREFIX;

        return Command.INVALID;

    }

    private void help(MessageChannel channel) {
        channel.sendMessage(
                new EmbedBuilder()
                        .setAuthor("FilterBot", null, "https://cdn.discordapp.com/avatars/593348457306259469/10d5a0b8b4bab79f7e878b03203d919c.png?size=128") // for the "iconUrl" switch "128" to "2048" if needed
                        .setTitle("__Filterbot commands__")
                        .setDescription("FilterBot filters specified words, images, and videos")
                        .addField("Help", "Lists commands and description", false)
                        .addField("Add", "Adds filter for current server", false)
                        .addField("Remove", "Removes filter for current server", false)
                        .addField("Test", "Provides list of possible filters given an Image or Video", false)
                        .addField("List", "Returns the list of filters added to the current server", false)
                        .addField("SetPrefix", "Sets the command prefix for the bot (applies server-wide)", false)
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
        } else if (!filter.contains(",")) {
            database.addFilters(message.getGuild().getId(), Collections.singletonList(filter));
            message.getChannel().sendMessage("Filter term: **\"" + filter + "\"** added").queue();
        } else {
            String output = "\"" + filter.substring(0, filter.indexOf(",")) + "\" ";
            filter = filter.substring(filter.indexOf(",") + 1);
            while (filter.contains(",")) {
                database.addFilters(message.getGuild().getId(), Collections.singletonList(filter.substring(0, filter.indexOf(","))));
                output += ", \"" + filter.substring(0, filter.indexOf(",")) + "\"";
                filter = filter.substring(filter.indexOf(",") + 1);
            }
            message.getChannel().sendMessage("Filter terms: **" + output + "** added").queue();
        }
    }

    private void remove(Message message) {
        boolean removedObj = false; // set to true if an object is removed from the database
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
            message.getChannel().sendMessage("Successfully removed filter **\"" + filter + "\"**").queue();
        } else
            message.getChannel().sendMessage("Unable to find filter **\"" + filter + "\"**").queue();
    }

    private void test(Message message) {
        // TODO
    }

    private void list(Message message) {
        List<String> filterList = database.getFilters(message.getGuild().getId());
        // no filters are present
        if (filterList.isEmpty()) {
            message.getChannel().sendMessage("No filters have been added for this server, use **f!add [term]** to add a filter.").queue();
            return;
        }

        StringBuilder filters = new StringBuilder();
        // loop: building a StringBuilder from the filterList
        for (String tag : filterList)
            filters.append(tag).append(", ");

        // removing the last "," in the StringBuilder
        filters.deleteCharAt(filters.length() - 1);
        // sends message with list of filters
        message.getChannel().sendMessage("Filters in " + message.getGuild().getName() + ": " + filters).queue();
    }
    private void setPrefix(Message message) {
        String cmdPrefix = database.getCommandPrefix(message.getGuild().getId());
        String messageWithoutPrefix = message.getContentStripped().toLowerCase().substring(cmdPrefix.length()).trim();
        String newPrefix = messageWithoutPrefix.substring("setPrefix".length()).trim();
        if (!newPrefix.isEmpty()) {
            database.setCommandPrefix(message.getGuild().getId(), newPrefix);
            message.getChannel().sendMessage("Prefix successfully set to " + newPrefix + "(applies server-wide).").queue();
        } else {
            message.getChannel().sendMessage("Prefix cannot be blank").queue();
        }
    }

    private enum Command {
        HELP, // Informational Help Command
        ADD, // Add new Filter
        REMOVE, // Remove existing Filter
        TEST, // Test image or video for possible filters
        LIST, // Lists filters for the given server
        SET_PREFIX, // Sets command prefix for the server
        INVALID // Invalid command
    }
}
