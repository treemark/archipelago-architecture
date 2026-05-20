package com.archipelago.plugins.publish

import com.archipelago.plugins.git.GitSupport;
import org.gradle.api.GradleException
import org.gradle.api.Project

class PublishUtils {
    public static String determineArtifactName(Project p) {
        def artifactName = p.name;
        while (p.getParent() != null) {
            p = p.getParent();
            artifactName = p.getName() + "-" + artifactName;
        }
        return artifactName;
    }

    public static String determineLocalDockerImageName(Project project) {
        // Match docker-compose.yml naming pattern: local/shipwell-{project-name}:local-dev
        def imageName = "local/shipwell-${project.name}:local-dev"
        return imageName;
    }

    public static String determineEcrDockerImageName(Project project) {
        def imageName = "${project.rootProject.name}/${project.name}"
        return imageName;
    }

    public static String determineEcrRepoName(Project p) {
        def artifactName = p.name;
        while (p.getParent() != null) {
            p = p.getParent();
            artifactName = p.getName() + "/" + artifactName;
        }
        return artifactName;
    }

    public static String determineVersion(Project project) {
        return determineVersionNumber(project, false);
    }

    public static String determineVersionNumber(Project project, boolean addTs) {

        if (System.getProperty("publishVersionOverride") != null) {
            project.version = System.getProperty("publishVersionOverride"); ;
            return project.version
        }

        if (project.getRootProject().hasProperty("publishVersionOverride")) {
            project.version = project.getRootProject().getProperty("publishVersionOverride");
            return project.version;
        }

        if (!project.getRootProject().hasProperty("publishTimestamp")) {
            project.getRootProject().ext.set("publishTimestamp", System.currentTimeMillis());
        }
        def now = project.getRootProject().property("publishTimestamp");

        def branchName = "default";
        try {
            branchName = GitSupport.getWorkingBranch(project).replaceAll("/", "-");
        } catch (Exception e) {
            project.logger.lifecycle("Could not get the branch name from the repo, using default.");
        }

//        def artifactName = "@dcli-com/" + getArtifactName(project);
        // Retrieve and increment the current version
        def currentVersion = project.rootProject.findProperty('version') ?: '0.0.1';
        def sanitizedVersion = currentVersion.replaceAll("-SNAPSHOT", ""); // Remove -SNAPSHOT if present
        if (sanitizedVersion.equals("unspecified"))
            sanitizedVersion = "1.0.0"
        def versionParts = sanitizedVersion.split("\\.");

        if (versionParts.length < 3) {
            throw new GradleException("Version format is invalid. Expected format: X.Y.Z or X.Y.Z-feature-SN. " + sanitizedVersion );
        }

        def major = versionParts[0].toInteger();
        def minor = versionParts[1].toInteger();

        // Extract patch even if additional qualifiers are present (e.g., feature, SN)
        def patchQualifier = versionParts[2].replaceAll("^(\\d+)", ""); // Keep any qualifiers (e.g., feature-SN)
        def patch = versionParts[2].replaceAll("[^\\d]", "").toInteger();

        def releaseFlag = project.findProperty('githubref')?.toString()?.startsWith("refs/tags/v");

        String newVersion = releaseFlag
                ? project.findProperty('githubref').toString().substring(11)
                : "${major}.${minor}.${patch}${patchQualifier}-${branchName}-SNAPSHOT" + ((addTs) ? ".${now}" : "");
        return newVersion
    }


    static String  sanitizeBranch(String s) {
        return s.replaceAll("/","-");
    }
}
