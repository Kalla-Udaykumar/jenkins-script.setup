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
                    PrepareWS()
                } 
            }
        }
        
        stage('COPY: ROM') {
            steps {
                script {
                    create_directories()
                    dir("${WORKING_DIR}\\IFWI_Automation\\Builder"){
                        bat """   
                        copy \\\\amr.corp.intel.com\\ec\\proj\\IOTG\\Releases\\BIOS\\MeteorLake\\ARL-H\\${params.BIOS_VERSION}\\ROM\\**.rom ${WORKING_DIR}\\IFWI_Automation\\ARL\\ARL_H\\INPUT\\IOTG_BIOS\\

                        copy \\\\amr.corp.intel.com\\ec\\proj\\IOTG\\Releases\\BIOS\\MeteorLake\\ARL-H\\${params.BIOS_VERSION}\\BIOS_Manifest.json ${WORKSPACE}\\abi\\
                        """
                    }
                }
            }
        }
        stage('Build') {
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
