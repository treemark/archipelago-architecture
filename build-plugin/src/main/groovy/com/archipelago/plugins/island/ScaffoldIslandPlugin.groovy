package com.archipelago.plugins.island

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.variant.VariantComputer
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Registers the scaffoldIsland task, which generates the full directory structure
 * and pnpm workspace setup for a new Archipelago island and its submodules.
 *
 * Usage:
 *   ./gradlew scaffoldIsland -PislandName=<name> -PsubModules=<comma-separated list>
 *
 * SubModule types recognized:
 *   spring-service   → Spring Boot service/BFF submodule (archipelago.spring-lib)
 *   spring-lib       → Shared Java library submodule (archipelago.spring-lib, no web layer)
 *   react-lib        → Shared React library (archipelago.react-lib, pnpm init)
 *   react-ui         → Module Federation remote app: pnpm create vite (react-ts) +
 *                       @module-federation/vite + @archipelago/core-react-lib
 *   pulumi           → Infrastructure submodule (archipelago.pulumi)
 *
 * If -PsubModules is omitted, all module types are scaffolded.
 *
 * Example (full island):
 *   ./gradlew scaffoldIsland -PislandName=inventory -PsubModules=spring-service,react-lib,react-ui,pulumi
 *
 * Example (all modules, default):
 *   ./gradlew scaffoldIsland -PislandName=inventory
 */
class ScaffoldIslandPlugin implements Plugin<Project> {

    static final String TASK_NAME = 'scaffoldIsland'
    static final String GROUP     = 'archipelago'

    static final String DEFAULT_NODE_VERSION = '22.13.0'
    static final String DEFAULT_PNPM_VERSION = '9.5.0'

    static final List<String> KNOWN_MODULE_TYPES = [
        'spring-service', 'spring-lib', 'react-lib', 'react-ui', 'pulumi'
    ]

    void apply(Project project) {
        // Only register on the root project
        if (project != project.rootProject) return

        // Apply and configure com.github.node-gradle.node at the root so the
        // scaffold task can reuse the plugin's Node/pnpm provisioning rather
        // than relying on a globally-installed pnpm. This mirrors the
        // configuration used by ReactLibPlugin / ReactContainerPlugin so all
        // archipelago modules share the same Node/pnpm versions.
        project.plugins.apply('com.github.node-gradle.node')

        def nodeVersion = project.findProperty('node.version') ?: DEFAULT_NODE_VERSION
        def pnpmVersion = project.findProperty('pnpm.version') ?: DEFAULT_PNPM_VERSION

        project.node {
            version       = nodeVersion
            pnpmVersion   = pnpmVersion
            download      = true
            distBaseUrl   = 'https://nodejs.org/dist'
        }

        project.tasks.register(TASK_NAME) { t ->
            t.group       = GROUP
            t.description = 'Scaffold a new Archipelago island with selected submodules.'

            // Ensure pnpm (and the Node distribution it sits on) is provisioned
            // before scaffold runs anything that shells out to pnpm.
            t.dependsOn 'pnpmSetup'

            t.doLast {
                def islandName  = resolveIslandName(project)
                def subModTypes = resolveSubModules(project)
                scaffold(project, islandName, subModTypes)
            }
        }
    }

    /**
     * Resolves the absolute path to the pnpm executable provisioned by
     * com.github.node-gradle.node. Returns null if the path cannot be computed
     * (caller should fall back to PATH lookup).
     */
    private String resolvePnpmExec(Project project) {
        try {
            def nodeExt = NodeExtension.get(project)
            def vc = new VariantComputer()
            
            // Step 1: Get Node directory (Provider<File>)
            def nodeDirProvider = vc.computeNodeDir(nodeExt)
            def nodeDir = nodeDirProvider.get().asFile
            
            // Step 2: Get pnpm directory from NodeExtension (Provider<File>) - then use IT
            // Note: computePnpmBinDir takes a Provider, not a resolved File
            def pnpmDirProvider = vc.computePnpmDir(nodeExt)
            def pnpmBinDirProvider = vc.computePnpmBinDir(pnpmDirProvider)
            def pnpmBinDir = pnpmBinDirProvider.get().asFile
            
            // Step 3: Get the pnpm executable name (Provider<String>), passing pnpmDirProvider
            def pnpmExecProvider = vc.computePnpmExec(nodeExt, pnpmDirProvider)
            def pnpmExec = pnpmExecProvider.get()
            
            // If the exec is not already absolute, combine with bin directory
            def execFile = new File(pnpmExec)
            if (!execFile.isAbsolute()) {
                execFile = new File(pnpmBinDir, pnpmExec)
            }
            
            // FIX: Ensure we're using the bin directory explicitly (workaround for VariantComputer bug)
            // The bug resolved pnpm-latest/pnpm instead of pnpm-latest/bin/pnpm
            if (!execFile.absolutePath.contains('/bin/') && !execFile.absolutePath.endsWith('/bin')) {
                execFile = new File(pnpmBinDir, pnpmExec)
            }
            
            project.logger.debug("scaffoldIsland: resolved pnpm at ${execFile.absolutePath} (exists: ${execFile.exists()})")
            return execFile.absolutePath
        } catch (Throwable t) {
            project.logger.warn(
                "scaffoldIsland: could not resolve provisioned pnpm path (${t.class.simpleName}: ${t.message}); " +
                "falling back to 'pnpm' on PATH."
            )
            return 'pnpm'
        }
    }
    
