package com.goldbox.app.terminal;

import android.app.Service;

import androidx.annotation.NonNull;

import com.goldbox.app.GoldBOXService;
import com.goldbox.shared.goldbox.shell.command.runner.terminal.GoldBOXSession;
import com.goldbox.shared.goldbox.terminal.GoldBOXTerminalSessionClientBase;
import com.goldbox.terminal.TerminalSession;
import com.goldbox.terminal.TerminalSessionClient;

/** The {@link TerminalSessionClient} implementation that may require a {@link Service} for its interface methods. */
public class GoldBOXTerminalSessionServiceClient extends GoldBOXTerminalSessionClientBase {

    private static final String LOG_TAG = "GoldBOXTerminalSessionServiceClient";

    private final GoldBOXService mService;

    public GoldBOXTerminalSessionServiceClient(GoldBOXService service) {
        this.mService = service;
    }

    @Override
    public void setTerminalShellPid(@NonNull TerminalSession terminalSession, int pid) {
        GoldBOXSession goldboxSession = mService.getGoldBOXSessionForTerminalSession(terminalSession);
        if (goldboxSession != null)
            goldboxSession.getExecutionCommand().mPid = pid;
    }

}
