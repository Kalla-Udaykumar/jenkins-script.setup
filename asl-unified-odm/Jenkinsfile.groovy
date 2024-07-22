#!groovy
@Library('abi@2.7.0') _

import owm.common.BuildInfo
Map buildinfo = BuildInfo.instance.data

/**
  * ASL WIN IFWI Project
  *
**/
email_receipients = "vamsi.mutra@intel.com"
subject = '$DEFAULT_SUBJECT'
body = '${SCRIPT, template="managed:abi.html"}'

def create_directories() {
    dir("${WORKSPACE}/abi"){
        bat """     
        mkdir ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\INPUT\\BASE_IFWI
        mkdir ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\INPUT\\IOTG_BIOS
        mkdir ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\INPUT\\Tool
        mkdir ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\INPUT\\IOTG_FW
        mkdir ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\INPUT\\PMC_FW
        mkdir ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\INPUT\\CSINIT_FW
        mkdir ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\INPUT\\PCHC_FW
        mkdir ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\INPUT\\IPU_FW
        mkdir ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\INPUT\\STATIC\\FW_Component
        mkdir ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\INPUT\\STATIC\\PMC_Component
        mkdir ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\INPUT\\STATIC\\PCHC_Component
        mkdir ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\INPUT\\STATIC\\CSINIT_Component
        mkdir ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\INPUT\\STATIC\\IFWI
        mkdir ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\INPUT\\STATIC\\IFWI_UNZIP
    
        mkdir ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\OUTPUT\\IOTG_XML
        mkdir ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\OUTPUT\\IOTG_IFWI\\External\\NR02\\CRB_DDR5
        mkdir ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\OUTPUT\\IOTG_IFWI\\External\\NR02\\RVP_DDR5
        mkdir ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\OUTPUT\\IOTG_IFWI\\Internal\\NR01
        mkdir ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\OUTPUT\\IOTG_IFWI\\Internal\\NR02\\CRB_DDR5
        mkdir ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\OUTPUT\\IOTG_IFWI\\Internal\\NR02\\RVP_DDR5
        mkdir ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\OUTPUT\\IOTG_IFWI\\Internal\\NR03
                
        mkdir ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\Logs
        """
    }
}