    /**
     * Resolves whether to overwrite an existing island directory.
     */
    private boolean resolveOverwrite(Project project) {
        return project.findProperty('overwrite')?.toString()?.trim()?.toLowerCase() == 'true'
    }

    // -------------------------------------------------------------------------
    // Input resolution
    // -------------------------------------------------------------------------

    private String resolveIslandName(Project project) {
        def name = project.findProperty('islandName')?.toString()?.trim()
        if (!name) {
            printUsage(project)
            throw new IllegalArgumentException(
                "Missing -PislandName. See usage above."
            )
        }
        if (!(name ==~ /^[a-z][a-z0-9-]*$/)) {
            throw new IllegalArgumentException(
                "islandName '${name}' must be lowercase alphanumeric with hyphens (e.g. 'my-island')."
            )
        }
        return name
    }

    private List<String> resolveSubModules(Project project) {
        def raw = project.findProperty('subModules')?.toString()?.trim()
        if (!raw) {
            project.logger.lifecycle("  (no -PsubModules specified — scaffolding all module types)")
            return new ArrayList<>(KNOWN_MODULE_TYPES)
        }
        def types = raw.split(',')*.trim().findAll { it }
        def unknown = types - KNOWN_MODULE_TYPES
        if (unknown) {
            throw new IllegalArgumentException(
                "Unknown subModule type(s): ${unknown}. Known types: ${KNOWN_MODULE_TYPES}."
            )
        }
        return types
    }

    // -------------------------------------------------------------------------
    // Scaffolding orchestration
    // -------------------------------------------------------------------------

    private void scaffold(Project project, String islandName, List<String> subModTypes) {
        def rootDir   = project.rootDir
        def islandDir = new File(rootDir, islandName)
        def overwrite = resolveOverwrite(project)

        // Check if island already exists
        if (islandDir.exists()) {
            if (!overwrite) {
                throw new IllegalArgumentException(
                    "Island '${islandName}' already exists. Use -Poverwrite=true to replace it."
                )
            }
            project.logger.lifecycle("  ⚠  Overwriting existing island: ${islandName}")
            islandDir.deleteDir()
        }

        // Resolve once; reused by any submodule that needs pnpm.
        def pnpmExec = resolvePnpmExec(project)

        project.logger.lifecycle("╔══════════════════════════════════════════════════════")
        project.logger.lifecycle("║  Scaffolding island: ${islandName}")
        project.logger.lifecycle("║  SubModules: ${subModTypes.join(', ')}")
        project.logger.lifecycle("║  pnpm: ${pnpmExec}")
        project.logger.lifecycle("╚══════════════════════════════════════════════════════")

        // 1. Island root directory + build.gradle
        islandDir.mkdirs()
        writeIslandBuildGradle(islandDir, islandName)

        // 2. Each submodule
        subModTypes.each { type ->
            def modName = "${islandName}-${type}"
            def modDir  = new File(islandDir, modName)
            modDir.mkdirs()

            switch (type) {
                case 'spring-service':
                    scaffoldSpringService(project, modDir, islandName, modName)
                    break
                case 'spring-lib':
                    scaffoldSpringLib(modDir, islandName, modName)
                    break
                case 'react-lib':
                    scaffoldReactLib(project, modDir, islandName, modName)
                    break
                case 'react-ui':
                    scaffoldReactUi(project, modDir, islandName, modName, pnpmExec)
                    break
                case 'pulumi':
                    scaffoldPulumi(modDir, islandName, modName)
                    break
            }

            project.logger.lifecycle("  ✔  ${modName}")
        }

        // 3. Append island includes to settings.gradle
        appendToSettings(rootDir, islandName, subModTypes)

        project.logger.lifecycle("")
        project.logger.lifecycle("  Done. Review settings.gradle, then run:")
        if (subModTypes.contains('react-lib')) {
            project.logger.lifecycle("    ./gradlew ${islandName}:${islandName}-react-lib:pnpmInstall")
        }
        if (subModTypes.contains('react-ui')) {
            project.logger.lifecycle("    ./gradlew ${islandName}:${islandName}-react-ui:pnpmInstall")
        }
        project.logger.lifecycle("")
    }

