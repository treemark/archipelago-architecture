package com.archipelago.plugins.git

import com.archipelago.plugins.shell.ShellUtils
import org.apache.commons.lang3.StringUtils
import org.gradle.api.Project

class GitSupport {
    static String getWorkingBranch(Project project) {
        // Triple double-quotes for the breaklines
//        def workingBranch = ("""git --git-dir="""+project.projectDir+"""/.git
//                               --work-tree="""+project.projectDir+"""/..
//                               rev-parse --abbrev-ref HEAD""").execute().text.trim()
        String[] gitCommand = ["git", "rev-parse", "--abbrev-ref", "HEAD"]
        return ShellUtils.executeCommand(gitCommand , project.projectDir);
    }

    static String getShortSha(Project project) {
        String[] gitCommand = ["git", "rev-parse", "--short=8", "HEAD"]
        return ShellUtils.executeCommand(gitCommand , project.projectDir);
    }


    static String getRepoName(Project project) {
        // Triple double-quotes for the breaklines
        String[] gitCommand = ["git", "rev-parse", "--show-toplevel"]
        String repo= ShellUtils.executeCommand(gitCommand , project.projectDir)
//        def repoName = ("""git rev-parse --show-toplevel""").execute().text.trim()
        return repo.substring(repo.lastIndexOf("/")+1)
    }

    static String getTagName(Project project) {
        // Triple double-quotes for the breaklines
        def repoName = ("""git describe --tags --abbrev=0""").execute().text.trim()
        return repoName
    }

    static String getGitRemoteRepoName() {
//        def repoName = ("git config --get remote.origin.url | rev | cut -d '/' -f 1 | rev | cut -f1 -d'.'").execute().text.trim()
        String repoUrl = ("git config --get remote.origin.url").execute().text.trim()
        return StringUtils.substringAfterLast(repoUrl, "/").split("[.]")[0]

    }
}
