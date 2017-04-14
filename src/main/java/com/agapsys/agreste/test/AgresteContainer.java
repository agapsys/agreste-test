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

import com.agapsys.agreste.JpaTransactionFilter;
import com.agapsys.rcf.Controller;
import com.agapsys.rcf.ControllerRegistrationListener;
import com.agapsys.rcf.WebController;
import com.agapsys.web.toolkit.AbstractWebApplication;
import com.agapsys.web.toolkit.MockedWebApplication;
import com.agapsys.web.toolkit.WebApplicationContainer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

/**
 * Servlet container builder for AGRESTE applications
 */
public class AgresteContainer<AC extends AgresteContainer<AC>> extends WebApplicationContainer<AC> {
    // STATIC SCOPE ============================================================

    public static AgresteContainer<?> newInstance(Class<? extends HttpServlet>...servletsOrControllers) {
        return newInstance(MockedWebApplication.class, servletsOrControllers);
    }

    public static AgresteContainer<?> newInstance(Class<? extends AbstractWebApplication> webApp, Class<? extends HttpServlet>...servletsOrControllers) {
        AgresteContainer container = new AgresteContainer<>(webApp);

        for (Class<? extends HttpServlet> servlet : servletsOrControllers) {

            if (Controller.class.isAssignableFrom(servlet)) {
                container.registerController(servlet);

                if (servlet.getAnnotation(WebServlet.class) != null) {
                    container.registerServlet(servlet);
                }

            } else {
                container.registerServlet(servlet);
            }
        }

        return container;
    }

    private static final String EMBEDDED_CONTROLER_INFO = "/META-INF/rcf.info";
    private static final String EMBEDDED_CONTROLER_INFO_ENCODING = "utf-8";
    // =========================================================================

    // INSTANCE SCOPE ==========================================================
    private void __registerScannedControllers() {
        InputStream is = null;
        try {

            is = AgresteContainer.class.getResourceAsStream(EMBEDDED_CONTROLER_INFO);

            if (is == null)
                return;

            BufferedReader in = new BufferedReader(new InputStreamReader(is, EMBEDDED_CONTROLER_INFO_ENCODING));

            String readLine;

            while ((readLine = in.readLine()) != null) {
                readLine = readLine.trim();

                if (readLine.isEmpty() || readLine.startsWith("#"))
                    continue;

                registerController((Class<? extends Controller>) Class.forName(readLine));
            }

            in.close();

        } catch (IOException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        } finally {
            if (is != null)
                try {
                    is.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void __init() {
        super.registerFilter(JpaTransactionFilter.class, "/*");

        __registerScannedControllers();
    }

    public AgresteContainer(Class<? extends AbstractWebApplication> webApp) {
        super(webApp);
        __init();
    }

    public AgresteContainer() {
        this(MockedWebApplication.class);
    }

    public AC registerController(Class<? extends Controller> controllerClass, String name) {
        return (AC) super.registerServlet(controllerClass, String.format("/%s/*", name));
    }

    public AC registerController(Class<? extends Controller> controllerClass) {
        WebController webController = controllerClass.getAnnotation(WebController.class);

        if (webController == null)
            throw new IllegalArgumentException(String.format("Missing annotation '%s' for '%s'", WebController.class.getName(), controllerClass.getName()));

        String name = webController.value().trim();

        if (name.isEmpty())
            name = ControllerRegistrationListener.getDefaultMapping(controllerClass);

        return registerController(controllerClass, name);
    }
    // =========================================================================
}
