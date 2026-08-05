pipeline {

    agent any

    tools {
        jdk 'JDK21'
        maven 'Maven-3.9'
    }

    environment {

        DOCKER_USERNAME = "YOUR_DOCKERHUB_USERNAME"

        IMAGE_TAG = "${BUILD_NUMBER}"

        HELM_RELEASE = "ecommerce"

        HELM_CHART = "./helm/ecommerce-chart"

    }

    stages {

        stage('Checkout') {

            steps {

                checkout scm

            }

        }

        stage('Compile') {

            steps {

                sh 'mvn clean compile'

            }

        }

        stage('Unit Test') {

            steps {

                sh 'mvn test'

            }

        }

        stage('SonarQube Analysis') {

            steps {

                withSonarQubeEnv('SonarQube') {

                    sh '''
                    mvn sonar:sonar \
                    -Dsonar.projectKey=ecommerce \
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

                sh 'mvn clean package -DskipTests'

            }

        }

        stage('Build Docker Images') {

            steps {

                sh 'docker compose build'

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

                    sh '''
                    echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                    '''

                }

            }

        }

        stage('Tag Images') {

            steps {

                sh '''

                docker tag ${DOCKER_USERNAME}/product-service:latest ${DOCKER_USERNAME}/product-service:${IMAGE_TAG}

                docker tag ${DOCKER_USERNAME}/order-service:latest ${DOCKER_USERNAME}/order-service:${IMAGE_TAG}

                docker tag ${DOCKER_USERNAME}/payment-service:latest ${DOCKER_USERNAME}/payment-service:${IMAGE_TAG}

                docker tag ${DOCKER_USERNAME}/auth-service:latest ${DOCKER_USERNAME}/auth-service:${IMAGE_TAG}

                docker tag ${DOCKER_USERNAME}/api-gateway:latest ${DOCKER_USERNAME}/api-gateway:${IMAGE_TAG}

                docker tag ${DOCKER_USERNAME}/eureka-server:latest ${DOCKER_USERNAME}/eureka-server:${IMAGE_TAG}

                '''

            }

        }

        stage('Push Images') {

            steps {

                sh '''

                docker push ${DOCKER_USERNAME}/product-service:${IMAGE_TAG}

                docker push ${DOCKER_USERNAME}/order-service:${IMAGE_TAG}

                docker push ${DOCKER_USERNAME}/payment-service:${IMAGE_TAG}

                docker push ${DOCKER_USERNAME}/auth-service:${IMAGE_TAG}

                docker push ${DOCKER_USERNAME}/api-gateway:${IMAGE_TAG}

                docker push ${DOCKER_USERNAME}/eureka-server:${IMAGE_TAG}

                '''

            }

        }

        stage('Deploy To Kubernetes') {

            steps {

                sh """

                helm upgrade --install ${HELM_RELEASE} ${HELM_CHART}

                """

            }

        }

        stage('Verify Deployment') {

            steps {

                sh 'kubectl get pods -n ecommerce'

                sh 'kubectl get svc -n ecommerce'

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

            sh 'docker image prune -f'

            cleanWs()

        }

    }

}