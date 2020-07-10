job("Groovy 1") {
	description("This is the first job of groovy project")
	keepDependencies(false)
	scm {
		git {
			remote {
				github("sumayya-github/jenkins-groovy", "https")
			}
			branch("*/master")
		}
	}
	disabled(false)
	triggers {
		scm("* * * * *") {
			ignorePostCommitHooks(false)
		}
	}
	concurrentBuild(false)
	steps {
		shell("""if ls | grep php
then
cat <<EOF |sudo kubectl apply -f -
 apiVersion: v1
kind: Service
metadata:
  name: php-svc
  labels:
    app: php
spec:
  type: NodePort
  selector:
    app: php
  ports: 
    - port: 80
      targetPort: 80
      nodePort: 30007
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: php-pv-claim
  labels:
    app: php
spec:
  accessModes:
  - ReadWriteOnce
  resources:
    requests:
      storage: 10Gi
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: php-pod
  labels:
    app: php
spec:
  replicas: 1
  selector:
    matchLabels:
      app: php
  strategy:
     type: Recreate
  template:
    metadata:
      name: php
      labels:
        app: php
    spec:
      containers:
      - name: php-con
        image: httpd
        volumeMounts:
          - name: php-vol
            mountPath: /var/www/html
      volumes:
      - name: php-vol
        persistentVolumeClaim:
          claimName:  php-pv-claim
EOF
else
cat <<EOF |sudo kubectl apply -f -
apiVersion: v1
kind: Service
metadata:
  name: apache-svc
  labels:
    app: apache
spec:
  type: NodePort
  selector:
    app: apache
  ports:
    - port: 80
      targetPort: 80
      nodePort: 30007
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: apache-pv-claim
  labels:
    app: apache
spec:
  accessModes:
  - ReadWriteOnce
  resources:
    requests:
     storage: 10Gi
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: apache-pod
  labels:
    app: apache
spec:
  replicas: 1
  selector:
    matchLabels:
      app: apache
  strategy:
    type: Recreate
  template:
    metadata:
      name: apache
      labels:
        app: apache
    spec:
      containers:
      - name: apache-con
        image: httpd
        volumeMounts:
          - name: apache-vol
            mountPath: /var/www/html
      volumes:
      - name: apache-vol
        persistentVolumeClaim:
          claimName: apache-pv-claim
EOF
fi""") 
	}
}

job("Groovy 2") {
	description("This is the second job of groovy project")
	
	triggers {
	        
	        upstream {
	            upstreamProjects('Groovy 1')
	            threshold('SUCCESS')
	        }
	    }
	steps {
		shell("""status=\$(curl -sL -w "%{http_code}" -I "http://192.168.99.100:30007" -o /dev/null)
if [[ \$status == 200 ]]
then
exit 0
else
exit 1
fi""")
	}
	publishers {
		mailer("sumayyakhatoon58@gmail.com", false, false)
	}
}
buildPipelineView('Groovy Project') {
    filterBuildQueue()
    filterExecutors()
    title('Groovy Pipeline Complete View')
    displayedBuilds(3)
    selectedJob('Groovy 1')
    alwaysAllowManualTrigger()
    showPipelineParameters()
    refreshFrequency(60)
}
