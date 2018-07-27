package modules.commandLine;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.util.concurrent.ExecutorService;

public class SignalExecutorServiceHandlerRun implements SignalHandler {
    private ExecutorService executorService, oneExecutorService, reportExecutorService;

    public SignalExecutorServiceHandlerRun(ExecutorService executorService, ExecutorService oneExecutorService, ExecutorService reportExecutorService) {
        this.executorService = executorService;
        this.oneExecutorService = oneExecutorService;
        this.reportExecutorService = reportExecutorService;
    }

    @Override
    public void handle(Signal sig) {
        try {
            if (executorService != null) executorService.shutdownNow();
        } catch (Throwable t) {
        }
        try {
            if (oneExecutorService != null) oneExecutorService.shutdownNow();
        } catch (Throwable t) {
        }
        try {
            if (reportExecutorService != null) reportExecutorService.shutdownNow();
        } catch (Throwable t) {
        }
    }
}
