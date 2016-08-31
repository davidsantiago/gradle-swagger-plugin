package com.github.swagger.docgen.gradleplugin

import com.github.kongchen.swagger.docgen.plugin.ApiDocumentMojo
import com.github.kongchen.swagger.docgen.plugin.ApiSource
import io.swagger.models.Info

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * GradleSwaggerTask 
 */
class GenerateSwaggerDocsTask extends DefaultTask {
    public static final String TASK_NAME = 'swagger'

    String description = "Generates swagger documentation"

    @TaskAction
    def generateSwaggerDocuments() {
        SwaggerPluginExtension swagger = project.swagger
        Iterable dependencies = project.configurations.runtime.resolve()
        File classesDir = project.sourceSets.main.output.classesDir

        project.logger.debug "Swagger outputPath=${swagger.outputPath}, outputTemplate=${swagger.outputTemplate}"

        ApiSource apiSource = new ApiSource();
        String locations = String.join(";", swagger.endPoints)
        apiSource.setLocations(locations)
        apiSource.setInfo(new Info());
        apiSource.getInfo().setVersion(swagger.apiVersion)
        apiSource.setBasePath(swagger.basePath)
        apiSource.setSwaggerDirectory(swagger.swaggerDirectory)
        apiSource.getInfo().setTitle(swagger.title)
        apiSource.setHost(swagger.host)
        apiSource.getInfo().setTermsOfService(swagger.termsOfService)
        apiSource.getInfo().setDescription(swagger.description)
        apiSource.setSchemes(swagger.schemes)
        apiSource.setSwaggerDirectoryEncoding(swagger.swaggerDirectoryEncoding)

        project.logger.debug "locations=${locations} ApiSource=${apiSource}"

        ClassLoader classLoader = prepareClassLoader(dependencies, classesDir)

        ApiDocumentMojo documentMojo = new ApiDocumentMojo();
        List<ApiSource> apiSources = new ArrayList<ApiSource>();
        apiSources.add(apiSource);
        documentMojo.execute(logger, apiSources, classLoader)
    }

    URLClassLoader prepareClassLoader(Iterable<File> dependencies, File dir) {
        List<URL> urls = dependencies.collect { it.toURI().toURL() }
        urls.add(dir.toURI().toURL())

        logger.debug "Preparing classloader with urls: {}", urls

        return new URLClassLoader(urls as URL[], this.getClass().getClassLoader())
    }
}
