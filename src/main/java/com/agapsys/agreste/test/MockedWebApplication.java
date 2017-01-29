/*
 * Copyright 2016-2017 Agapsys Tecnologia Ltda-ME.
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

import com.agapsys.agreste.AgresteApplication;
import com.agapsys.web.toolkit.modules.LogModule;
import com.agapsys.web.toolkit.modules.LogModule.ConsoleLogStream;
import com.agapsys.web.toolkit.utils.FileUtils;
import com.agapsys.web.toolkit.utils.Settings;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.servlet.annotation.WebListener;

@WebListener
public class MockedWebApplication extends AgresteApplication {

    @Override
    public String getName() {
        return "test-app";
    }

    @Override
    public String getVersion() {
        return "0.1.0";
    }

    @Override
    protected File getParentDir() {
        try {
            return FileUtils.getRandomNonExistentFile(FileUtils.DEFAULT_TEMPORARY_FOLDER, 8, 1000);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected void afterAgresteStop() {
        try {
            super.afterAgresteStop();
            FileUtils.deleteFile(getDirectory());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected Settings getDefaults() {
        Settings superSettings = super.getDefaults();
        if (superSettings == null)
            superSettings = new Settings();

        superSettings.setProperty(KEY_ABUSE_CHECK_ENABLED, "" + false);
        return superSettings;
    }

    @Override
    protected void beforeAgresteStart() {
        super.beforeAgresteStart();

        LogModule logModule = getModule(LogModule.class);
        ConsoleLogStream consoleLogStream = new ConsoleLogStream();
        logModule.addStream(consoleLogStream);
    }

    public long getAbuseInterval() {
        return super.getAbuseInterval();
    }

    public int getAbuseCountLimit() {
        return super.getAbuseCountLimit();
    }

    public boolean isAbuseCheckEnabled() {
        return super.isAbuseCheckEnabled();
    }
}
