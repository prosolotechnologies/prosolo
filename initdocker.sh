#!/usr/bin/env bash

function check_docker {
    docker ps > /dev/null
}
onErrorQuit () {
    local message=$1
    echo $message 1>&2
    exit 1
}
function createBranchVolumes {
    docker volume create --name=cassandra_data_volume_${VCS_BRANCH} && \
    docker volume create --name=elasticsearch_data_volume_${VCS_BRANCH} && \
    docker volume create --name=mysql_data_volume_${VCS_BRANCH}
}
function getBranch() {
        git rev-parse --abbrev-ref HEAD | sed -E 's/[\/\\]+/_/g'
}

function check_cassandra {
    printf "Is cassandra on? "
    cassandra_port=9042
    if is_db_running $cassandra_port ; then
        printf "Is cassandra running in container? "
        if is_container_running docker_cassandra_1 ; then
            echo "Cassandra is running"
        else
            echo "ERROR"
            onErrorQuit "Cassandra is still running as a local process. Wait until cassandra shut down and retry"
        fi
    else
        echo "DOWN. Will start container"
        START_CONTAINERS=true
    fi
}

function is_db_running {
    status=$(netstat -na | grep -E '(:::|0.0.0.0|127.0.0.1|\*)[:.]*'$1' ')
    [[ -z $status ]] && return 1
    return 0
}

function is_container_running {
    status=$(docker inspect $1 | grep Running | sed 's/.*: //' | sed 's/,.*//') || status=false
    if [ "$status" == 'true' ]; then
        return 0
    else
        return 1
    fi
}

function start_database {
    pushd docker
    if $DEBUG; then
        docker-compose up || onErrorQuit " cannot start container orchestration"
    else
        docker-compose up -d || onErrorQuit " cannot start container orchestration"
    fi

    popd
    echo "started databases"
}
function handle_parameter {
    while [[ $# -gt 0 ]]
    do
        key="$1"

        case $key in
            -y|--yes)
                 ALWAYSYES=true
                 shift
                 ;;
            -n|--no)
                ALWAYSNO=true
                shift
                ;;
            reset)
                RESET=true
                shift
                ;;
            debug)
                DEBUG=true
                shift
                ;;
            stop)
                STOP=true
                shift
                ;;
            -h|--help)
                displayHelp
                exit 0
                ;;
            -es6)
                ES_VERSION_MANIFEST=docker.elastic.co/elasticsearch/elasticsearch:6.2.4
                shift
                ;;
			-d|--dev)
			    VCS_BRANCH='dev'
			    shift
                ;;
			-b*)
            	if  [[ "$2" == -* ]] || [[ "$2" == 'reset' ]] || [[ "$2" == 'start-database' ]] || [[ -z $2 ]] ; then
            	   VCS_BRANCH=$(getBranch)
            	   echo "geting branch"
            	   shift
            	else
            	   VCS_BRANCH=$2
            	   echo "second parameter branch $VCS_BRANCH"
            	   shift 2
            	fi
            	;;
            --)
                shift
                RESIDUAL_ARGS=( "$@" )
                break # all arguments after -- will be simply forwarded to SBT
                ;;
            *)
                echo "unknown option: " $1 > /dev/stderr
                echo " ---------------------------------------- "
                displayHelp
                exit 1
                ;;

        esac
    done

    [[ -z $RESET ]] && RESET=false
    [[ -z $STOP ]] && STOP=false
    [[ -z $ALWAYSYES ]] && ALWAYSYES=false
    [[ -z $ALWAYSNO ]] && ALWAYSNO=false
    [[ -z $START_VSERVER ]] && START_VSERVER=true
}

function ask_question() {
    if $ALWAYSYES ; then return 0; fi
    if $ALWAYSNO ; then return 1; fi

    read -r -p "${1} " response
    response=$(echo "${response}" | awk '{print tolower($0)}') # tolower
    if [[ $response =~ ^(yes|y| ) ]] || [[ -z $response ]]; then
        return 0
    else
        return 1
    fi
}

function reset_database {
    pushd docker
    docker-compose down --remove-orphans || onErrorQuit "cannot teardown containers"
    popd
    local container=$(docker ps -a -q -f status=exited -f status=created)
    if [ ! -z "${container}" ]; then
        docker rm -v ${container}
    fi
    docker volume rm -f cassandra_data_volume_${VCS_BRANCH}
    docker volume rm -f mysql_data_volume_${VCS_BRANCH}
    docker volume rm -f elasticsearch__data_volume_${VCS_BRANCH}
}

function stop_database {
    pushd docker
    docker-compose down || onErrorQuit "cannot stop containers"
    popd
  }

function displayHelp() {
    echo -e "
    Server script takes care of coordinating database layer bootstrap and update.
        Cassandra, Elasticsearch, MySql and RabbitMQ are handled by this script.

    Usage: $0 <parameters> [ -- <parameters> ]

    Available parameters
        -h | --help    : shows this screen and exists
        -n | --no      : does not prompt for database updates (assumes no as answer)
        -y | --yes     : does not prompt for database updates (assumes yes as answer)
        -b <branch>    : uses named branch to label container volumes. If no branch is specified (just -b) it will use
                         the branch name from current repository (same as not passing either of -b and -d)
        -d | --dev     : uses 'dev' as branch name to label container volumes
        -es6           : initializes docker container with elasticsearch v6.2.4 instead of deafult v2.3.0
        reset          : resets database content and repeats bootstrap process
        stop           : stops the currently running databases
        debug          : prevents running containers in the background which is set as default and makes it possible to investigate container initialization problems

    "
}

######################################################
#            LOGIC FLOW STARTS HERE                  #
######################################################

 ES_VERSION_MANIFEST=elasticsearch:2.3.0

handle_parameter $@

echo "ES VERSION MANIFEST:" $ES_VERSION_MANIFEST
export ES_VERSION_MANIFEST=$ES_VERSION_MANIFEST

if $STOP; then
    stop_database
    exit 1
fi

if [ -z ${VCS_BRANCH+x} ];
    then
    VCS_BRANCH=$(getBranch)
fi
export VCS_BRANCH=$VCS_BRANCH
echo "BRANCH is set to:"  $VCS_BRANCH

if ! check_docker ; then
    echo "ERROR"
    onErrorQuit "Docker is not running or this shell lacks the necessary rights to run docker commands."
fi

createBranchVolumes || onErrorQuit "Cannot create data volumes for containers"

check_cassandra


[[ -z $START_CONTAINERS ]] && START_CONTAINERS=false
if [[ $START_CONTAINERS || ! is_bootstrapped ]]; then
    start_database
fi


if $RESET ; then
    if (ask_question "Will reset database. Are you sure? (Y/n)"); then
        reset_database
    fi
fi

