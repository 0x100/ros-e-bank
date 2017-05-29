node('master') {
    def m = (env.JOB_BASE_NAME =~ /.+?-(.+?)-pipeline/)
    // get env and app name by job name
    def app_name = m[0][1]
    m = null

    def downstreamJob = 'build-service-pipeline-generic'

    try {
        step([$class: 'WsCleanup']) // clean workspace

        stage 'Pulling sources'
            git branch: env.GIT_BRANCH, url: env.GIT_URL

        stage 'Build & Deploy Application'
            def buildResult = build job: downstreamJob, parameters: [
                    string(name: 'GIT_BRANCH', value: env.GIT_BRANCH),
                    string(name: 'GIT_URL', value: env.GIT_URL),
                    string(name: 'DOCKER_TAG', value: env.BUILD_ID),
                    string(name: 'app_name', value: app_name)
            ]
            echo buildResult.rawBuild.log
    } catch (err){
        throw err
    }
}