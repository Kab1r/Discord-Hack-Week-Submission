package hackweek.group.filterbot;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

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
                        .setFooter("Help Command", "") // TODO add footer image url
                        .build()
        ).queue();
    }

    private void add(Message message) {
      List<String> filter = new ArrayList<>();
      String messageWithoutPrefix = message.getContentStripped().toLowerCase().substring(commandPrefix.length()).trim();
      filter.add(messageWithoutPrefix);
      database.addFilters(message.getGuild().getId(), filter);
      message.getChannel().sendMessage("Filter term: \"" + messageWithoutPrefix + "\" added").queue();
    }

    private void remove(Message message) {
        // TODO
    }

    private void test(Message message) {
        // TODO
    }

    private enum Command {
        HELP, // Informational Help Command
        ADD, // Add new Filter
        REMOVE, // Remove existing Filter
        TEST, // Test image or video for possible filters
    }
}