    // -------------------------------------------------------------------------
    // SubModule scaffolders
    // -------------------------------------------------------------------------

    private void scaffoldSpringService(Project project, File modDir, String islandName, String modName) {
        // Standard Spring Boot service/BFF layout
        def srcMain = new File(modDir, 'src/main/java')
        def srcTest = new File(modDir, 'src/test/java')
        srcMain.mkdirs()
        srcTest.mkdirs()

        new File(modDir, 'build.gradle').text = """\
plugins {
    id 'archipelago.spring-service'
}

description = '${modName} — Spring service/BFF for the ${islandName} island'
""".stripIndent()
    }

    private void scaffoldReactLib(Project project, File modDir, String islandName, String modName) {
        new File(modDir, 'src').mkdirs()

        new File(modDir, 'build.gradle').text = """\
plugins {
    id 'archipelago.react-lib'
}

description = '${modName} — shared React library for the ${islandName} island'
""".stripIndent()

        writePackageJson(modDir, modName, 'library')
        writeTsConfig(modDir)

        // Placeholder entry so the project is immediately valid
        new File(modDir, 'src/index.ts').text = "// ${modName} entry point\nexport {};\n"
    }

    private void scaffoldReactUi(Project project, File modDir, String islandName, String modName, String pnpmExec) {
        def parentDir = modDir.parentFile  // the island directory

        project.logger.lifecycle("  → Running pnpm create vite for ${modName}...")

        // ── 1. Bootstrap with pnpm create vite (react-ts template) ──────────
        // pnpm create vite <projectName> --template react-ts
        // Must run from the island parent dir so the project lands at
        // <island>/<island>-react-ui/
        exec(project, parentDir, pnpmExec, 'create', 'vite', modName, '--template', 'react-ts')

        // ── 2. Add Module Federation and Archipelago dependencies ────────────
        // @module-federation/vite  — official MF plugin for Vite
        // @archipelago/core-react-lib — the shared Radix/token library from core
        exec(project, modDir, pnpmExec, 'add', '-D',
            '@module-federation/vite',
            '@module-federation/enhanced'
        )
        exec(project, modDir, pnpmExec, 'add',
            '@archipelago/core-react-lib'
        )

        // ── 3. Overlay Archipelago-specific files ────────────────────────────
        // build.gradle
        new File(modDir, 'build.gradle').text = """\
plugins {
    id 'archipelago.react-container'
}

description = '${modName} — Module Federation remote UI for the ${islandName} island'
""".stripIndent()

        // vite.config.ts  — replaces the one created by create-vite
        new File(modDir, 'vite.config.ts').text = """\
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { federation } from '@module-federation/vite';
import { federationShared } from '../../libs/webpack-shared-config/src/federation-shared';

// https://module-federation.io/guide/framework/vite
export default defineConfig({
  plugins: [
    react(),
    federation({
      name: '${toCamelCase(modName)}',
      filename: 'remoteEntry.js',
      exposes: {
        // './Widget': './src/components/${toPascalCase(islandName)}Widget',
      },
      shared: federationShared,
    }),
  ],
  build: {
    // Required for Module Federation's async container initialisation
    target: 'chrome89',
    modulePreload: { polyfill: false },
  },
  server: {
    port: 3000,
    cors: true,
  },
});
""".stripIndent()

        // src/module-entry.ts  — Module Federation public surface
        new File(modDir, 'src/module-entry.ts').text = """\
// Module Federation entry — export components for the shell to consume.
// Uncomment and expand as components are built.
// export { default as ${toPascalCase(islandName)}Widget } from './components/${toPascalCase(islandName)}Widget';
""".stripIndent()

        // src/api/generated placeholder so the directory is tracked by git
        new File(modDir, 'src/api/generated').mkdirs()
        new File(modDir, 'src/api/generated/.gitkeep').text = ''

        // .gitignore addendum — ignore generated API client
        def gitignore = new File(modDir, '.gitignore')
        gitignore.append('\n# Generated OpenAPI client\nsrc/api/generated/*.ts\n!src/api/generated/.gitkeep\n')
    }

