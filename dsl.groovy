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
		shell('sudo cp -r -v -f * /t6')
	}
}

job("Groovy 2")
{
description ("This is my second job for Groovy project ")
steps{
shell('''sudo kubectl version
if sudo ls /t6 | grep html
then
 if sudo kubectl get svc | grep apache-svc
 then
 echo "Service for apache is Running"
 else
 sudo kubectl create -f /t6/apache_svc.yml 
 fi
 if sudo kubectl get pvc | grep apache-pvc
 then
 echo " PVC for apache is already running"
 else
 sudo kubectl create -f /t6/apache_pvc.yml 
 fi
 if sudo kubectl get deploy | grep apache_deploy
 then
 echo "Deployment for apache running"
 else
 sudo  kubectl create -f /t6/apache_deploy.yml
else 
echo "no html code from developer to host"
fi
}
triggers {
   upstream('Groovy 1', 'SUCCESS')
     }
  }
job("Groovy 3") {
	description("This is the third job of groovy project")
	
	triggers {
	        
	        upstream {
	            upstreamProjects('Groovy 2')
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
	
job("Groovy 4")
{
description ("This is mailing job")
 authenticationToken('mail')
   publishers {
		mailer("sumayyakhatoon58@gmail.com", false, false)
	}
   triggers {
   upstream('Groovy 3', 'SUCCESS')
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
