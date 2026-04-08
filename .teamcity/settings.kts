import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2025.11"

project {

    vcsRoot(GitHub)

    buildType(MavenBuild2)
}

object MavenBuild2 : BuildType({
    name = "Build"

    enablePersonalBuilds = false
    artifactRules = "target/*.jar => artifacts"
    type = BuildTypeSettings.Type.DEPLOYMENT
    maxRunningBuilds = 1
    publishArtifacts = PublishMode.SUCCESSFUL

    vcs {
        root(GitHub, "+:. => .")
    }

    steps {
        maven {
            name = "deploy"

            conditions {
                equals("teamcity.build.branch", "master")
            }
            goals = "clean deploy"
        }
        maven {
            name = "test"
            id = "text"

            conditions {
                doesNotEqual("teamcity.build.branch", "master")
            }
            goals = "clean test"
        }
    }

    triggers {
        vcs {
            branchFilter = ""
            enableQueueOptimization = false
        }
    }

    features {
        feature {
            type = "publish-artifacts"
        }
    }
})

object GitHub : GitVcsRoot({
    name = "GitHub"
    url = "https://github.com/slateeho/example-teamcity-DSL.git"
    branch = "master"
    checkoutSubmodules = GitVcsRoot.CheckoutSubmodules.IGNORE
    checkoutPolicy = GitVcsRoot.AgentCheckoutPolicy.NO_MIRRORS
})
