package de.maxkroner.implementation;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import de.maxkroner.database.JokeDatabase;
import de.maxkroner.ui.JokeBotMenue;
import de.maxkroner.ui.UserInput;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class JokeBot extends Bot {
	private static final String token = "MjUwMzQwMjI5NzA4MjUxMTM2.DL9RIw.0HcxTLw-K_VCEf2yiipaYYX-428";
	private List<String> jokeCategories;
	private JokeDatabase jokeDatabase;

	public JokeBot(Scanner scanner, UserInput userInput) {
		super(token, new JokeBotMenue(scanner, userInput, new JokeDatabase()));
		JokeBotMenue jokeBotMenue= (JokeBotMenue) botMenue;
		this.jokeDatabase = jokeBotMenue.getJokeDatabase();
	}

	private void tellJoke(String category, IChannel channel) {
		if (category.equals("random")){
			String joke = jokeDatabase.getRandomJoke(Optional.empty());
			sendMessage(joke, channel, true);
		}
		else if (jokeCategories.contains(category)) {

			String joke = jokeDatabase.getRandomJoke(Optional.of(category));
			sendMessage(joke, channel, true);

		} else {
			sendMessage("I dont know the category '" + category + "' yet.", channel, false);
		}
	}

	private void tellCategories(IChannel channel) {
		String categories = "If you use `!joke` `[category]` i will tell you a joke from the specified category.\n"
				+ "These are the categories I know so far:\n\n";

		for (String category : jokeCategories) {
			categories = categories + "`" + category + "`" + "\n";
		}

		categories = categories + "Use `random` for a joke from any category.";
		
		sendMessage(categories, channel, false);
	}

	@EventSubscriber
	public void onReady(ReadyEvent event) {
		super.onReady(event);
		updateJokeCategories();
	}

	public void updateJokeCategories() {
		jokeCategories = jokeDatabase.getJokeCategories();

	}

	@EventSubscriber
	public void onMessageReceivedEvent(MessageReceivedEvent event) throws MissingPermissionsException, DiscordException,
			RateLimitException, ClassNotFoundException, SQLException {
		String message = event.getMessage().getContent();
		IChannel channel = event.getMessage().getChannel();
		if (message.startsWith("!change nick")) {
			User user = (User) client.getOurUser();
			channel.getGuild().setUserNickname(user, message.substring(13));
		} else if ((message.startsWith("!joke ") && (message.length() > 6))) {
			tellJoke(message.substring(message.indexOf(" ") + 1), channel);
		} else if (message.equals("!joke")) {
			tellCategories(channel);
		}
	}
	
	public void disconnect(){
		super.disconnect();
		jokeDatabase.close();
	}
}
