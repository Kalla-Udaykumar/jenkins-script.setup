import static groovy.json.JsonOutput.*

pipeline{
    agent {
        node {
            label 'BSP-DOCKER-POOL'
        }
    }

    environment {
        PREINT = ""
        ARTIFACTORY_SERVER = "af01p-png.devtools.intel.com";
        MANIFEST_REPO = "hspe-edge-png-local/ubuntu/manifest-file";
        KEYS_REPO = "hspe-edge-png-local/ubuntu/keys";
        DATETIME = new Date().format("yyyyMMdd-HHmm");
        DAY = new Date().format("yyyyMMdd");
        WORK_WEEK = new Date().format("ww");
        BUILD_YEAR = new Date().format("yyyy");
        YEAR = new Date().format("yy")
        TIME = new Date().format("HHmm")
    //Downstream QA Job inclusive of BDBA and Virus Scan
        DOWNSTREAM_QA_JOB = "GEN-LIN-EDGE-QA-ODM-PKGGEN";
        CMD_DOCKER_RUN = "docker run --rm -t \
        --privileged -e LOCAL_USER=lab_bldmstr -e LOCAL_USER_ID=`id -u` -e LOCAL_GROUP_ID=`id -g` \
        -v /nfs/png/home/lab_bldmstr/.gitconfig:/home/lab_bldmstr/.gitconfig \
        -v /nfs/png/home/lab_bldmstr/.netrc:/home/lab_bldmstr/.netrc \
        -v /nfs/site/disks/ecg_es_disk2:/nfs/site/disks/ecg_es_disk2 \
        -v /nfs/png/home/lab_bldmstr/.git-credentials:/home/lab_bldmstr/.git-credentials \
        -v /nfs/png/home/lab_bldmstr/.ssh:/home/lab_bldmstr/.ssh \
        -v /nfs/png/disks/ecg_es_disk2:/nfs/png/disks/ecg_es_disk2 \
        -e http_proxy=${http_proxy} -e https_proxy=${https_proxy} \
        -v ${WORKSPACE}:${WORKSPACE} -w ${WORKSPACE} --name ${BUILD_TAG}_GENERAL \
        amr-registry.caas.intel.com/esc-devops/gen/lin/edge/ubuntu/package_builder:20221114_1503"

        PACKAGE_DOCKER_IMAGE = "amr-registry.caas.intel.com/esc-devops/gen/lin/edge/ubuntu/package_builder_jammy:20220829_0855"

        PACKAGE_DOCKER_BLD_CMD = "docker run --rm -t \
        --privileged -e LOCAL_USER=lab_bldmstr -e LOCAL_USER_ID=`id -u` -e LOCAL_GROUP_ID=`id -g` \
        -v /nfs/png/home/lab_bldmstr/.gitconfig:/home/lab_bldmstr/.gitconfig \
        -v /nfs/png/home/lab_bldmstr/.netrc:/home/lab_bldmstr/.netrc \
        -v /nfs/site/disks/ecg_es_disk2:/nfs/site/disks/ecg_es_disk2 \
        -v /nfs/png/home/lab_bldmstr/.git-credentials:/home/lab_bldmstr/.git-credentials \
        -v /nfs/png/home/lab_bldmstr/.ssh:/home/lab_bldmstr/.ssh \
        -v /nfs/png/disks/ecg_es_disk2:/nfs/png/disks/ecg_es_disk2 \
        -e http_proxy=${http_proxy} -e https_proxy=${https_proxy} \
        -v ${WORKSPACE}/build-target:/workspace \
        -v ${WORKSPACE}/PPA:/PPA -w /workspace --name ${BUILD_TAG} \
        ${PACKAGE_DOCKER_IMAGE}"

        // We are using the --security-opt seccomp=unconfined here because it stalls forever at gst-plugins-bad
        // Chee Yang from the coreos team recommended this workaround
        PACKAGE_DOCKER_BLD_UNCONFINED_CMD = "docker run --rm -t \
        --privileged -e LOCAL_USER=lab_bldmstr -e LOCAL_USER_ID=`id -u` -e LOCAL_GROUP_ID=`id -g` \
        -v /nfs/png/home/lab_bldmstr/.gitconfig:/home/lab_bldmstr/.gitconfig \
        -v /nfs/png/home/lab_bldmstr/.netrc:/home/lab_bldmstr/.netrc \
        -v /nfs/site/disks/ecg_es_disk2:/nfs/site/disks/ecg_es_disk2 \
        -v /nfs/png/home/lab_bldmstr/.git-credentials:/home/lab_bldmstr/.git-credentials \
        -v /nfs/png/home/lab_bldmstr/.ssh:/home/lab_bldmstr/.ssh \
        -v /nfs/png/disks/ecg_es_disk2:/nfs/png/disks/ecg_es_disk2 \
        -e http_proxy=${http_proxy} -e https_proxy=${https_proxy} \
        -v ${WORKSPACE}/build-target:/workspace \
        -v ${WORKSPACE}/PPA:/PPA -w /workspace --name ${BUILD_TAG} \
        --security-opt seccomp=unconfined \
        ${PACKAGE_DOCKER_IMAGE}"

        PACKAGE_SIGN_DOCKER = "docker run --rm -t \
        --privileged -e LOCAL_USER=lab_bldmstr -e LOCAL_USER_ID=`id -u` -e LOCAL_GROUP_ID=`id -g` \
        -v ${WORKSPACE}:${WORKSPACE} --name ${BUILD_TAG}_PKG_SIGN \
        amr-registry.caas.intel.com/esc-devops/gen/lin/ubuntu/pkg-sign/package-signature:20220817_1216"
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '30', artifactDaysToKeepStr: '30'))
        skipDefaultCheckout()
    }
    parameters {
        string(name: 'MANIFEST_GIT_REPO', description: 'Set this value if you wish to change the default manifest repo', defaultValue: 'https://github.com/intel-innersource/os.linux.ubuntu.iot.utilities.custom-image-creator-config.git')
        string(name: 'MANIFEST_REPO_BRANCH', description: 'Set this value if you wish to change the default manifest repo branch', defaultValue: 'main')
        string(name: 'MANIFEST_FILE', description: 'Set this value if you wish to change the default manifest file', defaultValue: 'jammy.json')
        string(name: 'KERNELS_FILE', description: 'Path for the kernels json file, eg: manifest/kernels.json', defaultValue: '')
        string(name: 'EXTRA_DEBS', description: 'Extra debs file path, eg: manifest/jammy-extra-debs.json', defaultValue: 'manifest/jammy-extra-debs.json')
        string(name: 'DISTRO_NAME', description: 'Set this value if you wish to change the distro name used in the artifactory path', defaultValue: 'jammy')
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
        booleanParam(name: 'USER_UPLOAD', defaultValue: false, description: 'Toggle this value if you wish to not upload artifacts to artifactory')
    }

    stages{
        stage("Cleanup"){
            steps {
                script{
                    deleteDir()
                    // Strip the .json ending from the filename
                    def manifest_name = (params.MANIFEST_FILE).replace(".json","").trim()
                    env.manifest_name = (params.MANIFEST_FILE).replace(".json","").trim()
                    env.ARTIFACTORY_REPO = "hspe-edge-png-local/ubuntu/" + params.DISTRO_NAME.trim() + "/${manifest_name}"
                    ARTIFACTORY_REPO = "hspe-edge-png-local/ubuntu/" + params.DISTRO_NAME.trim() + "/${manifest_name}"
                    println("ARTIFACTORY_REPO: " + ARTIFACTORY_REPO)
                    env.MANIFEST_LOCATION = "${WORKSPACE}/manifest_repo/manifest"
                }
            }
        }

        stage("SCM"){
            steps {
                checkout([$class: 'GitSCM',
                branches: [[name: "master"]],
                doGenerateSubmoduleConfigurations: false,
                extensions: [[$class: 'CloneOption', noTags: false, reference: '', timeout: 20],
                [$class: 'SubmoduleOption', disableSubmodules: false, parentCredentials: false, recursiveSubmodules: true, reference: '', timeout: 20, trackingSubmodules: false],
                [$class: 'RelativeTargetDirectory', relativeTargetDir: 'engservices']],
                submoduleCfg: [],
                userRemoteConfigs: [[credentialsId: 'GitHub-Token', url: 'https://github.com/intel-innersource/libraries.devops.henosis.build.automation.services.git']]])

                checkout([$class: 'GitSCM',
                branches: [[name: "main"]],
                doGenerateSubmoduleConfigurations: false,
                extensions: [[$class: 'CloneOption', noTags: false, reference: '', timeout: 20],
                [$class: 'SubmoduleOption', disableSubmodules: false, parentCredentials: false, recursiveSubmodules: true, reference: '', timeout: 20, trackingSubmodules: false],
                [$class: 'RelativeTargetDirectory', relativeTargetDir: 'jenkins-script']],
                submoduleCfg: [],
                userRemoteConfigs: [[credentialsId: 'GitHub-Token', url: 'https://github.com/intel-innersource/os.linux.ubuntu.iot.utilities.jenkins-script.git']]])

                checkout([$class: 'GitSCM',
                branches: [[name: params.MANIFEST_REPO_BRANCH]],
                doGenerateSubmoduleConfigurations: false,
                extensions: [[$class: 'CloneOption', noTags: false, reference: '', timeout: 20],
                [$class: 'SubmoduleOption', disableSubmodules: false, parentCredentials: false, recursiveSubmodules: true, reference: '', timeout: 20, trackingSubmodules: false],
                [$class: 'RelativeTargetDirectory', relativeTargetDir: 'manifest_repo']],
                submoduleCfg: [],
                userRemoteConfigs: [[credentialsId: 'GitHub-Token', url: params.MANIFEST_GIT_REPO]]])
            }
        }
        stage("Convert git urls to https"){
            steps{
                sh"""
                    python3 ${WORKSPACE}/engservices/tools/etc/change-regex/git_to_https.py --manifest ${env.MANIFEST_LOCATION}/${params.MANIFEST_FILE}
                    # Copy manifest to the jenkins-script manifest folder
                    cp ${env.MANIFEST_LOCATION}/${params.MANIFEST_FILE} ${WORKSPACE}/jenkins-script/manifest/

                    curl -o artifactory-manifest.json https://${ARTIFACTORY_SERVER}/artifactory/${MANIFEST_REPO}/${params.MANIFEST_FILE}
                """
            }
        }
        stage("Check for changes"){
            steps{
                script{
                    if(params.USE_COMMIT){
                        env.diff = sh(returnStatus: true, script: "python3 engservices/tools/etc/cmp-json/cmp-json.py -f artifactory-manifest.json -s ${env.MANIFEST_LOCATION}/${params.MANIFEST_FILE} -c")
                    }
                    else{
                        def props = readJSON file: "manifest_repo/manifest/${params.MANIFEST_FILE}"
                        def current_commits = []
                        for(item in props){
                            def command = $/git ls-remote -h ${item["source_url"]} | grep -E '${item["branch"]}($|\s)' | awk '{ print $1}'/$
                            def commit_id = sh(script: command, returnStdout: true).trim()
                            current_commits.add(["name":item["name"],"source_url":item["source_url"],"branch":item["branch"],"commit": commit_id,"component":item["component"]])
                        }

                        def pretty_string = prettyPrint(toJson(current_commits))
                        println(pretty_string)
                        writeFile(file: "current_run_commits.json", text: pretty_string, encoding: "UTF-8")

                        env.diff = sh(returnStatus: true, script: "python3 engservices/tools/etc/cmp-json/cmp-json.py -f artifactory-manifest.json -s current_run_commits.json")
                    }
                    if(params.FORCE_BUILD){
                        env.diff = 1
                    }
                    if(env.diff == '1'){
                        echo "BUILD PROCEEDING..."
                    }
                    else{
                        echo"!!! SKIPPING BUILD !!! NO CHANGES DETECTED"
                    }
                }
            }
        }
        // TODO: Extract the package build steps from build-package-docker.sh and run them directly here!
        stage("Run the sort manifest script & build packages"){
            when{
                expression{
                    env.diff == '1'
                }
            }
            steps{
                sh"""
                    $CMD_DOCKER_RUN bash -c "cd ${WORKSPACE}/jenkins-script/ &&\
                    python3 sort-manifest.py --manifest ${params.MANIFEST_FILE} --workspace temp_workspace
                    #cat sorted-manifest.json
                    "
                """

                script{
                    def props = readJSON file: "${WORKSPACE}/jenkins-script/sorted-manifest.json"
                    // println(props)
                    sh"""
                        curl -o private.key https://ubit-artifactory-sh.intel.com/artifactory/esc-internal-local/gpg-key/private.key
                        mkdir -p ${WORKSPACE}/PPA
                        cd ${WORKSPACE}/PPA
                        export no_proxy=intel.com,*.intel.com
                        export NO_PROXY=intel.com,*.intel.com
                        mkdir ${WORKSPACE}/PPA/conf
                        curl -o ${WORKSPACE}/PPA/conf/distributions https://${ARTIFACTORY_SERVER}/artifactory/${KEYS_REPO}/jammy-multi-component.key
                    """

                    for (item in props){
                        println("Name: " + item["name"] + " | " + "Branch: " + item["branch"] + " | " + "source_url: " + item["source_url"] + " | " + "commit: " + item["commit"] + " | " + "component: " + item["component"])

                        // Cause the script they provided does some stupid chown stuff
                        sh"""
                            $CMD_DOCKER_RUN bash -c "sudo sh -c 'chown -R root:root ${WORKSPACE}/PPA'"
                            rm -rf ${WORKSPACE}/build-target || true
                            rm -rf ${WORKSPACE}/binaries || true
                            rm -rf ${WORKSPACE}/jenkins-script/binaries/ || true
                            rm -rf ${WORKSPACE}/jenkins-script/build-target || true
                            $CMD_DOCKER_RUN bash -c "sudo sh -c 'cd ${WORKSPACE}/PPA/ && dpkg-scanpackages . /dev/null > ${WORKSPACE}/PPA/Packages'"
                            ls -la ${WORKSPACE}/PPA/Packages
                        """

                        // Clone the ingredient to be built
                        checkout([$class: 'GitSCM',
                        branches: [[name: item["branch"]]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions: [[$class: 'CloneOption', noTags: false, reference: '', timeout: 90],
                        [$class: 'SubmoduleOption', disableSubmodules: false, parentCredentials: false, recursiveSubmodules: false, reference: '', timeout: 90, trackingSubmodules: false],
                        [$class: 'RelativeTargetDirectory', relativeTargetDir: 'build-target']],
                        submoduleCfg: [],
                        userRemoteConfigs: [[credentialsId: 'GitHub-Token', url: item["source_url"]]]])

                        def checkout_target = item["branch"]

                        if(params.USE_COMMIT){
                            println("Use commit was set to true, using commit ID instead of branch")
                            checkout_target = item["commit"]
                        }
                        else{
                            println("Use commit was set to false, using branch instead of commit ID")
                        }

                        sh"""
                            DIR="source"
                            cd build-target
                            git checkout ${checkout_target}
                            git log -1
                            if [ -d \${DIR} ]; then git submodule update --init --recursive; fi
                        """

                        /*  ###################################################################
                         *  ################# START BLOCK FOR PACKAGE BUILD ###################
                         *  ###################################################################
                         */

                        // ######## The following block has been deprecated now that we have the apt pinning thing in place ####
                        // ######## just keeping it for future reference #######################################################
                        //
                        // If name is in the special list, then we need a special command, the default apt resolver refuses to downgrade
                        // packages/pick a lower package version to install even if it is available
                        // if (special_cases.contains(item["name"].trim())){
                        //     println("Using special package resolver!")
                        //     MK_BUILD_DEPS_CMD = $/yes|mk-build-deps -i -t "apt-cudf-get --solver aspcud -o APT::Get::Assume-Yes=1 -o APT::Get::allow-downgrades=1"/$
                        // }
                        // else{
                        //     println("Not using special package resolver!")
                        // }

                        def MK_BUILD_DEPS_CMD = "yes|mk-build-deps -i"
                        println("Package build command: " + MK_BUILD_DEPS_CMD)

                        def GBP_CMD = "gbp export-orig --submodules --compression=gzip --compression-level=9 --upstream-tree=HEAD"
                        println("Generate source tarball command: " + GBP_CMD)

                        def clean ="""
                        Source_Str="\$(dpkg-parsechangelog -S source)" && \
                        rm \$Source_Str*.deb && \
                        rm \$Source_Str*.buildinfo && \
                        rm \$Source_Str*.changes
                        """

                        println("Clean command: " + clean)

                        def build_command = $/pwd && apt-get update && /$ +
                        $/if [ -d "$${PWD}/source" ] && [ -f  "$${PWD}/.gitmodules" -a -d "$${PWD}/source" ]; then echo "repo/branch uses submodule to manage source code." && rm -rf $${PWD}/source/debian || true && cp -r $${PWD}/debian $${PWD}/source/debian && cd $${PWD}/source && git config --global --add safe.directory /workspace/source && for file in $$(find /workspace/source -type d);do echo "$$file" && git config --global --add safe.directory $$file;done || true ;fi &&/$ +
                        $/echo "Command to run is: ${MK_BUILD_DEPS_CMD}" && /$ +
                        $/${MK_BUILD_DEPS_CMD} && /$ +
                        $/pwd && ls -l && /$ +
                        $/rm -f debian/gbp.conf || true && ${GBP_CMD} && ls && /$ +
                        $/Source_Str="$$(dpkg-parsechangelog -S source)" && /$ +
                        $/rm $$Source_Str*.deb || true && /$ +
                        $/rm $$Source_Str*.buildinfo || true && /$ +
                        $/rm $$Source_Str*.changes || true && echo "Done removing files" && ls && /$ +
                        $/debian/autogen.sh || true && /$ +
                        $/if [ -d "../.git/modules/source/lfs" ] ; then yes|dpkg-buildpackage -b; else yes|dpkg-buildpackage -F -sa; fi && /$ +
                        "ls -la && pwd &&" +
                        $/find .. -maxdepth 1 -type f -execdir /bin/cp '{}' /workspace \; && /$ +
                        "ls -la && pwd"

                        def pkgbuild_docker_command = PACKAGE_DOCKER_BLD_UNCONFINED_CMD

                        println("Actual Build command: ${pkgbuild_docker_command} bash -c '${build_command}'")

                        // Apply a timeout to the build command
                        timeout(time: 90, unit: 'MINUTES') {
                            sh"""
                                ${pkgbuild_docker_command} bash -c '${build_command}'
                            """
                        }

                        /*  ###################################################################
                         *  ################# END BLOCK FOR PACKAGE BUILD #####################
                         *  ###################################################################
                         */

                        // Cause the script they provided does some stupid chown stuff
                        sh"""
                            ${CMD_DOCKER_RUN} bash -c "sudo chown -R 44051:17838 ${WORKSPACE}/ && sudo chmod -R 755 ${WORKSPACE}/"
                        """
                        // mv ${WORKSPACE}/build-target/binaries/* ${WORKSPACE}/PPA/

                        def component = item["component"]

                        sh"""
                            $PACKAGE_SIGN_DOCKER bash -c 'cd ${WORKSPACE}/build-target && \
                            for file in *.changes; do reprepro --keepunusednewfiles --keepunreferencedfiles -b ${WORKSPACE}/PPA -C ${component} include jammy "\$file"; done'
                        """
                    }
                }
            }
        }
        // This stage uses the github api and checks the latest releases in repos specified in params.PACKAGE_REPOS
        // for any files matching the PACKAGE_NAMES list. Any matches will then be downloaded into the PPA + Packages file updated
        stage("Download additional intel packages"){
            when{
                expression{
                    env.diff == '1'
                }
            }
            steps{
                script
                {
                    withCredentials([usernamePassword(credentialsId: 'GitHub-Token', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        def split_packages = params.PACKAGE_NAMES.tokenize("\n")
                        def split_repos = params.PACKAGE_REPOS.tokenize("\n")
                        println(split_packages)
                        println(split_repos)

                        def github_api_link = "https://api.github.com/repos/"
                        def api_tail = "/releases/latest"

                        def package_links = []

                        //################# START: PROCESS THE LINKS AND TRY TO GRAB ANY RELEVANT DOWNLOAD URLS #################
                        for(package_name in split_packages)
                        {
                            for (repo_name in split_repos)
                            {
                                def command = $/curl -u ${USERNAME}:${PASSWORD} -s ${github_api_link}${repo_name}${api_tail} | grep "browser_download_url" | cut -d : -f 2,3 | tr -d \" | grep ${package_name} || true/$
                                def download_links = sh(script: "${command}", returnStdout : true)
                                // Append the new items to the existing list
                                // Removing the whitespaces requires looping through the list as we cannot just append the list to the old one
                                for (temp_link in download_links.tokenize("\n"))
                                {
                                    package_links.add(temp_link.trim())
                                }
                            }
                        }

                        package_links.unique()
                        println(package_links)
                        //################# END: PROCESS THE LINKS AND TRY TO GRAB ANY RELEVANT DOWNLOAD URLS #################

                        sh"""
                            mkdir -p ${WORKSPACE}/PPA
                            $CMD_DOCKER_RUN bash -c "sudo chown -R 44051:17838 ${WORKSPACE}/"
                        """

                        // Download all the links that were grabbed when processing the latest files in the repos provided in the params
                        for(package_name in package_links)
                        {
                            sh"""
                                cd ${WORKSPACE}/PPA
                                export no_proxy=intel.com,*.intel.com
                                export NO_PROXY=intel.com,*.intel.com
                                wget ${package_name}
                            """
                        }

                        // Download all things in params.FIXED_PACKAGE_LINKS
                        for(package_link in params.FIXED_PACKAGE_LINKS.tokenize("\n"))
                        {
                            sh"""
                                cd ${WORKSPACE}/PPA
                                export no_proxy=intel.com,*.intel.com
                                export NO_PROXY=intel.com,*.intel.com
                                wget ${package_link}
                            """
                        }
                    }
                }
            }
        }
        stage("Download additional kernel packages"){
            when{
                expression{
                    env.diff == '1'
                }
            }
            steps{
                script{
                    // #################################################
                    // ######### BLOCK TO DOWNLOAD THE KERNELS #########
                    // #################################################
                    if(!params.KERNELS_FILE.isEmpty()){
                        def kernel_list = readJSON file: "${WORKSPACE}/manifest_repo/${params.KERNELS_FILE}"

                        for (components in kernel_list){

                            def component = components['component']

                            def kernels_list = components['links']

                            println("kernels_list: " + kernels_list)

                            sh"""
                                mkdir -p ${WORKSPACE}/PPA
                                $CMD_DOCKER_RUN bash -c "sudo chown -R 44051:17838 ${WORKSPACE}/"
                            """

                            // Download all the links that were grabbed when processing the latest files in the repos provided in the params
                            for(package_name in kernels_list)
                            {
                                sh"""
                                    cd ${WORKSPACE}/PPA
                                    export no_proxy=intel.com,*.intel.com
                                    export NO_PROXY=intel.com,*.intel.com
                                    wget ${package_name}
                                """
                            }

                            sh"""
                                ls -la ${WORKSPACE}/PPA
                                $CMD_DOCKER_RUN bash -c "sudo sh -c 'chown -R root:root ${WORKSPACE}/PPA'"
                                $CMD_DOCKER_RUN bash -c "sudo sh -c 'cd ${WORKSPACE}/PPA/ && dpkg-scanpackages . /dev/null > ${WORKSPACE}/PPA/Packages'"
                            """

                            sh"""
                                ${CMD_DOCKER_RUN} bash -c "sudo chown -R 44051:17838 ${WORKSPACE}/ && sudo chmod -R 755 ${WORKSPACE}/"
                                rm -rf ${WORKSPACE}/PPA/db/lockfile || true
                                $PACKAGE_SIGN_DOCKER bash -c 'cd ${WORKSPACE}/PPA && \
                                reprepro --keepunusednewfiles --keepunreferencedfiles -C ${component} includedeb jammy *.deb'
                            """
                        }
                    }
                    // #####################################################
                    // ######### END BLOCK TO DOWNLOAD THE KERNELS #########
                    // #####################################################
                }
            }
        }
        stage("Download additional debs"){
            when{
                expression{
                    env.diff == '1'
                }
            }
            steps{
                script{
                    // ####################################################
                    // ######### BLOCK TO DOWNLOAD THE EXTRA DEBS #########
                    // ####################################################
                    if(!params.EXTRA_DEBS.isEmpty()){
                        def extra_debs = readJSON file: "${WORKSPACE}/manifest_repo/${params.EXTRA_DEBS}"

                        for (components in extra_debs){

                            def component = components['component']

                            def extra_debs_list = components['links']

                            println("Extra debs list: " + extra_debs_list)

                            sh"""
                                mkdir -p ${WORKSPACE}/PPA
                                $CMD_DOCKER_RUN bash -c "sudo chown -R 44051:17838 ${WORKSPACE}/"
                            """

                            // Download all the links that were grabbed when processing the latest files in the repos provided in the params
                            for(package_name in extra_debs_list)
                            {
                                sh"""
                                    cd ${WORKSPACE}/PPA
                                    export no_proxy=intel.com,*.intel.com
                                    export NO_PROXY=intel.com,*.intel.com
                                    wget ${package_name}
                                """
                            }

                            sh"""
                                ls -la ${WORKSPACE}/PPA
                                $CMD_DOCKER_RUN bash -c "sudo sh -c 'chown -R root:root ${WORKSPACE}/PPA'"
                                $CMD_DOCKER_RUN bash -c "sudo sh -c 'cd ${WORKSPACE}/PPA/ && dpkg-scanpackages . /dev/null > ${WORKSPACE}/PPA/Packages'"
                            """

                            sh"""
                                ${CMD_DOCKER_RUN} bash -c "sudo chown -R 44051:17838 ${WORKSPACE}/ && sudo chmod -R 755 ${WORKSPACE}/"
                                rm -rf ${WORKSPACE}/PPA/db/lockfile || true
                                $PACKAGE_SIGN_DOCKER bash -c 'cd ${WORKSPACE}/PPA && \
                                reprepro --keepunusednewfiles --keepunreferencedfiles -C ${component} includedeb jammy *.deb'
                            """
                        }
                    }
                    // ########################################################
                    // ######### END BLOCK TO DOWNLOAD THE EXTRA DEBS #########
                    // ########################################################
                }
            }
        }
        stage("Delete Unwanted Packages"){
            when{
                expression{
                    env.diff == '1'
                }
            }
            steps{
                script{
                    def split_packages = params.REMOVE_PACKAGES.tokenize("\n")
                    def package_del_string = ""

                    if(split_packages.size() != 0){
                        println("Packages were specified, proceeding with deletion!")
                        package_del_string = split_packages.join(" ")
                        println("Packages to delete are: " + package_del_string)
                        sh"""
                            ${CMD_DOCKER_RUN} bash -c "sudo sh -c 'cd ${WORKSPACE}/PPA/ && rm ${package_del_string}'" || true
                        """
                    }
                    else{
                        println("No items were specified, skipping deletion stage!")
                    }
                }
            }
        }
        stage("Upload packages to repo"){
            when{
                expression{
                    env.diff == '1' && currentBuild.getBuildCauses('hudson.model.Causes$UpstreamCause').isEmpty()
                    //triggeredBy cause: "UpstreamCause"
                }
            }
            steps{
                script{
                    withCredentials([usernamePassword(credentialsId: 'BuildAutomation', passwordVariable: 'BDPWD', usernameVariable: 'BDUSR')]) {
                        if(params.UPLOAD){
                            println("Upload param was set to yes, uploading artifacts!")
                            dir("${WORKSPACE}/PPA") {
                    sh"""
                    ls -la
                                    $CMD_DOCKER_RUN bash -c "sudo chown -R 44051:17838 ${WORKSPACE}/PPA/ && \
                    mkdir ${WORKSPACE}/PPA/upload && mv ${WORKSPACE}/PPA/conf ${WORKSPACE}/PPA/db ${WORKSPACE}/PPA/dists ${WORKSPACE}/PPA/pool ${WORKSPACE}/PPA/upload/ && ls ${WORKSPACE}/PPA/upload/"
                """
                }
                dir("${WORKSPACE}/PPA/upload") { // https://ubit-artifactory-sh.intel.com/ui/repos/tree/General/esc-internal-local/sandbox
                                sh"""  
                                    # Delete the 'latest' folder in the artifactory
                                    curl -u ${BDUSR}:${BDPWD} -v -X DELETE https://ubit-artifactory-sh.intel.com/artifactory/esc-internal-local/sandbox/ppa/jammy-latest/ || true
                                """
                                def buildInfo = Artifactory.newBuildInfo()
                                def artServer = Artifactory.server "ubit-artifactory-sh.intel.com"
                                def kwrpt  = """{
                                    "files": [
                                        {
                                            "pattern": "*",
                                            "target": "esc-internal-local/sandbox/ppa/jammy/${DATETIME}/",
                                            "props": "retention.days=3",
                                            "flat" : "false"
                                        },
                                        {
                                            "pattern": "*",
                                            "target": "esc-internal-local/sandbox/ppa/jammy-latest/",
                                            "props": "retention.days=3",
                                            "flat" : "false"
                                        }
                                    ]
                                }"""
                                artServer.upload spec: kwrpt, buildInfo: buildInfo
                                artServer.publishBuildInfo buildInfo
                            }

                                // Set the repo url to be used during the image build
                                env.url_to_use = "https://ubit-artifactory-sh.intel.com/artifactory/esc-internal-local/sandbox/ppa/${DATETIME}/jammy"

                                sh"""
                                # Build-info file to store build-date && upload path
                                echo ${DATETIME} > ${WORKSPACE}/PPA/upload/build-info
                                echo https://ubit-artifactory-sh.intel.com/artifactory/esc-internal-local/sandbox/ppa/${DATETIME}/jammy/ >> ${WORKSPACE}/PPA/upload/build-info
                                cd ${WORKSPACE}/PPA/upload/
                                curl -u ${BDUSR}:${BDPWD} -v -X PUT -T build-info https://ubit-artifactory-sh.intel.com/artifactory/esc-internal-local/sandbox/ppa/${DATETIME}/jammy/build-info
                                curl -u ${BDUSR}:${BDPWD} -v -X PUT -T build-info https://ubit-artifactory-sh.intel.com/artifactory/esc-internal-local/sandbox/ppa/jammy-latest/build-info
                                """
                        }
                        else{
                            println("Upload param was set to false, not uploading artifacts!")
                        }
                       /* if(params.USE_COMMIT){
                            sh"curl -u ${BDUSR}:${BDPWD} -v -X PUT -T ${env.MANIFEST_LOCATION}/${params.MANIFEST_FILE} https:///artifactory/${MANIFEST_REPO}/${params.MANIFEST_FILE}"
                        }
                        else{
                            sh"curl -u ${BDUSR}:${BDPWD} -v -X PUT -T current_run_commits.json https://${ARTIFACTORY_SERVER}/artifactory/${MANIFEST_REPO}/${params.MANIFEST_FILE}"
                        }*/
                    }
                }
            }
        }
        stage('upload to user repo') {
            when {
                expression {
                    triggeredBy cause: 'UserIdCause' && !currentBuild.getBuildCauses('hudson.model.Causes$UpstreamCause').isEmpty()
                }
            }

            steps{
                script{
                    withCredentials([usernamePassword(credentialsId: 'BuildAutomation', passwordVariable: 'BDPWD', usernameVariable: 'BDUSR')]) {
                        if(params.UPLOAD){
                            println("Upload param was set to yes, uploading artifacts!")
                            dir("${WORKSPACE}/PPA") {
                                sh"""
                                ls -la
                                                $CMD_DOCKER_RUN bash -c "sudo chown -R 44051:17838 ${WORKSPACE}/PPA/ && \
                                mkdir ${WORKSPACE}/PPA/upload && mv ${WORKSPACE}/PPA/conf ${WORKSPACE}/PPA/db ${WORKSPACE}/PPA/dists ${WORKSPACE}/PPA/pool ${WORKSPACE}/PPA/upload/ && ls ${WORKSPACE}/PPA/upload/"
                                """
                            }
                            dir("${WORKSPACE}/PPA/upload") { 
                                def buildInfo = Artifactory.newBuildInfo()
                                def artServer = Artifactory.server "ubit-artifactory-sh.intel.com"
                                def kwrpt  = """{
                                    "files": [
                                        {
                                            "pattern": "*",
                                            "target": "esc-internal-local/sandbox/ppa/${DATETIME}/jammy/",
                                            "props": "retention.days=3",
                                            "flat" : "false"
                                        },
                                    ]
                                }"""
                                artServer.upload spec: kwrpt, buildInfo: buildInfo
                                artServer.publishBuildInfo buildInfo
                            }

                                // Set the repo url to be used during the image build
                                env.url_to_use = "https://ubit-artifactory-sh.intel.com/artifactory/esc-internal-local/sandbox/ppa/${DATETIME}/jammy"

                                sh"""
                                # Build-info file to store build-date && upload path
                                echo ${DATETIME} > ${WORKSPACE}/PPA/upload/build-info
                                echo https://ubit-artifactory-sh.intel.com/artifactory/esc-internal-local/sandbox/ppa/${DATETIME}/jammy/ >> ${WORKSPACE}/PPA/upload/build-info
                                cd ${WORKSPACE}/PPA/upload/
                                curl -u ${BDUSR}:${BDPWD} -v -X PUT -T build-info https://ubit-artifactory-sh.intel.com/artifactory/esc-internal-local/sandbox/ppa/${DATETIME}/jammy/build-info
                                """
                        }
                        else{
                            println("Upload param was set to false, not uploading artifacts!")
                        }
                        /* if(params.USE_COMMIT){
                            sh"curl -u ${BDUSR}:${BDPWD} -v -X PUT -T ${env.MANIFEST_LOCATION}/${params.MANIFEST_FILE} https:///artifactory/${MANIFEST_REPO}/${params.MANIFEST_FILE}"
                        }
                        else{
                            sh"curl -u ${BDUSR}:${BDPWD} -v -X PUT -T current_run_commits.json https://${ARTIFACTORY_SERVER}/artifactory/${MANIFEST_REPO}/${params.MANIFEST_FILE}"
                        }*/
                    }
                }
            }
        }
    }
    post{
        /*success{
        script{
                if(env.diff == '1'){
                // Only update commit in manifest file if built using branch option
            if(!params.USE_COMMIT){
                def DAY = new Date().getAt(Calendar.DAY_OF_WEEK) - 1
                git_tag = YEAR + "WW" + WORK_WEEK + "."+ DAY + "_" +TIME

                sh"""
                    cat ${WORKSPACE}/current_run_commits.json > ${WORKSPACE}/manifest_repo/manifest/${params.MANIFEST_FILE}
                cd ${WORKSPACE}/manifest_repo
                git diff -p -R --no-ext-diff --no-color | grep -E "^(diff|(old|new) mode)" --color=never | git apply
                git add manifest/${params.MANIFEST_FILE}
                git commit -m "HEAD commits of repos for ${git_tag}"
                SHA=\$(git log -n 1 --pretty=format:"%H")
                echo \$SHA
                git tag -a ${git_tag} \$SHA -m "Tag for ${ARTIFACTORY_REPO}/${DATETIME}/"
                git push origin ${git_tag}
                """
                }
                        if(params.UPLOAD){
                ARTIFACTORY_PATH= "${ARTIFACTORY_REPO}/${DATETIME}"

                build job: "${DOWNSTREAM_QA_JOB}", parameters: [string(name: 'ARTIFACTORY_SERVER', value: "${ARTIFACTORY_SERVER}"), \
                string(name: 'ARTIFACTORY_PATH', value: "${ARTIFACTORY_REPO}/${DATETIME}/"), string(name: 'JOB_NAME', value: "${JOB_BASE_NAME}")], wait: false

                //Add description to build promotion job
                JENKINS_PROMOTE_JOB = "https://cje-pg-prod01.devtools.intel.com/nex-cisv-devops01/job/GEN-LIN-BSP-VAL-ODM-PROMOTION/"
                PROMOTE_PARAMETER = "token=GEN-LIN-BSP-VAL-ODM-PROMOTION&ARTIFACTORY_SERVER=${ARTIFACTORY_SERVER}&ARTIFACTORY_PATH=${ARTIFACTORY_PATH}/&RETENTION_DAY=2555&BUILD_JOB_URL=${BUILD_URL}"
                PROMOTE_URL = "${JENKINS_PROMOTE_JOB}/buildWithParameters?${PROMOTE_PARAMETER}"
                currentBuild.description = "<a href=${PROMOTE_URL}> Click me to promote </a> <br> (Redirecting to blankpage is expected) <br> <a href=${JENKINS_PROMOTE_JOB}>Check status</a> or Refresh"
                        }
                }
            }
        }*/
        always{
            sh"""
                docker kill ${JOB_BASE_NAME}_${BUILD_NUMBER} || true
                docker kill ${BUILD_TAG} || true
                $CMD_DOCKER_RUN bash -c "sudo chown -R 44051:17838 ${WORKSPACE}/"
                ls -la ${WORKSPACE}/jenkins-script/binaries/ || true
                ls -la ${WORKSPACE}/PPA/ || true
            """
        }
    }
}
