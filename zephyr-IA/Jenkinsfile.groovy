#!groovy

// cp -r "${WORKSPACE}"/zephyr/drivers/serial/uart_ns16550.h "${WORKSPACE}"/Protex_IA/zephyr/drivers/serial/

def Protex_IA_Files() {
    dir("${WORKSPACE}") {
	sh """#!/bin/bash -xe
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/drivers/tgpio
	cp -r "${WORKSPACE}"/zephyr/drivers/misc/timeaware_gpio/ "${WORKSPACE}"/Protex_IA/zephyr/drivers/tgpio/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/build/modules/acpica
	cp -r "${WORKSPACE}"/zephyr/build/modules/acpica/ "${WORKSPACE}"/Protex_IA/zephyr/build/modules/acpica/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/arch/x86
	cp -r "${WORKSPACE}"/zephyr/arch/x86/ "${WORKSPACE}"/Protex_IA/zephyr/arch/x86/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/soc/intel
	cp -r "${WORKSPACE}"/zephyr/soc/intel/ "${WORKSPACE}"/Protex_IA/zephyr/soc/intel/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/boards/intel
	cp -r "${WORKSPACE}"/zephyr/boards/intel/ "${WORKSPACE}"/Protex_IA/zephyr/boards/intel/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/lib/acpi
	cp -r "${WORKSPACE}"/zephyr/lib/acpi/ "${WORKSPACE}"/Protex_IA/zephyr/lib/acpi/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/include/zephyr/acpi
	cp -r "${WORKSPACE}"/zephyr/include/zephyr/acpi/ "${WORKSPACE}"/Protex_IA/zephyr/include/zephyr/acpi/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/drivers/spi
	cp -r "${WORKSPACE}"/zephyr/drivers/spi/spi_pw.c "${WORKSPACE}"/Protex_IA/zephyr/drivers/spi/
	cp -r "${WORKSPACE}"/zephyr/drivers/spi/spi_pw.h "${WORKSPACE}"/Protex_IA/zephyr/drivers/spi/
	cp -r "${WORKSPACE}"/zephyr/drivers/spi/Kconfig.pw "${WORKSPACE}"/Protex_IA/zephyr/drivers/spi/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/tests/drivers/spi/spi_loopback/boards
	cp -r "${WORKSPACE}"/zephyr/tests/drivers/spi/spi_loopback/boards/intel_* "${WORKSPACE}"/Protex_IA/zephyr/tests/drivers/spi/spi_loopback/boards/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/drivers/serial
	cp -r "${WORKSPACE}"/zephyr/drivers/serial/uart_ns16550.c "${WORKSPACE}"/Protex_IA/zephyr/drivers/serial/
	cp -r "${WORKSPACE}"/zephyr/drivers/serial/Kconfig.ns16550 "${WORKSPACE}"/Protex_IA/zephyr/drivers/serial/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/drivers/gpio
	cp -r "${WORKSPACE}"/zephyr/drivers/gpio/gpio_intel.c "${WORKSPACE}"/Protex_IA/zephyr/drivers/gpio/
	cp -r "${WORKSPACE}"/zephyr/drivers/gpio/Kconfig.intel "${WORKSPACE}"/Protex_IA/zephyr/drivers/gpio/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/tests/drivers/gpio/gpio_basic_api/boards
	cp -r "${WORKSPACE}"/zephyr/tests/drivers/gpio/gpio_basic_api/boards/intel_* "${WORKSPACE}"/Protex_IA/zephyr/tests/drivers/gpio/gpio_basic_api/boards/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/drivers/misc/timeaware_gpio
	cp -r "${WORKSPACE}"/zephyr/drivers/misc/timeaware_gpio/ "${WORKSPACE}"/Protex_IA/zephyr/drivers/misc/timeaware_gpio/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/include/zephyr/drivers/misc/timeaware_gpio
	cp -r "${WORKSPACE}"/zephyr/include/zephyr/drivers/misc/timeaware_gpio/ "${WORKSPACE}"/Protex_IA/zephyr/include/zephyr/drivers/misc/timeaware_gpio/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/samples/drivers/misc/timeaware_gpio
	cp -r "${WORKSPACE}"/zephyr/samples/drivers/misc/timeaware_gpio/ "${WORKSPACE}"/Protex_IA/zephyr/samples/drivers/misc/timeaware_gpio/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/drivers/smbus
	cp -r "${WORKSPACE}"/zephyr/drivers/smbus/ "${WORKSPACE}"/Protex_IA/zephyr/drivers/smbus/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/include/zephyr/drivers
	cp -r "${WORKSPACE}"/zephyr/include/zephyr/drivers/smbus.h "${WORKSPACE}"/Protex_IA/zephyr/include/zephyr/drivers/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/samples/drivers/smbus
	cp -r "${WORKSPACE}"/zephyr/samples/drivers/smbus/ "${WORKSPACE}"/Protex_IA/zephyr/samples/drivers/smbus/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/drivers/pwm
	cp -r "${WORKSPACE}"/zephyr/drivers/pwm/pwm_intel_blinky.c "${WORKSPACE}"/Protex_IA/zephyr/drivers/pwm/
	cp -r "${WORKSPACE}"/zephyr/drivers/pwm/Kconfig.intel_blinky "${WORKSPACE}"/Protex_IA/zephyr/drivers/pwm/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/drivers/timer
	cp -r "${WORKSPACE}"/zephyr/drivers/timer/apic_timer.c "${WORKSPACE}"/Protex_IA/zephyr/drivers/timer/
	cp -r "${WORKSPACE}"/zephyr/drivers/timer/apic_tsc.c "${WORKSPACE}"/Protex_IA/zephyr/drivers/timer/
	cp -r "${WORKSPACE}"/zephyr/drivers/timer/hpet.c "${WORKSPACE}"/Protex_IA/zephyr/drivers/timer/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/drivers/rtc
	cp -r "${WORKSPACE}"/zephyr/drivers/rtc/rtc_mc146818.c "${WORKSPACE}"/Protex_IA/zephyr/drivers/rtc/
	cp -r "${WORKSPACE}"/zephyr/drivers/rtc/Kconfig.mc146818 "${WORKSPACE}"/Protex_IA/zephyr/drivers/rtc/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/drivers/dma
	cp -r "${WORKSPACE}"/zephyr/drivers/dma/dma_dw_common.h "${WORKSPACE}"/Protex_IA/zephyr/drivers/dma/
	cp -r "${WORKSPACE}"/zephyr/drivers/dma/dma_dw_common.c "${WORKSPACE}"/Protex_IA/zephyr/drivers/dma/
	cp -r "${WORKSPACE}"/zephyr/drivers/dma/dma_intel_lpss.c "${WORKSPACE}"/Protex_IA/zephyr/drivers/dma/
	cp -r "${WORKSPACE}"/zephyr/drivers/dma/Kconfig.dw_common "${WORKSPACE}"/Protex_IA/zephyr/drivers/dma/
	cp -r "${WORKSPACE}"/zephyr/drivers/dma/Kconfig.intel_lpss "${WORKSPACE}"/Protex_IA/zephyr/drivers/dma/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/drivers/watchdog
	cp -r "${WORKSPACE}"/zephyr/drivers/watchdog/wdt_tco.c "${WORKSPACE}"/Protex_IA/zephyr/drivers/watchdog/
	cp -r "${WORKSPACE}"/zephyr/drivers/watchdog/Kconfig.tco "${WORKSPACE}"/Protex_IA/zephyr/drivers/watchdog/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/drivers/i2c
	cp -r "${WORKSPACE}"/zephyr/drivers/i2c/i2c_dw.c "${WORKSPACE}"/Protex_IA/zephyr/drivers/i2c/
	cp -r "${WORKSPACE}"/zephyr/drivers/i2c/i2c_dw.h "${WORKSPACE}"/Protex_IA/zephyr/drivers/i2c/
	cp -r "${WORKSPACE}"/zephyr/drivers/i2c/Kconfig.dw "${WORKSPACE}"/Protex_IA/zephyr/drivers/i2c/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/drivers/sdhc
	cp -r "${WORKSPACE}"/zephyr/drivers/sdhc/intel_emmc_host.c "${WORKSPACE}"/Protex_IA/zephyr/drivers/sdhc/
	cp -r "${WORKSPACE}"/zephyr/drivers/sdhc/intel_emmc_host.h "${WORKSPACE}"/Protex_IA/zephyr/drivers/sdhc/
	cp -r "${WORKSPACE}"/zephyr/drivers/sdhc/Kconfig.intel "${WORKSPACE}"/Protex_IA/zephyr/drivers/sdhc/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/drivers/disk/nvme
	cp -r "${WORKSPACE}"/zephyr/drivers/disk/nvme/ "${WORKSPACE}"/Protex_IA/zephyr/drivers/disk/nvme/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/drivers/interrupt_controller
	cp -r "${WORKSPACE}"/zephyr/drivers/interrupt_controller/intc_intel_vtd.c "${WORKSPACE}"/Protex_IA/zephyr/drivers/interrupt_controller/
	cp -r "${WORKSPACE}"/zephyr/drivers/interrupt_controller/intc_intel_vtd.h "${WORKSPACE}"/Protex_IA/zephyr/drivers/interrupt_controller/
	cp -r "${WORKSPACE}"/zephyr/drivers/interrupt_controller/intc_loapic.c "${WORKSPACE}"/Protex_IA/zephyr/drivers/interrupt_controller/
	cp -r "${WORKSPACE}"/zephyr/drivers/interrupt_controller/intc_ioapic.c "${WORKSPACE}"/Protex_IA/zephyr/drivers/interrupt_controller/
	cp -r "${WORKSPACE}"/zephyr/drivers/interrupt_controller/intc_ioapic_priv.h "${WORKSPACE}"/Protex_IA/zephyr/drivers/interrupt_controller/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/drivers/ethernet
	cp -r "${WORKSPACE}"/zephyr/drivers/ethernet/CMakeLists.txt "${WORKSPACE}"/Protex_IA/zephyr/drivers/ethernet/
	cp -r "${WORKSPACE}"/zephyr/drivers/ethernet/Kconfig "${WORKSPACE}"/Protex_IA/zephyr/drivers/ethernet/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/drivers/ethernet/phy
	cp -r "${WORKSPACE}"/zephyr/drivers/ethernet/phy/CMakeLists.txt "${WORKSPACE}"/Protex_IA/zephyr/drivers/ethernet/phy/
	cp -r "${WORKSPACE}"/zephyr/drivers/ethernet/phy/Kconfig "${WORKSPACE}"/Protex_IA/zephyr/drivers/ethernet/phy/
	mkdir -p "${WORKSPACE}"/Protex_IA/zephyr/include/zephyr/net
	cp -r "${WORKSPACE}"/zephyr/include/zephyr/net/mii.h "${WORKSPACE}"/Protex_IA/zephyr/include/zephyr/net/
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
