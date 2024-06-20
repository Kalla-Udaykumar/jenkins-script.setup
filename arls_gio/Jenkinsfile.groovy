#!groovy

def getConfig() {
    // props = readYaml file: "${WORKSPACE}/henosis_devops/cac/arls/win/bios/gio/config.yml"
    props = readYaml file: "${WORKSPACE}/henosis_devops/arls_gio/config.yml" 
    gio_cmd = props.gio_cmd
    profile_id = props.profile_id
    println(gio_cmd)

    sh"""
        curl -k -H "Accept: application/json"  "http://gio.intel.com/gio/api/webexec/gio-plugin-profile?id=${profile_id}&response=json" >> ${WORKSPACE}/arls_bios_profile.json
    """
    props = readYaml file: "${WORKSPACE}/arls_bios_profile.json"
    ARTIFACTORY_DEBUG_PATH = "${props.general_param.Debug_IFWI[0]}"
    ARTIFACTORY_RELEASE_PATH = "${props.general_param.Release_IFWI[0]}"
    RELEASE_FILENAME = "${props.general_param.RELEASE_FILENAME[0]}"
    DEBUG_FILENAME = "${props.general_param.DEBUG_FILENAME[0]}"
    OS_IMAGE_PATH = "${props.general_param.OS_IMAGE_PATH[0]}"
    OS_IMAGE_REPO = "${props.general_param.OS_IMAGE_REPO[0]}"
    BSP_RELEASE_GIT_REPO = "${props.general_param.BSP_RELEASE_GIT_REPO[0]}"
    CBKC_BSP_RELEASE_TAG = "${props.general_param.CBKC_BSP_RELEASE_TAG[0]}"

    //split to get the Artifactory server and Repo
    ARTIFACTORY_SERVER_url = ARTIFACTORY_RELEASE_PATH.replace('//', '/')
    ARTIFACTORY_SERVER_name = ARTIFACTORY_SERVER_url.split('/')
    ARTIFACTORY_SERVER = ARTIFACTORY_SERVER_name[1]
    ARTIFACTORY_REPO = ARTIFACTORY_SERVER_name[3]
    //Split the Artifactory URL to get the Repo Path
    ARTIFACTORY_PATH_LIST = ARTIFACTORY_SERVER_url.split(ARTIFACTORY_REPO+'/')
    ARTIFACTORY_PATH = ARTIFACTORY_PATH_LIST[1]
    println("Artifactory server is : ${ARTIFACTORY_SERVER},  and Repo is : ${ARTIFACTORY_REPO}")
    println("IFWI ARTIFACTORY_PATH is : ${ARTIFACTORY_PATH}")

    //Read OS_IMAGE_PATH and split to get repo
    OS_IMAGE_REPO_url = OS_IMAGE_REPO.replace('//', '/')
    OS_IMAGE_REPO_new = OS_IMAGE_REPO_url.split('/')
    ARTIFACTORY_IMAGE_REPO = OS_IMAGE_REPO_new[3]
    ARTIFACTORY_IMAGE_SERVER = OS_IMAGE_REPO_new[1]
    println("ARTIFACTORY_OS_IMAGE REpo is : ${ARTIFACTORY_IMAGE_REPO}")
    
    //Read tag repo and release_tag keyword
    TAG_REPO = BSP_RELEASE_GIT_REPO.split('/tags')[0]
    parts = CBKC_BSP_RELEASE_TAG.split("::")
    TAG = "'${parts.collect { it.trim() }.join("' '")}'"
    println("BSP_RELEASE_GIT_REPO is : ${TAG_REPO}")
    println("CBKC_BSP_RELEASE_TAG is : ${TAG}")
}

