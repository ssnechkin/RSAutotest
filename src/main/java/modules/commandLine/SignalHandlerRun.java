package modules.commandLine;

import sun.misc.Signal;
import sun.misc.SignalHandler;

public class SignalHandlerRun implements SignalHandler {
    private Process proc = null;

    public SignalHandlerRun(Process proc) {
        this.proc = proc;
    }

    @Override
    public void handle(Signal sig) {
        if (proc != null) {
            proc.destroy();
        }
    }
}
