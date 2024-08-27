#!groovy

// cp -r "${WORKSPACE}"/zephyr/drivers/serial/uart_ns16550.h "${WORKSPACE}"/Protex_IA/zephyr/drivers/serial/

def Protex_IA_Files() {
    dir("${WORKSPACE}") {
	sh """#!/bin/bash -xe
 	mkdir  -p  "${WORKSPACE}"/Protex_IA/zephyr/drivers/ith/
	cp -r "${WORKSPACE}"/zephyr/drivers/ith "${WORKSPACE}"/Protex_IA/zephyr/drivers/ith
 	mkdir -p  "${WORKSPACE}"/Protex_IA/zephyr/subsys/tracing/ith
        cp -r "${WORKSPACE}"/zephyr/drivers/ith "${WORKSPACE}"/Protex_IA/zephyr/subsys/tracing/ith
	"""
    }
}

pipeline {
    agent {
        node {
            label 'ESC-DOCKER-UB16'
        }
    }

    environment {
        DATETIME = new Date().format("yyyy'_WW'ww'.'u");
        TIME = new Date().format("yyyyMMdd-HHmm");
        BDBA_SCAN_DIR = "BDBA_SCAN"
        ZEPHYR_TOOLCHAIN_VARIANT = 'zephyr';
        ZEPHYR_SDK_INSTALL_DIR = '/tmp/zephyr-sdk-0.16.1';
        wit_path = "${WORKSPACE}/apps/wit";
        BDSERVER = "https://amrprotex003.devtools.intel.com"
        BDPROJNAME1 = "RaptorLake_Zephyr_IA"
        BDPROJNAME2 = "RaptorLake_Zephyr_IA_BSD"
        REPO_TOOL_PATH = "/nfs/png/home/lab_bldmstr/bin";
        ZEPHYR_BASE = "${WORKSPACE}/zephyr"
        DOCKER_RUN_CMD_FETCH="docker run --rm  -e LOCAL_USER=lab_bldmstr -e LOCAL_USER_ID=`id -u` -e LOCAL_GROUP_ID=`id -g` \
            -e ftp_proxy='http://proxy-dmz.intel.com:911' \
            -e ZEPHYR_TOOLCHAIN_VARIANT='zephyr' \
            -e ZEPHYR_SDK_INSTALL_DIR='/tmp/zephyr-sdk-0.16.1' \
            -v ${WORKSPACE}:${WORKSPACE} \
            -v ${WORKSPACE}/.ccache:/home/lab_bldmstr/.ccache \
            -v /nfs/png/home/lab_bldmstr:/home/lab_bldmstr:ro \
            amr-registry.caas.intel.com/esc-devops/gen/lin/zephyr/ubuntu/22.04:20231006_1448"
        DOCKER_RUN_CMD_BUILD="docker run --rm  -e LOCAL_USER=lab_bldmstr -e LOCAL_USER_ID=`id -u` -e LOCAL_GROUP_ID=`id -g` \
            -e ftp_proxy='http://proxy-dmz.intel.com:911' \
            -e ZEPHYR_TOOLCHAIN_VARIANT='zephyr' \
            -e ZEPHYR_SDK_INSTALL_DIR='/tmp/zephyr-sdk-0.16.1' \
            -v ${WORKSPACE}:${WORKSPACE} \
            -v ${WORKSPACE}/.ccache:/home/lab_bldmstr/.ccache \
            amr-registry.caas.intel.com/esc-devops/gen/lin/zephyr/ubuntu/22.04:20231006_1448"
        }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '90', artifactDaysToKeepStr: '30'))
        skipDefaultCheckout()
    }
    parameters {
        booleanParam(name: 'CLEANWS', defaultValue: true, description: 'Clean workspace')
        booleanParam(name: 'EMAIL', defaultValue: true, description: 'Email notification upon job completion')
        booleanParam(name: 'PUBLISH', defaultValue: true, description: 'Artifacts deployment')
        string(name: 'BRANCH_IDENTIFIER', trim: true, defaultValue: 'ia_dev', description: 'Git Branch identifier')
        string(name: 'TAG_IDENTIFIER', trim: true, defaultValue: '', description: '''Tag identifier.
            - NOT MANDATORY (to be used only to checkout to a specific tag)''')
    }

    stages {
        stage ('CLEAN') {
            when {
                expression { params.CLEANWS == true }
            }
            steps {
                deleteDir()
            }
        }

        stage('SCM') {
            steps {
                /*script {
                    if ("${params.BRANCH_IDENTIFIER}".isEmpty()) {
                        error("Build failed - BRANCH_IDENTIFIER is NULL. \nPlease specify new branch name in the 'BRANCH_IDENTIFIER' parameter")
                    }
                    if ("${params.BRANCH_IDENTIFIER}" == 'rpl_dev') {
                        BRANCH = "rpl_dev"
                    } else {
                        BRANCH = "${BRANCH_IDENTIFIER}"
                    }
                }*/
                checkout([$class: 'GitSCM',
                userRemoteConfigs: [[credentialsId: 'GitHub-Token', url: 'https://github.com/intel-innersource/libraries.devops.henosis.build.automation.services.git']],
                branches: [[name: "refs/heads/master"]],
                extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'engservices'],
                [$class: 'ScmName', name: 'engservices'],
                [$class: 'CleanBeforeCheckout']]])

                sh """cd ${WORKSPACE} && \
                mkdir .ccache && \
                ${DOCKER_RUN_CMD_FETCH} bash -c "cd ${WORKSPACE} && \
                git clone https://github.com/intel-innersource/os.rtos.zephyr.iot.zephyr.git -b "${BRANCH_IDENTIFIER}" zephyr && \
                cd zephyr && \
                west init --local && \
                west update"
                """
            }
        }
        stage('BUILD') {
            steps {
                sh """${DOCKER_RUN_CMD_BUILD} bash -c "chmod +x ${WORKSPACE}/zephyr/scripts/reference_app_build.sh && cd ${WORKSPACE}/zephyr && source zephyr-env.sh && \
                ../zephyr/scripts/reference_app_build.sh && \
                mkdir ${WORKSPACE}/upload && \
                cp -r ${WORKSPACE}/zephyr/BDBA/* ${WORKSPACE}/upload/"
                """
            }
        }
        stage('QA: BDBA') {
            steps {
                dir("${WORKSPACE}/upload/${BDBA_SCAN_DIR}") {
                    deleteDir()
                }
                dir("${WORKSPACE}/upload") {
                    zip(zipFile: "${BDBA_SCAN_DIR}/${JOB_BASE_NAME}.zip")
                }
                dir("${WORKSPACE}/upload") {
                    withCredentials([usernamePassword(credentialsId: 'BuildAutomation', passwordVariable: 'BDPWD', usernameVariable: 'BDUSR')]) {
                        sh """#!/bin/bash -xe
                        cd ${WORKSPACE}/upload
                        python ${WORKSPACE}/engservices/tools/bdba/bdbascan.py -u ${BDUSR} -p ${BDPWD} -tn EHL-LIN-ZEPHYR.IA -so ${WORKSPACE}/upload/${BDBA_SCAN_DIR}/${JOB_BASE_NAME}.zip -o ${WORKSPACE}/upload -v -t 2000"""
                    }
                }
            }
        }
        stage('QA: PROTEX_Zephyr_IA') {
            steps {                
                Protex_IA_Files()
                dir("${WORKSPACE}/engservices/tools/protex") {
                    withCredentials([usernamePassword(credentialsId: 'BuildAutomation', passwordVariable: 'BDPWD', usernameVariable: 'BDUSR')]) {
                        withEnv(["PATH=" + env.PATH + ":/nfs/png/disks/ecg_es_disk2/engsrv/tools/protexIP/bin"]) {
                            sh """python3 -u bdscan.py --verbose --name ${BDPROJNAME1} --path ${WORKSPACE}/Protex_IA --cos ${WORKSPACE}/engservices/tools/protex/ --obl ${WORKSPACE}/engservices/tools/protex/
                            """                                    
                        }
                    }
                }
            }
		}
		stage('QA: PROTEX_Zephyr_IA_BSD') {
            steps {                            
                dir("${WORKSPACE}/engservices/tools/protex") {
                    withCredentials([usernamePassword(credentialsId: 'BuildAutomation', passwordVariable: 'BDPWD', usernameVariable: 'BDUSR')]) {
                        withEnv(["PATH=" + env.PATH + ":/nfs/png/disks/ecg_es_disk2/engsrv/tools/protexIP/bin"]) {
                            sh """python3 -u bdscan.py --verbose --name ${BDPROJNAME2} --path ${WORKSPACE}/Protex_IA --cos ${WORKSPACE}/engservices/tools/protex/ --obl ${WORKSPACE}/engservices/tools/protex/
                            """                                    
                        }
                    }
                }
            }
		}
    }
     post {
        always {
            script{
                echo "Logparser code removed!"
                if (params.EMAIL == true) {
                    emailext body: '${SCRIPT, template="managed:pipeline.html"}', subject: '$DEFAULT_SUBJECT', replyTo: '$DEFAULT_REPLYTO', to: '$DEFAULT_RECIPIENTS, nachiketa.kumar@intel.com'
                }
            }
        }
    }
}
