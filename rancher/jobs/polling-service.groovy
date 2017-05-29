node('master') {
    def m = (env.JOB_BASE_NAME =~ /.+?-(.+?)-pipeline/)
    // get env and app name by job name
    def app_name = m[0][1]
    m = null

    def downstreamJob = 'build-service-pipeline-generic'
    def git_url = 'ssh://git@rt.reliab.tech:55522/reb/microservices.git'
    def git_branch = 'master'

    try {
        step([$class: 'WsCleanup']) // clean workspace

        stage 'Pulling sources'
            git branch: git_branch, url: git_url

        stage 'Build & Deploy Application'
            def buildResult = build job: downstreamJob, parameters: [
                    string(name: 'GIT_BRANCH', value: git_branch),
                    string(name: 'GIT_URL', value: git_url),
                    string(name: 'DOCKER_TAG', value: env.BUILD_ID),
                    string(name: 'app_name', value: app_name)
            ]
            echo buildResult.rawBuild.log
    } catch (err){
        throw err
    }
}