pipeline {
    agent {
        node {
            label 'WIN-CONTAINER'
            customWorkspace 'C:\\asladlnunified'
        }
    }

    environment {
        DATE = new Date().format("yyyy-MM-dd");
        TIME = new Date().format("hhmmss");
        WORKWEEK = new Date().format("ww")
        DAY = new Date().format("u")
        CREDS = credentials('BuildAutomation')
        WORKING_DIR="${WORKSPACE}\\abi\\ifwi\\HSPE-SWS-SID-PLUTO"
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
        string(name: 'IFWI_VERSION', defaultValue: 'adl_n_win11_mr_blue_ifwi_2022_ww52_4_01', description: 'Provide ifwi_version like ADL_N_ES1_BLUE_IFWI_2022_WW13_5_02')
        string(name: 'BIOS_VERSION', defaultValue: '4424_00', description: 'Provide BIOS_VERSION like 4424_00')
        string(name: 'CSME_VERSION', defaultValue: '16.50.12.1453v2', description: 'Provide CSME_VERSION like 16.50.10.1351v7')
        string(name: 'PMC_VERSION', defaultValue: 'ADPN_A0_PMC_FW_160.50.00.1010', description: 'Provide PMC_VERSION like ADPN_A0_PMC_FW_160.50.00.1010')
        string(name: 'CSINIT_VERSION', defaultValue: 'V4', description: 'provide CSINIT_VERSION like V4')
        string(name: 'PCHC_VERSION', defaultValue: '16.50.0.1014', description: 'Provide PCHC_VERSION like 16.50.0.1012')
        string(name: 'IPU_VERSION', defaultValue: '63.22000.3.14762-adl-n-hdmi-prod-win10', description: 'provide IPU_VERSION like 63.22000.3.12713-rpl-p-hdmi-prod-win10')
        string(name: 'ISH_VERSION', defaultValue: '5.4.2.4595v3', description: 'Provide CSME_VERSION like 16.50.10.1351v7')
        string(name: 'ISH_FILE', defaultValue: '5.4.2.4595v3', description: 'provide ISH zip file name without .zip at the end')
    }

    stages {
        stage("CLEAN") {
            when {
                expression { params.CLEAN == true }
            }
            steps {
                script {
                    abi_workspace_clean deleteDirs: true 
                }
            }
        }

        stage("SCM") {
            agent {
                docker {
                    image "${DOCKER}"
                    args '--entrypoint= '
                    reuseNode true
                }
            }
            steps {
                /*checkout([$class: 'GitSCM',
                userRemoteConfigs: [[credentialsId: 'GitHub-Token', url: 'https://github.com/intel-innersource/libraries.devops.jenkins.cac.git']],
                branches: [[name: 'master']],
                extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'abi/henosis'],
                [$class: 'ScmName', name: 'henosis'],
                [$class: 'CleanBeforeCheckout'],
                [$class: 'CloneOption', timeout: 60],
                [$class: 'CheckoutOption', timeout: 60]]])*/
                
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
                extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'abi/ifwi'],
                [$class: 'ScmName', name: 'ifwi'],
                [$class: 'CloneOption', timeout: 60],
                [$class: 'CleanBeforeCheckout'], 
                [$class: 'CheckoutOption', timeout: 60]]])
            }
        }

        stage("ABI"){
            agent {
                docker {
                    image "${DOCKER}"
                    args '--entrypoint= '
                    reuseNode true
                }
            }   // abi\\henosis\\asl-unified-odm\\idf   abi\\henosis\\cac\\asl\\win\\ifwi\\docker\\idf
            steps {
                script {
                    bat """ xcopy /E /I ${WORKSPACE}\\abi\\henosis\\asl-unified-odm\\idf ${WORKSPACE}\\abi\\idf """
                    //bat """ mkdir ${WORKSPACE}\\abi\\OWRBin """
                    PrepareWS()
                } 
            }
        }
        stage('COPY: ROM') {
            steps {
                script {
                    create_directories()
                        dir("${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\Builder"){
                            bat """   
                            dir \\\\amr.corp.intel.com\\ec\\proj\\IOTG\\Releases\\BIOS\\Amstonlake\\Internal | grep _ | awk "{print \$5}" | grep -v [A-Z] | tail -1 > bios_version_internal.txt
                            FOR /F %%i IN (bios_version_internal.txt) DO (
                                set BIOS_VERSION_INTERNAL=%%i )
                                set BIOS_VERSION_INTERNAL=%BIOS_VERSION_INTERNAL: =%
                        
                            dir \\\\amr.corp.intel.com\\ec\\proj\\IOTG\\Releases\\BIOS\\Amstonlake\\External | grep _ | awk "{print \$5}" | grep -v [A-Z] | tail -1 > bios_version_external.txt
                            FOR /F %%i IN (bios_version_external.txt) DO (
                                set BIOS_VERSION_EXTERNAL=%%i )
                                set BIOS_VERSION_EXTERNAL=%BIOS_VERSION_EXTERNAL: =%
                    
                            copy \\\\amr.corp.intel.com\\ec\\proj\\IOTG\\Releases\\BIOS\\Amstonlake\\Internal\\%BIOS_VERSION_INTERNAL%\\ROM\\ADL_N_D_IntFspWrapperVs_%BIOS_VERSION_INTERNAL%.rom ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\INPUT\\IOTG_BIOS\\
                            copy \\\\amr.corp.intel.com\\ec\\proj\\IOTG\\Releases\\BIOS\\Amstonlake\\Internal\\%BIOS_VERSION_INTERNAL%\\ROM\\ADL_N_R_IntFspWrapperVs_%BIOS_VERSION_INTERNAL%.rom ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\INPUT\\IOTG_BIOS\\
                            copy \\\\amr.corp.intel.com\\ec\\proj\\IOTG\\Releases\\BIOS\\Amstonlake\\Internal\\%BIOS_VERSION_INTERNAL%\\ROM\\Prod_ADL_N_R_IntFspWrapperVs_%BIOS_VERSION_INTERNAL%.rom ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\INPUT\\IOTG_BIOS\\
                            copy \\\\amr.corp.intel.com\\ec\\proj\\IOTG\\Releases\\BIOS\\Amstonlake\\Internal\\%BIOS_VERSION_INTERNAL%\\ROM\\Prod_ADL_N_D_IntFspWrapperVs_%BIOS_VERSION_INTERNAL%.rom ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\INPUT\\IOTG_BIOS\\
                  
            
                            copy \\\\amr.corp.intel.com\\ec\\proj\\IOTG\\Releases\\BIOS\\Amstonlake\\External\\%BIOS_VERSION_EXTERNAL%\\ROM\\ADL_N_D_ExtFspWrapperVs_%BIOS_VERSION_EXTERNAL%.rom ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\INPUT\\IOTG_BIOS\\
                            copy \\\\amr.corp.intel.com\\ec\\proj\\IOTG\\Releases\\BIOS\\Amstonlake\\External\\%BIOS_VERSION_EXTERNAL%\\ROM\\ADL_N_R_ExtFspWrapperVs_%BIOS_VERSION_EXTERNAL%.rom ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\INPUT\\IOTG_BIOS\\
                            copy \\\\amr.corp.intel.com\\ec\\proj\\IOTG\\Releases\\BIOS\\Amstonlake\\External\\%BIOS_VERSION_EXTERNAL%\\ROM\\Prod_ADL_N_D_ExtFspWrapperVs_%BIOS_VERSION_EXTERNAL%.rom ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\INPUT\\IOTG_BIOS\\
                            copy \\\\amr.corp.intel.com\\ec\\proj\\IOTG\\Releases\\BIOS\\Amstonlake\\External\\%BIOS_VERSION_EXTERNAL%\\ROM\\Prod_ADL_N_R_ExtFspWrapperVs_%BIOS_VERSION_EXTERNAL%.rom ${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\INPUT\\IOTG_BIOS\\
                            
                            copy \\\\amr.corp.intel.com\\ec\\proj\\IOTG\\Releases\\BIOS\\Amstonlake\\Internal\\%BIOS_VERSION_INTERNAL%\\BIOS_Manifest.json ${WORKSPACE}\\abi\\
                            
                            """
                        }
                }
            }
        }

        stage ('download') {
            steps {
                script {
                    def artServer = Artifactory.server "af01p-png.devtools.intel.com"
                    def artFiles  = """ {
                        "files": [
                            {
                                "pattern": "hpse-adl-png-local/bios-adl/${JOB_BASE_NAME}/*/reports/ifwi_version.txt",
                                "target": "download/",
                                "flat": "true",
                                "recursive": "true",
                                "sortBy": ["modified"],
                                "sortOrder": "desc",
                                "limit" : 1
                            }
                            ,
                            {
                                "pattern": "hpse-adl-png-local/bios-adl/${JOB_BASE_NAME}/*/reports/bios_version.txt",
                                "target": "download/",
                                "flat": "true",
                                "recursive": "true",
                                "sortBy": ["modified"],
                                "sortOrder": "desc",
                                "limit" : 1
                            }
                            ,
                            {
                                "pattern": "hpse-adl-png-local/bios-adl/${JOB_BASE_NAME}/*/reports/csme_version.txt",
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
                    
                    def artServer2 = Artifactory.server "ubit-artifactory-or.intel.com"
                    def artFiles2  = """ {
                        "files": [
                            {
                                "pattern": "owr-repos/Submissions/csme/16.50.5*/CSxE_ADL_ALL_*_Release.json",
                                "target": "download/latest_csme.json",
                                "flat": "true",
                                "recursive": "true",
                                "sortBy": ["modified"],
                                "sortOrder": "desc",
                                "limit" : 1
                            }
                        ]
                    }"""
                    artServer2.download spec: artFiles2
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
                    abi_build subComponentName: "ASLWinIfwiDwnldApp"
                }
            }
        }

        stage ('Firmware Download') {
            steps {
                script {
                    def artServer = Artifactory.server "ubit-artifactory-or.intel.com"
                    def artFiles  = """ {
                        "files": [
                            {
                                "pattern": "owr-repos/Submissions/csme/${CSME_VERSION}/CSxE_ADL_ALL_${CSME_VERSION}_Release.zip",
                                "target": "download/",
                                "flat": "true",
                                "recursive": "true",
                                "limit" : 1
                            }
                            ,
                            {
                                "pattern": "owr-repos/Submissions/pmc/adpn/${PMC_VERSION}/${PMC_VERSION}.zip",
                                "target": "download/",
                                "flat": "true",
                                "recursive": "true",
                                "limit" : 1
                            }
                            ,
                            {
                                "pattern": "owr-repos/Submissions/pchc/adpn/${PCHC_VERSION}/PCHC_ADP_N_ALL_${PCHC_VERSION}_Release.zip",
                                "target": "download/",
                                "flat": "true",
                                "recursive": "true",
                                "limit" : 1
                            }
                            ,
                            {
                                "pattern": "one-windows-local/Submissions/chipsetinit/ADPN/A0/${CSINIT_VERSION}/*.zip",
                                "target": "download/",
                                "flat": "true",
                                "recursive": "true",
                                "limit" : 1
                            }
                        ]
                    }"""
                    artServer.download spec: artFiles
                        bat """
                            mkdir ${WORKSPACE}/FIRMWARE
                            unzip ${WORKSPACE}/download/CSxE_ADL_ALL_${CSME_VERSION}_Release.zip -d ${WORKSPACE}/FIRMWARE
                            unzip ${WORKSPACE}/download/${PMC_VERSION}.zip -d ${WORKSPACE}/FIRMWARE
                            unzip ${WORKSPACE}/download/AdpNA0V4ChipsetInit.zip -d ${WORKSPACE}/FIRMWARE
                            unzip ${WORKSPACE}/download/PCHC_ADP_N_ALL_${PCHC_VERSION}_Release.zip -d ${WORKSPACE}/FIRMWARE
                
                            xcopy /Y /S ${WORKSPACE}\\FIRMWARE\\External\\FW\\Consumer\\Images\\Silicon\\N\\A0\\cse_image.bin ${WORKSPACE}\\abi\\ifwi\\HSPE-SWS-SID-PLUTO\\IFWI_Automation\\ASL\\INPUT\\IOTG_FW\\
                            xcopy /Y /S ${WORKSPACE}\\FIRMWARE\\External\\FW\\Consumer\\Images\\Silicon\\N\\A0\\cse_image_production.bin ${WORKSPACE}\\abi\\ifwi\\HSPE-SWS-SID-PLUTO\\IFWI_Automation\\ASL\\INPUT\\IOTG_FW\\
                            xcopy /Y /S ${WORKSPACE}\\FIRMWARE\\External\\Tools\\System_Tools\\MFIT\\Windows32 ${WORKSPACE}\\\\External\\Tools\\System_Tools\\MFIT\\
                            xcopy /Y /S ${WORKSPACE}\\FIRMWARE\\${PMC_VERSION}.pmcp_prod_pv.bin ${WORKSPACE}\\abi\\ifwi\\HSPE-SWS-SID-PLUTO\\IFWI_Automation\\ASL\\INPUT\\PMC_FW\\
                            xcopy /Y /S ${WORKSPACE}\\FIRMWARE\\collateral_output\\AdpNPchChipsetInitA0V4.bin ${WORKSPACE}\\abi\\ifwi\\HSPE-SWS-SID-PLUTO\\IFWI_Automation\\ASL\\INPUT\\CSINIT_FW\\
                            xcopy /Y /S ${WORKSPACE}\\FIRMWARE\\PCHC_ADP_N_ALL_${PCHC_VERSION}_Release_Prod_PV1.bin ${WORKSPACE}\\abi\\ifwi\\HSPE-SWS-SID-PLUTO\\IFWI_Automation\\ASL\\INPUT\\PCHC_FW\\
                            """
                    def artServer2 = Artifactory.server "ubit-artifactory-sh.intel.com"
                    def artFiles2  = """ {
                        "files": [
                            {
                                "pattern": "one-windows-local/Submissions/Camera/${IPU_VERSION}/Camera-${IPU_VERSION}.zip",
                                "target": "download/",
                                "flat": "true",
                                "recursive": "true",
                                "limit" : 1
                            }
                        ]
                    }"""
                    artServer2.download spec: artFiles2
                        bat """
                            unzip ${WORKSPACE}/download/Camera-${IPU_VERSION}.zip -d ${WORKSPACE}/FIRMWARE
                            xcopy /Y /S ${WORKSPACE}\\FIRMWARE\\Drivers\\x64\\Bootloader\\cpd_btldr_signed_adln.bin ${WORKSPACE}\\abi\\ifwi\\HSPE-SWS-SID-PLUTO\\IFWI_Automation\\ASL\\INPUT\\IPU_FW\\
                        """
                    def artServer3 = Artifactory.server "ubit-artifactory-ba.intel.com"
                    def artFiles3  = """ {
                        "files": [
                            {
                                "pattern": "one-windows-local/Submissions/ish/${ISH_VERSION}/${ISH_FILE}.zip",
                                "target": "download/",
                                "flat": "true",
                                "recursive": "true",
                                "sortBy": ["modified"],
                                "sortOrder": "desc",
                                "limit" : 1
                            }
                        ]
                    }"""
                    artServer3.download spec: artFiles3
                        bat """ 
                            unzip ${WORKSPACE}/download/${ISH_FILE}.zip -d ${WORKSPACE}/FIRMWARE
                            xcopy /Y /S ${WORKSPACE}\\FIRMWARE\\FW\\bin\\ish_fw_images\\Production_ishC_*_ADL-N.bin  ${WORKSPACE}\\abi\\ifwi\\HSPE-SWS-SID-PLUTO\\IFWI_Automation\\ASL\\INPUT\\ISH_FW\\
                        """    
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
    			 abi_build subComponentName: "ASLWinIfwiBuildApp"
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
                        abi_artifact_deploy custom_file: "${WORKSPACE}/abi/reports/", custom_deploy_path: "hpse-adl-png-local/bios-adl/${JOB_BASE_NAME}/${env.DATETIME}-${BUILD_NUMBER}/reports/", custom_artifactory_url: "https://af01p-png.devtools.intel.com", additional_props: "retention.days=3", cred_id: 'BuildAutomation'
                }
            }
        }
    }

    post {
        success {
            script {
                echo "JOB STATUS - SUCCESS"
                def BUILD_PATH = null
                
                dir("${WORKING_DIR}\\IFWI_Automation\\Unified_IFWI\\ASL\\OUTPUT\\IOTG_IFWI\\Internal\\"){
                    def files = findFiles()
                    files.each{ f ->
                        if(f.directory && f.name.contains("ASL")) {
                            BUILD_PATH = files[0].name
                        }
                    } 
                }
                echo "BUILD_PATH: ${BUILD_PATH}"
                ARTIFACTORY_PATH = "ASL/Engineering/2024/Internal/${BUILD_PATH}/NR02/RVP_DDR5"
                ARTIFACTORY_EXTERNAL = "ASL/Engineering/2024/External/${BUILD_PATH}/NR02/CRB_DDR5"
                CRB_ARTIFACTORY_PATH = "ASL/Engineering/2024/Internal/${BUILD_PATH}/NR02/CRB_DDR5"
                JSON_ARTIFACTORY_PATH = "ASL/Engineering/2024/Internal/${BUILD_PATH}"
                
                ALL_IFWI_ARTIFACTORY_PATH = "https://af01p-png.devtools.intel.com/artifactory/hspe-iotgfw-adl-png-local/ASL/Engineering/2024/Internal/${BUILD_PATH}/"
                ARTIFACTORY_PATH = "ASL/Engineering/2024/Internal/${BUILD_PATH}/"
                ARTIFACTORY_EXTERNAL = "ASL/Engineering/2024/External/${BUILD_PATH}/"
                if (BUILD_PATH != null) {	
                    final job3Result = build job: "ASL-WIN-IFWI.ALL-VAL-DLY-GIO", parameters: [string(name: 'ARTIFACTORY_PATH', value: "${ARTIFACTORY_PATH}"), string(name: 'ARTIFACTORY_EXTERNAL', value: "${ARTIFACTORY_EXTERNAL}")],wait: true
                    echo "Downstream GIO Job3 URL: https://cbjenkins-pg.devtools.intel.com/teams-iotgdevops01/job/iotgdevops01/job/ASL-WIN-IFWI.ALL-VAL-DLY-GIO/${job3Result.number}"
                    final job3Result2 = build job: "ADLN-WIN-IFWI.ALL-VAL-DLY-GIO", parameters: [string(name: 'ARTIFACTORY_PATH', value: "${ARTIFACTORY_PATH}"), string(name: 'ARTIFACTORY_EXTERNAL', value: "${ARTIFACTORY_EXTERNAL}")],wait: true
                    echo "Downstream GIO Job3 URL: https://cbjenkins-pg.devtools.intel.com/teams-iotgdevops01/job/iotgdevops01/job/ADLN-WIN-IFWI.ALL-VAL-DLY-GIO/${job3Result2.number}"
                }
            }
        }
        always {
            script {
                echo "JOB COMPLETED!!!"
                echo "Logparser code removed!"

                if (params.EMAIL == true) {
                    abi_send_email.SendEmail("${email_receipients}","${body}","${subject}")
                }
                intelLogstashSend(failBuild: false, verbose: true)
            }
        }
        failure {
            echo "JOB STATUS - FAILURE"
        }
    }
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
