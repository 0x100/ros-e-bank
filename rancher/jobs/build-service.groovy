node('master') {
    currentBuild.displayName = "#${env.DOCKER_TAG} ${env.app_name}"
    currentBuild.description = "Build '${env.app_name}:${env.DOCKER_TAG}'"

    step([$class: 'WsCleanup']) // clean workspace

    stage 'Pulling sources'
        git branch: env.GIT_BRANCH, url: env.GIT_URL

    dir("${env.WORKSPACE}/${env.app_name}") {
        stage 'Change App Version'
            def pom = readMavenPom file: 'pom.xml'
            pom.version = "1.0.0-SNAPSHOT"
            pom.artifactId = env.app_name
            writeMavenPom model: pom

        stage 'Prepare Build Files'
            sh script: ". ${env.WORKSPACE}/rancher/vars/main; " +
                    "confd -confdir ${env.WORKSPACE}/rancher/roles/service/services/${env.app_name} " +
                    "-log-level debug " +
                    "-onetime "

        stage 'Build Application'
            def mvnHome = tool 'maven-3.5.0'
            sh "${mvnHome}/bin/mvn clean package -P docker -DskipTests=true -U"

        stage 'Publish Reports'
            step([$class: 'CheckStylePublisher', canComputeNew: false, defaultEncoding: '', healthy: '', pattern: '', unHealthy: ''])
            step([$class: 'FindBugsPublisher', canComputeNew: false, defaultEncoding: '', excludePattern: '', healthy: '', includePattern: '', pattern: '', unHealthy: ''])
            step([$class: 'PmdPublisher', canComputeNew: false, defaultEncoding: '', healthy: '', pattern: '', unHealthy: ''])

        stage 'Image Build & Push to Registry'
            docker.withRegistry('http://docker.registry:11008/') {
                docker.build("${env.app_name}", "--pull .").push("${env.DOCKER_TAG}")
            }
    }
    stage 'Clear Local Docker Repository'
        sh script: "docker rmi -f docker.registry:11008/${env.app_name}:${env.DOCKER_TAG} \$(docker images | grep '^<none>' | awk '{print \$3}')", returnStatus: true

    stage 'Deploy Application'
        def buildResult = build job: 'deploy-service-generic-pipeline', parameters: [
                string(name: 'DOCKER_TAG', value: env.DOCKER_TAG),
                string(name: 'app_name', value: env.app_name),
        ]
        echo buildResult.rawBuild.log
}