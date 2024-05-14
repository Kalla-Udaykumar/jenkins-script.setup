#!groovy
@Library('abi@2.7.0') _
import java.text.SimpleDateFormat
import owm.common.BuildInfo
Map buildinfo = BuildInfo.instance.data

/**
  * ARLH WIN IFWI Project
  *
**/
email_receipients = "vamsi.mutra@intel.com,abdul.r.nalband@intel.com,nithyaraj.j@intel.com,veerabhadra.sheelavant@intel.com"
subject = '$DEFAULT_SUBJECT'
body = '${SCRIPT, template="managed:abi.html"}'

def create_directories() {
    dir("${WORKSPACE}/abi"){
        bat """     
        mkdir ${WORKING_DIR}\\IFWI_Automation\\ARL\\ARL_H\\INPUT\\BASE_IFWI
        mkdir ${WORKING_DIR}\\IFWI_Automation\\ARL\\ARL_H\\INPUT\\IOTG_BIOS
        mkdir ${WORKING_DIR}\\IFWI_Automation\\ARL\\ARL_H\\INPUT\\Tool\\CCG_mFIT
        mkdir ${WORKING_DIR}\\IFWI_Automation\\ARL\\ARL_H\\INPUT\\Tool\\CSME_mFIT
        mkdir ${WORKING_DIR}\\IFWI_Automation\\ARL\\ARL_H\\INPUT\\STATIC\\IFWI
        mkdir ${WORKING_DIR}\\IFWI_Automation\\ARL\\ARL_H\\INPUT\\STATIC\\IFWI_UNZIP
        mkdir ${WORKING_DIR}\\IFWI_Automation\\ARL\\ARL_H\\INPUT\\STATIC\\FW_Component
        mkdir ${WORKING_DIR}\\IFWI_Automation\\ARL\\ARL_H\\INPUT\\Consumer_FW
        mkdir ${WORKING_DIR}\\IFWI_Automation\\ARL\\ARL_H\\INPUT\\Corporate_FW
        mkdir ${WORKING_DIR}\\IFWI_Automation\\ARL\\ARL_H\\OUTPUT\\IOTG_XML
        mkdir ${WORKING_DIR}\\IFWI_Automation\\ARL\\ARL_H\\OUTPUT\\IOTG_IFWI       
        mkdir ${WORKING_DIR}\\IFWI_Automation\\Logs
        """
    }
}
pipeline {
    agent {
        node {
            label 'WIN-CONTAINER'
            customWorkspace 'C:\\abi_jobs\\arlhifwi'
        }
    }
    environment {
        DATE = new Date().format("yyyy-MM-dd");
        TIME = new Date().format("hhmmss");
        WORKWEEK = new Date().format("ww")
        DAY = new Date().format("u")
        CREDS = credentials('BuildAutomation')
        WORKING_DIR="${WORKSPACE}\\abi\\arlhifwi\\HSPE-SWS-SID-PLUTO"
        DOCKER = "amr-registry.caas.intel.com/esc-devops/plat/adlps/win/ifwi/windows10/abi:202206301400"
        BuildVersion = "1.0.000"
        ABI_CONTAINER = "TRUE"
    } 
    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '90', artifactDaysToKeepStr: '30'))
        skipDefaultCheckout()
    }
    parameters {
        booleanParam(name: 'CLEAN', defaultValue: true, description: 'Clean workspace')
        booleanParam(name: 'EMAIL', defaultValue: true, description: 'Email notification upon job completion')
       // string(name: 'BIOS_VERSION', description: 'bios versin')
        //string(name: 'IFWI_VERSION', description: 'ifwi versin')
    }
    stages {
        stage ('CLEAN') {
            when {
                expression { params.CLEAN == true }
            }
            steps {
                script {
                    abi_workspace_clean deleteDirs: true 
                }
            }
        }
        stage('SCM') {
            agent {
                docker {
                    image "${DOCKER}"
                    args '--entrypoint= '
                    reuseNode true
                }
            }
            steps {  
                checkout([$class: 'GitSCM',
                userRemoteConfigs: [[credentialsId: 'GitHub-Token', url: 'https://github.com/Kalla-Udaykumar/jenkins-script.setup.git']],
                branches: [[name: 'master']],
                extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'abi/henosis'],
                [$class: 'ScmName', name: 'henosis'],
                [$class: 'CleanBeforeCheckout'],
                [$class: 'CloneOption', timeout: 60],
                [$class: 'CheckoutOption', timeout: 60]]]) 
                
                checkout changelog: false, scm: ([$class: 'GitSCM',
                userRemoteConfigs: [[credentialsId: 'GitHub-Token', url: 'https://github.com/intel-innersource/frameworks.automation.helios.pluto.git']],
                branches: [[name: 'main']],
                extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'abi/arlhifwi'],
                [$class: 'ScmName', name: 'arlhifwi'],
                [$class: 'CloneOption', timeout: 60],
                [$class: 'CleanBeforeCheckout'], 
                [$class: 'CheckoutOption', timeout: 60]]])
                                
            }
        }
        stage('ABI') {
            agent {
                docker {
                    image "${DOCKER}"
                    args '--entrypoint= '
                    reuseNode true
                }
            }
            steps {
                script {
                    bat """ xcopy /E /I ${WORKSPACE}\\abi\\henosis\\arhl-param-setup\\idf ${WORKSPACE}\\abi\\idf """
                    //bat """ mkdir ${WORKSPACE}\\abi\\OWRBin """
		    // \\arhl-param-setup\\idf
                    PrepareWS()
                } 
            }
        }
        stage ('DOWNLOAD') {
            steps {
                script {
                    def artServer = Artifactory.server "af01p-png.devtools.intel.com"
                    def artFiles  = """ {
                        "files": [
                            {
                                "pattern": "hspe-iotgfw-adl-png-local/ARL-H/Engineering/2024/Internal/ARL_H_WW12.3.020649/Payload-Details/ARL_H_Payload_Versions.json",
                                "target": "download/",
                                "flat": "true",
                                "recursive": "true",
                                "sortBy": ["modified"],
                                "sortOrder": "desc",
                                "limit" : 1
                            },
                            {
                                "pattern": "hpse-adl-png-local/bios-adl/${JOB_BASE_NAME}/*/reports/bios_version.txt",
                                "target": "download/",
                                "flat": "true",
                                "recursive": "true",
                                "sortBy": ["modified"],
                                "sortOrder": "desc",
                                "limit" : 1
                            },
                            {
                                "pattern": "hpse-adl-png-local/bios-adl/${JOB_BASE_NAME}/*/reports/ifwi_version.txt",
                                "target": "download/",
                                "flat": "true",
                                "recursive": "true",
                                "sortBy": ["modified"],
                                "sortOrder": "desc",
                                "limit" : 1
                            }
                        ]
                    }"""
                    artServer.download spec: artFiles
                }
            }
        }
        stage('COPY: ROM') {
            steps {
                script {
                    create_directories()
                    dir("${WORKING_DIR}\\IFWI_Automation\\Builder"){
                        bat """   
                         dir \\\\amr.corp.intel.com\\ec\\proj\\IOTG\\Releases\\BIOS\\MeteorLake\\ARL-H | grep _ | awk "{print \$5}" | grep -v [a-z] | grep -v [A-Z] | tail -1 > bios_version.txt
                        FOR /F %%i IN (bios_version.txt) DO (
                            set BIOS_VERSION=%%i )
                        set BIOS_VERSION=%BIOS_VERSION: =%

                        copy \\\\amr.corp.intel.com\\ec\\proj\\IOTG\\Releases\\BIOS\\MeteorLake\\ARL-H\\4134_42\\ROM\\pre_prod\\*.rom ${WORKING_DIR}\\IFWI_Automation\\ARL\\ARL_H\\INPUT\\IOTG_BIOS\\

                        copy \\\\amr.corp.intel.com\\ec\\proj\\IOTG\\Releases\\BIOS\\MeteorLake\\ARL-H\\4134_42\\BIOS_Manifest.json ${WORKSPACE}\\abi\\
                        """
                    }
                }
            }
        }
        stage('Build:Ifwi App download') {
            agent {
                docker {
                    image "${DOCKER}"
                    args '--entrypoint= '
                    reuseNode true
                }
            }
            steps {
                script {
                    BuildInfo.instance.data["Version"] = env.BuildVersion
                    abi_build subComponentName: "ARLHWinIfwiDwnldApp"
                }
            }
        }
        stage('Build:Ifwi App build') {
            agent {
                docker {
                    image "${DOCKER}"
                    args '--entrypoint= '
                    reuseNode true
                }
            }
            steps {
                script {
    			BuildInfo.instance.data["Version"] = env.BuildVersion
    			abi_build subComponentName: "ARLHWinIfwiBuildApp"
                }
            }
        }
        stage("Artifacts-Deploy") {
            agent {
                docker {
                    image "${DOCKER}"
                    args '--entrypoint= -v C:/ABI:C:/OWR/Tools'
                    reuseNode true
                }
            }
            steps {
                script {
                    abi_artifact_deploy custom_file: "${WORKSPACE}/abi/reports/", custom_deploy_path: "hpse-adl-png-local/bios-adl/${JOB_BASE_NAME}/${env.DATETIME}-${BUILD_NUMBER}/reports/", custom_artifactory_url: "https://af01p-png.devtools.intel.com", additional_props: "retention.days=365", cred_id: 'BuildAutomation'
                }
            }
        }
    }
    /*post {
        success {
            script {
                 echo "JOB STATUS - SUCCESS"
            }
        }
        always {
            script {
                echo "JOB COMPLETED!!!"
                // To trigger Log Parser build to push Build log to Splunk Server.
                build job: 'iotgdevops01/ADM-LOG_PARSER',
                parameters: [ stringParam(name: 'JOB_RESULT', value: "${currentBuild.result}"),
                stringParam(name: 'BUILD_URL', value: "${env.BUILD_URL}"), booleanParam(name: 'SPLUNK', value: true)
                ], wait: false, propagate: false
                
                if (params.EMAIL == true) {
                    abi_send_email.SendEmail("${email_receipients}","${body}","${subject}")
                }
            }
        }
        failure {
            echo "JOB STATUS - FAILURE"
        }
    }*/
}

// Prepare the workspace for the ingredient
void PrepareWS(String BuildConfig="${WORKSPACE}/abi/idf/BuildConfig.json") {
    log.Debug("Enter")

    log.Info("This build is running on Node:${env.NODE_NAME} WorkSpace: ${env.WORKSPACE}")

    abi_setup_proxy()

    abi_init config: BuildConfig, ingPath: "abi", checkoutPath: "abi", skipCheckout: true

	def ctx
	ctx = abi_get_current_context()
	ctx['IngredientVersion'] = env.BuildVersion
	abi_set_current_context(ctx)

    log.Debug("Exit")
}
