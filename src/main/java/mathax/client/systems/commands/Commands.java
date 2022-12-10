package mathax.client.systems.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mathax.client.systems.System;
import mathax.client.systems.Systems;
import mathax.client.systems.commands.commands.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.util.registry.DynamicRegistryManager;

import java.util.*;

import static mathax.client.MatHax.mc;

public class Commands extends System<Commands> {
    public static final CommandRegistryAccess REGISTRY_ACCESS = new CommandRegistryAccess(DynamicRegistryManager.BUILTIN.get());

    private final CommandDispatcher<CommandSource> DISPATCHER = new CommandDispatcher<>();
    private final CommandSource COMMAND_SOURCE = new ChatCommandSource(mc);
    private final List<Command> commands = new ArrayList<>();
    private final Map<Class<? extends Command>, Command> commandInstances = new HashMap<>();

    public Commands() {
        super("Commands", null);
    }

    public static Commands get() {
        return Systems.get(Commands.class);
    }

    @Override
    public void init() {
        add(new BaritoneCommand());
        add(new ClearChatCommand());
        add(new CommandsCommand());
        add(new FakePlayerCommand());
        add(new InputCommand());
        add(new NotebotCommand());
        add(new PingCommand());
        add(new ReloadCommand());
        add(new ResetCommand());
        add(new ToggleCommand());
        add(new SettingCommand());

        commands.sort(Comparator.comparing(Command::getCommand));
    }

    public void dispatch(String message) throws CommandSyntaxException {
        dispatch(message, new ChatCommandSource(mc));
    }

    public void dispatch(String message, CommandSource source) throws CommandSyntaxException {
        ParseResults<CommandSource> results = DISPATCHER.parse(message, source);
        DISPATCHER.execute(results);
    }

    public CommandDispatcher<CommandSource> getDispatcher() {
        return DISPATCHER;
    }

    public CommandSource getCommandSource() {
        return COMMAND_SOURCE;
    }

    private final static class ChatCommandSource extends ClientCommandSource {
        public ChatCommandSource(MinecraftClient client) {
            super(null, client);
        }
    }

    public void add(Command command) {
        commands.removeIf(command1 -> command1.getCommand().equals(command.getCommand()));
        commandInstances.values().removeIf(command1 -> command1.getCommand().equals(command.getCommand()));

        command.registerTo(DISPATCHER);
        commands.add(command);
        commandInstances.put(command.getClass(), command);
    }

    public int getCount() {
        return commands.size();
    }

    public List<Command> getAll() {
        return commands;
    }

    public <T extends Command> T get(Class<T> klass) {
        return (T) commandInstances.get(klass);
    }
}
