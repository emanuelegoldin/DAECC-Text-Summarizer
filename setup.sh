# Paths
ROOT_PATH="$PWD"
PROJECT_ROOT="$PWD/DAECCProject"
DEPLOYMENT_ROOT="$PWD/deployment"
GCP_DEPLOYMENT_ROOT="$DEPLOYMENT_ROOT/gcp_deployment"
GCP_DEPLOYMENT_CREDENTIALS="$GCP_DEPLOYMENT_ROOT/gcp_key.json"
AWS_DEPLOYMENT_ROOT="$DEPLOYMENT_ROOT/aws_deployment"
AWS_DEPLOYMENT_CREDENTIALS="$AWS_DEPLOYMENT_ROOT/modules/aws_credentials"
WORKFLOW_ROOT="$PWD/workflow"

#################
#   Functions   #
#################
placeCredentials (){
    # AWS credentials
    AWS_ACCESS_KEY=$(jq -r '.aws_credentials.access_key' credentials.json)
    AWS_SECRET_KEY=$(jq -r '.aws_credentials.secret_key' credentials.json)
    AWS_TOKEN=$(jq -r '.aws_credentials.token' credentials.json)

    # GCP credentials
    GCP_CREDENTIALS=$(jq -r '.gcp_credentials' credentials.json)

    for dir in $(find . -name 'resources'); do
        echo "{
    \"aws_credentials\": {
        \"access_key\": \"$AWS_ACCESS_KEY\",
        \"secret_key\": \"$AWS_SECRET_KEY\",
        \"token\": \"$AWS_TOKEN\"
    },
    \"gcp_credentials\": $GCP_CREDENTIALS
}" > $dir/credentials.json
    done

    # Place credentials in deployment folder
    echo "$GCP_CREDENTIALS" > $GCP_DEPLOYMENT_CREDENTIALS
    echo "[default]
aws_access_key_id = $AWS_ACCESS_KEY
aws_secret_access_key = $AWS_SECRET_KEY
aws_session_token = $AWS_TOKEN" > $AWS_DEPLOYMENT_CREDENTIALS
}

build (){
    # Install the jars from dependencies folder in the local maven repository
    INSTALL_JARS=$1

    if [ "$INSTALL_JARS" = "true" ]; then
        # Install the jars from dependencies folder in the local maven repository
        mvn install:install-file -Dfile=dependencies/shared-1.0-SNAPSHOT.jar -DgroupId=core -DartifactId=shared -Dversion=1.0-SNAPSHOT -Dpackaging=jar
        mvn install:install-file -Dfile=dependencies/storage-1.0-SNAPSHOT.jar -DgroupId=core -DartifactId=storage -Dversion=1.0-SNAPSHOT -Dpackaging=jar
    fi

    placeCredentials

    # Build the project
    cd $PROJECT_ROOT
    mvn clean install
}

deploy() {
    # Deploy to GCP
    cd $GCP_DEPLOYMENT_ROOT
    terraform init
    terraform apply -auto-approve
    # Get processor_name output from GCP deployment
    PROCESSOR_NAME=$(terraform output processor_name) 

    # Deploy to AWS
    cd $AWS_DEPLOYMENT_ROOT
    terraform init
    terraform apply -auto-approve -var="processor_name=$PROCESSOR_NAME"
}

destroy() {
    # Destroy GCP deployment
    cd $GCP_DEPLOYMENT_ROOT
    PROCESSOR_NAME=$(terraform output processor_name)
    terraform destroy -auto-approve

    # Destroy AWS deployment
    cd $AWS_DEPLOYMENT_ROOT
    terraform destroy -auto-approve -var="processor_name=$PROCESSOR_NAME"
}

while (( "$#" )); do
  case "$1" in
    --build)
      if ! command -v jq &> /dev/null
      then
        echo "jq could not be found. Please install it to proceed."
        exit 1
      fi
      shift
      if [ "$1" = "-install-jar" ]; then
        build true
        shift
      else
        build false
      fi
      ;;
    --deploy)
        deploy
      ;;
    --destroy)
        destroy
      ;;
    --exec)
      execute
      ;;
    *)
      echo "Unknown option: $1"
      echo "Usage: ./setup.sh [OPTION]"
        echo "Options:"
        echo "  --build [-install-jar]  Build the project"
        echo "  --deploy                Deploy the project"
        echo "  --destroy               Destroy the project"
        echo "  --exec                  Execute the project"
      exit 1
      ;;
  esac
done
