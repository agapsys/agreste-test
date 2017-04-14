/*
 * Copyright 2016 Agapsys Tecnologia Ltda-ME.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agapsys.agreste.test;

import com.agapsys.utils.console.printer.ConsoleColor;
import com.agapsys.utils.console.printer.ConsolePrinter;
import com.agapsys.web.toolkit.LogType;
import com.agapsys.web.toolkit.services.LogService;
import com.agapsys.web.toolkit.utils.DateUtils;
import java.util.Date;

public class ColoredConsoleLogger extends LogService.ConsoleLogger {
    private final boolean useLightColors;

    public ColoredConsoleLogger(boolean useLightColors) {
        this.useLightColors = useLightColors;
    }

    public ColoredConsoleLogger() {
        this(false);
    }

    private ConsoleColor __getColor(LogType logType) {
        switch (logType) {
            case INFO:
                return useLightColors ? ConsoleColor.LIGHT_GREEN : ConsoleColor.GREEN;

            case WARNING:
                return useLightColors ? ConsoleColor.LIGHT_YELLOW : ConsoleColor.YELLOW;

            case ERROR:
                return useLightColors ? ConsoleColor.LIGHT_RED : ConsoleColor.RED;

            default:
                throw new UnsupportedOperationException("Unsupported log type: " + logType);
        }
    }

    @Override
    protected String getMessage(Date timestamp, LogType logType, String message) {
        return String.format("%s [%s] %s", DateUtils.getIso8601Date(), ConsolePrinter.toString(__getColor(logType), logType.name()), message);
    }
}
