pipeline {

    agent any

    tools {
        jdk 'jdk21'
        maven 'maven3'
    }

    environment {

        DOCKER_USERNAME = "ankit9767"

        IMAGE_TAG = "${BUILD_NUMBER}"

        HELM_RELEASE = "ecommerce"

        HELM_CHART = ".\\helm\\ecommerce-chart"

    }

    stages {

        stage('Checkout') {

            steps {

                checkout scm

            }

        }


        stage('Compile') {

            steps {

                bat 'mvn clean compile'

            }

        }


        stage('Unit Test') {

            steps {

                bat 'mvn test'

            }

        }


        stage('SonarQube Analysis') {

            steps {

                withSonarQubeEnv('SonarQube') {

                    bat '''
                    mvn sonar:sonar ^
                    -Dsonar.projectKey=ecommerce ^
                    -Dsonar.projectName=ecommerce
                    '''

                }

            }

        }


        stage('Quality Gate') {

            steps {

                timeout(time: 5, unit: 'MINUTES') {

                    waitForQualityGate abortPipeline: true

                }

            }

        }


        stage('Package') {

            steps {

                bat 'mvn clean package -DskipTests'

            }

        }


        stage('Build Docker Images') {

            steps {

                bat 'docker compose build'

            }

        }


        stage('Docker Login') {

            steps {

                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )
                ]) {

                    bat '''
                    echo %DOCKER_PASS% | docker login -u %DOCKER_USER% --password-stdin
                    '''

                }

            }

        }


        stage('Tag Images') {

            steps {

                bat '''

                docker tag %DOCKER_USERNAME%/product-service:latest %DOCKER_USERNAME%/product-service:%IMAGE_TAG%

                docker tag %DOCKER_USERNAME%/order-service:latest %DOCKER_USERNAME%/order-service:%IMAGE_TAG%

                docker tag %DOCKER_USERNAME%/payment-service:latest %DOCKER_USERNAME%/payment-service:%IMAGE_TAG%

                docker tag %DOCKER_USERNAME%/auth-service:latest %DOCKER_USERNAME%/auth-service:%IMAGE_TAG%

                docker tag %DOCKER_USERNAME%/api-gateway:latest %DOCKER_USERNAME%/api-gateway:%IMAGE_TAG%

                docker tag %DOCKER_USERNAME%/eureka-server:latest %DOCKER_USERNAME%/eureka-server:%IMAGE_TAG%

                '''

            }

        }


        stage('Push Images') {

            steps {

                bat '''

                docker push %DOCKER_USERNAME%/product-service:%IMAGE_TAG%

                docker push %DOCKER_USERNAME%/order-service:%IMAGE_TAG%

                docker push %DOCKER_USERNAME%/payment-service:%IMAGE_TAG%

                docker push %DOCKER_USERNAME%/auth-service:%IMAGE_TAG%

                docker push %DOCKER_USERNAME%/api-gateway:%IMAGE_TAG%

                docker push %DOCKER_USERNAME%/eureka-server:%IMAGE_TAG%

                '''

            }

        }


        stage('Deploy To Kubernetes') {

            steps {

                bat '''
                helm upgrade --install %HELM_RELEASE% %HELM_CHART% --namespace ecommerce --create-namespace
                '''

            }

        }


        stage('Verify Deployment') {

            steps {

                bat 'kubectl get pods -n ecommerce'

                bat 'kubectl get svc -n ecommerce'

            }

        }

    }


    post {

        success {

            echo 'Deployment Successful'

        }


        failure {

            echo 'Pipeline Failed'

        }


        always {

            bat 'docker image prune -f'

            cleanWs()

        }

    }

}