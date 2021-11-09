package org.metafacture.metafix.web;

import org.metafacture.metafix.FixStandaloneSetup;
import org.metafacture.runner.Flux;

import org.antlr.runtime.RecognitionException;
import org.eclipse.xtext.util.DisposableRegistry;
import org.eclipse.xtext.web.servlet.XtextServlet;
import org.eclipse.xtext.xbase.lib.InputOutput;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Deploy this class into a servlet container to enable DSL-specific services.
 */
@WebServlet(name = "XtextServices", urlPatterns = "/xtext-service/*")
public class FixServlet extends XtextServlet {

    private static final String COMMAND_FIX = "fix";

    private static final String PARAM_DATA = "data";
    private static final String PARAM_FIX = "fix";
    private static final String PARAM_FLUX = "flux";

    private DisposableRegistry disposableRegistry;

    public FixServlet() {
    }

    @Override
    public void init() throws ServletException {
        disposableRegistry = new FixWebSetup().createInjectorAndDoEMFRegistration().getInstance(DisposableRegistry.class);
    }

    @Override
    public void destroy() {
        if (disposableRegistry != null) {
            disposableRegistry.dispose();
            disposableRegistry = null;
        }

        super.destroy();
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        InputOutput.println("POST Request: " + request);

        if (!request.getPathInfo().endsWith("/run") || !process(request, response)) {
            super.doPost(request, response);
        }
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        InputOutput.println("GET Request: " + request);

        if (!process(request, response)) {
            super.doGet(request, response);
        }
    }

    private boolean process(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final Map<String, String[]> params = request.getParameterMap();

        if (!params.containsKey(PARAM_DATA) || !params.containsKey(PARAM_FLUX) || !params.containsKey(PARAM_FIX)) {
            return false;
        }

        final StringBuilder builder = new StringBuilder();

        final String inData = request.getParameter(PARAM_DATA);
        builder.append(inData == null || inData.isEmpty() ? "" :
                "\"" + absPathToTempFile(inData, ".txt") + "\"|open-file|");

        final String fixFile = absPathToTempFile(request.getParameter(PARAM_FIX), ".fix");
        final String outFile = absPathToTempFile("", ".txt");

        builder.append(request.getParameter(PARAM_FLUX).replaceAll("\\s?\\|\\s?", "|").replace(
                "|" + COMMAND_FIX + "|",
                "|org.metafacture.metafix.Metafix(\"" + fixFile + "\")|"));
        builder.append("|write(\"");
        builder.append(outFile);
        builder.append("\");");

        final String fullFlux = builder.toString();
        InputOutput.println("full flux: " + fullFlux);

        try {
            Flux.main(new String[] {absPathToTempFile(fullFlux, ".flux")});
        }
        catch (final RecognitionException e) {
            throw new RuntimeException(e);
        }

        Files.copy(Paths.get(outFile), response.getOutputStream());
        return true;
    }

    private String absPathToTempFile(final String content, final String suffix) throws IOException {
        return FixStandaloneSetup.absPathToTempFile(new StringReader(content), suffix);
    }

}
