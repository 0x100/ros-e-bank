node('master'){
    currentBuild.displayName = "#${env.BUILD_ID} ${env.app_name}"
    currentBuild.description = "Deploy stack '${env.app_name}'"

    withCredentials([[$class: "UsernamePasswordMultiBinding", credentialsId: "rancher", usernameVariable: "AKEY", passwordVariable: "SKEY"]]) {
        stage 'Pulling files'
            git branch: 'master', url: 'ssh://git@rt.reliab.tech:55522/reb/microservices.git'

        stage 'Prepare compose files'
            sh script: ". rancher/vars/main; " +
                "confd -confdir ./rancher/roles/service " +
                "-log-level debug " +
                "-onetime "

        try {
            stage 'Deploy Application'
                timeout(time: 300, unit: 'SECONDS') {
                    sh  ". ${env.WORKSPACE}/rancher/vars/main;" +
                        "rancher-compose " +
                        "--debug " +
                        "--url=http://127.0.0.1:8080/ " +
                        "--access-key=${env.AKEY} " +
                        "--secret-key=${env.SKEY} " +
                        "--project-name=${env.app_name} " +
                        "up -d --pull --upgrade --batch-size '1'"
                }

        } catch (Exception err) {
            echo 'Rolling back'
                sh  ". ${env.WORKSPACE}/rancher/vars/main;" +
                    "rancher-compose " +
                    "--debug " +
                    "--url=http://127.0.0.1:8080/ " +
                    "--access-key=${env.AKEY} " +
                    "--secret-key=${env.SKEY} " +
                    "--project-name=${env.app_name} " +
                    "up -d --rollback"
            throw new Exception("Can't deploy applications. Rolling back..", err)
        }
        stage 'Confirm Upgrade'
            sh  ". ${env.WORKSPACE}/rancher/vars/main;" +
                "rancher-compose " +
                "--debug " +
                "--url=http://127.0.0.1:8080/ " +
                "--access-key=${env.AKEY} " +
                "--secret-key=${env.SKEY} " +
                "--project-name=${env.app_name} " +
                "up -d --confirm-upgrade"
    }
}