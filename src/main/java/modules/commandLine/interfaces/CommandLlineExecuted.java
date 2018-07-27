package modules.commandLine.interfaces;

import modules.configuration.interfaces.ProgramSettings;
import modules.logger.interfaces.RSLogger;

public interface CommandLlineExecuted {
    void execute(ProgramSettings programSettings, RSLogger rsLogger);
}
