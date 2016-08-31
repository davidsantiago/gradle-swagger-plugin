package com.github.kongchen.swagger.docgen.plugin;

import java.io.File;
import java.util.List;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.GenerateException;
import org.slf4j.Logger;

/**
 * Created with IntelliJ IDEA. User: kongchen Date: 3/7/13
 */
public class ApiDocumentMojo {

    public void execute(Logger logger, List<ApiSource> apiSources, ClassLoader classLoader) throws Exception {
        if (apiSources == null) {
            throw new Exception("You must configure at least one apiSources element");
        }
        if (useSwaggerSpec11()) {
            throw new Exception(
                    "You may use an old version of swagger which is not supported by swagger-maven-plugin 2.0+\n"
                            + "swagger-maven-plugin 2.0+ only supports swagger-core 1.3.x");
        }

        if (useSwaggerSpec13()) {
            throw new Exception(
                    "You may use an old version of swagger which is not supported by swagger-maven-plugin 3.0+\n"
                            + "swagger-maven-plugin 3.0+ only supports swagger spec 2.0");
        }

        // try {
        logger.debug(apiSources.toString());
        for (ApiSource apiSource : apiSources) {

            validateConfiguration(apiSource);

            AbstractDocumentSource documentSource;

            if (apiSource.isSpringmvc()) {
                documentSource = new SpringMavenDocumentSource(apiSource, logger);
            } else {
                documentSource = new MavenDocumentSource(apiSource, logger);
            }

            documentSource.loadTypesToSkip();
            documentSource.loadModelModifier();
            documentSource.loadDocuments(classLoader);
            if (apiSource.getOutputPath() != null) {
                File outputDirectory = new File(apiSource.getOutputPath()).getParentFile();
                if (outputDirectory != null && !outputDirectory.exists()) {
                    if (!outputDirectory.mkdirs()) {
                        throw new Exception("Create directory[" + apiSource.getOutputPath() + "] for output failed.");
                    }
                }
            }
            if (apiSource.getTemplatePath() != null) {
                documentSource.toDocuments();
            }
            documentSource.toSwaggerDocuments(apiSource.getSwaggerUIDocBasePath() == null ? apiSource.getBasePath()
                    : apiSource.getSwaggerUIDocBasePath(), apiSource.getOutputFormats());

            /*
             * if ( apiSource.isAttachSwaggerArtifact() && apiSource.getSwaggerDirectory() != null && this.project !=
             * null) { String classifier = new File(apiSource.getSwaggerDirectory()).getName(); File swaggerFile = new
             * File( apiSource.getSwaggerDirectory(), "swagger.json"); this.projectHelper.attachArtifact(project,
             * "json", classifier, swaggerFile); }
             */
        }

        /*
         * } catch (GenerateException e) { throw new MojoFailureException(e.getMessage(), e); } catch (Exception e) {
         * throw new MojoExecutionException(e.getMessage(), e); }
         */
    }

    /**
     * validate configuration according to swagger spec and plugin requirement
     *
     * @param apiSource
     * @throws GenerateException
     */
    private void validateConfiguration(ApiSource apiSource) throws GenerateException {
        if (apiSource == null) {
            throw new GenerateException("You do not configure any apiSource!");
        } else if (apiSource.getInfo() == null) {
            throw new GenerateException("`<info>` is required by Swagger Spec.");
        }
        if (apiSource.getInfo().getTitle() == null) {
            throw new GenerateException("`<info><title>` is required by Swagger Spec.");
        }

        if (apiSource.getInfo().getVersion() == null) {
            throw new GenerateException("`<info><version>` is required by Swagger Spec.");
        }

        if (apiSource.getInfo().getLicense() != null && apiSource.getInfo().getLicense().getName() == null) {
            throw new GenerateException("`<info><license><name>` is required by Swagger Spec.");
        }

        if (apiSource.getLocations() == null) {
            throw new GenerateException("<locations> is required by this plugin.");
        }

    }

    private boolean useSwaggerSpec11() {
        try {
            Class<?> tryClass = Class.forName("com.wordnik.swagger.annotations.ApiErrors");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private boolean useSwaggerSpec13() {
        try {
            Class<?> tryClass = Class.forName("com.wordnik.swagger.model.ApiListing");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
