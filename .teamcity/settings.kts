import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.MavenBuildStep
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.projectFeatures.githubIssues
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

    features {
        githubIssues {
            id = "PROJECT_EXT_3"
            displayName = "slateeho/TeamCityPipeline"
            repositoryURL = "https://github.com/slateeho/TeamCityPipeline"
            param("tokenId", "")
        }
    }
}

object MavenBuild2 : BuildType({
    name = "Build"

    artifactRules = "target/plaindoll.jar => artifacts"
    type = BuildTypeSettings.Type.DEPLOYMENT
    maxRunningBuilds = 1
    publishArtifacts = PublishMode.SUCCESSFUL

    params {
        param("branch", "teamcity.build.branch")
    }

    vcs {
        root(GitHub, "+:. => .")
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            name = "deploy"

            conditions {
                equals("teamcity.build.branch", "refs/heads/master")
            }
            goals = "clean package"
            isIncremental = true
        }
        maven {
            name = "test"
            id = "text"

            conditions {
                doesNotEqual("teamcity.build.branch", "refs/heads/master")
            }
            goals = "clean test"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
            localRepoScope = MavenBuildStep.RepositoryScope.BUILD_CONFIGURATION
        }
    }

    triggers {
        vcs {
            branchFilter = ""
            enableQueueOptimization = false
        }
    }
})

object GitHub : GitVcsRoot({
    name = "GitHub"
    url = "https://github.com/slateeho/TeamCityPipeline.git"
    branch = "refs/heads/master"
    checkoutSubmodules = GitVcsRoot.CheckoutSubmodules.IGNORE
    checkoutPolicy = GitVcsRoot.AgentCheckoutPolicy.NO_MIRRORS
    authMethod = password {
        userName = "slateeho"
        password = "credentialsJSON:54e33c7d-ffaa-4abc-a44d-eed4f6afda5e"
    }
})
