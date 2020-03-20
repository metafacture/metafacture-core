package org.metafacture.fix.web;

import org.metafacture.runner.Flux;

import com.google.common.base.Charsets;
import com.google.inject.Injector;
import org.antlr.runtime.RecognitionException;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.util.DisposableRegistry;
import org.eclipse.xtext.web.servlet.XtextServlet;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.InputOutput;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
    public void init() {
        try {
            super.init();
            final Injector injector = new FixWebSetup().createInjectorAndDoEMFRegistration();
            this.disposableRegistry = injector.<DisposableRegistry>getInstance(DisposableRegistry.class);
        }
        catch (final ServletException e) {
            throw Exceptions.sneakyThrow(e);
        }
    }

    @Override
    public void destroy() {
        if (this.disposableRegistry != null) {
            this.disposableRegistry.dispose();
            this.disposableRegistry = null;
        }
        super.destroy();
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) {
        try {
            InputOutput.<String>println("POST Request: " + request);
            if (((request.getPathInfo().endsWith("/run") && request.getParameterMap().containsKey(PARAM_DATA)) &&
                        request.getParameterMap().containsKey(PARAM_FLUX)) && request.getParameterMap().containsKey(PARAM_FIX)) {
                this.process(request, response);
            }
            else {
                super.doPost(request, response);
            }
        }
        catch (final IOException | ServletException e) {
            throw Exceptions.sneakyThrow(e);
        }
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) {
        try {
            InputOutput.<String>println("GET Request: " + request);
            if ((request.getParameterMap().containsKey(PARAM_DATA) && request.getParameterMap().containsKey(PARAM_FLUX)) &&
                    request.getParameterMap().containsKey(PARAM_FIX)) {
                this.process(request, response);
            }
            else {
                super.doGet(request, response);
            }
        }
        catch (final IOException | ServletException e) {
            throw Exceptions.sneakyThrow(e);
        }
    }

    public void process(final HttpServletRequest request, final HttpServletResponse response) {
        try {
            final String inData = request.getParameter(PARAM_DATA);
            String prefix = "";
            if ((inData != null) && (!inData.isEmpty())) {
                final String inFile = this.absPathToTempFile(inData, ".txt");
                final StringConcatenation builder = new StringConcatenation();
                builder.append("\"");
                builder.append(inFile);
                builder.append("\"|open-file|");
                prefix = builder.toString();
            }
            final String fixFile = this.absPathToTempFile(request.getParameter(PARAM_FIX), ".fix");
            final String outFile = this.absPathToTempFile("", ".txt");
            final String passedFlux = request.getParameter(PARAM_FLUX).replace(COMMAND_FIX,
                    "org.metafacture.metamorph.Metafix(fixFile=\"" + fixFile + "\")");
            final StringConcatenation builder1 = new StringConcatenation();
            builder1.append(prefix);
            builder1.append(passedFlux);
            builder1.append("|write(\"");
            builder1.append(outFile);
            builder1.append("\");");
            final String fullFlux = builder1.toString();
            InputOutput.<String>println("full flux: " + fullFlux);
            final String absPathToTempFile = this.absPathToTempFile(fullFlux, ".flux");
            Flux.main(new String[] {absPathToTempFile});
            final List<String> result = Files.readAllLines(Paths.get(outFile));
            response.getOutputStream().write(result.stream().collect(Collectors.joining("\n")).getBytes(Charsets.UTF_8));
        }
        catch (final IOException | RecognitionException e) {
            throw Exceptions.sneakyThrow(e);
        }
    }

    protected String absPathToTempFile(final String content, final String suffix) {
        try {
            final File file = File.createTempFile("fixweb", suffix);
            Files.write(file.toPath(), Collections.<CharSequence>unmodifiableList(CollectionLiterals.<CharSequence>newArrayList(content)), StandardCharsets.UTF_8);
            return file.getAbsolutePath();
        }
        catch (final IOException e) {
            throw Exceptions.sneakyThrow(e);
        }
    }

}
