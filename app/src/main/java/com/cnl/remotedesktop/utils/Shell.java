package com.cnl.remotedesktop.utils;

import java.io.DataOutputStream;
import java.io.IOException;

public class Shell extends AbstractShell {

    private Process process;
    private DataOutputStream dos;

    public Shell(){
        super();
    }

    public Shell(boolean root){
        super(root);
    }

    @Override
    protected void init(String initialCommand) {
        try {
            process = Runtime.getRuntime().exec(initialCommand);
            dos = new DataOutputStream(process.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exec(String command) {
        try {
            dos.write(command.getBytes());
            dos.write(COMMAND_LINE_END.getBytes());
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exit() {
        try {
            dos.close();
            process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
