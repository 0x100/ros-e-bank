node(){
    currentBuild.displayName = "#${env.BUILD_ID} ${env.stack_name}"

    stage "Pulling compose files"
        git branch: 'master', url: 'ssh://git@rt.reliab.tech:55522/reb/microservices.git'

    try {
        withCredentials([[$class: "UsernamePasswordMultiBinding", credentialsId: "rancher", usernameVariable: "AKEY", passwordVariable: "SKEY"]]) {
            try {
                echo "Deploy Application"
                timeout(time: 300, unit: 'SECONDS') {
                    sh ". ${env.WORKSPACE}/rancher/vars/main;" +
                            "rancher-compose " +
                            "--debug " +
                            "--url=http://127.0.0.1:8080/ " +
                            "--access-key=${env.AKEY} " +
                            "--secret-key=${env.SKEY} " +
                            "--file=rancher/roles/${env.stack_name}/docker-compose.yml " +
                            "--rancher-file=rancher/roles/${env.stack_name}/rancher-compose.yml " +
                            "up -d --pull --upgrade --batch-size '1'"
                }

            } catch (Exception err) {
                echo "Rolling back"
                sh ". ${env.WORKSPACE}/rancher/vars/main;" +
                        "rancher-compose " +
                        "--debug " +
                        "--url=http://127.0.0.1:8080/ " +
                        "--access-key=${env.AKEY} " +
                        "--secret-key=${env.SKEY} " +
                        "--file=rancher/roles/${env.stack_name}/docker-compose.yml " +
                        "--rancher-file=rancher/roles/${env.stack_name}/rancher-compose.yml " +
                        "up -d --rollback"
                throw new Exception("Can't deploy applications. Rolling back..", err)
            }
            echo "Confirm Upgrade"
            sh ". ${env.WORKSPACE}/rancher/vars/main;" +
                    "rancher-compose " +
                    "--debug " +
                    "--url=http://127.0.0.1:8080/ " +
                    "--access-key=${env.AKEY} " +
                    "--secret-key=${env.SKEY} " +
                    "--file=rancher/roles/${env.stack_name}/docker-compose.yml " +
                    "--rancher-file=rancher/roles/${env.stack_name}/rancher-compose.yml " +
                    "up -d --confirm-upgrade"
        }
    } catch (Exception err){
        throw new Exception("Cant found env with this name", err)
    }
}