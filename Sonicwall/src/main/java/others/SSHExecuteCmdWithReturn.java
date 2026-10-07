package others;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class SSHExecuteCmdWithReturn implements Nlp {
    @InputParams({ @InputParam(name = "SSH Client", type = "net.schmizz.sshj.SSHClient"),
            @InputParam(name = "Exec Cmd", type = "java.lang.String") })
    @ReturnType(name = "output", type = "java.lang.String")

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
        SSHClient sSHClient = (SSHClient) programElementsInput.get("SSH Client");
        String execCmd = (String) programElementsInput.get("Exec Cmd");
        String returnValue = null;

        NlpResponseModel nlpResponseModel = new NlpResponseModel();
        try {
            returnValue = executeCommand(sSHClient, execCmd);
            nlpResponseModel.setStatus(CommonConstants.pass);
            nlpResponseModel.setMessage("Successfully executed the ssh cmd");
        } catch (Exception e) {
            nlpResponseModel.setMessage("Failed to execute the ssh cmd" + e);
            nlpResponseModel.setStatus(CommonConstants.fail);
        }
        nlpResponseModel.getAttributes().put("output", returnValue);
        return nlpResponseModel;
    }

    public static String executeCommand(SSHClient ssh, String command) throws Exception {

        if (ssh == null || !ssh.isConnected()) {
            throw new IllegalStateException("SSH connection is not connected.");
        }

        Session session = null;
        StringBuilder output = new StringBuilder();

        try {
            session = ssh.startSession();

            System.out.println("Executing command: " + command);

            Session.Command cmd = session.exec(command);

            Thread stdoutThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(cmd.getInputStream()))) {

                    String line;

                    while ((line = reader.readLine()) != null) {
                        synchronized (output) {
                            output.append(line).append(System.lineSeparator());
                        }

                        System.out.println("[OUT] " + line);
                    }

                } catch (IOException e) {
                    synchronized (output) {
                        output.append("STDOUT ERROR: ").append(e.getMessage()).append(System.lineSeparator());
                    }
                }
            });

            Thread stderrThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(cmd.getErrorStream()))) {

                    String line;

                    while ((line = reader.readLine()) != null) {
                        synchronized (output) {
                            output.append(line).append(System.lineSeparator());
                        }

                        System.err.println("[ERR] " + line);
                    }

                } catch (IOException e) {
                    synchronized (output) {
                        output.append("STDERR ERROR: ").append(e.getMessage()).append(System.lineSeparator());
                    }
                }
            });

            stdoutThread.start();
            stderrThread.start();

            cmd.join();

            stdoutThread.join();
            stderrThread.join();

            int exitStatus = cmd.getExitStatus();

            System.out.println("Command exit status: " + exitStatus);

            return output.toString();

        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (Exception ignored) {
                }
            }
            disconnect(ssh);
        }
    }

    public static void disconnect(SSHClient ssh) {

        if (ssh != null) {
            try {
                if (ssh.isConnected()) {
                    ssh.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}