#!groovy

def getConfig() {
    props = readYaml file: "${WORKSPACE}/henosis_devops/Nexuie_75703/gio/config.yml"
    gio_cmd = props.gio_cmd
    println(gio_cmd)
}
// https://github.com/Kalla-Udaykumar/jenkins-script.setup/blob/master/Nexuie_75703/gio/release_searchArt.aql
def getBinFiles_release() {
    String binList_aql = readFile("${WORKSPACE}/henosis_devops/Nexuie_75703/gio/release_searchArt.aql").replaceAll('ARTIFACTORY_REPO',"${ARTIFACTORY_REPO}").replaceAll('ARTIFACTORY_PATH',"${ARTIFACTORY_PATH}")
    writeFile file:"${WORKSPACE}/henosis_devops/cac/arl-s/win/ifwi/gio/release_binList.aql", text: binList_aql
    def read_release_binList_aql = readFile file: "${WORKSPACE}/henosis_devops/cac/arl-s/win/ifwi/gio/release_binList.aql"
    println read_release_binList_aql

    dir("${WORKSPACE}/henosis_devops/cac/arl-s/win/ifwi/gio") {
        withCredentials([usernamePassword(credentialsId: 'BuildAutomation', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
            sh """ #!/bin/bash -xe
                curl -u ${USERNAME}:${PASSWORD} -X POST -k https://${ARTIFACTORY_SERVER}/artifactory/api/search/aql -d @release_binList.aql -H "content-type: text/plain" > ${WORKSPACE}/release_binLIST.json
            """
        }
    }

    props = readYaml file: "${WORKSPACE}/release_binLIST.json"
    binFile = props.results.name[0]
    artifactoryPath = props.results.path[0]
    artifactoryRepo = props.results.repo[0]
    release_binFiles = "https://${ARTIFACTORY_SERVER}/artifactory/${artifactoryRepo}/${artifactoryPath}/${binFile}".replaceAll(" ","%20")
    println("release_ifwi = ${release_binFiles}")
}
def getBinFiles_debug() {
    String binList_aql = readFile("${WORKSPACE}/henosis_devops/Nexuie_75703/gio/debug_searchArt.aql").replaceAll('ARTIFACTORY_REPO',"${ARTIFACTORY_REPO}").replaceAll('ARTIFACTORY_PATH',"${ARTIFACTORY_PATH}")
    writeFile file:"${WORKSPACE}/henosis_devops/cac/arl-s/win/ifwi/gio/debug_binList.aql", text: binList_aql
    def read_debug_binList_aql = readFile file: "${WORKSPACE}/henosis_devops/cac/arl-s/win/ifwi/gio/debug_binList.aql"
    println read_debug_binList_aql

    dir("${WORKSPACE}/henosis_devops/cac/arl-s/win/ifwi/gio") {
        withCredentials([usernamePassword(credentialsId: 'BuildAutomation', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
            sh """ #!/bin/bash -xe
                curl -u ${USERNAME}:${PASSWORD} -X POST -k https://${ARTIFACTORY_SERVER}/artifactory/api/search/aql -d @debug_binList.aql -H "content-type: text/plain" > ${WORKSPACE}/debug_binLIST.json
            """
        }
    }
    props = readYaml file: "${WORKSPACE}/debug_binLIST.json"
    binFile = props.results.name[0]
    artifactoryPath = props.results.path[0]
    artifactoryRepo = props.results.repo[0]
    debug_binFiles = "https://${ARTIFACTORY_SERVER}/artifactory/${artifactoryRepo}/${artifactoryPath}/${binFile}".replaceAll(" ","%20")
    println("debug_ifwi = ${debug_binFiles}")
    println("test_repo = ${artifactoryPath}")
    parts = artifactoryPath.split("/")
    BuildID = parts[3]
    println("BuildID = ${BuildID}")
}

def create_dynamicJson(release_binFiles, debug_binFiles, BuildID) {
    ifwi_image_path_release = "${release_binFiles}"
    println("release_ifwi = ${ifwi_image_path_release}")

    ifwi_image_path_debug = "${debug_binFiles}"
    println("debug_ifwi = ${ifwi_image_path_debug}")
    
    test_BuildID = "${BuildID}"
    println("BuildID = ${test_BuildID}")

    payload_path = "https://${ARTIFACTORY_SERVER}/artifactory/${ARTIFACTORY_REPO}/${ARTIFACTORY_PATH}/../../../${test_BuildID}/Payload-Details/ARL_S_Payload_Versions.json"
    println("payload path = ${payload_path}")

    String dynamicParam = readFile("${WORKSPACE}/henosis_devops/Nexuie_75703/gio//dynamic_param.json").replaceAll('ifwi_image_path_release',"${ifwi_image_path_release}").replaceAll('payload_path',"${payload_path}").replaceAll('ifwi_image_path_debug',"${ifwi_image_path_debug}")
    writeFile file:"${WORKSPACE}/release_gio_validation/dynamic_param.json", text: dynamicParam
    def dynamicParam_json = readJSON file: "${WORKSPACE}/release_gio_validation/dynamic_param.json"
    println dynamicParam_json
}


// GIO Test trigger Function
def trigger_giocmd (gio_cmd) {
    dynamic_giocmd = "${gio_cmd}".replaceAll('-nowait',"") + " -nowait"
    
    sh """ #!/bin/bash -xe
          docker run --rm -v ${WORKSPACE}:${WORKSPACE} -w ${WORKSPACE}/release_gio_validation \
          -e LOCAL_USER_ID=`id -u` -e LOCAL_GROUP_ID=`id -g` -e LOCAL_USER="lab_bldmstr" ${DOCKER_IMG} \
          bash -c "pip install --upgrade gio_plugin --extra-index-url https://af01p-png.devtools.intel.com/artifactory/api/pypi/gio-testing-center-pyrepo-png-local/simple \
          && gio_plugin ${dynamic_giocmd} "
			"""
}
def fullGIO(gio_cmd) {
    getBinFiles_release()
	getBinFiles_debug()
    create_dynamicJson(release_binFiles, debug_binFiles, BuildID)
	  //create_dynamicJson_debug(debug_binFiles)
    //gio_cmd = gio_cmd.values()[0]
    trigger_giocmd (gio_cmd)
}

pipeline {
    agent {
      node {
         label "BSP-DOCKER-POOL"
        }
    }
    environment {
        DATETIME = new Date().format("yyyyMMdd-HHmm")
        DOCKER_IMG = "amr-registry.caas.intel.com/esc-apps/gio/ubuntu18.04:20210927_1110"
        ARTIFACTORY_SERVER = "af01p-png.devtools.intel.com"
        ARTIFACTORY_REPO = "hspe-iotgfw-adl-png-local"
    }
    options {
       timestamps()
       buildDiscarder(logRotator(numToKeepStr: '90', artifactDaysToKeepStr: '30'))
       skipDefaultCheckout()
    }
    parameters {
        booleanParam(name: 'CLEANWS', defaultValue: true, description: 'Clean workspace')
        string(name: 'ARTIFACTORY_PATH', defaultValue: '', description: 'GIO Value from Upstream')
    }
    stages {
        stage ('CLEAN') {
            when {
                expression { params.CLEANWS == true }
            }
            steps {
                // Recursively deletes the current directory and its contents.
                deleteDir()
                }
        }
        stage ('READ CONFIG.JSON FILE') {
            steps {
                checkout([$class: 'GitSCM',
                userRemoteConfigs: [[credentialsId: 'GitHub-Token', url: 'https://github.com/Kalla-Udaykumar/jenkins-script.setup.git']],
                branches: [[name: "master"]],
                extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'henosis_devops'],
                [$class: 'ScmName', name: 'henosis_devops'],
                [$class: 'CleanBeforeCheckout']]])
            }
        }
        stage('GIO triggering') {
            steps {
                getConfig()
                script {
                    fullGIO(gio_cmd)
                }
            }
        }
    }
}
