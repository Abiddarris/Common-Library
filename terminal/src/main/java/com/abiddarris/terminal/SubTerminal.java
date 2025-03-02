/***********************************************************************************
 * Copyright 2024-2025 Abiddarris
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***********************************************************************************/
package com.abiddarris.terminal;

import java.io.File;

public class SubTerminal extends Terminal {

    private File workingDirectory;
    private Terminal terminal;

    public SubTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    @Override
    public Process execute(String command) {
        try {
            return super.execute(command);
        } catch (CommandNotFoundException ignored) {
            return terminal.execute(command);
        }
    }

    @Override
    public void setParser(Parser parser) {
        super.setParser(parser);

        terminal.setParser(parser);
    }

    @Override
    public void setWorkingDirectory(File workingDirectory) {
        super.setWorkingDirectory(workingDirectory);

        this.workingDirectory = workingDirectory;
    }

    @Override
    public File getWorkingDirectory() {
        return workingDirectory == null ? terminal.getWorkingDirectory() : workingDirectory;
    }
}