    private void scaffoldSpringLib(File modDir, String islandName, String modName) {
        def srcMain = new File(modDir, 'src/main/java')
        def srcTest = new File(modDir, 'src/test/java')
        srcMain.mkdirs()
        srcTest.mkdirs()

        new File(modDir, 'build.gradle').text = """\
plugins {
    id 'archipelago.spring-lib'
}

description = '${modName} — shared Java library for the ${islandName} island'
""".stripIndent()
    }

    private void scaffoldPulumi(File modDir, String islandName, String modName) {
        new File(modDir, 'build.gradle').text = """\
plugins {
    id 'archipelago.pulumi'
}

description = '${modName} — infrastructure for the ${islandName} island'
""".stripIndent()

        new File(modDir, 'Pulumi.yaml').text = """\
name: ${modName}
runtime: java
description: Infrastructure for the ${islandName} Archipelago island
""".stripIndent()
    }

    // -------------------------------------------------------------------------
    // File writers
    // -------------------------------------------------------------------------

    private void writeIslandBuildGradle(File islandDir, String islandName) {
        new File(islandDir, 'build.gradle').text = """\
plugins {
    id 'archipelago.island'
}

description = '${islandName} — Archipelago island aggregator'
""".stripIndent()

        // Also create .gitignore for the island root
        new File(islandDir, '.gitignore').text = """\
# Gradle
.gradle/
build/
!gradle/wrapper/*.jar

# IDE
.idea/
*.iml
*.ipr
*.iws

# OS
.DS_Store

# Test output (per submodule)
test-island-*/*/build/
test-island-*/*/target/
"""
    }

    private void writePackageJson(File modDir, String modName, String moduleType) {
        def scripts = moduleType == 'library'
            ? '"build": "tsc", "generate:api": "openapi-typescript ../../../build/openapi.yaml -o src/api/generated/index.ts"'
            : '"build": "webpack --mode production", "start": "webpack serve --mode development", "generate:api": "openapi-typescript ../../../build/openapi.yaml -o src/api/generated/index.ts"'

        new File(modDir, 'package.json').text = """\
{
  "name": "@archipelago/${modName}",
  "version": "0.1.0",
  "private": true,
  "scripts": {
    ${scripts}
  },
  "dependencies": {
    "react": "workspace:*",
    "react-dom": "workspace:*"
  },
  "devDependencies": {
    "typescript": "workspace:*",
    "openapi-typescript": "workspace:*"
  },
  "peerDependencies": {
    "react": "*",
    "react-dom": "*"
  }
}
""".stripIndent()
    }

    private void writeTsConfig(File modDir) {
        new File(modDir, 'tsconfig.json').text = """\
{
  "extends": "../../tsconfig.base.json",
  "compilerOptions": {
    "outDir": "./dist",
    "rootDir": "./src"
  },
  "include": ["src"]
}
""".stripIndent()
    }

    private void writeWebpackConfig(File modDir, String islandName, String modName) {
        def exposedName = toCamelCase(islandName)
        new File(modDir, 'webpack.config.ts').text = """\
import { ModuleFederationPlugin } from '@module-federation/enhanced';
import { federationShared } from '../../libs/webpack-shared-config/src/federation-shared';

export default {
  entry: './src/module-entry.ts',
  output: {
    publicPath: 'auto',
  },
  plugins: [
    new ModuleFederationPlugin({
      name: '${exposedName}',
      filename: 'remoteEntry.js',
      exposes: {
        // './Widget': './src/components/${toPascalCase(islandName)}Widget',
      },
      shared: federationShared,
    }),
  ],
};
""".stripIndent()
    }

