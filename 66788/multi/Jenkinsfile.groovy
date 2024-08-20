import static groovy.json.JsonOutput.*

pipeline {
    agent {
        node {
            label 'BSP-DOCKER-POOL' //BSP-DOCKER1-SLES12
        }
    }
    environment {
        PREINT = ""
        DATETIME = new Date().format("yyyyMMdd-HHmm");
        ARTIFACTORY_SERVER = "https://af01p-png.devtools.intel.com";
        ARTIFACTORY_REPO = "hspe-edge-png-local/ubuntu/ppa/";
        API_ID = credentials('github-api-token');
        MANIFEST_REPO = "hspe-edge-png-local/ubuntu/manifest-file";
        KEYS_REPO = "hspe-edge-png-local/ubuntu/keys";
        
    }
    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '90', artifactDaysToKeepStr: '30'))
        skipDefaultCheckout()
    }
    parameters {
        string(name: 'MANIFEST_REPO_JAMMY', description: 'Set this value if you wish to change the default manifest repo', defaultValue: 'https://github.com/intel-innersource/os.linux.ubuntu.iot.utilities.custom-image-creator-config.git')
        string(name: 'MANIFEST_REPO_NOBLE', description: 'Set this value if you wish to change the default manifest repo', defaultValue: 'https://github.com/intel-innersource/os.linux.ubuntu-integration.build-automation.git')
        string(name: 'JAMMY_MANIFEST_BRANCH', description: 'Set this value if you wish to change the default manifest repo branch', defaultValue: 'main')
        string(name: 'NOBLE_MANIFEST_BRANCH', description: 'Set this value if you wish to change the default manifest repo branch', defaultValue: 'main')
        string(name: 'MANIFEST_FILE_JAMMY', description: 'Set this value if you wish to change the default manifest file', defaultValue: 'jammy.json')
        string(name: 'MANIFEST_FILE_NOBLE', description: 'Set this value if you wish to change the default manifest file', defaultValue: 'noble.json')
        string(name: 'KERNELS_FILE', description: 'Path for the kernels json file, eg: manifest/kernels.json', defaultValue: '')
        string(name: 'EXTRA_DEBS_JAMMY', description: 'Extra debs file path, eg: manifest/jammy-extra-debs.json', defaultValue: 'manifest/jammy-extra-debs.json')
        string(name: 'EXTRA_DEBS_NOBLE', description: 'Extra debs file path, eg: manifest/jammy-extra-debs.json', defaultValue: 'manifest/noble-extra-debs.json')
        string(name: 'DISTRO_NAME_JAMMY', description: 'Set this value if you wish to change the distro name used in the artifactory path', defaultValue: 'jammy')
        string(name: 'DISTRO_NAME_NOBLE', description: 'Set this value if you wish to change the distro name used in the artifactory path', defaultValue: 'noble')
        string(name: 'VARIANT_TO_BUILD', description: 'Change this value to change the default image variant to build', defaultValue: 'default')
        text(name: 'FIXED_PACKAGE_LINKS',
             defaultValue: "",
             description: "newline delimited package links, specify this here for those that you don't want the latest version. Remember to remove the packages from PACKAGE_NAMES below")
        text(name: 'PACKAGE_NAMES',
             defaultValue: "",
             description: 'newline delimited package names')
        text(name: 'PACKAGE_REPOS',
             defaultValue: "intel/compute-runtime\n"
                            + "intel/intel-graphics-compiler\n",
             description: 'list of repos to check for the PACKAGE_NAMES above')
        text(name: 'REMOVE_PACKAGES',
             defaultValue: "",
             description: 'newline delimited package names to be deleted before upload, please include the file type eg. abcd.deb')
        booleanParam(name: 'UPLOAD', defaultValue: false, description: 'Toggle this value if you wish to not upload artifacts to artifactory')
        booleanParam(name: 'USE_COMMIT', defaultValue: false, description: 'Toggle this value if you wish use commit id instead of branch')
        booleanParam(name: 'FORCE_BUILD', defaultValue: false, description: 'Toggle this value if you wish to force build')
        booleanParam(name: 'EMAIL', defaultValue: true, description: 'Email notification upon job completion')
        booleanParam(name: 'CLEANWS', defaultValue: true, description: 'Clean workspace')
        booleanParam(name: 'JAMMY', defaultValue: true, description: 'Run the Jammy Pipeline')
        booleanParam(name:'NOBLE', defaultValue: true, description: 'Run the Noble Pipeline')
    }

    stages {
        stage('CLEAN'){
            when {
                expression { params.CLEANWS == true }
            }
            steps {
                deleteDir()
                script {
                    dir("${WORKSPACE}") {
                       // if(params.JAMMY) { }
                        def manifest_name_jammy = (params.MANIFEST_FILE_JAMMY).replace(".json","").trim()
                        env.manifest_name_jammy = (params.MANIFEST_FILE_JAMMY).replace(".json","").trim()
                        env.ARTIFACTORY_REPO_JAMMY = "hspe-edge-png-local/ubuntu/" + params.DISTRO_NAME_JAMMY.trim() + "/${manifest_name_jammy}"
                        ARTIFACTORY_REPO_JAMMY = "hspe-edge-png-local/ubuntu/" + params.DISTRO_NAME_JAMMY.trim() + "/${manifest_name_jammy}"
                        println("ARTIFACTORY_REPO: " + ARTIFACTORY_REPO_JAMMY)
                        env.MANIFEST_LOCATION_JAMMY = "${WORKSPACE}/jammy_repo/manifest"
                    }
                    dir("${WORKSPACE}"){
                        //if(params.NOBLE) { }
                        def manifest_name_noble = (params.MANIFEST_FILE_NOBLE).replace(".json","").trim()
                        env.manifest_name_noble = (params.MANIFEST_FILE_NOBLE).replace(".json","").trim()
                        env.ARTIFACTORY_REPO_NOBLE = "hspe-edge-png-local/ubuntu/" + params.DISTRO_NAME_NOBLE.trim() + "/${manifest_name_noble}"
                        ARTIFACTORY_REPO_NOBLE = "hspe-edge-png-local/ubuntu/" + params.DISTRO_NAME_NOBLE.trim() + "/${manifest_name_noble}"
                        println("ARTIFACTORY_REPO: " + ARTIFACTORY_REPO_NOBLE)
                        env.MANIFEST_LOCATION_NOBLE = "${WORKSPACE}/noble_repo/manifest"  
                    }
                }
            }   
        }

        stage('SCM') {
            steps {
                parallel (

                    "SERVICES_REPO": {
                        checkout([$class: 'GitSCM',
                        branches: [[name: "master"]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions: [[$class: 'CloneOption', noTags: false, reference: '', timeout: 20],
                        [$class: 'SubmoduleOption', disableSubmodules: false, parentCredentials: false, recursiveSubmodules: true, reference: '', timeout: 20, trackingSubmodules: false],
                        [$class: 'RelativeTargetDirectory', relativeTargetDir: 'engservices']],
                        submoduleCfg: [],
                        userRemoteConfigs: [[credentialsId: 'GitHub-Token', url: 'https://github.com/intel-innersource/libraries.devops.henosis.build.automation.services.git']]])
                    },
                    "JENKINS_SCRIPT": {
                        checkout([$class: 'GitSCM',
                        branches: [[name: "main"]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions: [[$class: 'CloneOption', noTags: false, reference: '', timeout: 20],
                        [$class: 'SubmoduleOption', disableSubmodules: false, parentCredentials: false, recursiveSubmodules: true, reference: '', timeout: 20, trackingSubmodules: false],
                        [$class: 'RelativeTargetDirectory', relativeTargetDir: 'jenkins-script']],
                        submoduleCfg: [],
                        userRemoteConfigs: [[credentialsId: 'GitHub-Token', url: 'https://github.com/intel-innersource/os.linux.ubuntu.iot.utilities.jenkins-script.git']]])
                    },
                    "JAMMY_REPO": {
                        checkout([$class: 'GitSCM',
                        branches: [[name: params.JAMMY_MANIFEST_BRANCH]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions: [[$class: 'CloneOption', noTags: false, reference: '', timeout: 20],
                        [$class: 'SubmoduleOption', disableSubmodules: false, parentCredentials: false, recursiveSubmodules: true, reference: '', timeout: 20, trackingSubmodules: false],
                        [$class: 'RelativeTargetDirectory', relativeTargetDir: 'jammy_repo']],
                        submoduleCfg: [],
                        userRemoteConfigs: [[credentialsId: 'GitHub-Token', url: params.MANIFEST_REPO_JAMMY]]])
                    },
                    "NOBLE_REPO": {
                        checkout([$class: 'GitSCM',
                        branches: [[name: params.NOBLE_MANIFEST_BRANCH]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions: [[$class: 'CloneOption', noTags: false, reference: '', timeout: 20],
                        [$class: 'SubmoduleOption', disableSubmodules: false, parentCredentials: false, recursiveSubmodules: true, reference: '', timeout: 20, trackingSubmodules: false],
                        [$class: 'RelativeTargetDirectory', relativeTargetDir: 'noble_repo']],
                        submoduleCfg: [],
                        userRemoteConfigs: [[credentialsId: 'GitHub-Token', url: params.MANIFEST_REPO_NOBLE]]])
                    },
                    "CAC_REPO": {
                        checkout([$class: 'GitSCM',
                        branches: [[name: "master"]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions: [[$class: 'CloneOption', noTags: false, reference: '', timeout: 20],
                        [$class: 'SubmoduleOption', disableSubmodules: false, parentCredentials: false, recursiveSubmodules: true, reference: '', timeout: 20, trackingSubmodules: false],
                        [$class: 'RelativeTargetDirectory', relativeTargetDir: 'cac_repo']],
                        submoduleCfg: [],
                        userRemoteConfigs: [[credentialsId: 'GitHub-Token', url: 'https://github.com/Kalla-Udaykumar/jenkins-script.setup.git']]])   
                    }
                )
            }
        }

        stage('BUILD') {
            steps {
                parallel(
                    "ADL-LIN-EDGE-BD-DLY-PKGGEN": {
                        build job: 'ADL-LIN-EDGE-BD-DLY-PKGGEN', \
                        parameters: [string(name: 'MANIFEST_GIT_REPO', value: "${MANIFEST_REPO_JAMMY}"), \
                        string(name: 'MANIFEST_REPO_BRANCH', value: "${JAMMY_MANIFEST_BRANCH}"), \
                        string(name: 'MANIFEST_FILE', value: "${MANIFEST_FILE_JAMMY}"), \
                        string(name: 'KERNELS_FILE', value: "${KERNELS_FILE}"), \
                        string(name: 'EXTRA_DEBS', value: "${EXTRA_DEBS_JAMMY}"), \
                        string(name: 'DISTRO_NAME', value: "${DISTRO_NAME_JAMMY}"), \
                        string(name: 'VARIANT_TO_BUILD', value: "${VARIANT_TO_BUILD}"), \
                        text(name: 'FIXED_PACKAGE_LINKS', value: "${params.FIXED_PACKAGE_LINKS}"), \
                        text(name: "PACKAGE_NAMES", value: "${params.PACKAGE_NAMES}"), \
                        text(name: "PACKAGE_REPOS", value: "${params.PACKAGE_REPOS}"), \
                        text(name: "REMOVE_PACKAGES", value: "${params.REMOVE_PACKAGES}"), \
                        booleanParam(name: 'UPLOAD', value: "${params.UPLOAD}"), \
                        booleanParam(name: 'USE_COMMIT', value: "${params.USE_COMMIT}"), \
                        booleanParam(name: 'FORCE_BUILD', value: "${params.FORCE_BUILD}"), \
                        string(name: 'UPSTREAM_DATE', value: "${DATETIME}"), \
                        string(name: "ARTIFACTORY_SERVER", value: "${ARTIFACTORY_SERVER}"), \
                        string(name: "ARTIFACTORY_REPO", value: "${ARTIFACTORY_REPO}")], wait: true
                    },
                    "ADL-LIN-EDGE-BD-DLY-PKGGEN-NOBLE": {
                        build job: 'ADL-LIN-EDGE-BD-DLY-PKGGEN-NOBLE', \
                        parameters: [string(name: 'MANIFEST_GIT_REPO', value: "${MANIFEST_REPO_NOBLE}"), \
                        string(name: 'MANIFEST_REPO_BRANCH', value: "${NOBLE_MANIFEST_BRANCH}"), \
                        string(name: 'MANIFEST_FILE', value: "${MANIFEST_FILE_NOBLE}"), \
                        string(name: 'KERNELS_FILE', value: "${KERNELS_FILE}"), \
                        string(name: 'EXTRA_DEBS', value: "${EXTRA_DEBS_NOBLE}"), \
                        string(name: 'DISTRO_NAME', value: "${DISTRO_NAME_NOBLE}"), \
                        string(name: 'VARIANT_TO_BUILD', value: "${VARIANT_TO_BUILD}"), \
                        text(name: 'FIXED_PACKAGE_LINKS', value: "${params.FIXED_PACKAGE_LINKS}"), \
                        text(name: "PACKAGE_NAMES", value: "${params.PACKAGE_NAMES}"), \
                        text(name: "PACKAGE_REPOS", value: "${params.PACKAGE_REPOS}"), \
                        text(name: "REMOVE_PACKAGES", value: "${params.REMOVE_PACKAGES}"), \
                        booleanParam(name: 'UPLOAD', value: "${params.UPLOAD}"), \
                        booleanParam(name: 'USE_COMMIT', value: "${params.USE_COMMIT}"), \
                        booleanParam(name: 'FORCE_BUILD', value: "${params.FORCE_BUILD}"), \
                        string(name: 'UPSTREAM_DATE', value: "${DATETIME}"), \
                        string(name: "ARTIFACTORY_SERVER", value: "${ARTIFACTORY_SERVER}"), \
                        string(name: "ARTIFACTORY_REPO", value: "${ARTIFACTORY_REPO}")], wait: true
                    }
                )
            }
        }
        
        stage('DOWNLOAD MANIFEST') {
            steps {
                script {
                    dir("${WORKSPACE}"){
                        sh"""
                            mkdir -p ${WORKSPACE}/download
                            mkdir -p ${WORKSPACE}/download/jammy
                            mkdir -p ${WORKSPACE}/download/noble
                        """
                        
                        def artServer = Artifactory.server "af01p-png.devtools.intel.com"
                        def artFiles1  = """ {
                        "files": [
                            {
                                "pattern": "${MANIFEST_REPO}/${params.MANIFEST_FILE_JAMMY}",
                                "target": "download/jammy/",
                                "flat": "true",
                                "recursive": "true",
                                "sortBy": ["modified"],
                                "sortOrder": "desc",
                                "limit" : 1
                            }
                        ]
                    }"""
                    artServer.download spec: artFiles1
                    
                    sh"""
                    cp -r ${WORKSPACE}/download/jammy/${params.MANIFEST_FILE_JAMMY} ${WORKSPACE}/jammy-artifactory-manifest.json
                    """
                    }

                    dir("${WORKSPACE}") {
                        
                        def artServer = Artifactory.server "af01p-png.devtools.intel.com"
                        def artFiles2  = """ {
                        "files": [
                            {
                                "pattern": "${MANIFEST_REPO}/${params.MANIFEST_FILE_NOBLE}",
                                "target": "download/noble/",
                                "flat": "true",
                                "recursive": "true",
                                "sortBy": ["modified"],
                                "sortOrder": "desc",
                                "limit" : 1
                            }
                        ]
                    }"""
                    artServer.download spec: artFiles2
                    
                    sh"""
                    cp ${WORKSPACE}/download/noble/${params.MANIFEST_FILE_NOBLE} ${WORKSPACE}/noble-artifactory-manifest.json
                    """
                    }
                }
            }
        }

        stage('Convert Git urls to https') {
            steps {
                script {
                    sh"""
                    
                        python3 ${WORKSPACE}/engservices/tools/etc/change-regex/git_to_https.py --manifest ${env.MANIFEST_LOCATION_JAMMY}/${params.MANIFEST_FILE_JAMMY}
                        # Copy manifest to the jenkins-script manifest folder
                        cp ${env.MANIFEST_LOCATION_JAMMY}/${params.MANIFEST_FILE_JAMMY} ${WORKSPACE}/jenkins-script/manifest/
                    """    
                       // curl -o jammy-artifactory-manifest.json https://${ARTIFACTORY_SERVER}/artifactory/${MANIFEST_REPO}/${params.MANIFEST_FILE_JAMMY}
                    // """
                    sh"""
                        python3 ${WORKSPACE}/engservices/tools/etc/change-regex/git_to_https.py --manifest ${env.MANIFEST_LOCATION_NOBLE}/${params.MANIFEST_FILE_NOBLE}
                        # Copy manifest to the jenkins-script manifest folder
                        cp ${env.MANIFEST_LOCATION_NOBLE}/${params.MANIFEST_FILE_NOBLE} ${WORKSPACE}/jenkins-script/manifest/
                    """ 
                       // curl -o noble-artifactory-manifest.json https://${ARTIFACTORY_SERVER}/artifactory/${MANIFEST_REPO}/${params.MANIFEST_FILE_NOBLE}
                        // """
                }
            }
        }

        stage ('Check for Jammy Changes') {
            when {
                expression { params.JAMMY == true }
            }
            steps {
                script {
                    if(params.USE_COMMIT){
                        env.jammy = sh(returnStatus: true, script: "python3 engservices/tools/etc/cmp-json/cmp-json.py -f jammy-artifactory-manifest.json -s ${env.MANIFEST_LOCATION_JAMMY}/${params.MANIFEST_FILE_JAMMY} -c")
                    }
                    else{
                        def props = readJSON file: "jammy_repo/manifest/${params.MANIFEST_FILE_JAMMY}"
                        def current_commits = []
                        for(item in props){
                            def command = $/git ls-remote -h ${item["source_url"]} | grep -E '${item["branch"]}($|\s)' | awk '{ print $1}'/$
                            def commit_id = sh(script: command, returnStdout: true).trim()
                            current_commits.add(["name":item["name"],"source_url":item["source_url"],"branch":item["branch"],"commit": commit_id,"component":item["component"]])
                        }

                        def pretty_string = prettyPrint(toJson(current_commits))
                        println(pretty_string)
                        writeFile(file: "jammy_current_run_commits.json", text: pretty_string, encoding: "UTF-8")

                        env.jammy = sh(returnStatus: true, script: "python3 engservices/tools/etc/cmp-json/cmp-json.py -f jammy-artifactory-manifest.json -s jammy_current_run_commits.json")
                    }
                    if(params.FORCE_BUILD){
                        env.jammy = 1
                    }
                    if(env.jammy == '1'){
                        echo "BUILD PROCEEDING..."
                    }
                    else{
                        echo"!!! SKIPPING BUILD !!! NO CHANGES DETECTED"
                    }
                }
            }
        }

        stage ('Check for Noble Changes') {
            when {
                expression { params.NOBLE == true }
            }
            steps {
                script {
                    if(params.USE_COMMIT){
                        env.noble = sh(returnStatus: true, script: "python3 engservices/tools/etc/cmp-json/cmp-json.py -f noble-artifactory-manifest.json -s ${env.MANIFEST_LOCATION_NOBLE}/${params.MANIFEST_FILE_NOBLE} -c")
                    }
                    else{
                        def props = readJSON file: "noble_repo/manifest/${params.MANIFEST_FILE_NOBLE}"
                        def current_commits = []
                        for(item in props){
                            def command = $/git ls-remote -h ${item["source_url"]} | grep -E '${item["branch"]}($|\s)' | awk '{ print $1}'/$
                            def commit_id = sh(script: command, returnStdout: true).trim()
                            current_commits.add(["name":item["name"],"source_url":item["source_url"],"branch":item["branch"],"commit": commit_id,"component":item["component"]])
                        }

                        def pretty_string = prettyPrint(toJson(current_commits))
                        println(pretty_string)
                        writeFile(file: "noble_current_run_commits.json", text: pretty_string, encoding: "UTF-8")

                        env.noble = sh(returnStatus: true, script: "python3 engservices/tools/etc/cmp-json/cmp-json.py -f noble-artifactory-manifest.json -s noble_current_run_commits.json")
                    }
                    if(params.FORCE_BUILD){
                        env.noble = 1
                    }
                    if(env.noble == '1'){
                        echo "BUILD PROCEEDING..."
                    }
                    else{
                        echo"!!! SKIPPING BUILD !!! NO CHANGES DETECTED"
                    }
                }
            }
        }

        stage ('DOWNLOAD LATEST FILES FOR JAMMY AND NOBLE') {
            steps {
                script {
                    dir('${WORKSPACE}') {
                        sh""" 
                        mkdir -p ${WORKSPACE}/Latest-files
                        mkdir -p ${WORKSPACE}/Latest-files/noble
                        mkdir -p ${WORKSPACE}/Latest-files/jammy
                        """

                        def artServer = Artifactory.server "ubit-artifactory-sh.intel.com"
                        def artFiles1  = """ {
                            "files": [
                                {
                                    "pattern": "esc-internal-local/sandbox/ppa/noble-latest/(*)",
                                    "target": "Latest-files/noble/{1}",
                                    "flat": "false",
                                }
                            ]
                        }"""
                        artServer.download spec: artFiles1
                    }
                    dir('${WORKSPACE}') {
                        def artServer = Artifactory.server "ubit-artifactory-sh.intel.com"
                        def artFiles2  = """ {
                            "files": [
                                {
                                    "pattern": "esc-internal-local/sandbox/ppa/jammy-latest/(*)",
                                    "target": "Latest-files/jammy/{1}",
                                    "flat": "false",
                                }
                            ]
                        }"""
                        artServer.download spec: artFiles2
                    }
                }
            }
        }

        stage('COPY ONE CONTENT TO OTHER') {
            steps {
                script {
                    sh"""
                    cp ${WORKSPACE}/cac_repo/66788/noble/nobleppaa.sh ${WORKSPACE}/

                    """
                }
            }
        }

        stage ('PUBLISH WHEN BOTH HAVE CHANGES') {
            when {
                expression {
                    env.jammy == '1' && env.noble == '1'
                }
            }
            steps {
                script {
                    
                    echo "this one worked"
                    echo "check other methods"
                    echo "PUBLISH WHEN BOTH HAVE CHANGES"
                }
            }
        }

        stage ('PUBLISH WHEN JAMMY HAVE CHANGES') {
            when {
                expression {
                    env.jammy == '1' && env.noble != '1'
                }
            }
            steps {
                script {
                    echo "PUBLISH WHEN JAMMY HAVE CHANGES"
                }
            }
        }

        stage ('PUBLISH WHEN NOBLE HAVE CHANGES') {
            when {
                expression {
                    env.jammy != '1' && env.noble == '1'
                }
            }
            steps {
                script {
                    echo "PUBLISH WHEN NOBLE HAVE CHANGES"
                }
            }
        }

    }
}
