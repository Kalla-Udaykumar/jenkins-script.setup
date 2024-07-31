#!groovy

def getConfig(environments) {
    props = readYaml file: "${WORKSPACE}/henosis_devops/Kernel_Gio/config.yml"
    def environmentConfigs = props.environments
    println "platform: ${environmentConfigs}"

    // Create a dictionary to store the environment-gio_cmd mapping
    def gioCmdMapping = [:]

    // Split the input string of environments into a list
    def envList = environments.split(',')

    // Loop through the given environment names
    envList.each { env ->
        // Find the environment configuration based on the given environment name
        def targetEnvironment = null
        for (config in environmentConfigs) {
            if (config.name == env) {
                targetEnvironment = config
                break
            }
        }

        if (targetEnvironment) {
            gioCmdMapping[env] = targetEnvironment.gio_cmd
        } else {
            println("Environment '${env}' not found in the config.yml.")
        }
    }
    return gioCmdMapping
}

def trigger_giocmd(gioCmdMapping) {
    gioCmdMapping.each { env, gio_cmd ->
        // Execute the GIO command for each environment one by one
        println "PLATFORM '${env}'"
        for (each_cmd in gio_cmd) {
            dynamic_giocmd = "${each_cmd}"
            def exitCode = sh (
                script: """
                        #!/bin/bash -xe
                        docker run --rm -v ${WORKSPACE}:${WORKSPACE} -w ${WORKSPACE}/gio_validation \
                        -e LOCAL_USER_ID=`id -u` -e LOCAL_GROUP_ID=`id -g` -e LOCAL_USER="lab_bldmstr" ${DOCKER_IMG} \
                        bash -c "pip install --upgrade gio_plugin --extra-index-url https://af01p-png.devtools.intel.com/artifactory/api/pypi/gio-testing-center-pyrepo-png-local/simple \
                        && ${dynamic_giocmd}"
                        """,
                returnStatus: true // Capture the exit status
            )
    
            if (exitCode != 0) {
            echo "Error: GIO command failed with exit code ${exitCode}"
            }
    
            sleep 30
        }
    }
}

def fullGIO(gioCmdMapping) {
        println gioCmdMapping
    	trigger_giocmd(gioCmdMapping)
}

pipeline {
    agent{ label 'BSP-DOCKER-POOL'}
    
    environment {
        DATETIME = new Date().format("yyyyMMdd-HHmm")
        DOCKER_IMG = "amr-registry.caas.intel.com/esc-apps/gio/ubuntu18.04:20210927_1110"
        
    }
    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '90', artifactDaysToKeepStr: '30'))
        skipDefaultCheckout()
    }
    parameters {
	    string(name: 'GIO_IMG_VERSION', defaultValue: '', description: 'GIO Value from Upstream')
    	string(name: 'GIO_IMG_URL', defaultValue: '', description: 'GIO Value from Upstream')
		string(name: 'config_branch', defaultValue: 'refs/heads/master', description: 'Repo branch to checkout config file')
		choice(name: 'KERNEL', choices: ['Primary_Kernel_Config', 'Secondary_Kernel_Config'], description: 'Select a kernel')
		choice(name: 'PLATFORM', choices: ['ADL', 'ADLPS','ADLN','ICX','RPLS'], description: 'PLATFORM Value from Upstream')
    }
    stages {
        
        stage ('CLEAN') {
            steps {
                // Recursively deletes the current directory and its contents.
                deleteDir()
            }
        }
        stage('SCM') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    userRemoteConfigs: [[credentialsId: 'GitHub-Token', url: 'https://github.com/Kalla-Udaykumar/jenkins-script.setup.git']],
                    branches: [[name: "master"]],
                    extensions: [
                        [$class: 'RelativeTargetDirectory', relativeTargetDir: 'henosis_devops'],
                        [$class: 'ScmName', name: 'henosis_devops'],
                        [$class: 'CleanBeforeCheckout']
                    ]
                ])
            }
        }
        stage('GENERATE DYNAMIC_PARAM JSON'){
            steps {
                script {
                    def kernel = readYaml file: "${WORKSPACE}/henosis_devops/Kernel_Gio/config.yml"
                    
                    println "Kernel is: ${KERNEL}"
                    // Access the values from the kernel_mapping dictionary
                    def LTS_kernel = kernel.kernel_mapping."${KERNEL}".LTS_kernel
                    def RT_kernel = kernel.kernel_mapping."${KERNEL}".RT_kernel
                    def Kernel_version_number = kernel.kernel_mapping."${KERNEL}".Kernel_version_number
            
                    // Print the values
                    println "LTS_kernel: ${LTS_kernel}"
                    println "RT_kernel: ${RT_kernel}"
                    println "Kernel_version_number: ${Kernel_version_number}"
                    
                    
                    String dynamicParam = readFile("${WORKSPACE}/henosis_devops/Kernel_Gio/RPLS_dynamic_param.json").replaceAll('image_path',"${GIO_IMG_URL}").replaceAll('image_version',"${GIO_IMG_VERSION}").replaceAll('LTS_kernel',"${LTS_kernel}").replaceAll('RT_kernel',"${RT_kernel}").replaceAll('Kernel_version_number',"${Kernel_version_number}")
                    writeFile file:"${WORKSPACE}/gio_validation/dynamic_param.json", text: dynamicParam
                    def dynamicParam_json = readJSON file: "${WORKSPACE}/gio_validation/dynamic_param.json"
                    println dynamicParam_json
                }
            }
        }
        stage('GIO triggering') {
            steps {
                script {
                    def gioCmdMapping = getConfig(PLATFORM)
                    //println(gioCmdMapping)                    
                    fullGIO(gioCmdMapping)
                   
                }
            }
        }
    }
}