    private void appendToSettings(File rootDir, String islandName, List<String> subModTypes) {
        def settingsFile = new File(rootDir, 'settings.gradle')
        
        // Check if island is already registered in settings.gradle
        if (settingsFile.exists()) {
            def content = settingsFile.text
            // Check if island name appears in an include statement
            def islandIncludeRegex = /\binclude\s*\(\s*['"][^'"]*${islandName}[^'"]*['"]\s*\)/
            if (content ==~ islandIncludeRegex) {
                throw new IllegalArgumentException(
                    "Island '${islandName}' is already registered in settings.gradle. " +
                    "Use -Poverwrite=true to replace the entire island."
                )
            }
        }
        
        def addition = new StringBuilder("\n// Island: ${islandName}\ninclude('${islandName}')\n")
        subModTypes.each { type ->
            addition.append("include('${islandName}:${islandName}-${type}')\n")
        }
        settingsFile.append(addition.toString())
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Executes a command in the given working directory, streaming output to
     * Gradle's lifecycle log and capturing a tail of each stream for inclusion
     * in error messages. Inherits the parent PATH (and full environment) so
     * that git and other system tools remain discoverable even though the
     * pnpm executable is now passed as an absolute path.
     *
     * Throws a RuntimeException with the working dir, exit code, and the
     * last few lines of stdout/stderr on non-zero exit.
     */
    private void exec(Project project, File workDir, String... cmd) {
        project.logger.lifecycle("     (cwd: ${workDir}) ${cmd.join(' ')}")

        def pb = new ProcessBuilder(cmd.toList())
                .directory(workDir)
        // ProcessBuilder.environment() is pre-populated with the current
        // process environment (including PATH), so git and other system
        // tools remain findable. We intentionally do not clear it.

        Process proc
        try {
            proc = pb.start()
        } catch (IOException ioe) {
            throw new RuntimeException(
                "scaffoldIsland: failed to start command '${cmd[0]}' in ${workDir}: ${ioe.message}. " +
                "Verify Node/pnpm provisioning (./gradlew pnpmSetup) and that '${cmd[0]}' is an absolute path or on PATH.",
                ioe
            )
        }

        def stdoutTail = new LinkedList<String>()
        def stderrTail = new LinkedList<String>()
        final int TAIL_LINES = 40

        def stdoutThread = Thread.start {
            proc.inputStream.eachLine { line ->
                System.out.println(line)
                synchronized (stdoutTail) {
                    stdoutTail.add(line)
                    if (stdoutTail.size() > TAIL_LINES) stdoutTail.removeFirst()
                }
            }
        }
        def stderrThread = Thread.start {
            proc.errorStream.eachLine { line ->
                System.err.println(line)
                synchronized (stderrTail) {
                    stderrTail.add(line)
                    if (stderrTail.size() > TAIL_LINES) stderrTail.removeFirst()
                }
            }
        }

        int exit = proc.waitFor()
        stdoutThread.join()
        stderrThread.join()

        if (exit != 0) {
            def msg = new StringBuilder()
            msg << "scaffoldIsland: command failed (exit ${exit})\n"
            msg << "  command : ${cmd.join(' ')}\n"
            msg << "  cwd     : ${workDir.absolutePath}\n"
            if (!stderrTail.isEmpty()) {
                msg << "  stderr  (last ${stderrTail.size()} lines):\n"
                stderrTail.each { msg << "    ${it}\n" }
            }
            if (stderrTail.isEmpty() && !stdoutTail.isEmpty()) {
                msg << "  stdout  (last ${stdoutTail.size()} lines):\n"
                stdoutTail.each { msg << "    ${it}\n" }
            }
            throw new RuntimeException(msg.toString())
        }
    }

    private String toPascalCase(String kebab) {
        kebab.split('-').collect { it.capitalize() }.join('')
    }

    private String toCamelCase(String kebab) {
        def parts = kebab.split('-')
        parts[0] + parts.tail().collect { it.capitalize() }.join('')
    }

    private void printUsage(Project project) {
        project.logger.lifecycle("""
╔══════════════════════════════════════════════════════════════════════╗
║  scaffoldIsland — Usage                                              ║
╠══════════════════════════════════════════════════════════════════════╣
║  ./gradlew scaffoldIsland                                            ║
║      -PislandName=<name>                                             ║
║      -PsubModules=<comma-separated types>                            ║
║                                                                      ║
║  SubModule types (-PsubModules is optional; omit for all):          ║
║    spring-service   Spring Boot service/BFF (archipelago.spring-lib) ║
║    spring-lib       Shared Java library (archipelago.spring-lib)     ║
║    react-lib        Shared React library (archipelago.react-lib)     ║
║    react-ui         MF remote UI (vite + @module-federation/vite)    ║
║    pulumi           Infrastructure (archipelago.pulumi)              ║
║                                                                      ║
║  Example (all modules — default when -PsubModules omitted):         ║
║    ./gradlew scaffoldIsland -PislandName=inventory                   ║
║                                                                      ║
║  Example (selected modules):                                         ║
║    ./gradlew scaffoldIsland                                          ║
║      -PislandName=inventory                                          ║
║      -PsubModules=spring-service,react-lib,react-ui                  ║
║                                                                      ║
║  Example (frontend only):                                            ║
║    ./gradlew scaffoldIsland                                          ║
║      -PislandName=inventory                                          ║
║      -PsubModules=react-lib,react-ui                                 ║
╚══════════════════════════════════════════════════════════════════════╝
""")
    }
}