def getBinFiles_release() {
    /*String binList_aql = readFile("${WORKSPACE}/henosis_devops/cac/mtl-ps/win/bios/gio/release_searchArt.aql").replaceAll('ARTIFACTORY_REPO',"${ARTIFACTORY_REPO}").replaceAll('ARTIFACTORY_RELEASE_PATH',"${ARTIFACTORY_PATH}").replaceAll('RELEASE_FILENAME',"${RELEASE_FILENAME}")
    writeFile file:"${WORKSPACE}/henosis_devops/cac/mtl-ps/win/bios/gio/release_binList.aql", text: binList_aql
    def read_release_binList_aql = readFile file: "${WORKSPACE}/henosis_devops/cac/mtl-ps/win/bios/gio/release_binList.aql"
    println read_release_binList_aql*/

    String binList_aql = readFile("${WORKSPACE}/henosis_devops/arls_gio/release_searchArt.aql").replaceAll('ARTIFACTORY_REPO',"${ARTIFACTORY_REPO}").replaceAll('ARTIFACTORY_RELEASE_PATH',"${ARTIFACTORY_PATH}").replaceAll('RELEASE_FILENAME',"${RELEASE_FILENAME}")
    writeFile file:"${WORKSPACE}/henosis_devops/arls_gio/release_binList.aql", text: binList_aql
    def read_release_binList_aql = readFile file: "${WORKSPACE}/henosis_devops/arls_gio/release_binList.aql"
    println read_release_binList_aql

  
    dir("${WORKSPACE}/henosis_devops/cac/mtl-ps/win/bios/gio") {
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
    /*String binList_aql = readFile("${WORKSPACE}/henosis_devops/cac/mtl-ps/win/bios/gio/debug_searchArt.aql").replaceAll('ARTIFACTORY_REPO',"${ARTIFACTORY_REPO}").replaceAll('ARTIFACTORY_DEBUG_PATH',"${ARTIFACTORY_PATH}").replaceAll('DEBUG_FILENAME',"${DEBUG_FILENAME}")
    writeFile file:"${WORKSPACE}/henosis_devops/cac/mtl-ps/win/bios/gio/debug_binList.aql", text: binList_aql
    def read_debug_binList_aql = readFile file: "${WORKSPACE}/henosis_devops/cac/mtl-ps/win/bios/gio/debug_binList.aql"
    println read_debug_binList_aql*/

     String binList_aql = readFile("${WORKSPACE}/henosis_devops/aarls_gio/debug_searchArt.aql").replaceAll('ARTIFACTORY_REPO',"${ARTIFACTORY_REPO}").replaceAll('ARTIFACTORY_DEBUG_PATH',"${ARTIFACTORY_PATH}").replaceAll('DEBUG_FILENAME',"${DEBUG_FILENAME}")
    writeFile file:"${WORKSPACE}/henosis_devops/aarls_gio/debug_binList.aql", text: binList_aql
    def read_debug_binList_aql = readFile file: "${WORKSPACE}/henosis_devops/arls_gio/debug_binList.aql"
    println read_debug_binList_aql

    dir("${WORKSPACE}/henosis_devops/cac/mtl-ps/win/bios/gio") {
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
}

def getosimage_path() {
    /*String binList_aql = readFile("${WORKSPACE}/henosis_devops/cac/mtl-ps/win/bios/gio/image_searchArt.aql").replaceAll('ARTIFACTORY_IMAGE_REPO',"${ARTIFACTORY_IMAGE_REPO}").replaceAll('OS_IMAGE_PATH',"${OS_IMAGE_PATH}")
    writeFile file:"${WORKSPACE}/henosis_devops/cac/mtl-ps/win/bios/gio/image_binList.aql", text: binList_aql
    def read_image_binList_aql = readFile file: "${WORKSPACE}/henosis_devops/cac/mtl-ps/win/bios/gio/image_binList.aql"
    println read_image_binList_aql*/

  String binList_aql = readFile("${WORKSPACE}/henosis_devops/arls_gio/image_searchArt.aql").replaceAll('ARTIFACTORY_IMAGE_REPO',"${ARTIFACTORY_IMAGE_REPO}").replaceAll('OS_IMAGE_PATH',"${OS_IMAGE_PATH}")
    writeFile file:"${WORKSPACE}/henosis_devops/arls_gio/image_binList.aql", text: binList_aql
    def read_image_binList_aql = readFile file: "${WORKSPACE}/henosis_devops/arls_gio/image_binList.aql"
    println read_image_binList_aql

    dir("${WORKSPACE}/henosis_devops/cac/mtl-ps/win/bios/gio") {
        withCredentials([usernamePassword(credentialsId: 'BuildAutomation', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
            sh """ #!/bin/bash -xe
                curl -u ${USERNAME}:${PASSWORD} -X POST -k https://${ARTIFACTORY_IMAGE_SERVER}/artifactory/api/search/aql -d @image_binList.aql -H "content-type: text/plain" > ${WORKSPACE}/image_binLIST.json
            """
        }
    }
    props = readYaml file: "${WORKSPACE}/image_binLIST.json"
    binFile = props.results.name[0]
    artifactoryPath = props.results.path[0]
    artifactoryRepo = props.results.repo[0]
    image_binFiles = "https://${ARTIFACTORY_IMAGE_SERVER}/artifactory/${artifactoryRepo}/${artifactoryPath}/${binFile}".replaceAll(" ","%20")
    println("OS_IMAGE = ${image_binFiles}")
}

def create_dynamicJson(release_binFiles, debug_binFiles, image_binFiles) {
    ifwi_image_path_release = "${release_binFiles}"
    println("release_ifwi = ${ifwi_image_path_release}")

    ifwi_image_path_debug = "${debug_binFiles}"
    println("debug_ifwi = ${ifwi_image_path_debug}")
    
    OS_IMAGE_PATH_url = "${image_binFiles}"
    println("OS_IMAGE = ${OS_IMAGE_PATH_url}")
    
    def latest_tag = readFile "${WORKSPACE}/latest_tag.txt"
    println("latest tag = ${latest_tag}")

    //String dynamicParam = readFile("${WORKSPACE}/henosis_devops/cac/mtl-ps/win/bios/gio/dynamic_param.json").replaceAll('OS_IMAGE_PATH_url',"${OS_IMAGE_PATH_url}").replaceAll('ifwi_image_path_release',"${ifwi_image_path_release}").replaceAll('ifwi_image_path_debug',"${ifwi_image_path_debug}").replaceAll('latest_tag',"${latest_tag}")
    String dynamicParam = readFile("${WORKSPACE}/henosis_devops/arls_gio/dynamic_param.json").replaceAll('OS_IMAGE_PATH_url',"${OS_IMAGE_PATH_url}").replaceAll('ifwi_image_path_release',"${ifwi_image_path_release}").replaceAll('ifwi_image_path_debug',"${ifwi_image_path_debug}").replaceAll('latest_tag',"${latest_tag}")
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
    getosimage_path()
    create_dynamicJson(release_binFiles, debug_binFiles, image_binFiles)
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
        ARTIFACTORY_IMAGE_REPO = "hspe-edge-png-local"
    }
    options {
       timestamps()
       buildDiscarder(logRotator(numToKeepStr: '90', artifactDaysToKeepStr: '30'))
       skipDefaultCheckout()
    }
    parameters {
        booleanParam(name: 'CLEANWS', defaultValue: true, description: 'Clean workspace')
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
                userRemoteConfigs: [[credentialsId: 'GitHub-Token', url: 'https://github.com/Kalla-Udaykumar/jenkins-script.setup.git']], // https://github.com/intel-innersource/libraries.devops.jenkins.cac.git'
                branches: [[name: "master"]],
                extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'henosis_devops'],
                [$class: 'ScmName', name: 'henosis_devops'],
                [$class: 'CleanBeforeCheckout']]])
                
                checkout([$class: 'GitSCM',
                userRemoteConfigs: [[credentialsId: 'GitHub-Token', url: 'https://github.com/intel-innersource/os.linux.ubuntu.iot.utilities.custom-image-creator-config']],
                branches: [[name: "main"]],
                extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'tag'],
                [$class: 'ScmName', name: 'tag'],
                [$class: 'CleanBeforeCheckout']]])
            }
        }
        stage('GIO triggering') {
            steps {
                getConfig()
                dir('tag') {
                    script {
                        echo "TAG is: ${TAG}"
                        
                        // Get the latest tag that matches the 'meteor-lake/*', 'MTL-PS*', or 'meteorlake-*' patterns
                        def patterns = TAG.tokenize("' '").collect { it.replaceAll("'", "") }
                        def latestTag = sh(script: "git tag -l ${patterns.join(' ')} | sort -V | tail -n 1", returnStdout: true).trim()
                        // Print out the latest tag
                        echo "Latest tag: ${latestTag}"
                        // Save the latest tag to a file in the workspace
                        writeFile file: "${env.WORKSPACE}/latest_tag.txt", text: latestTag
                        
                        fullGIO(gio_cmd)
                    }
                }
            }
        }
    }
}
