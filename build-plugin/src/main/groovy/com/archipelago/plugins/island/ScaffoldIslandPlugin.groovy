package com.archipelago.plugins.island

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

    static final List<String> KNOWN_MODULE_TYPES = [
        'spring-service', 'spring-lib', 'react-lib', 'react-ui', 'pulumi'
    ]

    void apply(Project project) {
        // Only register on the root project
        if (project != project.rootProject) return

        project.tasks.register(TASK_NAME) {
            group       = GROUP
            description = 'Scaffold a new Archipelago island with selected submodules.'

            doLast {
                def islandName  = resolveIslandName(project)
                def subModTypes = resolveSubModules(project)
                scaffold(project, islandName, subModTypes)
            }
        }
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

        project.logger.lifecycle("╔══════════════════════════════════════════════════════")
        project.logger.lifecycle("║  Scaffolding island: ${islandName}")
        project.logger.lifecycle("║  SubModules: ${subModTypes.join(', ')}")
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
                    scaffoldReactUi(project, modDir, islandName, modName)
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
    id 'archipelago.spring-lib'
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

    private void scaffoldReactUi(Project project, File modDir, String islandName, String modName) {
        def parentDir = modDir.parentFile  // the island directory
        def nodeVersion  = project.findProperty('node.version')  ?: '22.4.1'
        def pnpmVersion  = project.findProperty('pnpm.version')  ?: '9.5.0'

        project.logger.lifecycle("  → Running pnpm create vite for ${modName}...")

        // ── 1. Bootstrap with pnpm create vite (react-ts template) ──────────
        // pnpm create vite <projectName> --template react-ts
        // Must run from the island parent dir so the project lands at
        // <island>/<island>-react-ui/
        exec(project, parentDir, 'pnpm', 'create', 'vite', modName, '--template', 'react-ts')

        // ── 2. Add Module Federation and Archipelago dependencies ────────────
        // @module-federation/vite  — official MF plugin for Vite
        // @archipelago/core-react-lib — the shared Radix/token library from core
        exec(project, modDir, 'pnpm', 'add', '-D',
            '@module-federation/vite',
            '@module-federation/enhanced'
        )
        exec(project, modDir, 'pnpm', 'add',
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
     * Gradle's lifecycle log. Throws if the process exits non-zero.
     */
    private void exec(Project project, File workDir, String... cmd) {
        project.logger.lifecycle("     ${cmd.join(' ')}")
        def proc = cmd.toList().execute(null, workDir)
        proc.consumeProcessOutput(
            new PrintStream(System.out),
            new PrintStream(System.err)
        )
        proc.waitFor()
        if (proc.exitValue() != 0) {
            throw new RuntimeException(
                "Command failed (exit ${proc.exitValue()}): ${cmd.join(' ')}"
            )
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
