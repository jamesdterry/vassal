package org.vassalengine.tools.vsav.model;

/**
 * Command marker for setup stack initialization.
 * Format: SETUP_STACK\t{content}
 *
 * This is primarily a marker command used by NewGameIndicator to track
 * whether a save file represents a new game or a continuing game.
 */
public class SetupStackCommand extends CommandData {
    private String content;

    public SetupStackCommand() {
        setType(CommandType.SETUP_STACK);
    }

    public SetupStackCommand(String rawCommand) {
        super(CommandType.SETUP_STACK, rawCommand);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
